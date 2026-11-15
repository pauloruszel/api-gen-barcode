import { test } from 'node:test';
import assert from 'node:assert/strict';
import { discoverColumns, mapRows } from './rows.ts';

test('maps common accented spreadsheet headings and keeps group optional', () => {
  const columns = discoverColumns(['Família', 'Nome completo', 'Confirmação']);
  assert.deepEqual(mapRows([[' Silva ', ' Ana ', 'Sim'], ['', 'João', 'Não']], columns), [
    { id: null, name: 'Ana', group: 'Silva', rsvp: 'YES' },
    { id: null, name: 'João', group: '', rsvp: 'NO' },
  ]);
});

test('identifies the row when the name or identifier is invalid', () => {
  const columns = discoverColumns(['Nome', 'ID']);
  assert.throws(() => mapRows([['Ana', 'invalid']], columns), /linha 2/);
  assert.throws(() => mapRows([['']], columns), /linha 2/);
});

test('keeps missing RSVP distinct from a supplied pending status', () => {
  const rows = mapRows([['Ana', ''], ['Bia', 'Pendente']], discoverColumns(['Nome', 'RSVP']));
  assert.equal(rows[0].rsvp, null);
  assert.equal(rows[1].rsvp, 'UNKNOWN');
});
