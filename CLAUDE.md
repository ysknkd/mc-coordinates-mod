# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Minecraft Fabric mod that allows players to record in-game coordinates and display them on-screen. It tracks player positions across multiplayer sessions and provides a HUD overlay with coordinate management features.

## Build System

**Gradle Commands:**
- `./gradlew build` - Build the mod JAR
- `./gradlew runClient` - Launch Minecraft client with mod for testing
- `./gradlew runServer` - Launch Minecraft server with mod for testing
- `./gradlew clean` - Clean build artifacts

**Project Structure:**
- Built with Fabric Mod Loader for Minecraft 1.21.5
- Uses Java 21 and Gradle with Fabric Loom plugin
- Outputs JAR to `build/libs/` directory

## Architecture

**Multi-Environment Design:**
- Common entry point: `CoordinatesApp` (shared between client/server)
- Client-specific: `CoordinatesClient` (HUD, keybindings, screens)
- Server-specific: `CoordinatesServer` (network broadcasting)

**Data Storage:**
- World-scoped data persistence in `config/mc-coordinates/{worldId}/`
- Client-side storage only (no server-side data persistence)
- JSON format for both config and coordinate data
- UUID-based coordinate indexing for efficient operations

**Key Components:**
- `CoordinatesDataManager` - Singleton managing coordinate CRUD operations
- `Config` - Per-world configuration management
- Network handlers for real-time player position sharing
- HUD renderers for coordinate display and indicators
- Screen classes for coordinate list and settings UI

**Network Communication:**
- Custom payload system for coordinate sharing between players
- Server broadcasts player positions every 20 ticks (1 second)
- Three payload types: coordinate sharing, player positions, logout notifications

## Development Patterns

**Singleton Pattern:**
- `CoordinatesDataManager.getInstance()` for coordinate data
- `Config.getInstance()` for world configuration

**Observer Pattern:**
- `CoordinatesDataListener` interface for UI updates
- Register listeners to react to coordinate data changes

**World Identification:**
- Single-player: UUID stored in world save folder
- Multi-player: MD5 hash of server address
- Enables proper data isolation between worlds/servers

## Testing and Development

**Client Testing:**
- Use `./gradlew runClient` to launch test client
- Default keybindings: G (save coordinates), B (open coordinate list)
- Test data persists in `run/config/mc-coordinates/`

**Server Testing:**
- Use `./gradlew runServer` for multiplayer testing
- Test coordinate sharing between multiple connected clients
- Verify player position broadcasting functionality

**Fabric Documentation Research:**
- Use deepwiki to investigate Fabric-related questions: `mcp__deepwiki__ask_question` with `repoName: "FabricMC/fabric"`
- Query Fabric API documentation for specific implementation details
- Reference official Fabric patterns and best practices

**Key Files for Common Tasks:**
- Adding new keybindings: `src/client/java/.../event/KeyBindingEventHandler.java`
- Modifying HUD display: `src/client/java/.../hud/CoordinatesRenderer.java`
- Network protocol changes: `src/main/java/.../network/` package
- Configuration options: `src/client/java/.../config/Config.java`
- Screen modifications: `src/client/java/.../screen/` package

## Mod Metadata

- Mod ID: `mc-coordinates`
- Supports both client and server environments
- Requires Fabric API and Java 21+
- Compatible with Minecraft 1.21.4+