# Byte floating keyboard feasibility spike

Branch: `byte-keyboard-spike`
Base: `byte-overlay-test`

## Purpose

Test whether the existing native Byte overlay can become focusable, accept text through an Android `EditText`, display the soft keyboard, and return to its normal non-focusable floating state.

This is an isolated experiment. It is not the V0.3 chat implementation.

## Scope

The spike changes only `OverlayTestService.java`:

- Keeps the existing native floating robot and drag interaction.
- Adds a small floating panel with a native text field, transcript and Send control.
- Opens the panel in a focusable overlay state and requests the Android IME.
- Uses `SOFT_INPUT_ADJUST_RESIZE`.
- Keeps `FLAG_NOT_TOUCH_MODAL` so touches outside the overlay can pass through.
- Restores `FLAG_NOT_FOCUSABLE` when the panel is minimized.
- Echoes submitted text locally; there is no AI request, voice feature, shared repository or persistent history.

## Device test procedure

1. Build and install this branch as a separate test APK if package identity permits; otherwise uninstall only the previous test APK after preserving it.
2. Grant the app's overlay permission.
3. Launch the test activity and show the floating robot.
4. Open another app, such as Chrome or Notes.
5. Tap the floating robot.
6. Tap the text field and confirm the Android keyboard appears.
7. Type a sentence and press Send.
8. Confirm both the typed message and local echo response appear.
9. Minimize the panel; confirm the keyboard closes and the robot remains visible.
10. Reopen the panel and repeat.
11. Tap outside the panel and interact with the underlying app; confirm outside touches pass through.
12. Rotate the device if supported and note any positioning or resizing problems.

## Record

- Device and Android/HyperOS versions:
- Keyboard app:
- Keyboard appears:
- Text field receives input:
- Send button works:
- Enter key works:
- Panel resizes above keyboard:
- Outside touches pass through:
- Keyboard closes on minimize:
- Robot remains draggable:
- Crashes, visual issues or other notes:

## Interpretation

A pass requires all key interaction steps to work while another app is in the foreground. If the keyboard appears only while the test activity itself is foregrounded, the floating keyboard requirement is not yet proven.

Do not merge this branch into the main application until the result is recorded and reviewed.
