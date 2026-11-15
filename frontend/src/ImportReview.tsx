import { useState } from 'react';
import type { GuestRow, ImportPreview } from './api';

export function ImportReview({ preview, busy, onApply, onCancel }: {
  preview: ImportPreview;
  busy: boolean;
  onApply: (rows: GuestRow[], remove: string[]) => void;
  onCancel: () => void;
}) {
  const [choices, setChoices] = useState(preview.matches.map(match => match.id ?? (match.needsReview ? '' : 'new')));
  const [remove, setRemove] = useState<string[]>([]);
  const people = new Map(preview.current.map(guest => [guest.id, guest]));
  const selected = new Set(choices.filter(choice => choice && choice !== 'new'));
  const missing = preview.current.filter(guest => !selected.has(guest.id));
  const rows = preview.incoming.map((row, index) => ({
    ...row, id: people.has(choices[index]) ? choices[index] : null,
  }));
  const changed = rows.filter(row => {
    const guest = row.id ? people.get(row.id) : undefined;
    return guest && (guest.name !== row.name || (row.group && guest.group !== row.group) || guest.rsvp !== row.rsvp);
  }).length;
  const canApply = choices.every(Boolean) && selected.size === choices.filter(choice => people.has(choice)).length;

  return <div className="review">
    <h3>Prévia da lista</h3>
    <p>{choices.filter(choice => choice === 'new').length} novos · {changed} alterações · {missing.length} ausentes no arquivo</p>
    <ol>{rows.map((row, index) => {
      const guest = row.id ? people.get(row.id) : undefined;
      const previous = guest ? guest.name + ' — ' + guest.group : '';
      const moved = guest && row.group && row.group !== guest.group;
      return <li key={index}>
        <strong>{row.name}</strong>{row.group && ' · ' + row.group} · {row.rsvp}
        {guest && <span> · Vinculado a {previous}</span>}
        {moved && <p>Troca de grupo. O convidado passará a aparecer no QR do novo convite.</p>}
        {preview.matches[index].needsReview && <label>Resolver {row.name}
          <select value={choices[index]} onChange={event => {
            const next = event.target.value;
            setChoices(current => current.map((choice, i) => i === index ? next : choice));
            setRemove(current => current.filter(id => id !== next));
          }}>
            <option value="">Escolha uma ação</option>
            <option value="new">Adicionar como novo</option>
            {preview.current.filter(candidate => candidate.id === choices[index] || !selected.has(candidate.id))
              .map(candidate => <option key={candidate.id} value={candidate.id}>
                {candidate.name} — {candidate.group}
              </option>)}
          </select>
        </label>}
      </li>;
    })}</ol>
    {missing.length > 0 && <>
      <h4>Ausentes no arquivo</h4>
      <p>A lista atual mantém estas pessoas. Marque quem deve sair da lista ativa.</p>
      {missing.map(guest => <label className="check" key={guest.id}>
        <input type="checkbox" checked={remove.includes(guest.id)} onChange={event =>
          setRemove(old => event.target.checked ? [...old, guest.id] : old.filter(id => id !== guest.id))} />
        Remover {guest.name} — {guest.group}
      </label>)}
    </>}
    <button disabled={busy || !canApply} onClick={() => onApply(rows, remove.filter(id => !selected.has(id)))}>Aplicar lista</button>
    <button disabled={busy} onClick={onCancel}>Cancelar revisão</button>
  </div>;
}
