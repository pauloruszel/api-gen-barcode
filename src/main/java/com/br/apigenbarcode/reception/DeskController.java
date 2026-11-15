package com.br.apigenbarcode.reception;

import com.br.apigenbarcode.service.QRCodeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static com.br.apigenbarcode.reception.DeskModels.*;
import static com.br.apigenbarcode.reception.ReceptionModels.Event;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v2/events/{eventId}")
class DeskController {
    private final AdminAccess access;
    private final ReceptionStore lists;
    private final DeskStore desk;
    private final QRCodeService qr;

    DeskController(AdminAccess access, ReceptionStore lists, DeskStore desk, QRCodeService qr) {
        this.access = access;
        this.lists = lists;
        this.desk = desk;
        this.qr = qr;
    }

    @PostMapping("/operators")
    Mono<OperatorAccess> createOperator(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                        @PathVariable UUID eventId, @RequestBody CreateOperator request) {
        access.requireAdmin(key);
        return desk.createOperator(eventId, GuestRows.required(request.name(), "Nome do operador"));
    }

    @GetMapping("/invites/{inviteId}/qr")
    Mono<InviteQr> qr(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                      @PathVariable UUID eventId, @PathVariable UUID inviteId) {
        access.requireAdmin(key);
        return desk.guests(eventId, inviteId).hasElements().flatMap(exists -> {
            if (!exists) {
                return Mono.error(new ResponseStatusException(NOT_FOUND, "Convite sem convidados ativos."));
            }
            return qr.gerarQRCodeReativo(inviteId.toString(), 320, "#000000", "#FFFFFF")
                    .map(bytes -> new InviteQr(qr.encodeBase64(bytes)));
        });
    }

    @GetMapping("/desk/event")
    Mono<Event> event(@RequestHeader(value = AUTHORIZATION, required = false) String key, @PathVariable UUID eventId) {
        return desk.operator(eventId, key).then(lists.event(eventId, false));
    }

    @GetMapping("/desk/guests")
    Mono<List<DeskGuest>> guests(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                @PathVariable UUID eventId) {
        return desk.operator(eventId, key).thenMany(desk.guests(eventId, null)).collectList();
    }

    @GetMapping("/desk/invites/{code}")
    Mono<List<DeskGuest>> invite(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                 @PathVariable UUID eventId, @PathVariable String code) {
        return desk.operator(eventId, key).then(Mono.defer(() -> desk.guests(eventId, DeskStore.decode(code))
                .collectList().filter(guests -> !guests.isEmpty())
                .switchIfEmpty(Mono.error(new ResponseStatusException(NOT_FOUND, "Convite não localizado neste evento.")))));
    }

    @PostMapping("/desk/checkins")
    Mono<List<CheckinResult>> checkin(@RequestHeader(value = AUTHORIZATION, required = false) String key,
                                      @PathVariable UUID eventId, @RequestBody CheckinRequest request) {
        return desk.operator(eventId, key).flatMap(operator -> desk.checkIn(eventId, operator, request.guestIds()));
    }
}
