import type { GuestRow, Rsvp } from './api.ts';

export type ColumnMap = { name: number; group: number; id: number; rsvp: number };
export const normalize = (text: string) => text.normalize('NFD').replace(/\p{M}/gu, '').trim().replace(/\s+/g, ' ').toLowerCase();

export function discoverColumns(headers: string[]): ColumnMap {
  const normalized = headers.map(normalize);
  const find = (...options: string[]) => normalized.findIndex(header => options.includes(header));
  return {
    name: find('nome', 'convidado', 'nome completo'),
    group: find('grupo', 'familia', 'convite'),
    id: find('id', 'id do convidado'),
    rsvp: find('rsvp', 'confirmacao', 'status rsvp'),
  };
}

export function mapRows(data: string[][], columns: ColumnMap): GuestRow[] {
  if (columns.name < 0) throw new Error('Escolha a coluna de nomes.');
  return data.map((cells, index) => {
    const text = (column: number) => cells[column]?.trim() ?? '';
    const name = text(columns.name);
    const id = text(columns.id);
    if (!name) throw new Error('Nome ausente na linha ' + (index + 2) + '.');
    if (id && !/^[\da-f]{8}(?:-[\da-f]{4}){3}-[\da-f]{12}$/i.test(id)) {
      throw new Error('ID inválido na linha ' + (index + 2) + '.');
    }
    const status = normalize(text(columns.rsvp));
    const rsvp: Rsvp | null = !status ? null : ['sim', 'confirmado', 'yes'].includes(status) ? 'YES'
      : ['nao', 'recusado', 'no'].includes(status) ? 'NO' : 'UNKNOWN';
    return { id: id || null, name, group: text(columns.group), rsvp };
  });
}
