import { expect, test } from '@playwright/test';

test('creates an event and imports a real CSV through the browser', async ({ page }) => {
  const key = process.env.RECEPTION_ADMIN_KEY;
  if (!key) throw new Error('Configure RECEPTION_ADMIN_KEY igual à API.');
  await page.goto('/');
  await page.getByLabel('Chave do cerimonial').fill(key);
  await page.getByLabel('Nome do evento').fill('Casamento Silva');
  await page.getByRole('button', { name: 'Criar evento' }).click();
  await expect(page.getByRole('heading', { name: 'Casamento Silva' })).toBeVisible();
  await page.getByLabel('Planilha de convidados').setInputFiles({
    name: 'convidados.csv',
    mimeType: 'text/csv',
    buffer: Buffer.from('Nome;Grupo;RSVP\nAna Silva;Família Silva;Sim\nJoão Silva;Família Silva;Sim'),
  });
  await page.getByRole('button', { name: 'Revisar importação' }).click();
  await page.getByRole('button', { name: 'Aplicar lista' }).click();
  await expect(page.getByRole('cell', { name: 'Ana Silva' })).toBeVisible();
  await expect(page.getByRole('cell', { name: 'João Silva' })).toBeVisible();
});

test('reviews a changed list, reuses a guest ID and retains missing people by default', async ({ page, request }) => {
  const headers = { Authorization: 'Bearer ' + process.env.RECEPTION_ADMIN_KEY };
  const api = '/api/v2/events';
  const event = await (await request.post(api, { headers, data: { name: 'Casamento revisão' } })).json();
  await request.post(api + '/' + event.id + '/imports', {
    headers,
    data: { revision: 0, rows: [{ name: 'Ana Silva', group: 'Silva' }, { name: 'João Silva', group: 'Silva' }] },
  });
  const list = await (await request.get(api + '/' + event.id + '/guests', { headers })).json();
  const ana = list.find((guest: { name: string }) => guest.name === 'Ana Silva');
  await page.goto('/?evento=' + event.id);
  await page.getByLabel('Chave do cerimonial').fill(process.env.RECEPTION_ADMIN_KEY!);
  await page.getByRole('button', { name: 'Abrir evento' }).click();
  await page.getByLabel('Planilha de convidados').setInputFiles({
    name: 'atualizacao.csv', mimeType: 'text/csv',
    buffer: Buffer.from('ID;Nome;Grupo\n' + ana.id + ';Ana Maria;Silva\n;Bia Silva;Silva'),
  });
  await page.getByRole('button', { name: 'Revisar importação' }).click();
  await expect(page.getByRole('button', { name: 'Aplicar lista' })).toBeDisabled();
  await page.getByLabel('Resolver Bia Silva').selectOption('new');
  await expect(page.getByText('1 novos · 1 alterações · 1 ausentes no arquivo')).toBeVisible();
  await page.getByRole('button', { name: 'Aplicar lista' }).click();
  await expect(page.getByRole('cell', { name: 'Ana Maria' })).toBeVisible();
  await expect(page.getByRole('cell', { name: 'João Silva' })).toBeVisible();
  const updated = await (await request.get(api + '/' + event.id + '/guests', { headers })).json();
  expect(updated.find((guest: { id: string }) => guest.id === ana.id).inviteId).toBe(ana.inviteId);
});

test('imports the first worksheet of a real XLSX file', async ({ page }) => {
  await page.goto('/');
  await page.getByLabel('Chave do cerimonial').fill(process.env.RECEPTION_ADMIN_KEY!);
  await page.getByLabel('Nome do evento').fill('Casamento Excel');
  await page.getByRole('button', { name: 'Criar evento' }).click();
  await page.getByLabel('Planilha de convidados').setInputFiles('e2e/fixtures/convidados.xlsx');
  await page.getByRole('button', { name: 'Revisar importação' }).click();
  await page.getByRole('button', { name: 'Aplicar lista' }).click();
  await expect(page.getByRole('cell', { name: 'Cecília Gomes' })).toBeVisible();
  await expect(page.getByRole('cell', { name: 'Confirmado' })).toBeVisible();
});
