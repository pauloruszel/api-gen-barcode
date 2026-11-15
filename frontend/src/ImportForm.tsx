import { useState } from 'react';
import Papa from 'papaparse';
import readXlsxFile from 'read-excel-file/browser';
import type { GuestRow } from './api';
import { type ColumnMap, discoverColumns, mapRows } from './rows';

const labels: Record<keyof ColumnMap, string> = { name: 'Nome', group: 'Grupo', id: 'ID do convidado', rsvp: 'RSVP' };

export function ImportForm({ onSubmit }: { onSubmit: (rows: GuestRow[]) => Promise<void> }) {
  const [table, setTable] = useState<string[][]>([]);
  const [columns, setColumns] = useState<ColumnMap>(discoverColumns([]));
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [headers, ...rows] = table;

  async function load(file?: File) {
    setTable([]);
    setError('');
    if (!file) return;
    if (file.size > 2_000_000) return setError('O arquivo deve ter até 2 MB.');
    setBusy(true);
    try {
      const data = file.name.toLowerCase().endsWith('.xlsx')
        ? ((await readXlsxFile(file))[0]?.data ?? []).map(row => row.map(cell => String(cell ?? '')))
        : await new Promise<string[][]>((resolve, reject) => {
          Papa.parse<string[]>(file, {
            skipEmptyLines: 'greedy',
            complete: result => {
              const error = result.errors.find(issue => issue.code !== 'UndetectableDelimiter');
              if (error) reject(new Error('CSV inválido. Verifique a estrutura do arquivo.'));
              else resolve(result.data);
            },
            error: reject,
          });
        });
      const nonEmpty = data.filter(row => row.some(cell => cell.trim()));
      if (nonEmpty.length < 2 || nonEmpty.length > 1001) throw new Error('Use um cabeçalho e de 1 a 1.000 linhas.');
      setColumns(discoverColumns(nonEmpty[0]));
      setTable(nonEmpty);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao ler o arquivo.');
    } finally {
      setBusy(false);
    }
  }

  return <form onSubmit={async event => {
    event.preventDefault();
    setError('');
    setBusy(true);
    try { await onSubmit(mapRows(rows, columns)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Falha na importação.'); }
    finally { setBusy(false); }
  }}>
    <label>Planilha de convidados
      <input type="file" accept=".csv,.xlsx" onChange={event => void load(event.target.files?.[0])} disabled={busy}/>
    </label>
    {headers && <>
      <div className="columns">{(Object.keys(labels) as (keyof ColumnMap)[]).map(field =>
        <label key={field}>{labels[field]}
          <select value={columns[field]} disabled={busy}
            onChange={event => setColumns({ ...columns, [field]: Number(event.target.value) })}>
            <option value={-1}>Selecione</option>
            {headers.map((header, index) => <option key={index} value={index}>{header || 'Coluna ' + (index + 1)}</option>)}
          </select>
        </label>)}</div>
      <p>{rows.length} linhas para importar</p>
      <button disabled={busy}>Revisar importação</button>
    </>}
    {error && <p role="alert">{error}</p>}
  </form>;
}
