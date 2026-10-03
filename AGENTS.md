# Repository Guidelines

## Project Structure & Module Organization

This is a Fabric mod for Minecraft 26.3. Shared and server code lives in `src/main/java/dev/ysknkd/mc/coordinates/`; client-only HUD, screens, key bindings, storage, and configuration live under the matching package in `src/client/java/`. Mod metadata and translations are in `src/main/resources/`, while indicator textures are in `src/client/resources/`. Repository screenshots are in `assets/`. The project currently has no `src/test/` tree.

## Build, Test, and Development Commands

Use Java 25 and the checked-in Gradle wrapper. `./gradlew build` compiles the mod and produces JARs in `build/libs/`; it is also the CI check. `./gradlew runClient` launches a development client for HUD and screen checks. `./gradlew runServer` launches a dedicated server for multiplayer and networking checks. `./gradlew clean` removes generated build output. Keep Minecraft, Fabric Loader, and Fabric API versions aligned with `gradle.properties` and `src/main/resources/fabric.mod.json`.

## Coding Style & Naming Conventions

Follow the surrounding Java style: four-space indentation in most classes, descriptive `PascalCase` class names, `camelCase` methods and fields, and package names under `dev.ysknkd.mc.coordinates`. Place client-only classes in `src/client/java`; keep shared payloads and server handlers in `src/main/java`. Use the existing `mc-coordinates` namespace for assets and translation keys. There is no configured formatter or linter, so keep formatting consistent with nearby code.

## Testing Guidelines

There is no automated test framework or coverage target yet. Run `./gradlew build` for every change. For UI or key-binding changes, verify behavior in `runClient` (G saves a coordinate; B opens the list). For player-position or sharing changes, test with `runServer` and connected clients, including join and logout behavior. If adding automated tests, put them in `src/test/java`, add the required test dependency, and use descriptive `*Test` class names.

## Commit & Pull Request Guidelines

Recent commits use short, action-oriented subjects such as `Fix HUD projection for 26.2` and `Guard server payload sends by channel support`. Keep commits focused and use a similarly direct subject. Release automation uses its own version and `[skip ci]` commits; avoid those forms for ordinary changes. In pull requests, explain the behavior changed, link any relevant issue, report build and manual test results, and include screenshots for visible HUD or screen changes.
