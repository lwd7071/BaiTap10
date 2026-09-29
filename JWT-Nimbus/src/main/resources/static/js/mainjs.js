const $ = (selector) => document.querySelector(selector);
const message = (text, error = false) => { const el = $('#message'); if (el) { el.textContent = text; el.className = `message${error ? ' error' : ''}`; } };
const tokenKey = 'jwt-demo-token';
async function api(path, options = {}) {
  const token = localStorage.getItem(tokenKey);
  const response = await fetch(path, { ...options, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.detail || data.message || `Yêu cầu thất bại (${response.status})`);
  return data;
}
const login = $('#login-form');
if (login) login.addEventListener('submit', async (event) => {
  event.preventDefault(); message('Đang đăng nhập…');
  const body = Object.fromEntries(new FormData(login));
  try { const result = await api('/auth/login', { method: 'POST', body: JSON.stringify(body) }); localStorage.setItem(tokenKey, result.token); location.href = '/profile.html'; }
  catch (error) { message(error.message, true); }
});
const register = $('#register-form');
if (register) register.addEventListener('submit', async (event) => {
  event.preventDefault(); message('Đang tạo tài khoản…');
  const body = Object.fromEntries(new FormData(register));
  try { const result = await api('/auth/register', { method: 'POST', body: JSON.stringify(body) }); message(`${result.message}. Bạn có thể đăng nhập ngay.`); register.reset(); }
  catch (error) { message(error.message, true); }
});
if ($('#profile')) {
  api('/users/me').then((user) => { $('#profile').innerHTML = `<div><strong>Tên đăng nhập</strong>${escapeHtml(user.username)}</div><div><strong>ID</strong>${user.id}</div><div><strong>Vai trò</strong>${escapeHtml(user.role)}</div>`; })
    .catch((error) => { message(`${error.message}. Vui lòng đăng nhập lại.`, true); localStorage.removeItem(tokenKey); setTimeout(() => location.href = '/login.html', 1200); });
  $('#users-button').addEventListener('click', async () => { try { const users = await api('/users'); const box = $('#users'); box.textContent = JSON.stringify(users, null, 2); box.hidden = false; } catch (error) { message(error.message, true); } });
  $('#logout').addEventListener('click', () => { localStorage.removeItem(tokenKey); location.href = '/login.html'; });
}
function escapeHtml(value) { return String(value).replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]); }
