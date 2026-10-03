---
name: fabric-mod-upgrade
description: Update an existing Fabric Minecraft mod to a requested or latest stable Minecraft version, including Fabric dependencies, build tooling, API migrations, and verification. Use for version ports; not for creating a new mod.
---

# Fabric Mod Upgrade

Port the existing mod while preserving its features, saved data, and project conventions.

## Determine the target

For “latest,” check [Fabric Develop](https://fabricmc.net/develop/) and the [Fabric game metadata](https://meta.fabricmc.net/v2/versions/game) at task time. Choose the newest stable Minecraft release unless the user requests a snapshot. The Develop page renders versions with JavaScript; if its text view is incomplete or stale, cross-check the current [Fabric example mod](https://github.com/FabricMC/fabric-example-mod), Fabric release notes, and official Maven metadata. Select a Fabric API artifact built for the exact target game version. Record the checked versions and source links in the handoff.

## Port the project

Inspect `gradle.properties`, the Gradle wrapper, build scripts, mod metadata, CI, and existing instructions before editing. Align Minecraft, Loader, Loom, Fabric API, Java, and Gradle requirements. For unobfuscated Minecraft 26.1 and later, follow the current example mod's `net.fabricmc.fabric-loom` setup; do not carry forward Yarn or identity mappings without a demonstrated need. Update the mod's declared compatibility to match what was actually built.

Compile and address errors from changed Minecraft or Fabric APIs. Check input, rendering, screens, networking, and client/server environment boundaries where this mod uses them. For 26.3 and later, GLFW key constants and `InputConstants.Type.KEYSYM` are obsolete; use the game's current input constants and type. Treat that as a version-specific migration example, not a rule for older targets.

## Verify and hand off

Run the repository's build with the required JDK. Where practical, exercise the development client for visible behavior and a dedicated server for networking changes. Inspect the produced JAR and metadata when packaging changed. Stop after resolving concrete failures; do not publish a release unless requested. Report the versions, features checked, and any unverified runtime behavior.
