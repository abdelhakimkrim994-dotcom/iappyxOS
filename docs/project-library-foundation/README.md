# Byte Project Library Foundation

Branch: `byte-shared-conversation-spike`

## First milestone

This change introduces the first local Project Library foundation while preserving the existing Byte chat and floating overlay.

- Project records are stored in app-private SQLite (`byte_projects.db`).
- Each project has an ID, name, description, status, creation time and update time.
- The initial schema reserves linked tables for project notes and file metadata.
- The full-screen Byte UI has a Project Library screen with create and list actions.
- Project records are accessed through a narrow `byteOverlay` JavaScript bridge.

## Deliberate limits

- File import and file-byte storage are not implemented yet.
- Project detail editing, notes UI, archive/delete controls and Gemini context retrieval are not implemented yet.
- The existing chat still uses local demo replies; Gemini is not connected.
- The floating overlay and conversation repository are unchanged.

## Acceptance test

1. Install the build without uninstalling an existing Byte app unless signing compatibility is confirmed.
2. Open the folder button in the top bar.
3. Create a project named `Byte` with a short description.
4. Close and reopen the Project Library; confirm the project remains.
5. Create a second project named `Coffee Shop App`.
6. Stop and relaunch Byte; confirm both projects remain.
7. Confirm the original full-screen chat and floating overlay still work.

Do not mark the device acceptance test as passed until it has been performed on the physical device.
