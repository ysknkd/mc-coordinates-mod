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
- Built with Fabric Mod Loader for modern Minecraft versions
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

**Common Problem Areas:**
- Rendering components: Check for API changes in texture/text rendering methods
- Screen implementations: Verify background rendering and blur usage patterns
- Color values: Ensure proper alpha channel in color specifications
- Matrix operations: Validate transformation method calls and parameters

## Minecraft Version Migration Guide

**Version Upgrade Process:**
- Update dependency versions in `gradle.properties` (minecraft_version, yarn_mappings, loader_version, fabric_version)
- Adjust compatibility requirements in `fabric.mod.json`
- Compile to identify API changes and compatibility issues
- Address compilation errors systematically by priority

**Common API Changes:**
- Rendering API evolution (method references, parameter changes, new pipeline systems)
- Color value format changes (RGB vs ARGB, alpha channel requirements)
- Screen rendering pattern updates (background rendering, blur limitations)
- Matrix transformation API modifications

**Migration Strategy:**
- Update dependencies incrementally to isolate issues
- Use compilation errors as a roadmap for required changes
- Test frequently during migration to catch regressions early
- Document changes for future reference

## Debugging and Troubleshooting

**Systematic Problem-Solving Approach:**
- Prioritize compilation errors by impact and dependency
- Analyze error messages to identify root causes
- Use experimental fixes to validate hypotheses
- Implement changes incrementally with testing

**Investigation Tools and Techniques:**
- Java decompilation tools (javap, etc.) for internal implementation analysis
- Source code pattern analysis and search
- Fabric deepwiki queries for API documentation
- Step-by-step problem isolation through targeted fixes

**Common Problem Patterns:**
- Text rendering issues: Check color values for proper alpha channel
- Screen rendering crashes: Verify background rendering call patterns
- API compatibility: Investigate method signature changes
- Matrix operations: Validate transformation API usage

**Verification and Testing:**
- Compile after each significant change
- Run client/server tests to verify functionality
- Check for regression in existing features
- Document solutions for future reference

## Development Best Practices

**Compatibility-Focused Coding:**
- Avoid deprecated APIs when possible
- Use recommended implementation patterns from Fabric documentation
- Design for maintainability across version updates
- Follow established project conventions and patterns

**API Usage Guidelines:**
- Research proper usage patterns before implementing new features
- Validate API calls with proper parameter types and values
- Consider future-proofing when choosing between alternative approaches
- Test edge cases and error conditions

**Maintenance and Documentation:**
- Document non-obvious implementation decisions
- Track technical debt and plan for resolution
- Share knowledge through code comments and documentation
- Establish testing procedures for critical functionality

**Version Migration Preparation:**
- Monitor Fabric and Minecraft development for upcoming changes
- Maintain compatibility with multiple versions when feasible
- Create rollback procedures for failed migrations
- Establish testing environments for new versions

## Mod Metadata

- Mod ID: `mc-coordinates`
- Supports both client and server environments
- Requires Fabric API and Java 21+
- Compatible with modern Minecraft versions (check gradle.properties for current target)