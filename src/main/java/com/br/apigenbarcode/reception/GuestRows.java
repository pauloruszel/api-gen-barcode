package com.br.apigenbarcode.reception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

final class GuestRows {
    private GuestRows() {}

    static String required(String value, String field) {
        if (value == null || value.isBlank() || value.strip().length() > 160) {
            throw new ResponseStatusException(BAD_REQUEST, field + " deve ter de 1 a 160 caracteres.");
        }
        return value.strip();
    }

    static List<GuestRow> validate(List<GuestRow> incoming) {
        if (incoming == null || incoming.isEmpty() || incoming.size() > 1000) {
            throw new ResponseStatusException(BAD_REQUEST, "Importe de 1 a 1.000 convidados.");
        }
        var rows = new ArrayList<GuestRow>();
        for (int index = 0; index < incoming.size(); index++) {
            GuestRow row = incoming.get(index);
            String prefix = "Linha " + (index + 2) + ": ";
            if (row == null) {
                throw new ResponseStatusException(BAD_REQUEST, prefix + "linha inválida.");
            }
            String name = required(row.name(), prefix + "nome");
            String group = row.group() == null || row.group().isBlank()
                    ? name : required(row.group(), prefix + "grupo");
            rows.add(new GuestRow(row.id(), group, name, row.rsvp() == null ? Rsvp.UNKNOWN : row.rsvp()));
        }
        return List.copyOf(rows);
    }
}
