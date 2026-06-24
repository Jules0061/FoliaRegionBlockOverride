# FoliaRegionBlockOverride

Folia-native WorldGuard addon. Lets configured blocks bypass WorldGuard placement denial inside specific regions, then removes them automatically after a per-block timer. Optional manual breaking. Unlimited regions, worlds, and block types — all driven by `config.yml`.

## Features

- WorldGuard integration: configured blocks bypass placement denial; every other protection stays intact.
- Per-region, per-block rules: despawn timer and break permission.
- Unlimited regions, worlds, and block types via config — no hardcoded materials.
- Automatic removal after a configurable timer (`5s`, `10s`, `30s`, `1m`, `5m`, `1h`, raw ticks `100t`).
- Tracks only plugin-placed blocks. Natural, admin, and unrelated blocks are never touched.
- Verifies block still exists and matches type before removal.
- Optional manual breaking per block, with a configurable denial message.
- `regionblock.bypass` permission to ignore plugin restrictions.
- Folia region-thread-safe scheduling. No BukkitScheduler, no global repeating tasks, O(1) tracking lookups.
- MiniMessage, legacy `&`, and hex color support in `messages.yml`.

## Installation

1. Use Folia `1.21.11+` and Java 21.
2. Install WorldGuard (and WorldEdit).
3. Drop `FoliaRegionBlockOverride-<version>.jar` into `plugins/`.
4. Start the server. Edit `plugins/FoliaRegionBlockOverride/config.yml`, then `/regionblock reload`.

## WorldGuard Setup

Create regions and deny building as usual:

```
/rg define swordzone
/rg flag swordzone build deny
```

Then list the region name (`swordzone`) under `regions` in `config.yml` with the blocks you want to allow. Region names are matched case-insensitively against WorldGuard region IDs.

## Example Configuration

`config.yml`

```yaml
settings:
  debug: false

defaults:
  despawn-time: 5s
  allow-break: true

regions:
  swordzone:
    enabled: true
    blocks:
      COBWEB:
        despawn-time: 5s
        allow-break: true

  arena:
    enabled: true
    blocks:
      COBWEB:
        despawn-time: 3s
        allow-break: true
      POWDER_SNOW:
        despawn-time: 10s
        allow-break: false
```

A block omitting `despawn-time` / `allow-break` falls back to the values under `defaults`.

`messages.yml`

```yaml
prefix: "&6RegionBlocks &8»"
no-permission: "&cNo permission."
break-denied: "&cYou cannot break this block."
```

Supported color formats: MiniMessage (`<red>`, `<#ff0000>`), legacy `&` codes, and hex `&#rrggbb`. Placeholders: `%error%` (reload-failed).

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/regionblock reload` | Reload `config.yml` and `messages.yml`. | `regionblock.reload` |
| `/regionblock info` | Show regions, blocks, despawn times, break permissions. | `regionblock.admin` |

Aliases: `/rbo`, `/regionblocks`.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `regionblock.admin` | op | Admin commands and info. |
| `regionblock.reload` | op | Reload configuration. |
| `regionblock.bypass` | false | Ignore plugin placement and break restrictions. |

## Time Formats

`5s`, `10s`, `30s`, `1m`, `5m`, `1h`, `500ms`, `100t` (raw ticks). Bare numbers are treated as seconds.

## Troubleshooting

- Plugin disables on start: WorldGuard is missing. Install it first.
- Blocks still denied: confirm the WorldGuard region ID matches a key under `regions`, the region is `enabled`, and the block name is a valid Bukkit `Material`.
- Blocks not despawning: check `settings.debug: true` logs; verify the chunk stays loaded long enough for the region scheduler to fire.

## FAQ

- **Does this disable WorldGuard?** No. Only configured blocks inside configured regions bypass placement denial. Everything else is untouched.
- **Can players keep the blocks?** No, they always despawn after the timer unless broken first.
- **Does it remove naturally generated or admin blocks?** No. Only blocks the plugin placed and tracked are removed, and only if the type still matches.

## Folia Notes

- `folia-supported: true`.
- All block reads/writes run on the owning region thread via `RegionScheduler`.
- No `BukkitScheduler`, no async block access, no Paper-only hacks.
- Despawn tasks are scheduled at the block location and self-cancel on manual break, reload, and shutdown.

## Performance Notes

- Event-driven; no global repeating tasks.
- O(1) tracking via `ConcurrentHashMap` keyed by world + coordinates.
- Region lookups are short-circuited when a location is in no WorldGuard region.
- Suitable for hundreds of concurrent players on large PvP / lifesteal / arena servers.

## Build

```
mvn clean package
```

Output: `target/FoliaRegionBlockOverride-<version>.jar`. Requires Java 21.
