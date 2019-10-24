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
  const jwt = {};
  jwt.expires = (new Date()).getTime() + 24 * 60 * 60 * 1000;
  jwt.item = token;
  localStorage.setItem('jwt', JSON.stringify(jwt));
  return localStorage.setItem('token', token);
}
export function getClientId() {
  return localStorage.getItem('clientId');
}
export function setClientId(clientId) {
  return localStorage.setItem('clientId', clientId);
}
export function getCurrentUser() {
  return localStorage.getItem('currentUser');
}
export function setCurrentUser(currentUser) {
  return localStorage.setItem('currentUser', currentUser);
}
export function getCanvasUrl() {
  return '/orchsym-web/canvas.html';
}
