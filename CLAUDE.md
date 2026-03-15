# CLAUDE.md — crDroid Android Manifest

## Overview

This repository is the **crDroid Android** `repo` manifest. It is a single XML
file (`default.xml`) that `repo` uses to orchestrate the checkout of ~557
individual Git repositories that together form a full Android OS source tree
based on **Android 5.0.2 (Lollipop) / CyanogenMod 12.0**.

This repo does **not** contain application or OS source code. All source code
lives in the sub-repositories declared in `default.xml`.

---

## Repository Contents

| File | Purpose |
|------|---------|
| `default.xml` | `repo` manifest — defines all sub-projects, remotes, and branch mappings |
| `README.mkdn` | Basic setup instructions for initializing and building the tree |

---

## Manifest Structure (`default.xml`)

### Remotes

| Remote name | Fetch base | Used for |
|-------------|-----------|---------|
| `aosp` | `https://android.googlesource.com` | AOSP upstream projects; pinned to `refs/tags/android-5.0.2_r1` |
| `github` | `https://github.com` | CyanogenMod and crDroid forks (default remote) |
| `private` | `ssh://git@github.com` | Private/proprietary GitHub repos |

### Default Revision

All projects without an explicit `revision` attribute track **`refs/heads/cm-12.0`**
on the `github` remote.

### Project Categories

Projects are organized into `groups` which control which projects `repo sync`
checks out by default. Key groups:

| Group | Meaning |
|-------|---------|
| `pdk` | Platform Development Kit — low-level platform components |
| `pdk-cw-fs` | PDK + connectivity/filesystem components |
| `device` | Device-specific configurations |
| `qcom` | Qualcomm-specific HALs/libraries |
| `intel` | Intel-specific hardware support |
| `notdefault` | Not synced by default (opt-in) |
| `tools` / `tradefed` | Build tooling and test infrastructure |
| `broadcom_wlan` | Broadcom WiFi drivers |
| `omap4-aah` / `nvidia_audio` / `tegra124` | SoC-specific extras |

### Source Breakdown (approximate)

- **~284 projects** from AOSP (`android.googlesource.com`)
- **~257 projects** from GitHub (`CyanogenMod/*` or `crdroidandroid/*`)
- Total: **557 projects**

---

## crDroid-Specific Overrides

These projects are forks under `crdroidandroid/` on GitHub (branch `cm-12.0`)
that replace the corresponding CyanogenMod versions:

| Local Path | Repository |
|-----------|-----------|
| `build` | `crdroidandroid/android_build` |
| `android` | `crdroidandroid/android` |
| `frameworks/base` | `crdroidandroid/android_frameworks_base` |
| `packages/apps/Settings` | `crdroidandroid/android_packages_apps_Settings` |
| `packages/apps/OmniSwitch` | `crdroidandroid/android_packages_apps_OmniSwitch` |
| `packages/apps/PackageInstaller` | `crdroidandroid/android_packages_apps_PackageInstaller` |
| `packages/apps/ScreenRecorder` | `crdroidandroid/android_packages_apps_ScreenRecorder` |
| `packages/apps/ThemeChooser` | `crdroidandroid/android_packages_apps_ThemeChooser` |
| `packages/inputmethods/LatinIME` | `crdroidandroid/android_packages_inputmethods_LatinIME` |
| `packages/providers/MediaProvider` | `crdroidandroid/android_packages_providers_MediaProvider` |
| `system/core` | `crdroidandroid/android_system_core` |
| `system/extras/su` | `crdroidandroid/android_system_extras_su` |
| `vendor/crdroid` | `crdroidandroid/android_vendor_crdroid` |

`packages/apps/crDroidOTA` is present but commented out in the manifest.

---

## Key Source Tree Paths (post-sync)

| Path | Contents |
|------|---------|
| `build/` | Android build system (Make/Ninja) |
| `frameworks/base/` | Android application framework (crDroid fork) |
| `frameworks/native/` | Native C++ system libraries |
| `packages/apps/` | ~45 system applications |
| `packages/providers/` | Content providers |
| `system/core/` | Core system daemons (init, adb, etc.) — crDroid fork |
| `hardware/` | HAL implementations (Broadcom, Intel, QCOM, etc.) |
| `vendor/crdroid/` | crDroid vendor-specific overlays and scripts |
| `vendor/cyngn/` | Cyanogen Inc. proprietary vendor bits |
| `external/` | Third-party libraries (chromium_org, openssl, etc.) |
| `device/` | Device trees and board configurations |
| `kernel/` | Kernel sources (device-specific, added via local manifests) |

---

## Development Workflows

### Initial Setup

```bash
# Initialize the manifest repo
repo init -u https://github.com/crdroidandroid/android.git -b cm-12.0

# Sync all projects (parallel, shallow)
repo sync
```

### Syncing a Single Project

```bash
# By path or repo name
repo sync frameworks/base
repo sync packages/apps/Settings
```

### Building

```bash
# Set up the environment
. build/envsetup.sh

# Choose a device target
lunch <device_codename>-userdebug   # e.g., lunch hammerhead-userdebug

# Build (uses brunch = breakfast + mka bacon)
brunch <device_codename>
```

### Making Changes to a Sub-Repository

```bash
# Create a topic branch in the sub-repo
cd frameworks/base
git checkout -b my-feature

# Make changes, commit
git add -p
git commit -m "Brief description of change"

# Push to your fork or Gerrit
git push origin my-feature
```

### Modifying the Manifest

When adding, removing, or changing sub-repositories:

1. Edit `default.xml` directly.
2. Keep entries alphabetically sorted within each logical section (the manifest
   history uses alphabetization as a convention).
3. Specify `remote=`, `revision=`, and `groups=` explicitly when deviating from
   the defaults.
4. Comment out (`<!-- ... -->`) rather than deleting projects you want to
   temporarily disable.

---

## Conventions and Rules

### Manifest Conventions

- **Alphabetical ordering** within path namespaces (enforced by past commits —
  see "Manifest: Alphabetize the manifest").
- crDroid overrides always specify `remote="github"` and `revision="cm-12.0"`
  explicitly to distinguish them from CyanogenMod defaults.
- Proprietary/device blobs go under `vendor/` with `remote="private"` if they
  require SSH access.
- Use `groups="notdefault"` for projects that should not be synced in a standard
  build environment.

### Branch Naming

- The main development branch is `cm-12.0` (CyanogenMod 12.0 / Android 5.x).
- AOSP projects are pinned to the tag `android-5.0.2_r1` and should not be
  rebranched.
- Feature/fix branches in sub-repos follow the pattern `feature/<name>` or
  `fix/<name>`.

### Commit Messages

Follow the pattern observed in the manifest history:

```
Component: Short imperative description

Optional body explaining why, not just what.
```

Examples from history:
- `Manifest: add ManagedProvisioning`
- `manifest: add new omap4 hardware repo for current device support`
- `Manifest: Track cm Dialer app`

Use `Manifest:` (capital M) for manifest-repo commits.

---

## Working with `repo`

### Useful `repo` Commands

```bash
# Show status across all projects
repo status

# Create a branch in all (or specific) projects
repo start <branch-name> --all
repo start <branch-name> packages/apps/Settings

# Diff across all projects
repo diff

# Upload changes to Gerrit
repo upload

# Run a command in every project
repo forall -c 'git log --oneline -1'
```

### Local Manifests

To add device-specific repositories without editing `default.xml`, create files
under `.repo/local_manifests/`:

```xml
<!-- .repo/local_manifests/device-mako.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <project path="device/lge/mako"
           name="CyanogenMod/android_device_lge_mako"
           remote="github"
           revision="cm-12.0" />
  <project path="kernel/lge/mako"
           name="CyanogenMod/android_kernel_lge_mako"
           remote="github"
           revision="cm-12.0" />
</manifest>
```

---

## Notes for AI Assistants

- **Do not modify individual project source code** from this manifest repo;
  changes to OS source belong in the respective sub-repositories.
- **`default.xml` is the only file that matters** in this repo. All other work
  happens downstream after `repo sync`.
- When asked to add a new device or package, add the appropriate `<project>`
  entries to `default.xml` following the alphabetical and grouping conventions
  above.
- AOSP-pinned projects (`remote="aosp"`) must keep `revision="refs/tags/android-5.0.2_r1"`
  unless there is an explicit reason to upgrade; mixing tag revisions across
  projects breaks the build.
- The `vendor/cyngn` project uses the default `github` remote and `cm-12.0`
  branch but is maintained by Cyanogen Inc., not crDroid — treat it as upstream.
- Commented-out projects (e.g., `crDroidOTA`) may be re-enabled by uncommenting;
  they were disabled intentionally and should only be re-enabled with a clear
  reason.
