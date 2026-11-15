package com.br.apigenbarcode.reception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ReceptionModels {
    private ReceptionModels() {}

    public enum Rsvp { UNKNOWN, YES, NO }

    public record Event(UUID id, String name, long revision) {}
    public record CreateEvent(String name) {}
    public record Guest(UUID id, UUID inviteId, String group, String name, Rsvp rsvp, OffsetDateTime arrivedAt) {}
    public record GuestRow(UUID id, String group, String name, Rsvp rsvp) {}
    public record ImportRequest(long revision, List<GuestRow> rows, List<UUID> remove) {}
    public record ImportPreviewRequest(List<GuestRow> rows) {}
    public record ImportPreview(long revision, List<Guest> current, List<GuestRow> incoming,
                                List<Match> matches) {}
    public record Match(UUID id, boolean needsReview) {}
}
