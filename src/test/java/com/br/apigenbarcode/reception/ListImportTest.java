package com.br.apigenbarcode.reception;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.junit.jupiter.api.Assertions.*;

class ListImportTest {
    private final Guest ana = new Guest(UUID.randomUUID(), UUID.randomUUID(), "Família Silva", "Ana", Rsvp.YES, null);

    @Test
    void usesStableIdEvenWhenNameAndGroupChange() {
        assertEquals(List.of(new Match(ana.id(), false)), ListImport.matches(List.of(ana),
                List.of(new GuestRow(ana.id(), "Novo grupo", "Ana Maria", Rsvp.YES))));
    }

    @Test
    void suggestsOnlyUnambiguousMatches() {
        var row = new GuestRow(null, "familia silva", "Ana", Rsvp.YES);
        assertEquals(List.of(new Match(ana.id(), false)), ListImport.matches(List.of(ana), List.of(row)));
        assertEquals(List.of(new Match(null, true), new Match(null, true)),
                ListImport.matches(List.of(ana), List.of(row, row)));
    }

    @Test
    void rejectsForeignIdentifiers() {
        var row = new GuestRow(UUID.randomUUID(), "", "Ana", Rsvp.YES);
        assertThrows(ResponseStatusException.class, () -> ListImport.matches(List.of(ana), List.of(row)));
    }
}
