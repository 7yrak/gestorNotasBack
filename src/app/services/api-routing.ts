// Business API stays at the server root. Authentication uses the application's base.
export const NOTES_API_PATH = '/api';

export function notesApiPath(baseURI: string): string {
  const path = new URL('./', baseURI).pathname;
  return /^\/apps\/gestor-notas\/gn-[a-f0-9]{32}\/$/.test(path) ? path + 'api' : NOTES_API_PATH;
}

export function isNotesApiRequest(requestUrl: string, baseURI: string): boolean {
  const base = new URL(baseURI);
  const url = new URL(requestUrl, base);
  return url.origin === base.origin &&
    (url.pathname === notesApiPath(baseURI) || url.pathname.startsWith(notesApiPath(baseURI) + '/'));
}
