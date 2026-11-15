package com.br.apigenbarcode.reception;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeskStoreTest {
    @Test
    void acceptsOnlyCanonicalInviteIdentifiers() {
        UUID id = UUID.randomUUID();
        assertEquals(id, DeskStore.decode(id.toString()));
        assertThrows(ResponseStatusException.class, () -> DeskStore.decode("https://example.org/" + id));
        assertThrows(ResponseStatusException.class, () -> DeskStore.decode("1-1-1-1-1"));
    }
}
