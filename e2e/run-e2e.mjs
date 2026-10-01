import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const playwrightCli = path.join(here, 'node_modules', '@playwright', 'test', 'cli.js');

for (const variant of ['jjwt', 'nimbus']) {
  console.log(`\nRunning browser tests for ${variant.toUpperCase()}...`);
  const result = spawnSync(process.execPath, [playwrightCli, 'test'], {
    cwd: here,
    env: { ...process.env, JWT_VARIANT: variant },
    stdio: 'inherit'
  });
  if (result.error) throw result.error;
  if (result.status !== 0) process.exit(result.status ?? 1);
}
