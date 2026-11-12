# Marker prototype for SFS Android

## Goal

Build an Android companion prototype for Spaceflight Simulator 1.6.00.22 that can show other players as floating rocket markers over the game.

This is a fallback prototype, not a native SFS multiplayer mod. It must not claim to create real SFS rocket objects or provide collision/physics interaction.

## First prototype scope

- 2–4 players on the same Wi-Fi network.
- Each device has a player name and a distinct marker color.
- Devices exchange marker state over the local network.
- A small floating overlay can be shown above SFS after the user grants Android's display-over-other-apps permission.
- Markers show the other players' names and their shared marker positions.
- The overlay can be hidden or closed at any time.

## Important limitation

SFS Android does not currently expose a documented public API for reading the player's rocket world coordinates or inserting another rocket into the game scene. Therefore the first prototype cannot automatically place markers at the true in-game rocket coordinates. The first test must establish the Wi-Fi connection and display shared marker positions; manual marker positioning may be needed until a safe, documented integration point is found.

## Development stages

1. Create a minimal Android app and verify that it can build with GitHub Actions.
2. Implement a local Wi-Fi room for 2–4 devices and exchange player marker state.
3. Add the floating overlay and marker controls.
4. Test on real Android phones.
5. Investigate whether SFS provides a supported way to read rocket position. Only then consider automatic in-game alignment.

## Safety and privacy

- Use local Wi-Fi only for the first test; no public server is required.
- Do not collect location, contacts, or unrelated device data.
- Show a clear connected/disconnected state.
- The overlay must be user-started and easy to stop.

## Current status

Planning and repository research only. No APK has been built yet.
