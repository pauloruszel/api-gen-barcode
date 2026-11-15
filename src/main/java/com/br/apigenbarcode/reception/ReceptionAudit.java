package com.br.apigenbarcode.reception;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
class ReceptionAudit {
    private final DatabaseClient db;

    ReceptionAudit(DatabaseClient db) {
        this.db = db;
    }

    Mono<Void> write(UUID eventId, UUID guestId, String action, String actor, String reason, OffsetDateTime arrival) {
        var query = db.sql("""
                INSERT INTO reception_log(event_id, guest_id, action, actor, reason, arrived_at)
                VALUES (:event, :guest, :action, :actor, :reason, :arrival)
                """).bind("event", eventId).bind("guest", guestId).bind("action", action)
                .bind("actor", actor).bind("reason", reason);
        return (arrival == null ? query.bindNull("arrival", OffsetDateTime.class) : query.bind("arrival", arrival))
                .fetch().rowsUpdated().then();
    }

    Flux<History> history(UUID eventId) {
        return db.sql("""
                SELECT l.id, g.name, l.action, l.actor, l.reason, l.arrived_at, l.recorded_at
                FROM reception_log l
                JOIN reception_guest g ON g.event_id = l.event_id AND g.id = l.guest_id
                WHERE l.event_id = :event ORDER BY l.id DESC
                """).bind("event", eventId).map((row, metadata) -> new History(
                row.get("id", Long.class), row.get("name", String.class), row.get("action", String.class),
                row.get("actor", String.class), row.get("reason", String.class),
                row.get("arrived_at", OffsetDateTime.class), row.get("recorded_at", OffsetDateTime.class))).all();
    }

    record History(long id, String name, String action, String actor, String reason,
                   OffsetDateTime arrivedAt, OffsetDateTime recordedAt) {}
}
