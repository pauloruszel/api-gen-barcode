package com.br.apigenbarcode.reception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

final class DeskModels {
    private DeskModels() {}

    record CreateOperator(String name) {}
    record OperatorAccess(String token, OffsetDateTime expiresAt) {}
    record Operator(UUID id, String name) {}
    record DeskGuest(UUID id, UUID inviteId, String group, String name, OffsetDateTime arrivedAt) {}
    record CheckinRequest(List<UUID> guestIds) {}
    record CheckinResult(UUID id, boolean recorded, OffsetDateTime arrivedAt) {}
    record InviteQr(String image) {}
}
