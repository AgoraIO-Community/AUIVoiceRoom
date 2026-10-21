# AUI Voice Room

## Overview

Build a voice room experience with Agora UI components, host controls, and real-time audience audio.

Repository: [AgoraIO-Community/AUIVoiceRoom](https://github.com/AgoraIO-Community/AUIVoiceRoom)

## Prerequisites and Setup

1. Choose Android or iOS and read that platform's README. Provision RTC, RTM (including the Storage/Lock capabilities required by the sample) and the Agora Chat/Easemob application.
2. Deploy the backend described in `backend/README.md`; it points to the shared AUIKit backend. Configure its room, RTC/RTM token and chat services.
3. For Android, open `Android/`, create `local.properties`, and fill `SERVER_HOST`, `AGORA_APP_ID`, `AGORA_APP_CERT`, `AGORA_BASIC_AUTH`, `IM_APP_KEY`, `IM_CLIENT_ID` and `IM_CLIENT_SECRET` for isolated testing. Sync and run `app-voice`.
4. For iOS, follow `iOS/AUIVoiceRoom/README.md` for its own configuration, dependencies and signing. Use two users to enter the same room.

## What To Check

- Create/list/join a room and test audience-to-speaker seat transitions.
- Exercise invitations, mute, chat and user removal with distinct users.
- Leave/destroy the room and verify RTC, RTM and chat state are cleaned up.

## Integration Notes

This is a multi-service room application, not a standalone RTC sample. Shared test backends and embedded test credentials from the upstream instructions are not production deployment configurations.

Keep App Certificates, customer secrets and privileged token generation on a trusted backend for production. Use only your own test credentials while configuring historical demos; do not commit them. Preserve the sample's existing dependency versions and lifecycle until a separate migration is tested.

## Source References

- [README.md](../../README.md)
- [Android/README.md](../../Android/README.md)
- [iOS/AUIVoiceRoom/README.md](../../iOS/AUIVoiceRoom/README.md)
- [backend/README.md](../../backend/README.md)

## Validation Status

This recipe was checked against the repository's source and setup documents. The platform build and live service/device flow have not been run as part of this documentation change. Complete the checks above in an environment with the required SDKs, hardware and service access before treating it as a validated deployment.

## Implementation Prompt

```text
You are working in AgoraIO-Community/AUIVoiceRoom. Use docs/ai/RECIPE.md and its linked source files to adapt the AUI Voice Room reference to this project.

Start by inspecting the actual sample directory and configuration described above. Resolve its listed missing prerequisites before claiming it is runnable. Preserve the existing architecture and compatible dependency versions. Implement the recipe's feature-specific flow, permissions and resource cleanup. Use a trusted backend for privileged credentials and tokens in production.

Run the repository-specific build or launch flow, then perform each What To Check item. Report the commands run, the observed results and anything blocked by service access, hardware or incomplete sample code. Do not claim live validation from a documentation or catalog build alone.
```
