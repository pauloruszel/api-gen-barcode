import { expect, test } from '@playwright/test';

test('isolates operator access and rolls back a check-in when a selected guest is inactive', async ({ request }) => {
  const admin = { Authorization: 'Bearer ' + process.env.RECEPTION_ADMIN_KEY };
  const create = async (name: string) => {
    const response = await request.post('/api/v2/events', { headers: admin, data: { name } });
    expect(response.ok()).toBeTruthy();
    return response.json();
  };
  const first = await create('Casamento A');
  const second = await create('Casamento B');
  const event = '/api/v2/events/' + first.id;
  const post = (path: string, data: unknown) => request.post(event + path, { headers: admin, data });
  expect((await post('/imports', {
    revision: 0, rows: [{ name: 'Ana', group: 'Silva', rsvp: 'YES' }, { name: 'Bia', group: 'Silva' }],
  })).ok()).toBeTruthy();
  const [ana, bia] = await (await request.get(event + '/guests', { headers: admin })).json();
  expect((await post('/imports', {
    revision: 1, rows: [{ id: ana.id, name: 'Ana Maria', group: 'silva' }], remove: [bia.id],
  })).ok()).toBeTruthy();
  const updated = await (await request.get(event + '/guests', { headers: admin })).json();
  expect(updated).toMatchObject([{ id: ana.id, inviteId: ana.inviteId, name: 'Ana Maria', rsvp: 'YES' }]);

  const { token } = await (await post('/operators', { name: 'Carol' })).json();
  const operator = { Authorization: 'Bearer ' + token };
  const foreign = await request.get('/api/v2/events/' + second.id + '/desk/guests', { headers: operator });
  expect(foreign.status()).toBe(401);

  const checkIn = (ids: string[]) => request.post(event + '/desk/checkins', {
    headers: operator, data: { guestIds: ids },
  });
  expect((await checkIn([ana.id, bia.id])).status()).toBe(409);
  const initialReport = await (await request.get(event + '/report', { headers: admin })).json();
  expect(initialReport).toMatchObject([{ id: ana.id, arrivedAt: null }]);
  expect((await checkIn([ana.id])).ok()).toBeTruthy();

  const correction = { actor: 'Mariana', reason: 'Entrada lançada na pessoa errada' };
  expect((await post('/guests/' + ana.id + '/undo', correction)).ok()).toBeTruthy();
  expect((await post('/extras', { ...correction, name: 'Clara', group: 'Lima' })).ok()).toBeTruthy();
  const history = await (await request.get(event + '/history', { headers: admin })).json();
  expect(history.map((entry: { action: string }) => entry.action)).toEqual(['EXTRA', 'UNDO', 'CHECK_IN']);
});
