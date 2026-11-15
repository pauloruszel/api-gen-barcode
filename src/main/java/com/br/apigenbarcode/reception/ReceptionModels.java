package com.br.apigenbarcode.reception;

import java.util.List;
import java.util.UUID;

public final class ReceptionModels {
    private ReceptionModels() {}

    public enum Rsvp { UNKNOWN, YES, NO }

    public record Event(UUID id, String name, long revision) {}
    public record CreateEvent(String name) {}
    public record Guest(UUID id, UUID inviteId, String group, String name, Rsvp rsvp) {}
    public record GuestRow(UUID id, String group, String name, Rsvp rsvp) {}
    public record ImportRequest(long revision, List<GuestRow> rows) {}
}
