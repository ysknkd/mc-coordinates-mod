# Minecraft Coordinates Mod - Development Guide

## Build Commands
- `./gradlew build` - Build the mod
- `./gradlew runClient` - Run the Minecraft client with the mod
- `./gradlew publish` - Publish the mod artifacts

## Project Structure
- Uses Fabric Mod Loader (version 0.16.10)
- Minecraft version: 1.21.4
- Java 21 compatibility (sourceCompatibility and targetCompatibility)

## Code Style Guidelines
- **Imports:** Organized by package (Java imports first, then mod-specific); no wildcards
- **Naming:** Classes: PascalCase, Methods: camelCase, Constants: UPPER_SNAKE_CASE
- **Packages:** lowercase (e.g., dev.ysknkd.mc.coordinates)
- **Error Handling:** Try-catch with Log4j logging for detailed error messages
- **Documentation:** Javadoc for public methods/classes; comments in English/Japanese
- **Architecture:** Clear client/server code separation; uses Fabric API event system