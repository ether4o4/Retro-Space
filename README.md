# Retro-Space

Retro-Space is the Android rebuild of ReProgman: a self-contained retro desktop/workspace designed to keep its own files, browser data, photos, downloads, and terminal workspace inside the app until the user explicitly imports or exports data.

## Current Android build
- Windows 3.1 Program Manager-inspired desktop UI
- Private app-owned workspace folders
- Multi-file import through Android's document picker
- Camera capture directly into private `Pictures`
- Dedicated WebView browser profile/data directory
- Linux-style terminal scoped to the private workspace
- File browser for workspace folders
- Android 16 / API 36 target
- CI build and APK verification workflow

## Isolation direction
The app is structured as the host shell for a future stronger guest/virtualized backend. A normal third-party APK cannot create an unrestricted second Android installation, so the current build enforces the boundary at app-private storage/browser/process level while keeping the guest layer replaceable.

The long-term target is a device-supported Android virtualization backend where available, with dynamic resource allocation and explicit host import/export bridges.
