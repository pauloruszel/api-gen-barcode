package com.br.apigenbarcode.reception;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.springframework.http.HttpStatus.*;

@Service
class ReceptionReport {
    private final ReceptionStore store;
    private final ReceptionAudit audit;

    ReceptionReport(ReceptionStore store, ReceptionAudit audit) {
        this.store = store;
        this.audit = audit;
    }

    Flux<ReportRow> rows(UUID eventId) {
        return store.database().sql("""
                SELECT g.id, g.name, i.label, g.rsvp, g.active, c.arrived_at, o.name operator
                FROM reception_guest g
                JOIN reception_invite i ON i.event_id = g.event_id AND i.id = g.invite_id
                LEFT JOIN reception_checkin c ON c.event_id = g.event_id AND c.guest_id = g.id
                LEFT JOIN reception_operator o ON o.event_id = c.event_id AND o.id = c.operator_id
                WHERE g.event_id = :event AND (g.active OR c.arrived_at IS NOT NULL)
                ORDER BY i.label, g.name, g.id
                """).bind("event", eventId).map((row, metadata) -> new ReportRow(
                row.get("id", UUID.class), row.get("name", String.class), row.get("label", String.class),
                Rsvp.valueOf(row.get("rsvp", String.class)), Boolean.TRUE.equals(row.get("active", Boolean.class)),
                row.get("arrived_at", OffsetDateTime.class), row.get("operator", String.class))).all();
    }

    Mono<UUID> addExtra(UUID eventId, String name, String group, String actor, String reason) {
        GuestRow row = new GuestRow(null, group, name, Rsvp.UNKNOWN);
        return store.inTransaction(store.event(eventId, true)
                .then(store.insertGuest(eventId, row)).flatMap(guestId ->
                        audit.write(eventId, guestId, "EXTRA", actor, reason, null)
                                .then(store.nextRevision(eventId))
                                .flatMap(revision -> store.recordRevision(eventId, revision, 1, 0, 0))
                                .thenReturn(guestId)));
    }

    Mono<Boolean> undo(UUID eventId, UUID guestId, String actor, String reason) {
        return store.inTransaction(store.event(eventId, true).then(store.database().sql("""
                DELETE FROM reception_checkin WHERE event_id = :event AND guest_id = :guest RETURNING arrived_at
                """).bind("event", eventId).bind("guest", guestId)
                .map((row, metadata) -> row.get("arrived_at", OffsetDateTime.class)).one()
                .switchIfEmpty(Mono.error(new ResponseStatusException(CONFLICT, "Não há entrada para desfazer.")))
                .flatMap(arrival -> audit.write(eventId, guestId, "UNDO", actor, reason, arrival))
                .thenReturn(true)));
    }

    Mono<Boolean> recordManual(UUID eventId, UUID guestId, String actor, String reason, OffsetDateTime arrival) {
        if (arrival == null || arrival.isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Informe um horário de chegada válido e já ocorrido.");
        }
        return store.inTransaction(store.event(eventId, true).then(Mono.defer(() -> {
            UUID operatorId = UUID.randomUUID();
            return store.database().sql("""
                    INSERT INTO reception_operator(event_id, id, name, token_hash, expires_at)
                    VALUES (:event, :id, :actor, :disabled, NOW())
                    """).bind("event", eventId).bind("id", operatorId).bind("actor", actor)
                    .bind("disabled", "manual-" + operatorId).fetch().rowsUpdated()
                    .then(store.database().sql("""
                            INSERT INTO reception_checkin(event_id, guest_id, operator_id, arrived_at)
                            SELECT :event, :guest, :operator, :arrival
                            FROM reception_guest WHERE event_id = :event AND id = :guest AND active
                            ON CONFLICT (event_id, guest_id) DO NOTHING
                            """).bind("event", eventId).bind("guest", guestId).bind("operator", operatorId)
                            .bind("arrival", arrival).fetch().rowsUpdated())
                    .flatMap(count -> count == 1
                            ? audit.write(eventId, guestId, "MANUAL", actor, reason, arrival).thenReturn(true)
                            : Mono.error(new ResponseStatusException(CONFLICT, "Convidado inativo ou já registrado.")));
        })));
    }

    record ReportRow(UUID id, String name, String group, Rsvp rsvp, boolean active,
                     OffsetDateTime arrivedAt, String operator) {}
}
