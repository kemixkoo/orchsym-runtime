// use localStorage to store the authority info, which might be sent from server in actual project.
export function getAuthority() {
  return ['admin'];
}
export function setAuthority(token) {
  // return localStorage.setItem('token', token);
}
// use localStorage to store the authority info, which might be sent from server in actual project.
export function getToken() {
  return localStorage.getItem('token');
}
export function setToken(token) {
  return localStorage.setItem('token', token);
}
export function getClientId() {
  return localStorage.getItem('clientId');
}
export function setClientId(clientId) {
  return localStorage.setItem('clientId', clientId);
}
