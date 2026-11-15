import { useEffect, useState } from 'react';
import { type Rsvp, request } from './api';
import { downloadCsv } from './guestExport';

type Row = { id: string; name: string; group: string; rsvp: Rsvp; active: boolean; arrivedAt: string | null; operator: string | null };
type Entry = { id: number; name: string; action: string; actor: string; reason: string; recordedAt: string };
const actions: Record<string, string> = {
  CHECK_IN: 'Entrada', EXTRA: 'Extra autorizado', UNDO: 'Entrada desfeita', MANUAL: 'Entrada de contingência',
};
const dateTime = (value: string) => new Date(value).toLocaleString('pt-BR');
const rsvp = { YES: 'Confirmado', NO: 'Recusado', UNKNOWN: 'Pendente' };

export function Report({ eventId, accessKey, revision, onExtra }: {
  eventId: string; accessKey: string; revision: number; onExtra: () => Promise<void>;
}) {
  const [rows, setRows] = useState<Row[]>([]);
  const [history, setHistory] = useState<Entry[]>([]);
  const [actor, setActor] = useState('');
  const [reason, setReason] = useState('');
  const [name, setName] = useState('');
  const [group, setGroup] = useState('');
  const [guestId, setGuestId] = useState('');
  const [time, setTime] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const api = '/' + eventId;

  async function refresh() {
    const [guests, entries] = await Promise.all([
      request<Row[]>(accessKey, api + '/report'), request<Entry[]>(accessKey, api + '/history'),
    ]);
    setRows(guests);
    setHistory(entries);
  }

  async function run(action: () => Promise<void>) {
    setBusy(true);
    setError('');
    try { await action(); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Não foi possível concluir.'); }
    finally { setBusy(false); }
  }

  useEffect(() => { void run(refresh); }, [eventId, accessKey, revision]);

  const guest = rows.find(row => row.id === guestId);
  const arrived = rows.filter(row => row.arrivedAt).length;
  const expected = rows.filter(row => row.active).length;

  return <section>
    <h2>Chegadas e exceções</h2>
    <p>{arrived} presentes · {expected} convidados ativos · {rows.filter(row => row.active && row.rsvp === 'YES').length} confirmados</p>
    <button disabled={busy} onClick={() => void run(refresh)}>Atualizar chegadas</button>
    <button disabled={busy || !rows.length} onClick={() => downloadCsv('relatorio-chegadas.csv',
      ['Convidado', 'Grupo', 'RSVP', 'Lista ativa', 'Chegada', 'Responsável'], rows.map(row => [
        row.name, row.group, rsvp[row.rsvp], row.active ? 'Sim' : 'Não', row.arrivedAt ?? '', row.operator ?? '',
      ]))}>Exportar lista de contingência</button>
    <details>
      <summary>Corrigir uma entrada ou autorizar convidado extra</summary>
      <label>Responsável<input value={actor} onChange={e => setActor(e.target.value)} /></label>
      <label>Motivo<input value={reason} onChange={e => setReason(e.target.value)} /></label>
      <form onSubmit={e => {
        e.preventDefault();
        if (!guest) return;
        void run(async () => {
          await request(accessKey, api + '/guests/' + guest.id + (guest.arrivedAt ? '/undo' : '/manual-arrival'),
            { actor, reason, arrivedAt: guest.arrivedAt ? null : new Date(time).toISOString() });
          setTime('');
          await refresh();
        });
      }}>
        <label>Convidado para correção<select value={guestId} onChange={e => setGuestId(e.target.value)}>
          <option value="">Selecione o convidado</option>
          {rows.map(row => <option key={row.id} value={row.id}>{row.name} — {row.group}</option>)}
        </select></label>
        {guest && !guest.arrivedAt && <label>Horário anotado no local
          <input type="datetime-local" required value={time} onChange={e => setTime(e.target.value)} />
        </label>}
        <button disabled={busy || !guest || !actor.trim() || !reason.trim()}>
          {guest?.arrivedAt ? 'Desfazer entrada' : 'Registrar chegada anotada'}
        </button>
      </form>
      <form onSubmit={e => {
        e.preventDefault();
        void run(async () => {
          await request(accessKey, api + '/extras', { name, group, actor, reason });
          setName('');
          setGroup('');
          await onExtra();
          await refresh();
        });
      }}>
        <label>Nome do convidado extra<input required value={name} onChange={e => setName(e.target.value)} /></label>
        <label>Grupo do convidado extra<input required value={group} onChange={e => setGroup(e.target.value)} /></label>
        <button disabled={busy || !actor.trim() || !reason.trim()}>Autorizar convidado extra</button>
      </form>
    </details>
    <details>
      <summary>Histórico de entradas e correções</summary>
      {history.length === 0 ? <p>Nenhuma ocorrência registrada.</p> : <ul>{history.map(entry => <li key={entry.id}>
        {actions[entry.action] ?? entry.action} · {entry.name} · {entry.actor} · {dateTime(entry.recordedAt)}
        {entry.reason && ' · ' + entry.reason}
      </li>)}</ul>}
    </details>
    {error && <p role="alert">{error}</p>}
  </section>;
}
