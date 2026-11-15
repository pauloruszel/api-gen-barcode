import Papa from 'papaparse';
import type { Guest } from './api';

export function downloadCsv(filename: string, headers: string[], rows: (string | number)[][]) {
  const csv = Papa.unparse({ fields: headers, data: rows }, { delimiter: ';', escapeFormulae: true });
  const url = URL.createObjectURL(new Blob(['\uFEFF', csv], { type: 'text/csv;charset=utf-8' }));
  const anchor = Object.assign(document.createElement('a'), { href: url, download: filename });
  anchor.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function exportGuests(guests: Guest[]) {
  downloadCsv('convidados.csv', ['ID do convidado', 'Nome', 'Grupo', 'RSVP'],
    guests.map(guest => [guest.id, guest.name, guest.group, guest.rsvp]));
}
