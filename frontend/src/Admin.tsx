import { useState } from 'react';
import { type Event, type Guest, type ImportPreview, request } from './api';
import { ImportForm } from './ImportForm';
import { ImportReview } from './ImportReview';
import { exportGuests } from './guestExport';

export function Admin() {
  const [key, setKey] = useState('');
  const [name, setName] = useState('');
  const [eventId, setEventId] = useState(new URLSearchParams(location.search).get('evento') ?? '');
  const [event, setEvent] = useState<Event>();
  const [guests, setGuests] = useState<Guest[]>([]);
  const [pending, setPending] = useState<ImportPreview>();
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function open(selected: Event) {
    const list = await request<Guest[]>(key, '/' + selected.id + '/guests');
    setEvent(selected);
    setGuests(list);
    history.replaceState(null, '', '?evento=' + selected.id);
  }

  async function run(action: () => Promise<void>) {
    setBusy(true);
    setError('');
    try { await action(); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Ocorreu um erro.'); }
    finally { setBusy(false); }
  }

  return <main>
    <header><h1>{event?.name ?? 'Recepção de eventos'}</h1></header>
    {!event ? <section>
      <label>Chave do cerimonial<input type="password" value={key} onChange={e => setKey(e.target.value)} /></label>
      <form onSubmit={e => { e.preventDefault(); void run(async () => open(await request<Event>(key, '', { name }))); }}>
        <label>Nome do evento<input required value={name} onChange={e => setName(e.target.value)} /></label>
        <button disabled={busy || !key}>Criar evento</button>
      </form>
      <form onSubmit={e => {
        e.preventDefault();
        void run(async () => open(await request<Event>(key, '/' + encodeURIComponent(eventId.trim()))));
      }}>
        <label>ID de um evento existente<input required value={eventId} onChange={e => setEventId(e.target.value)} /></label>
        <button disabled={busy || !key}>Abrir evento</button>
      </form>
    </section> : <>
      <section><p>ID do evento <code>{event.id}</code></p>
        <h2>Lista de convidados</h2>
        <p>Versão {event.revision}</p>
        {guests.length > 0 && <button onClick={() => exportGuests(guests)}>Exportar lista com IDs</button>}
        <ImportForm onSubmit={async rows => {
          setError('');
          setPending(await request<ImportPreview>(key, '/' + event.id + '/imports/preview', { rows }));
        }} />
        {pending && <ImportReview key={JSON.stringify(pending)} preview={pending} busy={busy}
          onCancel={() => setPending(undefined)} onApply={(rows, remove) => void run(async () => {
            const updated = await request<Event>(key, '/' + event.id + '/imports',
              { revision: pending.revision, rows, remove });
            await open(updated);
            setPending(undefined);
          })} />}
        {guests.length > 0 && <table><thead><tr><th>Nome</th><th>Convite</th><th>RSVP</th></tr></thead>
          <tbody>{guests.map(guest => <tr key={guest.id}>
            <td>{guest.name}</td><td>{guest.group}</td><td>{{ YES: 'Confirmado', NO: 'Recusado', UNKNOWN: 'Pendente' }[guest.rsvp]}</td>
          </tr>)}</tbody></table>}
      </section>
    </>}
    {error && <p role="alert">{error}</p>}
  </main>;
}
