import { expect, test } from '@playwright/test';

test('scans a family QR in Chromium and deduplicates concurrent arrivals', async ({ page, request }) => {
  const headers = { Authorization: 'Bearer ' + process.env.RECEPTION_ADMIN_KEY };
  const api = '/api/v2/events';
  const event = await (await request.post(api, { headers, data: { name: 'Casamento QR' } })).json();
  const endpoint = api + '/' + event.id;
  await request.post(endpoint + '/imports', { headers, data: {
    revision: 0, rows: [{ name: 'Ana Silva', group: 'Silva' }, { name: 'João Silva', group: 'Silva' }],
  } });
  const guests = await (await request.get(endpoint + '/guests', { headers })).json();
  const ana = guests.find((guest: { name: string }) => guest.name === 'Ana Silva');
  const joao = guests.find((guest: { name: string }) => guest.name === 'João Silva');
  const qr = await (await request.get(endpoint + '/invites/' + ana.inviteId + '/qr', { headers })).json();
  const operators = await Promise.all(['Carol', 'Bruna'].map(async name =>
    (await request.post(endpoint + '/operators', { headers, data: { name } })).json()));
  await page.addInitScript((imageData: string) => {
    Object.defineProperty(navigator.mediaDevices, 'getUserMedia', {
      value: async () => {
        const image = new Image();
        image.src = 'data:image/png;base64,' + imageData;
        await image.decode();
        const canvas = document.createElement('canvas');
        canvas.width = 640;
        canvas.height = 480;
        const ctx = canvas.getContext('2d')!;
        setInterval(() => {
          ctx.fillStyle = 'white';
          ctx.fillRect(0, 0, 640, 480);
          ctx.drawImage(image, 160, 80, 320, 320);
        }, 40);
        return canvas.captureStream(25);
      },
    });
  }, qr.image);
  await page.goto('/recepcao?evento=' + event.id + '#chave=' + operators[0].token);
  await page.getByRole('button', { name: 'Escanear QR' }).click();
  await page.getByRole('checkbox', { name: 'Ana Silva' }).check();
  await page.getByRole('button', { name: 'Registrar entrada' }).click();
  await expect(page.getByRole('checkbox', { name: /Ana Silva · Já entrou/ })).toBeDisabled();

  const results = await Promise.all(operators.map(async ({ token }) => {
    const response = await request.post(endpoint + '/desk/checkins', {
      headers: { Authorization: 'Bearer ' + token }, data: { guestIds: [joao.id] },
    });
    expect(response.ok()).toBeTruthy();
    return response.json();
  }));
  expect(results.flat().map(result => result.recorded).sort()).toEqual([false, true]);

  await page.getByLabel('Buscar pelo nome ou grupo').fill('Joao');
  await page.getByRole('button', { name: 'Buscar', exact: true }).click();
  await expect(page.getByRole('checkbox', { name: /João Silva.*Já entrou/ })).toBeDisabled();
  const forbidden = await request.get(endpoint + '/guests', { headers: { Authorization: 'Bearer ' + operators[0].token } });
  expect(forbidden.status()).toBe(401);
});
