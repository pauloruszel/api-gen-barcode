package com.br.apigenbarcode.reception;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.junit.jupiter.api.Assertions.*;

class GuestRowsTest {
    @Test
    void trimsNamesAndDefaultsMissingRsvp() {
        assertEquals(List.of(new GuestRow(null, "Ana", "Ana", Rsvp.UNKNOWN)),
                GuestRows.validate(List.of(new GuestRow(null, null, " Ana ", null))));
    }

    @Test
    void reportsInvalidRow() {
        var error = assertThrows(ResponseStatusException.class,
                () -> GuestRows.validate(List.of(new GuestRow(null, null, " ", null))));
        assertTrue(error.getReason().contains("Linha 2"));
    }
}
