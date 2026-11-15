import { expect, test } from '@playwright/test';

test('keeps QR and check-in history through reimport and records contingency', async ({ page, request }) => {
  const key = process.env.RECEPTION_ADMIN_KEY!;
  const headers = { Authorization: 'Bearer ' + key };
  const api = '/api/v2/events';
  const event = await (await request.post(api, { headers, data: { name: 'Casamento contingência' } })).json();
  const endpoint = api + '/' + event.id;
  const post = (path: string, data: unknown) => request.post(endpoint + path, { headers, data });
  expect((await post('/imports', { revision: 0, rows: [{ name: 'Ana', group: 'Silva' }, { name: 'João', group: 'Silva' }] })).ok()).toBeTruthy();
  const [ana, joao] = await (await request.get(endpoint + '/guests', { headers })).json();
  const qr = await (await request.get(endpoint + '/invites/' + ana.inviteId + '/qr', { headers })).json();
  const { token } = await (await post('/operators', { name: 'Carol' })).json();
  expect((await request.post(endpoint + '/desk/checkins', {
    headers: { Authorization: 'Bearer ' + token }, data: { guestIds: [ana.id] },
  })).ok()).toBeTruthy();

  const updated = { revision: 1, rows: [{ id: ana.id, name: 'Ana Maria', group: 'Silva' }] };
  expect((await post('/imports', updated)).ok()).toBeTruthy();
  expect((await post('/imports', updated)).status()).toBe(409);
  const repeated = await (await post('/imports', { ...updated, revision: 2 })).json();
  expect(repeated.revision).toBe(2);
  const sameQr = await (await request.get(endpoint + '/invites/' + ana.inviteId + '/qr', { headers })).json();
  expect(sameQr.image).toBe(qr.image);
  const checked = await (await request.get(endpoint + '/desk/invites/' + ana.inviteId,
    { headers: { Authorization: 'Bearer ' + token } })).json();
  expect(checked.find((guest: { id: string }) => guest.id === ana.id).arrivedAt).toBeTruthy();
  expect(checked.find((guest: { id: string }) => guest.id === joao.id).arrivedAt).toBeNull();

  await page.goto('/?evento=' + event.id);
  await page.getByLabel('Chave do cerimonial').fill(key);
  await page.getByRole('button', { name: 'Abrir evento' }).click();
  await page.getByText('Corrigir uma entrada ou autorizar convidado extra').click();
  await page.getByLabel('Responsável', { exact: true }).fill('Mariana');
  await page.getByLabel('Motivo', { exact: true }).fill('Internet indisponível');
  await page.getByLabel('Convidado para correção').selectOption(joao.id);
  await page.getByLabel('Horário anotado no local').fill('2026-01-01T13:30');
  await page.getByRole('button', { name: 'Registrar chegada anotada' }).click();
  await expect(page.getByText('2 presentes · 2 convidados ativos · 0 confirmados')).toBeVisible();
  await page.getByText('Histórico de entradas e correções').click();
  await expect(page.getByText(/Entrada de contingência.*Mariana.*Internet indisponível/)).toBeVisible();
  const download = page.waitForEvent('download');
  await page.getByRole('button', { name: 'Exportar lista de contingência' }).click();
  expect((await download).suggestedFilename()).toBe('relatorio-chegadas.csv');
});
