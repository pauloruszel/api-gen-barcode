package com.br.apigenbarcode.reception;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/v2/events/{eventId}")
class ReportController {
    private final AdminAccess access;
    private final ReceptionStore lists;
    private final ReceptionReport report;
    private final ReceptionAudit audit;

    ReportController(AdminAccess access, ReceptionStore lists, ReceptionReport report, ReceptionAudit audit) {
        this.access = access;
        this.lists = lists;
        this.report = report;
        this.audit = audit;
    }

    @GetMapping("/report")
    Mono<List<ReceptionReport.ReportRow>> report(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                                 @PathVariable UUID eventId) {
        access.requireAdmin(key);
        return lists.event(eventId, false).thenMany(report.rows(eventId)).collectList();
    }

    @GetMapping("/history")
    Mono<List<ReceptionAudit.History>> history(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                               @PathVariable UUID eventId) {
        access.requireAdmin(key);
        return lists.event(eventId, false).thenMany(audit.history(eventId)).collectList();
    }

    @PostMapping("/extras")
    Mono<Map<String, UUID>> extra(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                   @PathVariable UUID eventId, @RequestBody Extra request) {
        access.requireAdmin(key);
        return report.addExtra(eventId, GuestRows.required(request.name(), "Convidado"),
                GuestRows.required(request.group(), "Grupo"), GuestRows.required(request.actor(), "Responsável"),
                GuestRows.required(request.reason(), "Motivo")).map(id -> Map.of("id", id));
    }

    @PostMapping("/guests/{guestId}/undo")
    Mono<Boolean> undo(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                       @PathVariable UUID eventId, @PathVariable UUID guestId, @RequestBody Correction request) {
        access.requireAdmin(key);
        return report.undo(eventId, guestId, GuestRows.required(request.actor(), "Responsável"),
                GuestRows.required(request.reason(), "Motivo"));
    }

    @PostMapping("/guests/{guestId}/manual-arrival")
    Mono<Boolean> manual(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                         @PathVariable UUID eventId, @PathVariable UUID guestId, @RequestBody Correction request) {
        access.requireAdmin(key);
        return report.recordManual(eventId, guestId, GuestRows.required(request.actor(), "Responsável"),
                GuestRows.required(request.reason(), "Motivo"), request.arrivedAt());
    }

    record Extra(String name, String group, String actor, String reason) {}
    record Correction(String actor, String reason, OffsetDateTime arrivedAt) {}
}
