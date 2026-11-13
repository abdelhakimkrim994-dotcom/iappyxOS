# Byte shared conversation spike

Branch: `byte-shared-conversation-spike`
Base: `byte-keyboard-spike`

## Goal

Prove that the full-screen Byte interface and the native floating panel use one persistent conversation rather than independent histories.

## Implementation

- `ConversationRepository` stores messages in app-private SharedPreferences as JSON.
- Each message has an ID, role, text, and timestamp.
- `ConversationManager` validates user text, stores it, and generates the existing local demo reply.
- The full-screen WebView calls the narrow `byteOverlay.getMessagesJson()` and `byteOverlay.sendMessage(text)` bridge methods.
- The floating native panel calls the same manager/repository directly.
- The full-screen page refreshes on focus/visibility and polls for new messages; the open overlay panel refreshes its transcript periodically.
- The gear control toggles the floating Byte service. If overlay permission is missing, Android's overlay-permission screen is requested.

## Deliberate limits

- Replies are still local demo replies. No external AI provider, API key, network request, voice service, or cloud sync is included.
- The first repository is app-private SharedPreferences, capped at the newest 300 messages. This is a feasibility spike, not the final database layer.
- The overlay shows a compact recent transcript; the full-screen view shows the conversation history.
- The two interfaces share data, but do not yet share live typing/streaming state.

## Device acceptance test

1. Install this spike build and launch Byte.
2. Confirm the original V0.1 full-screen appearance is present.
3. Send a message in full-screen Byte; confirm the user message and demo reply appear.
4. Tap the gear control and grant overlay permission if Android requests it.
5. Open another app, tap the floating robot, and confirm the same messages are visible.
6. Send a different message from the floating panel.
7. Return to full-screen Byte; within about two seconds, confirm the new message and reply appear.
8. Send a message in full-screen Byte while the floating panel is open; confirm the overlay updates.
9. Minimize/reopen the panel and confirm the same history remains.
10. Stop and relaunch Byte; confirm the conversation persists.
11. Confirm the Android keyboard still opens and Send works in the floating panel.

## Pass criteria

The same ordered messages appear in both surfaces after sending from either one, and remain after closing/reopening the panel and relaunching the app. No test should be described as passed until it has been performed on the physical device.

## Build

The branch workflow builds on pushes to `byte-shared-conversation-spike` and can also be run manually. Artifact name: `byte-shared-conversation-spike-apk`.

The release build currently uses the Gradle debug signing configuration. Do not assume it can update an APK signed by a different ephemeral GitHub runner. Preserve the existing test installation until signing compatibility is checked; if replacing it becomes necessary, warn that uninstalling removes that app's local data.
