# Sea Chart Navigator

**Author: Corndwell**

Sea Chart Navigator helps you find your next sea charting task in Old School
RuneScape. It automatically selects the nearest task that meets your Sailing
level, then shows its direction, activity type and distance. Completed tasks
are excluded by default.

## Contents

- [Getting started](#getting-started)
- [Follow a charting target](#follow-a-charting-target)
- [How targets are selected](#how-targets-are-selected)
- [Settings reference](#settings-reference)
- [Troubleshooting](#troubleshooting)
- [Help and credits](#help-and-credits)

## Getting started

1. Open **Sea Chart Navigator** in RuneLite's plugin settings and leave
   **Enable navigator** switched on.
2. Board your Sailing boat. The navigation overlay appears when an eligible
   charting task is available.
3. Follow the direction shown, check the activity type, and complete the task
   in the game. The navigator selects another eligible task automatically.

The defaults are suitable for finding unfinished tasks at your normal Sailing
level. To inspect a target while on land, change **HUD visibility** to
**Always show**.

## Follow a charting target

The movable on-screen overlay starts in the top-left corner and contains:

| Display | Meaning |
| --- | --- |
| Task title | The charting task currently selected. |
| Direction arrow | The direction to the target relative to your camera. Rotating the camera changes the arrow's direction on screen. |
| Type | The activity associated with the task: Generic, Spyglass, Current duck, Crate, Diving or Weather. |
| Sailing level | The level required by the selected task. |
| Distance | Direct tile distance from your current world-map position to the target. While aboard a boat, this uses the boat's world-map position. |
| **At location** | You are on the target tile. A circular arrival marker replaces the direction arrow. This does not mean the task is complete. |

Open the world map to see the selected task's pin. Hover over it to see the
task title. The plugin can also show RuneScape's native hint arrow; both the
map pin and native arrow have separate settings.

The navigator provides directions only. You steer the boat and perform the
charting activity yourself. A direction may cross land or other obstacles,
so choose a navigable route using the game world and map.

### Optional notifications

Enable **New target notification** to receive an alert when the selected task
changes. Enable **Arrival notification** to receive an alert when you reach
the configured **Arrival radius**. Both are off by default.

The arrival alert is sent at most once for a selected target until the target
changes or navigation is reset. Brief loading or connection-loss transitions
do not repeat it for the same target. The default radius is eight tiles, so an
arrival alert can occur before the overlay says **At location**.

## How targets are selected

- The nearest eligible task is selected automatically as you move. There is
  no manual target selection or target lock.
- Eligibility uses your normal Sailing level unless **Use boosted Sailing
  level** is enabled.
- Completion is read from the game's task progress. Leave **Include completed
  tasks** off to focus on unfinished charting.
- Distance is measured directly in tiles, allowing diagonal distance. It is
  not sailing route length or estimated travel time.
- Quest progress, carried items, boat equipment and other activity
  requirements are not checked. Meeting the displayed Sailing level alone
  does not guarantee you can complete the task immediately.

The plugin includes 358 charting tasks. If no task meets the level and
completion filters, no navigation target is displayed.

## Settings reference

Open **Sea Chart Navigator** in RuneLite's plugin settings to change these
options.

| Setting | Default | What it does |
| --- | --- | --- |
| **Enable navigator** | On | Enables target selection, the overlay, map pin, native hint arrow and notifications according to their individual settings. Turning it off clears the current navigation. |
| **HUD visibility** | Only while sailing | **Only while sailing** shows the overlay aboard a Sailing boat. **Always show** also shows it on land. **Hide HUD** hides the overlay. This setting does not hide the map pin or native hint arrow. |
| **Include completed tasks** | Off | Allows already-completed charting tasks to be selected. |
| **Use boosted Sailing level** | Off | Uses your current boosted Sailing level instead of your normal level. The target can change as your level changes. |
| **Show world-map pin** | On | Marks the selected target on the world map. |
| **Use native hint arrow** | On | Shows RuneScape's hint arrow when it is available. Another activity or plugin's different hint arrow takes priority. |
| **Arrival radius** | 8 tiles | Distance at which the arrival notification can trigger. Choose 1–64 tiles. It does not change the overlay's **At location** marker. |
| **New target notification** | Off | Sends a notification when the selected task changes during navigation. |
| **Arrival notification** | Off | Sends a notification when you enter the selected task's arrival radius. |
| **Arrow colour** | Mint green | Changes the overlay's direction arrow and arrival marker colour. |
| **Overlay background** | Translucent black | Changes the overlay background colour and transparency. |

For a quieter display, leave notifications off. For map-only navigation,
choose **Hide HUD**, leave **Show world-map pin** on, and turn **Use native
hint arrow** off if you do not want it.

## Troubleshooting

| Issue | What to check |
| --- | --- |
| No overlay appears | Check the plugin is enabled and **Enable navigator** is on. The default HUD appears only aboard a boat; use **Always show** to inspect a target on land. No overlay appears if there is no eligible task. |
| The target changes while travelling | The plugin continually selects the closest eligible task. Your position, Sailing level or task completion may make a different task closer. |
| A completed task is selected | Turn **Include completed tasks** off. Completion updates automatically from the game's task progress. |
| I cannot perform the suggested activity | Check the task's quest, item and boat requirements in the game. The plugin filters by Sailing level and completion only. |
| The arrow points across an island | It shows the direct direction, not a route around obstacles. Plan the sailing route on the map. |
| The native hint arrow disappears | Other content or plugins can use RuneScape's shared hint arrow. Sea Chart Navigator yields to a different arrow and resumes once it is cleared. The HUD and map pin can still provide guidance. |
| Hiding the HUD leaves a map pin or hint arrow | These are controlled separately. Turn off **Show world-map pin** and/or **Use native hint arrow**, or switch **Enable navigator** off to clear all navigation. |
| An arrival alert appears before I reach the exact tile | The alert uses **Arrival radius**; **At location** appears only on the target tile. Reduce the radius for a closer alert. |
| No notification appears | Enable the relevant notification option. For arrival alerts, enter the configured radius; an alert does not repeat continuously while you remain near the same target. |

## Help and credits

Report incorrect locations or other problems through
[GitHub Issues](https://github.com/Cornwall92/SeaChartNavigator/issues).
Include the task name, relevant settings and what you expected to see. A
screenshot of the overlay and map can help explain a navigation issue.

Charting data is derived from the Sailing project. Attribution and licensing
details are in [Third-party notices](THIRD_PARTY_NOTICES.md). The plugin is
distributed under the [BSD 2-Clause License](LICENSE).
