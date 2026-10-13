# chy Mod for Minecraft 1.21.11

[简体中文](README.md) | **English**

Simulates keyboard and mouse input for AFK automation and other features.

**Current version: v3.3.1**

**Supported versions: Minecraft Java 1.21.11 · Fabric / Forge / NeoForge · Java 21**

## Usage

### Installation

1. Install Minecraft **1.21.11** and the matching mod loader listed below. Use **Java 21**.
2. Download the JAR for your loader and place it in the `mods` folder of that launch profile's game directory. **Install only one variant. Do not install a `-sources.jar`. When upgrading, remove any old `autoclick` or `chy` JAR first.**
3. Launch the game with the matching loader, enter a world, and run `/chy_help` for help.

| Loader | Requirements | Download |
| --- | --- | --- |
| Fabric | [Fabric Loader](https://fabricmc.net/use/installer/) 0.19.5+ and [Fabric API](https://modrinth.com/mod/fabric-api/versions?g=1.21.11) | [chy-fabric-3.3.1+mc1.21.11.jar](built/chy-fabric-3.3.1+mc1.21.11.jar?raw=true) |
| Forge | [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.11.html) 61.2.0+ | [chy-forge-3.3.1+mc1.21.11.jar](built/chy-forge-3.3.1+mc1.21.11.jar?raw=true) |
| NeoForge | [NeoForge](https://neoforged.net/) 21.11.45+ (for Minecraft 1.21.11) | [chy-neoforge-3.3.1+mc1.21.11.jar](built/chy-neoforge-3.3.1+mc1.21.11.jar?raw=true) |

This mod only needs to be installed on the client. Commands and behavior are the same across all three loaders. For build instructions and architecture details, see the [Development and Maintenance Guide](DEVELOPMENT.md) (in Chinese).

### In-game commands

| Command | Purpose |
| --- | --- |
| `/left_click` | Toggle automatic left-clicking; the default interval is 12 ticks |
| `/left_click <tick>` | Enable or adjust the automatic left-click interval, e.g. `/left_click 20` |
| `/right_click` | Toggle automatic right-clicking; the default interval is 12 ticks |
| `/right_click <tick>` | Enable or adjust the automatic right-click interval, e.g. `/right_click 4` |
| `/shift` | Toggle continuous sneaking |
| `/stay_zhuren` | Toggle low-hunger alerts and automatic eating; off by default; hold golden carrots in your offhand |
| `/chy_help` | Show all help information |

- **Automatic left-clicking:** Attacks the entity under the crosshair. If no entity is targeted, swings your hand. Does not automatically mine blocks.
- **Automatic right-clicking:** Attempts to place blocks when holding one, and can trigger vanilla right-click interactions with containers, buttons, and other blocks. Combine it with `/shift` to place blocks against chests and similar blocks.
- **Interval settings:** The argument must be a positive integer number of ticks, with a minimum of 1. Left and right clicks have independent timers. Adjusting an interval restarts its timer. Each time you enable a feature without an argument, its interval resets to 12 ticks.
- **Status messages:** Shown in the default white color in chat, for example `自动左键，间隔12 tick` (automatic left-clicking, interval: 12 ticks). Run the corresponding command again without an argument to disable it.

**Low-hunger alerts and automatic eating:** Run `/stay_zhuren` to enable, and again to disable. Off by default. In survival/adventure mode, hunger at 14 or below triggers a local chat message with the game computer's current time (`HH:mm:ss`), hunger, and health. Health uses health points (normally 2 points = 1 heart), excluding absorption hearts.

- Put **golden carrots in your offhand** before enabling. The mod uses the offhand long enough to finish one meal without interacting with your main-hand item or the targeted block/container.
- Each low-hunger episode triggers one alert and one eating attempt, not repeated eating until full. Hunger must recover above 14 before another episode can trigger. Enabling while already hungry also triggers it.
- If you are manually using an item, the automatic meal waits until you finish; recovering above 14 cancels the pending meal. Without an offhand golden carrot, it only reports the missing food. After replenishing, toggle the feature off and on to retry immediately.
- Automatic left/right clicks and their timers pause during automatic eating and resume afterward. Rejected, interrupted, or timed-out meals are not retried in the same low-hunger episode.
- Works in single-player and multiplayer without a server mod; chat messages are visible only to you. No alerts or eating while dead, in creative, or in spectator mode. Disabling stops a meal started by the mod; disconnecting resets all features.

Opening chat, your inventory, or a menu does not turn these features off. Automatic clicking pauses when a single-player world is paused and resumes when you return to the game. All features turn off when you leave a world. Automatic clicking is disabled while dead or in spectator mode.

Automatic right-clicking performs individual clicks at regular intervals. Whether a block can be placed depends on your crosshair target, reach, remaining blocks, and server rules. Right-click actions are not restarted while you are already continuously using an item.

## Changelog

### v1.0.0

- Added automatic attacking via `/autoclick`.

### v1.0.1

- Renamed the automatic attack command to `/chy_autoattack`.

### v1.1.0

- Added custom attack intervals via `/chy_autoattack <seconds>`.
- Changed the default interval from 1 second to 0.6 seconds to match the sword attack cooldown in Java Edition.

### v2.0.0

- Added DeepSeek chat. Use `/chy_deepseek` to toggle conversations with DeepSeek-V3.

### v2.1.0

- Added `/chy_help` to display mod usage instructions in-game.

### v2.2.0

- Added `/chy_say <message>` to send messages when DeepSeek chat is enabled.

### v3.0.1

- Removed the automatic attack pause triggered by opening chat, the inventory, or the Esc menu, restoring background AFK usage.

### v3.1.0

- Removed DeepSeek chat and its related commands.
- Renamed the automatic attack command to `/auto_attack`, with a default interval of 12 ticks.
- Fixed custom intervals persisting after the feature was turned off. Enabling it again without an argument now restores the default interval.
- Added attack interval messages in chat and the client log when enabling or adjusting the feature, showing both ticks and seconds.

### v3.2.0

- Renamed the mod from `autoclick` to **chy**, with the description “Simulates keyboard and mouse input for AFK automation and other features.”
- Renamed `/auto_attack` to `/left_click`, preserving the existing attack behavior.
- Added `/right_click` for automatic right-clicking, including periodic block placement. Both left and right clicks default to 12 ticks, and all interval arguments use integer ticks.
- Changed status messages to `自动左/右键，间隔xx tick` (automatic left/right-clicking, interval: xx ticks), removing the seconds display.
- Added `/shift` to toggle continuous sneaking and updated `/chy_help` to cover all features.
- Changed chat status and help messages to red and added a custom mod icon.

### v3.3.0

- Added Forge and NeoForge client support for Minecraft 1.21.11 while retaining Fabric support.
- Migrated to a `common + fabric + forge + neoforge` multi-module project. Timing, state, commands, clicking, and sneaking logic are shared; each loader only handles its entry point and event registration.
- Standardized on official Mojang name mappings. Each loader has its own package, with no additional shared library installation required.
- Added regression tests for shared commands and tick scheduling. Build artifact names now include the loader name to help users choose the correct download.
- Added the minimum Mixin version and resource pack metadata, and fixed Forge development launch entry-point detection and macOS first-thread configuration.

### v3.3.1 (2026-10-13)

- Added `/stay_zhuren` to toggle low-hunger alerts and automatic eating; disabled by default.
- At hunger 14 or below, shows current local time, hunger, and health in local chat and attempts to eat one offhand golden carrot.
- Triggers once per low-hunger episode and rearms above 14; automatic clicks pause during the meal and resume afterward.
- Restored default white text for command feedback, help, and alerts.
