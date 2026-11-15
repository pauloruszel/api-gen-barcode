package com.br.apigenbarcode.reception;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/v2/events")
class ReceptionController {
    private final ReceptionStore store;
    private final AdminAccess access;
    private final ListImport imports;

    ReceptionController(ReceptionStore store, AdminAccess access, ListImport imports) {
        this.store = store;
        this.access = access;
        this.imports = imports;
    }

    @PostMapping
    Mono<Event> create(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                       @RequestBody CreateEvent request) {
        access.requireAdmin(key);
        return store.createEvent(GuestRows.required(request.name(), "Nome do evento"));
    }

    @GetMapping("/{eventId}")
    Mono<Event> event(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                      @PathVariable UUID eventId) {
        access.requireAdmin(key);
        return store.event(eventId, false);
    }

    @GetMapping("/{eventId}/guests")
    Mono<List<Guest>> guests(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                            @PathVariable UUID eventId) {
        access.requireAdmin(key);
        return store.event(eventId, false).thenMany(store.guests(eventId)).collectList();
    }

    @PostMapping("/{eventId}/imports/preview")
    Mono<ImportPreview> preview(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                @PathVariable UUID eventId, @RequestBody ImportPreviewRequest request) {
        access.requireAdmin(key);
        return imports.preview(eventId, GuestRows.validate(request.rows()));
    }

    @PostMapping("/{eventId}/imports")
    Mono<Event> apply(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                      @PathVariable UUID eventId, @RequestBody ImportRequest request) {
        access.requireAdmin(key);
        return imports.apply(eventId, request.revision(), GuestRows.validate(request.rows()), request.remove());
    }
}
