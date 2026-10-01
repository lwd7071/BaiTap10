import { defineConfig } from '@playwright/test';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const variant = process.env.JWT_VARIANT || 'jjwt';
if (!['jjwt', 'nimbus'].includes(variant)) throw new Error(`Unknown JWT_VARIANT: ${variant}`);
const appFolder = variant === 'jjwt' ? 'JWT-JJWT' : 'JWT-Nimbus';
const appPom = path.resolve(here, '..', appFolder, 'pom.xml');
const baseURL = 'http://127.0.0.1:8005';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  timeout: 30_000,
  expect: { timeout: 8_000 },
  reporter: 'list',
  use: {
    baseURL,
    browserName: 'chromium',
    viewport: { width: 1440, height: 900 },
    trace: 'retain-on-failure'
  },
  webServer: {
    command: `mvn -f "${appPom}" -Pe2e -Dspring-boot.run.profiles=e2e spring-boot:run`,
    url: `${baseURL}/login`,
    reuseExistingServer: false,
    timeout: 180_000
  }
});
