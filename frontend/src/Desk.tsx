import { useEffect, useState } from 'react';
import { type CheckinResult, type DeskGuest, type Event, request } from './api';
import { Camera } from './Camera';
import { normalize } from './rows';

const statusTime = (value: string) => new Date(value).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
const eventId = new URLSearchParams(location.search).get('evento') ?? '';
const storageKey = 'reception:' + eventId;

function accessToken() {
  const linkToken = new URLSearchParams(location.hash.slice(1)).get('chave');
  if (linkToken) {
    sessionStorage.setItem(storageKey, linkToken);
    history.replaceState(null, '', location.pathname + location.search);
  }
  return sessionStorage.getItem(storageKey) ?? '';
}

export function Desk() {
  const [key] = useState(accessToken);
  const [event, setEvent] = useState<Event>();
  const [guests, setGuests] = useState<DeskGuest[]>([]);
  const [invite, setInvite] = useState<DeskGuest[]>();
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<string[]>([]);
  const [camera, setCamera] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [status, setStatus] = useState('');
  const api = '/' + eventId + '/desk';
  const search = normalize(query);
  const visible = invite ?? (search
    ? guests.filter(guest => normalize(guest.name + ' ' + guest.group).includes(search)).slice(0, 40) : []);

  useEffect(() => {
    if (!key || !eventId) return;
    let active = true;
    void request<Event>(key, api + '/event').then(data => { if (active) setEvent(data); })
      .catch(cause => { if (active) setError(String(cause.message)); });
    return () => { active = false; };
  }, [key, api]);

  async function run(action: () => Promise<void>) {
    setError('');
    setBusy(true);
    try { await action(); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Operação não concluída.'); }
    finally { setBusy(false); }
  }

  async function refresh() {
    setGuests(await request<DeskGuest[]>(key, api + '/guests'));
  }

  function openInvite(members: DeskGuest[]) {
    setInvite(members);
    setSelected([]);
    setStatus('');
  }

  function applyCheckins(results: CheckinResult[]) {
    const arrivals = new Map(results.map(result => [result.id, result.arrivedAt]));
    const applyResult = (guest: DeskGuest) => ({ ...guest, arrivedAt: arrivals.get(guest.id) ?? guest.arrivedAt });
    setGuests(current => current.map(applyResult));
    setInvite(current => current?.map(applyResult));
    setSelected([]);
    const recorded = results.filter(result => result.recorded).length;
    setStatus(recorded + ' entradas registradas · ' + (results.length - recorded) + ' já registradas');
  }

  return <main>
    <header><h1>{event?.name ?? 'Recepção'}</h1></header>
    {!event && !error && <p>{key && eventId ? 'Abrindo a recepção…' : 'Abra o link enviado pelo cerimonial.'}</p>}
    {event && <section>
      <h2>Registrar chegada</h2>
      <button disabled={busy || camera} onClick={() => setCamera(true)}>Escanear QR</button>
      {camera && <Camera close={() => setCamera(false)} onScan={code => {
        setCamera(false);
        setInvite(undefined);
        setSelected([]);
        void run(async () => openInvite(await request<DeskGuest[]>(key, api + '/invites/' + encodeURIComponent(code))));
      }} />}
      <form onSubmit={form => {
        form.preventDefault();
        setInvite(undefined);
        setSelected([]);
        setStatus('');
        void run(refresh);
      }}>
        <label>Buscar pelo nome ou grupo<input value={query} onChange={e => {
          setQuery(e.target.value);
          setInvite(undefined);
          setSelected([]);
        }} /></label>
        <button disabled={busy || !search}>Buscar</button>
      </form>
      {invite && <h3>{invite[0]?.group}</h3>}
      {visible.map(guest => <label className="check" key={guest.id}>
        <input type="checkbox" checked={selected.includes(guest.id)} disabled={busy || Boolean(guest.arrivedAt)}
          onChange={e => setSelected(current => e.target.checked ? [...current, guest.id] : current.filter(id => id !== guest.id))} />
        {guest.name}{!invite && ' — ' + guest.group}{guest.arrivedAt && ' · Já entrou às ' + statusTime(guest.arrivedAt)}
      </label>)}
      {search && !visible.length && !busy && <p>Use Buscar para consultar a lista atual do evento.</p>}
      <button disabled={busy || !selected.length} onClick={() => void run(async () => {
        applyCheckins(await request<CheckinResult[]>(key, api + '/checkins', { guestIds: selected }));
      })}>Registrar entrada</button>
      {status && <p role="status">{status}</p>}
    </section>}
    {error && <p role="alert">{error}</p>}
  </main>;
}
