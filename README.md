# Sea Chart Navigator

A RuneLite Plugin Hub project that guides the player to the nearest sea
charting task they can do at their current Sailing level.

## What it does

- Uses the character's real Sailing level by default, with an optional boosted
  level setting.
- Ignores completed tasks by default, using the game's completion state rather
  than a separate checklist.
- Chooses the nearest eligible task by direct tile distance from the boat's
  real world-map position.
- Displays a live, camera-relative on-screen arrow, task type, required
  Sailing level and distance.
- Shows the HUD only while aboard a Sailing boat by default; it can instead be
  set to always show or be hidden. This does not affect the map pin or native
  hint arrow.
- Adds a marker to the world map.
- Uses the native RuneScape hint arrow by default, with a setting to turn it
  off if another activity needs the hint arrow.
- Can notify when a new target is selected or when the player reaches it.

The project contains the current 358-task location and completion-varbit data.
It performs guidance only: it does not click, move the character, or automate
gameplay.

## Deliberate first-release limits

- **Closest** means straight-line tile distance, not the fastest navigable
  sailing route.
- Eligibility is filtered by Sailing level and completion status. The overlay
  labels the activity type, but it does not yet verify quest, inventory or boat
  requirements.
- The arrow is directional guidance, not a collision-aware route planner.

## Build

Install a Java 17 JDK first. The project compiles its source for Java 11, but
the included Gradle wrapper requires a current JDK to run.

Open the folder as a Gradle project in IntelliJ, or run the following from a
terminal:

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

On macOS/Linux, use `./gradlew test` and `./gradlew build`.

## Test in RuneLite

With the project open in IntelliJ, click the **Gradle reload** button, then
open **Tasks > application > run** and double-click **run**. This starts a
separate development RuneLite client with Sea Chart Navigator already loaded.

Alternatively, from PowerShell in the project folder run:

```powershell
.\gradlew.bat run
```

The development client keeps its own settings and should be used only for
testing. Close it normally when you are done.

The resulting plugin JAR is written to `build/libs`. To distribute it through
RuneLite, publish the repository and submit it to the RuneLite Plugin Hub;
keep `runelite-plugin.properties` at the repository root.

## Recommended in-game settings

- Leave **Completed tasks** off.
- Leave **Use boosted Sailing level** off unless you intentionally want to use
  boosts.
- Leave **HUD visibility** on **Only while sailing** for the normal
  boat-only experience. Choose **Always show** only if you want to inspect a
  target from land.
- Turn the native hint arrow off when another activity or plugin needs it.
- Use target-change notifications sparingly; the on-screen arrow is normally
  enough.

## Updating task data

The packaged task data was generated from the BSD-licensed Sailing project
listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). The import helper
under `tools/` can regenerate it from a compatible upstream task enum and TSV
data file after a game update.
