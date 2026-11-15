package com.br.apigenbarcode.reception;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.br.apigenbarcode.reception.ReceptionModels.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Service
class ListImport {
    private final ReceptionStore store;

    ListImport(ReceptionStore store) {
        this.store = store;
    }

    Mono<ImportPreview> preview(UUID eventId, List<GuestRow> incoming) {
        return store.inTransaction(store.event(eventId, true)
                .flatMap(event -> store.snapshot(eventId)
                        .map(current -> new ImportPreview(event.revision(), current, incoming, matches(current, incoming)))));
    }

    static List<Match> matches(List<Guest> current, List<GuestRow> incoming) {
        var known = current.stream().collect(Collectors.toMap(Guest::id, Function.identity()));
        var claimed = new HashSet<UUID>();
        for (GuestRow row : incoming) {
            if (row.id() != null && (!known.containsKey(row.id()) || !claimed.add(row.id()))) {
                throw new ResponseStatusException(BAD_REQUEST, "Há um ID repetido ou que não pertence à lista ativa.");
            }
        }
        var candidates = current.stream().filter(guest -> !claimed.contains(guest.id()))
                .collect(Collectors.groupingBy(guest -> identity(guest.name(), guest.group())));
        var counts = incoming.stream().filter(row -> row.id() == null)
                .collect(Collectors.groupingBy(row -> identity(row.name(), row.group()), Collectors.counting()));
        return incoming.stream().map(row -> {
            if (row.id() != null) {
                return new Match(row.id(), false);
            }
            Identity identity = identity(row.name(), row.group());
            List<Guest> exact = candidates.getOrDefault(identity, List.of());
            if (exact.size() == 1 && counts.get(identity) == 1) {
                return new Match(exact.getFirst().id(), false);
            }
            return new Match(null, !current.isEmpty() || counts.get(identity) > 1);
        }).toList();
    }

    Mono<Event> apply(UUID eventId, long revision, List<GuestRow> incoming, List<UUID> remove) {
        return store.inTransaction(store.event(eventId, true).flatMap(event -> {
            if (event.revision() != revision) {
                return Mono.error(new ResponseStatusException(CONFLICT, "A lista mudou. Gere uma nova prévia."));
            }
            return store.snapshot(eventId).flatMap(current -> applyChanges(event, current, incoming, remove));
        }));
    }

    private Mono<Event> applyChanges(Event event, List<Guest> current, List<GuestRow> incoming, List<UUID> remove) {
        var known = current.stream().collect(Collectors.toMap(Guest::id, Function.identity()));
        var updates = new ArrayList<GuestRow>();
        var additions = new ArrayList<GuestRow>();
        var selected = new HashSet<UUID>();
        var newIdentities = new HashSet<Identity>();

        for (GuestRow row : incoming) {
            if (row.id() == null) {
                String group = groupOrName(row);
                if (!newIdentities.add(identity(row.name(), group))) {
                    throw new ResponseStatusException(BAD_REQUEST, "Há novos convidados indistinguíveis. Use grupos diferentes.");
                }
                additions.add(new GuestRow(null, group, row.name(), row.rsvp() == null ? Rsvp.UNKNOWN : row.rsvp()));
                continue;
            }
            Guest existing = known.get(row.id());
            if (existing == null || !selected.add(row.id())) {
                throw new ResponseStatusException(BAD_REQUEST, "Selecione cada convidado ativo uma única vez.");
            }
            String group = row.group().isBlank() || normalize(row.group()).equals(normalize(existing.group()))
                    ? existing.group() : row.group();
            Rsvp status = row.rsvp() == null ? existing.rsvp() : row.rsvp();
            if (!existing.name().equals(row.name()) || !existing.group().equals(group) || existing.rsvp() != status) {
                updates.add(new GuestRow(row.id(), group, row.name(), status));
            }
        }
        List<UUID> removals = remove == null ? List.of() : remove.stream().distinct().toList();
        if (removals.size() > 1000 || removals.stream().anyMatch(id -> !known.containsKey(id) || selected.contains(id))) {
            throw new ResponseStatusException(BAD_REQUEST, "A remoção deve indicar convidados ausentes nesta versão.");
        }
        var occupied = new HashSet<Identity>();
        current.stream().filter(guest -> !selected.contains(guest.id()) && !removals.contains(guest.id()))
                .forEach(guest -> occupied.add(identity(guest.name(), guest.group())));
        incoming.stream().filter(row -> row.id() != null).forEach(row -> {
            Guest existing = known.get(row.id());
            occupied.add(identity(row.name(), row.group().isBlank() ? existing.group() : row.group()));
        });
        if (newIdentities.stream().anyMatch(occupied::contains)) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Um novo convidado tem o mesmo nome e grupo de outro. Revise a correspondência.");
        }
        if (additions.isEmpty() && updates.isEmpty() && removals.isEmpty()) {
            return Mono.just(event);
        }
        UUID id = event.id();
        return Flux.fromIterable(additions).concatMap(row -> store.insert(id, row))
                .thenMany(Flux.fromIterable(updates).concatMap(row -> store.update(id, row, known.get(row.id()))))
                .thenMany(Flux.fromIterable(removals).concatMap(guestId -> store.deactivate(id, guestId)))
                .then(store.nextRevision(id)).flatMap(next ->
                        store.recordRevision(id, next, additions.size(), updates.size(), removals.size())
                                .thenReturn(new Event(id, event.name(), next)));
    }

    private static Identity identity(String name, String group) {
        return new Identity(normalize(name), normalize(group.isBlank() ? name : group));
    }

    private static String groupOrName(GuestRow row) {
        return row.group().isBlank() ? row.name() : row.group();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private record Identity(String name, String group) {}
}
