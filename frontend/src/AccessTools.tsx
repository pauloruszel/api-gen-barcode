import { useState } from 'react';
import { type Guest, request } from './api';

export function AccessTools({ eventId, accessKey, guests }: { eventId: string; accessKey: string; guests: Guest[] }) {
  const [operator, setOperator] = useState('');
  const [link, setLink] = useState('');
  const [invite, setInvite] = useState('');
  const [image, setImage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const groups = [...new Map(guests.map(guest => [guest.inviteId, guest.group]))];

  async function run(action: () => Promise<void>) {
    setError('');
    setBusy(true);
    try { await action(); }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Não foi possível concluir.'); }
    finally { setBusy(false); }
  }

  return <section>
    <h2>Acessos e convites</h2>
    <form onSubmit={event => {
      event.preventDefault();
      void run(async () => {
        const data = await request<{ token: string }>(accessKey, '/' + eventId + '/operators', { name: operator });
        setLink(location.origin + '/recepcao?evento=' + eventId + '#chave=' + data.token);
      });
    }}>
      <label>Nome da recepcionista<input required value={operator} onChange={e => setOperator(e.target.value)} /></label>
      <button disabled={busy}>Gerar acesso por 24 horas</button>
      {link && <label>Link da recepção<input readOnly value={link} onFocus={e => e.target.select()} /></label>}
    </form>
    {groups.length > 0 && <form onSubmit={event => {
      event.preventDefault();
      void run(async () => {
        const data = await request<{ image: string }>(accessKey, '/' + eventId + '/invites/' + invite + '/qr');
        setImage('data:image/png;base64,' + data.image);
      });
    }}>
      <label>Convite<select required value={invite} onChange={e => { setInvite(e.target.value); setImage(''); }}>
        <option value="">Selecionar convite</option>
        {groups.map(([id, group]) => <option key={id} value={id}>{group}</option>)}
      </select></label>
      <button disabled={busy || !invite}>Mostrar QR Code</button>
      {image && <div>
        <img className="qr" src={image} alt="QR Code do convite" />
        <a href={image} download="convite.png">Baixar QR Code</a>
      </div>}
    </form>}
    {error && <p role="alert">{error}</p>}
  </section>;
}
