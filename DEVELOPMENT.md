# chy 开发与维护

Minecraft Java 1.21.11，Java 21，Fabric / Forge / NeoForge。玩家安装和命令说明见 [README](README.md)。

## 架构与选型

工程保留在 `autoclick-template-1.21.11/`，以避免迁移本地存档和现有工程路径。v3.3.0 使用一个 Gradle 多模块工程，输出三个独立的客户端模组。

| 目录 | 职责 |
| --- | --- |
| `common/src/main/java` | 不依赖 Minecraft 或加载器的状态、计时、调度和 Brigadier 命令树 |
| `common/src/test/java` | 状态、命令参数、暂停、断线与点击调度测试 |
| `common/src/minecraft/java` | 三端共用的 Minecraft 操作、红色反馈及潜行/右键 Mixin |
| `common/src/main/resources` | 共用图标和客户端 Mixin 配置 |
| `fabric/` | Fabric 入口、生命周期事件、命令事件与 `fabric.mod.json` |
| `forge/` | Forge 入口、事件总线注册与 `META-INF/mods.toml` |
| `neoforge/` | NeoForge 客户端入口、事件注册与 `META-INF/neoforge.mods.toml` |

调研参考（2026-10-12）：

- [JEI](https://github.com/mezz/JustEnoughItems) 将通用代码与加载器代码放在同一仓库的独立模块中，适合统一维护功能。
- [MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template) 使用 common 源码与加载器子项目，无需增加跨平台运行时库。
- [Architectury Templates](https://github.com/architectury/architectury-templates) 提供另一种多加载器工程方案；当前功能只涉及客户端命令和输入，无需引入额外平台 API。
- 构建配置参考 [Forge 1.21.11 MDK](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.11.html)、[Forge Mixin 示例](https://github.com/MinecraftForge/MDKExamples/tree/master/mixins-only/fg7) 和 [NeoForge 1.21.11 MDK](https://github.com/NeoForgeMDKs/MDK-1.21.11-ModDevGradle)。

本项目采用共享源码编译：每个加载器在自己的 Minecraft 依赖环境中编译同一份 common 源码，再直接打进各自 JAR。三个模块均使用 Mojang 官方名称映射；Fabric Loom 将发布包映射为 Fabric 的运行时名称；Forge 1.21.11 和 NeoForge 使用官方运行时名称，不生成旧版 SRG refmap。`common` 的纯 Java 测试不需启动游戏。玩家无需安装 common 或 Architectury API。

Forge 和 NeoForge 的事件总线不同，入口分别实现；自动操作仍只在客户端注册。Mixin 配置只使用 `client` 列表，避免专用服务器加载客户端类。

## 构建与测试

从仓库根目录执行：

```bash
cd autoclick-template-1.21.11
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build collectJars
```

使用项目自带的 Gradle 9.5.0 Wrapper。首次构建需要联网下载依赖。ForgeGradle 7 的 Mavenizer 构建工具另外需要 Java 25，已配置官方 MDK 使用的 Foojay resolver，由 Gradle 自动下载到工具链缓存；游戏和模组仍使用 Java 21。版本固定在 `gradle.properties`：Fabric Loader 0.19.5、Fabric API 0.141.6+1.21.11、Forge 61.2.0、NeoForge 21.11.45。

`build` 编译三个加载器并运行共享逻辑测试，`collectJars` 将三个可安装 JAR 汇总至：

```text
build/libs/chy-fabric-3.3.0+mc1.21.11.jar
build/libs/chy-forge-3.3.0+mc1.21.11.jar
build/libs/chy-neoforge-3.3.0+mc1.21.11.jar
```

单独构建或测试：

```bash
./gradlew :common:test --configure-on-demand
./gradlew :fabric:build
./gradlew :forge:build
./gradlew :neoforge:build
open common/build/reports/tests/test/index.html
```

单加载器的产物位于 `<加载器>/build/libs/`。不要安装 `common`、`-sources.jar` 或 `build/devlibs` 中的开发 JAR。`built/` 保存历史发布副本，不会被构建自动覆盖；发布时手动复制本次的三个文件。

## 启动开发客户端

```bash
./gradlew :fabric:runClient
./gradlew :forge:runClient
./gradlew :neoforge:runClient
```

Forge 的开发启动会先生成 JAR，再用该完整 JAR 替换运行类路径中的散装类/资源目录，避免 Forge 61 将资源目录单独识别为缺失入口的模组；macOS 自动加入 `-XstartOnFirstThread`。

各加载器使用独立的 `run-fabric-1.21.11/`、`run-forge-1.21.11/`、`run-neoforge-1.21.11/`，日志在相应目录的 `logs/latest.log`。旧 `run/` 和 `run-1.21.11/` 保留，不自动迁移存档。开发客户端不应当作已登录的正式启动器客户端使用。

以下清单应在三个加载器上分别验证，自动化逻辑测试不能替代实际游戏行为验证：

1. 输入 `/chy_help`，确认包含左右键、tick 参数、持续潜行、开关方式及退出重置说明。
2. 准星对着实体输入 `/left_click`，确认显示 `自动左键，间隔12 tick` 并周期攻击；准星移开后只挥手、不挖方块。再次输入关闭。
3. 输入 `/left_click 40`，再无参数关闭、重开，应恢复 12 tick。`/right_click` 也执行同样检查。
4. 手持方块对着可放置的位置输入 `/right_click`，确认显示 `自动右键，间隔12 tick` 并周期放置；输入 `/right_click 4`，确认频率改变。移动准星继续放置，检查副手方块和方块耗尽时的行为。
5. 分别对两个命令测试参数 `1`、`20`，并测试 `0`、`-1`、`0.6`、`abc`、`2147483648`，非法参数应报错且不改变原设置。
6. 左右键同时开启、分别调整间隔，再关闭其中一个，另一个应继续。
7. 输入 `/shift` 后检查潜行移动、平台边缘防掉落、在箱子上放置方块；打开聊天/背包后应保持潜行。再次输入关闭，正常走动和手动 Shift 应恢复。也检查游戏设置中的“切换潜行”模式。
8. 在多人服务器或未暂停的世界中，打开聊天、背包、Esc 菜单后，左右键仍应按间隔执行。单人世界暂停时不执行，恢复后继续。
9. 三项功能开启后退出世界，重新进入应全部关闭；检查死亡重生后输入正常，旁观模式不自动点击。
10. `/auto_attack`、`/chy_autoattack`、`/chy_deepseek`、`/chy_say` 不再由本模组提供；日志中的模组标识为 `chy`。

## 资源与本地数据

- 共用图标：`common/src/main/resources/assets/chy/icon.png`。
- 共用聊天提示颜色：`common/src/minecraft/java/name/modid/ChyClient.java` 中的 `ChatFormatting.RED`。
- 只在 common 中修改功能；加载器入口只负责事件绑定。更改 Minecraft 版本时同时检查三个加载器及 Mixin 目标。
- `backup/` 保存旧代码，不参与构建。`.gitignore` 排除 `run/`、`run-*/`、`build/` 和 `.gradle/`。请勿提交测试存档、日志或依赖缓存。
- 手动安装时先关闭游戏，将对应加载器的一个 JAR 放入启动配置的 `mods` 目录；macOS 默认位置为 `~/Library/Application Support/minecraft/mods/`。Fabric 还需要 Fabric API。

## v3.3.0 验证记录（2026-10-12）

- 三加载器 `build collectJars` 通过，共享核心 14 项测试通过。
- 校验三个发布包的入口类、公共类、Java 21 字节码、版本、图标和客户端 Mixin 配置。
- Fabric、NeoForge 开发客户端启动并加载 chy。
- Forge 使用完整 JAR 启动，进入独立测试存档；日志记录左右键开关、两项 Mixin 加载及正常退出保存。
- 已补充 Mixin 最低版本和 Minecraft 1.21.11 资源包元数据。Windows / Forge 61.2.1 的最初日志只记录到启动完成，不能据此确认那次进入世界失败的原因。
- 方块放置结果、持续潜行的边缘保护、多人与三端完整行为清单仍需逐项实测。
