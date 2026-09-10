# Retro-Space

Retro-Space is the Android host for a self-contained computer environment inspired by ReProgman.

## Target
- Android 16 / API 36
- Private workspace storage by default
- Explicit import/export boundary
- ReProgman-style shell UI
- Linux-style terminal inside the workspace
- Browser/account state contained in the workspace
- Dynamic guest CPU/RAM allocation when the virtualization layer is available
- Android virtualization (AVF/pKVM) as the long-term guest architecture

## Current milestone
The repository contains the Android 16 host foundation: a private workspace, explicit host/guest boundary, and a retro shell surface. The virtualization layer is intentionally separate so the host can run on devices that do not expose the required VM capabilities.

## Architecture
```text
Android host
  └── Retro-Space shell
       ├── private workspace
       ├── import/export bridge
       └── guest controller
            └── isolated guest (AVF/pKVM where supported)
                 ├── ReProgman shell
                 ├── terminal
                 ├── browser
                 ├── apps
                 └── guest accounts/data
```

## Build
Open the repository in Android Studio Quail 4 or newer and run the `app` configuration on an Android 16 device/emulator.
