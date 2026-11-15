export type Rsvp = 'UNKNOWN' | 'YES' | 'NO';
export type Event = { id: string; name: string; revision: number };
export type GuestRow = { id: string | null; group: string; name: string; rsvp: Rsvp | null };
export type Guest = Omit<GuestRow, 'id' | 'rsvp'> & { id: string; inviteId: string; rsvp: Rsvp; arrivedAt: string | null };

export async function request<T>(key: string, path: string, body?: unknown): Promise<T> {
  const response = await fetch('/api/v2/events' + path, {
    method: body === undefined ? 'GET' : 'POST',
    headers: { Authorization: 'Bearer ' + key, ...(body === undefined ? {} : { 'Content-Type': 'application/json' }) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || 'Erro ao acessar o sistema (' + response.status + ').');
  }
  return response.json();
}

export type ImportPreview = {
  revision: number;
  current: Guest[];
  incoming: GuestRow[];
  matches: { id: string | null; needsReview: boolean }[];
};

export type DeskGuest = { id: string; inviteId: string; group: string; name: string; arrivedAt: string | null };
export type CheckinResult = { id: string; recorded: boolean; arrivedAt: string };
