package com.br.apigenbarcode.reception;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import static com.br.apigenbarcode.reception.DeskModels.*;
import static org.springframework.http.HttpStatus.*;

@Repository
class DeskStore {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ReceptionStore lists;
    private final ReceptionAudit audit;

    DeskStore(ReceptionStore lists, ReceptionAudit audit) {
        this.lists = lists;
        this.audit = audit;
    }

    Mono<OperatorAccess> createOperator(UUID eventId, String name) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        OffsetDateTime expiry = OffsetDateTime.now(ZoneOffset.UTC).plusHours(24);
        return lists.event(eventId, false).then(lists.database().sql("""
                INSERT INTO reception_operator(event_id, id, name, token_hash, expires_at)
                VALUES (:event, :id, :name, :hash, :expiry)
                """).bind("event", eventId).bind("id", UUID.randomUUID()).bind("name", name)
                .bind("hash", digest(token)).bind("expiry", expiry).fetch().rowsUpdated())
                .thenReturn(new OperatorAccess(token, expiry));
    }

    Mono<Operator> operator(UUID eventId, String header) {
        if (header == null || !header.matches("Bearer [A-Za-z0-9_-]{43}")) {
            return Mono.error(unauthorized());
        }
        return lists.database().sql("""
                SELECT id, name FROM reception_operator
                WHERE event_id = :event AND token_hash = :hash AND expires_at > NOW()
                """).bind("event", eventId).bind("hash", digest(header.substring(7)))
                .map((row, metadata) -> new Operator(row.get("id", UUID.class), row.get("name", String.class)))
                .one().switchIfEmpty(Mono.error(unauthorized()));
    }

    Flux<DeskGuest> guests(UUID eventId, UUID inviteId) {
        String filter = inviteId == null ? "" : " AND g.invite_id = :invite";
        DatabaseClient.GenericExecuteSpec query = lists.database().sql("""
                SELECT g.id, g.invite_id, i.label, g.name, c.arrived_at
                FROM reception_guest g
                JOIN reception_invite i ON i.event_id = g.event_id AND i.id = g.invite_id
                LEFT JOIN reception_checkin c ON c.event_id = g.event_id AND c.guest_id = g.id
                WHERE g.event_id = :event AND g.active
                """ + filter + " ORDER BY g.name, g.id").bind("event", eventId);
        if (inviteId != null) {
            query = query.bind("invite", inviteId);
        }
        return query.map((row, metadata) -> new DeskGuest(
                row.get("id", UUID.class), row.get("invite_id", UUID.class), row.get("label", String.class),
                row.get("name", String.class), row.get("arrived_at", OffsetDateTime.class))).all();
    }

    Mono<List<CheckinResult>> checkIn(UUID eventId, Operator operator, List<UUID> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 50 || ids.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ResponseStatusException(BAD_REQUEST, "Selecione de 1 a 50 convidados.");
        }
        return lists.inTransaction(lists.event(eventId, true).thenMany(Flux.fromIterable(ids)
                .distinct().concatMap(id -> checkOne(eventId, operator, id))).collectList());
    }

    private Mono<CheckinResult> checkOne(UUID eventId, Operator operator, UUID id) {
        return lists.database().sql("""
                INSERT INTO reception_checkin(event_id, guest_id, operator_id)
                SELECT :event, :guest, :operator
                FROM reception_guest WHERE event_id = :event AND id = :guest AND active
                ON CONFLICT (event_id, guest_id) DO NOTHING
                RETURNING arrived_at
                """).bind("event", eventId).bind("guest", id).bind("operator", operator.id())
                .map((row, metadata) -> new CheckinResult(id, true, row.get("arrived_at", OffsetDateTime.class)))
                .one().flatMap(result -> audit.write(eventId, id, "CHECK_IN", operator.name(), "", result.arrivedAt())
                        .thenReturn(result))
                .switchIfEmpty(Mono.defer(() -> lists.database().sql("""
                        SELECT c.arrived_at FROM reception_checkin c
                        JOIN reception_guest g ON g.event_id = c.event_id AND g.id = c.guest_id
                        WHERE c.event_id = :event AND c.guest_id = :guest AND g.active
                        """).bind("event", eventId).bind("guest", id)
                .map((row, metadata) -> new CheckinResult(id, false, row.get("arrived_at", OffsetDateTime.class)))
                .one().switchIfEmpty(Mono.error(new ResponseStatusException(CONFLICT,
                        "Um dos convidados não está mais na lista ativa. Atualize a consulta.")))));
    }

    static UUID decode(String text) {
        if (text == null || !text.matches("(?i)[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")) {
            throw new ResponseStatusException(BAD_REQUEST, "QR Code de convite inválido.");
        }
        return UUID.fromString(text);
    }

    private static String digest(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static ResponseStatusException unauthorized() {
        return new ResponseStatusException(UNAUTHORIZED, "Link da recepção inválido ou expirado.");
    }
}
