package com.br.apigenbarcode.reception;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Repository
class ReceptionStore {
    final DatabaseClient db;
    final TransactionalOperator transaction;

    ReceptionStore(DatabaseClient db, ReactiveTransactionManager manager) {
        this.db = db;
        this.transaction = TransactionalOperator.create(manager);
    }

    Mono<Event> createEvent(String name) {
        UUID id = UUID.randomUUID();
        return db.sql("INSERT INTO reception_event(id, name) VALUES (:id, :name)")
                .bind("id", id).bind("name", name).fetch().rowsUpdated()
                .thenReturn(new Event(id, name, 0));
    }

    Mono<Event> event(UUID id, boolean lock) {
        String query = "SELECT id, name, revision FROM reception_event WHERE id = :id";
        return db.sql(lock ? query + " FOR UPDATE" : query).bind("id", id)
                .map((row, metadata) -> new Event(row.get("id", UUID.class),
                        row.get("name", String.class), row.get("revision", Long.class)))
                .one().switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Evento não encontrado.")));
    }

    Flux<Guest> guests(UUID eventId) {
        return db.sql("""
                SELECT g.id, g.invite_id, i.label, g.name, g.rsvp
                FROM reception_guest g
                JOIN reception_invite i ON i.event_id = g.event_id AND i.id = g.invite_id
                WHERE g.event_id = :event AND g.active
                ORDER BY i.label, g.name, g.id
                """).bind("event", eventId).map((row, metadata) -> new Guest(
                row.get("id", UUID.class), row.get("invite_id", UUID.class), row.get("label", String.class),
                row.get("name", String.class), Rsvp.valueOf(row.get("rsvp", String.class)))).all();
    }

    Mono<UUID> invite(UUID eventId, String group) {
        return db.sql("""
                INSERT INTO reception_invite(event_id, id, label) VALUES (:event, :id, :label)
                ON CONFLICT (event_id, label) DO UPDATE SET label = EXCLUDED.label RETURNING id
                """).bind("event", eventId).bind("id", UUID.randomUUID()).bind("label", group)
                .map((row, metadata) -> row.get("id", UUID.class)).one();
    }

    Mono<Void> insert(UUID eventId, GuestRow row) {
        return invite(eventId, row.group()).flatMap(inviteId -> db.sql("""
                INSERT INTO reception_guest(event_id, id, invite_id, name, rsvp)
                VALUES (:event, :id, :invite, :name, :rsvp)
                """).bind("event", eventId).bind("id", UUID.randomUUID()).bind("invite", inviteId)
                .bind("name", row.name()).bind("rsvp", row.rsvp().name())
                .fetch().rowsUpdated()).then();
    }

    Mono<Long> nextRevision(UUID eventId) {
        return db.sql("UPDATE reception_event SET revision = revision + 1 WHERE id = :id RETURNING revision")
                .bind("id", eventId).map((row, metadata) -> row.get("revision", Long.class)).one();
    }

    Mono<Long> importFirst(UUID eventId, long revision, java.util.List<GuestRow> rows) {
        return transaction.transactional(event(eventId, true).flatMap(event -> {
            if (revision != 0 || event.revision() != revision) {
                return Mono.error(new ResponseStatusException(CONFLICT, "A lista já foi importada."));
            }
            return Flux.fromIterable(rows).concatMap(row -> insert(eventId, row)).then(nextRevision(eventId));
        }));
    }
}
