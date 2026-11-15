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
