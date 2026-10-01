import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);
});

test('registers, logs in, reads the protected profile and user list, then logs out', async ({ page }) => {
  const username = `qa${Date.now().toString().slice(-9)}`;

  await page.getByRole('tab', { name: 'Tạo tài khoản' }).click();
  await page.locator('#register-panel').getByLabel('Tên đăng nhập').fill(username);
  await page.locator('#register-panel').getByLabel('Mật khẩu').fill('StrongPass123');
  await page.getByRole('button', { name: 'Tạo tài khoản' }).click();
  await expect(page.locator('#login-error')).toContainText('Tạo tài khoản thành công');

  let profileAuthorization;
  page.on('request', (request) => {
    if (new URL(request.url()).pathname === '/users/me') profileAuthorization = request.headers()['authorization'];
  });
  await page.locator('#login-panel').getByLabel('Mật khẩu').fill('StrongPass123');
  await page.getByRole('button', { name: 'Đăng nhập' }).click();
  await expect(page).toHaveURL(/\/user\/profile$/);
  await expect(page.locator('#profile-username')).toHaveText(username);
  await expect(page.locator('#profile-details')).toBeVisible();
  await expect(page.locator('#auth-status')).toHaveText('Bearer token hợp lệ');
  expect(await page.evaluate(() => localStorage.getItem('jwt-demo-token'))).toBeTruthy();

  await page.getByRole('button', { name: 'Tải danh sách' }).click();
  await expect(page.getByRole('row').filter({ hasText: username })).toBeVisible();
  expect(profileAuthorization).toMatch(/^Bearer\s+.+/);

  await page.getByRole('button', { name: 'Đăng xuất' }).click();
  await expect(page).toHaveURL(/\/login$/);
  expect(await page.evaluate(() => localStorage.getItem('jwt-demo-token'))).toBeNull();
});

test('shows invalid credentials and field-specific duplicate username errors', async ({ page }) => {
  await page.route('**/auth/login', async (route) => {
    await new Promise((resolve) => setTimeout(resolve, 300));
    await route.continue();
  });
  await page.locator('#login-panel').getByLabel('Tên đăng nhập').fill('demo');
  await page.locator('#login-panel').getByLabel('Mật khẩu').fill('WrongPass123');
  await page.getByRole('button', { name: 'Đăng nhập' }).click();
  await expect(page.locator('#login-form .submit-button')).toBeDisabled();
  await expect(page.locator('#login-form .button-wait')).toBeVisible();
  await expect(page.locator('#login-error')).toContainText('không hợp lệ');

  await page.getByRole('tab', { name: 'Tạo tài khoản' }).click();
  await page.locator('#register-panel').getByLabel('Tên đăng nhập').fill('demo_ui_e2e');
  await page.locator('#register-panel').getByLabel('Mật khẩu').fill('StrongPass123');
  await page.getByRole('button', { name: 'Tạo tài khoản' }).click();
  await expect(page.locator('#login-error')).toContainText('Tạo tài khoản thành công');
  await page.getByRole('tab', { name: 'Tạo tài khoản' }).click();
  await page.locator('#register-panel').getByLabel('Tên đăng nhập').fill('demo_ui_e2e');
  await page.locator('#register-panel').getByLabel('Mật khẩu').fill('StrongPass123');
  await page.getByRole('button', { name: 'Tạo tài khoản' }).click();
  await expect(page.locator('#register-username-error')).toContainText('đã tồn tại');
  await expect(page.locator('#register-username')).toHaveAttribute('aria-invalid', 'true');
  await expect(page.locator('#register-username')).toBeFocused();
});

test('supports keyboard navigation between sign-in and registration tabs', async ({ page }) => {
  const loginTab = page.getByRole('tab', { name: 'Đăng nhập' });
  const registerTab = page.getByRole('tab', { name: 'Tạo tài khoản' });
  await loginTab.focus();
  await page.keyboard.press('ArrowRight');
  await expect(registerTab).toHaveAttribute('aria-selected', 'true');
  await expect(registerTab).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.locator('#register-panel')).toBeVisible();
});

test('returns to login for missing or invalid profile tokens', async ({ page }) => {
  await page.goto('/user/profile');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.locator('#login-error')).toContainText('cần đăng nhập');

  await page.evaluate(() => localStorage.setItem('jwt-demo-token', 'invalid-token'));
  await page.goto('/user/profile');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.locator('#login-error')).toContainText('không hợp lệ');
  expect(await page.evaluate(() => localStorage.getItem('jwt-demo-token'))).toBeNull();
});

test('keeps the sign-in page usable on narrow screens and with reduced motion', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await expect(page.getByRole('tab', { name: 'Đăng nhập' })).toBeVisible();
  await expect(page.locator('#login-panel').getByLabel('Tên đăng nhập')).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);

  await page.setViewportSize({ width: 1440, height: 900 });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true);
  const transitionDuration = await page.locator('#login-form .button-primary').evaluate((element) => getComputedStyle(element).transitionDuration);
  expect(transitionDuration).toBe('1e-05s');
});
