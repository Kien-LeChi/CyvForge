# CyvForge
 CyvFabric/CyvClient ported to 1.8.9

For the source code of CyvForge 1.12.2, please see [https://github.com/Morpheye/CyvForge-1.12] (CyvForge 1.12).

## Input recordings and position clips

The client always keeps the last **10 seconds (200 game ticks)** of inputs and
observed positions while a world is running. Paused ticks are excluded. History
and the displayed clip reset when leaving a world, changing dimension or respawning.

- Press **C** to capture and inspect the recent history. Rebind **Capture Recent
  Input and Position Clip** under Options > Controls > CyvForge Keys.
- `/cyv clip` (or `/cyv macro clip`) captures the current history.
- `/cyv clip seconds 15` changes the history to 15 seconds and saves the setting.
  The range is 1–600 seconds; `/cyv clip seconds` reports it. The same setting is
  available as **Clip history (seconds)** in the mod config GUI.
- Click a row, scroll, or use arrow/Page Up/Page Down/Home/End keys to inspect a
  tick. **Esc** returns to the world, leaving the path and tick markers visible.
  The selected tick has a yellow marker and a details overlay.
- `/cyv clip show` reopens the captured clip; `/cyv clip tick 42` selects tick 42;
  `/cyv clip hide` clears the displayed clip while history keeps recording.

Clips are frozen in-memory observations: they never simulate movement, drive the
player, replace the current macro, or change an ongoing recording. Increasing
the duration lets history fill naturally; it cannot restore already discarded ticks.

Normal macros still use `/cyv macro record start`, `/cyv macro record stop` and
`/cyv macro run` (playback is restricted to singleplayer). Recorded sprint requests
retain Minecraft's active sprint state even after releasing the sprint key, while
vanilla still handles stopping sprint (forward input, collisions, food, etc.). The
physical sprint key and active sprint state appear separately in clip details.

Both the clip inspector and selected macro row show XYZ position and **raw XYZ
velocity**. Velocity is sampled at the start of the player tick, after keyboard
polling and before Minecraft's small-motion cutoff; position is observed after
the tick. Velocity uses the complete stored `double`, without thresholding or
decimal-format rounding. It is incoming motion, not a calculated position delta.
The clip list rounds positions only for compact row summaries; selected details
show full precision. Manually editing macro inputs does not recompute observations.

Hover any tick row in the clip inspector or macro editor to see its inputs,
XYZ position, and full-precision **Raw VX / Raw VY / Raw VZ** without selecting it.
Aim the crosshair at a world tick marker for the same tooltip beside the crosshair;
the hovered marker is highlighted. This works for both captured clips and normal
recordings. Stopping a recording, or loading it in the macro editor, displays its
observed path for world-marker inspection. Opening `/cyv clip show` restores the
last captured clip. Rows from older input-only recordings report that position
and raw velocity are unavailable, rather than fabricating zero values.

Macro JSON keeps its original first ten string fields. New recordings append:
`x, y, z, rawVx, rawVy, rawVz, physicalSprintKey, activeSprinting`. Existing 9/10-field
files still load; those rows have no position/velocity observations. The macro
editor preserves appended observations when saving.

## Verification

With Java 8 configured: `gradlew.bat --offline --no-daemon recordingTest build`.
The regression task checks sprint-key gaps, raw sub-threshold velocities, JSON
round trips, buffer ordering/capacity changes, and immutable clip snapshots.

In-game check: sprint forward, briefly release/repress sprint while holding forward,
then record/replay the same sequence. Verify forward release and wall collision
still stop sprint. Press C after moving/jumping; inspect ticks and compare the
path to the actual route. Change the duration, test while recording/playing a macro,
pause/resume, and switch worlds to verify buffer isolation.
