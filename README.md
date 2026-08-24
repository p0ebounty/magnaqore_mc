# MagnaQore

Minecraft server network: Velocity proxy + Paper backends, Java + Bedrock crossplay, custom plugins, PostgreSQL, and (later) a Next.js website.

- **Docs**: [`docs/`](docs/) — architecture, roadmap, decisions, runbook (in Russian).
- **Rules & context for Claude**: [`CLAUDE.md`](CLAUDE.md).
- **Reproducible setup**: all jars are pinned in [`infra/manifest.json`](infra/manifest.json) and fetched by `infra/download.sh`; worlds and DB are data, moved by backup scripts.

Everything needed to run the project lives in this repository except secrets (`infra/secrets/`, gitignored) and runtime data (worlds, logs, DB).
