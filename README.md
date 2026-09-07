<p align="center">
  <img src="./assets/logo.png" alt="Aster Logo" width="120" />
</p>

<h1 align="center">Aster — Android AI Agent / Hands</h1>

<p align="center">
  <strong>Android execution layer for AI assistants — observe, resolve, act, verify.</strong>
</p>

<p align="center">
  <a href="https://github.com/paulafanasyev/aster-mcp/releases/latest"><img src="https://img.shields.io/github/v/release/paulafanasyev/aster-mcp?display_name=tag&style=flat-square" alt="Latest release" /></a>
  <a href="https://github.com/paulafanasyev/aster-mcp/releases/latest"><img src="https://img.shields.io/badge/Android-APK-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android APK" /></a>
  <img src="https://img.shields.io/badge/Android_7%2B-supported-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 7+" />
  <img src="https://img.shields.io/badge/no_root-required-ff6b6b?style=flat-square" alt="No root required" />
  <img src="https://img.shields.io/badge/self--hosted-privacy_first-gold?style=flat-square" alt="Self-hosted" />
  <a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square" alt="License" /></a>
</p>

## Project ownership and maintenance

**Project owner / maintainer of this GitHub fork: Paul Afanasyev.**

This repository is maintained as the Aster Android-agent codebase and as a reference implementation for the Hands execution layer used in the **Мир Самозанятых / OX2** project.

The upstream Aster project and its original authors/contributors remain credited in the repository history and license. This fork does **not** claim authorship of upstream code that was not written here.

## Download the Android APK

The latest verified GitHub Release currently available in this repository is:

**Светлана v1.7.1 — `v1.7.1-build.9`**

- Release page: https://github.com/paulafanasyev/aster-mcp/releases/tag/v1.7.1-build.9
- APK: https://github.com/paulafanasyev/aster-mcp/releases/download/v1.7.1-build.9/app-release.apk
- Artifact: `app-release.apk`
- Size: 18,244,550 bytes
- SHA-256: `7b00647182a308671dcdace71d4d0775c42e46e3ea71ce8bb2774901447dbc57`

Use the release APK above when you need the current Aster/Svetlana Android build. Releases are published through GitHub Releases; do not rely on an unverified local APK when a release artifact is available.

## What Aster provides

Aster is an Android AI-agent / device-control layer built around Android Accessibility and MCP. Its execution model is designed for reliable UI automation rather than blind coordinate clicking.

Core capabilities include:

- **Observe** — accessibility hierarchy, screenshots, windows, application state and events.
- **Snapshot** — capture a bounded, addressable representation of the current UI.
- **Stable element references** — retain matching descriptors instead of treating a live `AccessibilityNodeInfo` object as a durable identity.
- **Resolve** — re-find a live node from its descriptor when an action is executed.
- **Verify-before-act** — validate that the resolved node still matches the expected target before acting.
- **Act** — tap, long-press, type, scroll, swipe and invoke Android global actions.
- **Event-driven synchronization** — wait for accessibility changes/idle state instead of relying only on arbitrary sleeps.
- **Multi-window support** — account for application, system, dialog, split-screen and input-method windows.
- **Compose-aware matching** — support identifiers such as `testTag` where available, together with view IDs, labels, text, role/class and bounds.
- **Fail-closed stale references** — a missing or mismatched reference is rejected and can trigger re-observation instead of silently acting on the wrong element.
- **Safety rails** — kill-switch / STOP mechanisms and package-policy controls are part of the execution model.
- **MCP integration** — expose Android capabilities to AI agents through the Model Context Protocol.

## Execution protocol

The intended Hands loop is:

```text
OBSERVE
   ↓
SNAPSHOT
   ↓
RESOLVE target
   ↓
VERIFY target identity/state
   ↓
ACT
   ↓
WAIT for event/state change
   ↓
VERIFY result
   ↓
repeat until goal / STOP
```

If a reference is stale, missing, ambiguous or no longer matches the expected UI, the safe path is:

```text
STALE / MISMATCH
      ↓
RE-OBSERVE
      ↓
NEW SNAPSHOT
      ↓
RE-RESOLVE
      ↓
VERIFY
      ↓
ACT
```

The key design rule is simple: **never treat an ephemeral live Android accessibility object as a permanent element ID.**

## Relationship to OX2 and Светлана

Aster is a **reference implementation and execution-layer source**, not a replacement for the OX2 architecture.

The target architecture for the combined system is:

```text
                 ┌──────────────────────┐
                 │       СВЕТЛАНА       │
                 │   face + voice + chat│
                 └──────────┬───────────┘
                            │
                    ЕДИНЫЙ COMMAND BUS
                            │
                 ┌──────────▼───────────┐
                 │      OX2 AGENT       │
                 │ planning / loop /    │
                 │ state / memory       │
                 └──────────┬───────────┘
                            │
                 ┌──────────▼───────────┐
                 │     HANDS ENGINE     │
                 │ observe → act →      │
                 │ verify → repeat      │
                 └──────────┬───────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
     Accessibility       Shell/Termux       Files
       / UI control       / commands        / editor
                            │
                            ▼
                         Android
```

### Integration rules

1. **Светлана remains the user-facing assistant** — face, voice and chat are not replaced by an Aster dashboard.
2. **Chat and voice share one Command Bus** so both interfaces execute the same commands.
3. **OX2 remains the planner/loop/state layer.**
4. **Aster/Hands provides robust Android observation and execution primitives.**
5. **Do not blindly copy upstream Aster code.** Selectively integrate proven patterns and preserve OX2 contracts and forensic gates.
6. **Observe → resolve → verify → act → verify** is preferred over coordinate-only automation.
7. **STOP must always be available.** Destructive, financial and privacy-sensitive operations require appropriate safety boundaries.

## Why snapshot-based references matter

Android accessibility nodes are live framework objects. They can disappear or be recreated after a UI update, navigation event, dialog transition or window change. A robust agent therefore stores a description of the intended element and resolves it against the current accessibility tree immediately before acting.

A useful descriptor can combine:

- view/resource ID;
- text and content description;
- aggregated label;
- Compose `testTag`;
- role/class;
- bounds;
- window identity;
- structural/child path when available.

This makes automation resilient to UI refreshes and prevents a stale object identity from being mistaken for a stable target.

## OX2 forensic integration target

The Hands integration is considered successful only when runtime evidence demonstrates the complete chain, not merely a green CI job.

Required evidence includes:

- `ACCESSIBILITY_SERVICE_ENABLED=PASS`
- `HANDS_SMOKE_START`
- `HANDS_UI_OBSERVE_RESULT` with `nodes>0`
- `HANDS_UI_ACT_RESULT` with successful action
- `PASS:REAL_ACCESSIBILITY_TAP_VERIFIED`
- expected foreground package (for the smoke target)
- no `AndroidRuntime`, `FATAL EXCEPTION` or `ReactNativeJS` crash evidence
- `hands-logcat.txt` artifact

**Green CI is not by itself a Hands PASS.** Runtime evidence must prove that the real accessibility action happened and was verified.

## Safety

Aster is intended to give an AI controlled access to an Android device. Treat that capability as privileged.

Recommended safeguards:

- visible emergency **STOP** control;
- fail-closed behavior on stale or ambiguous UI references;
- package-level policy restrictions;
- explicit confirmation for destructive, financial or privacy-sensitive actions;
- least-privilege Android permissions;
- auditable execution logs;
- verification after every consequential UI action.

## Original Aster project

This fork retains the original Aster project structure and upstream history. The upstream project is the basis for the Android/MCP implementation; this repository is maintained independently under the `paulafanasyev` GitHub account for continued development and integration with OX2.

## License

See [`LICENSE`](./LICENSE).
