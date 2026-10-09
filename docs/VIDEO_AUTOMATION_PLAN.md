# ETHER Video Automation — Build Plan

## Goal
Build a reliable, zero-capital-first workflow that can create long YouTube videos and short vertical cutdowns, then publish them to YouTube and TikTok when official access is approved.

## Current implementation
- The Android app now has a Video Automation screen.
- It saves the chosen niche, output format, and target platforms locally.
- With a Gemini key configured, it can generate a detailed production blueprint for long videos, Shorts, and TikTok cutdowns.
- The blueprint can be copied or saved to the local Business Workspace.
- The screen links to CapCut, YouTube upload API documentation, and TikTok Content Posting API documentation.
- The UI explicitly distinguishes a production blueprint from a rendered/exported video.

## Not yet implemented
- Direct CapCut integration. The public CapCut AI workflow is an interactive editor workflow; do not assume an officially supported unattended rendering API exists for our use case.
- Video rendering/export service or backend render worker.
- Secure server-side AI credential storage and job queue.
- YouTube OAuth, upload, schedule, status polling, and error recovery.
- TikTok OAuth, Content Posting API, publishing audit, status polling, and error recovery.
- Durable cloud storage for generated video files.
- Background scheduling, retries, job logs, pause/resume, and daily limits.
- Automated checks for factual accuracy, copyright/licensing, audio, aspect ratio, captions, and output duration.

## Recommended architecture
1. Android app collects niche, formats, schedule, and channel settings.
2. A trusted backend stores secrets securely and queues production jobs.
3. AI planning creates the script, scene plan, metadata, thumbnail prompt, and checks.
4. A rendering provider or controlled rendering worker creates MP4 assets. Choose only after verifying API access, free quota, rights, and export limitations.
5. Quality checks validate the output before upload.
6. YouTube integration uploads/schedules with user-authorised OAuth.
7. TikTok integration posts through the official Content Posting API only after the video.publish scope is approved and the required audit is passed; otherwise, clearly show the available draft/private-only route.
8. Status polling/webhooks record provider-confirmed success or failure.
9. The user can pause automation at any time. Purchases, subscriptions, ad spend, and other financial commitments remain approval-gated.

## Important platform limits
- YouTube uploads through the Data API require OAuth. Unverified API projects can have uploads restricted to private visibility until audit.
- TikTok Direct Post requires an approved video.publish scope and user authorisation. Unaudited clients are restricted to private viewing.
- The mobile app alone should not be relied upon for unattended 24/7 jobs; use a backend scheduler/worker.
- Never store production API secrets in the Git repository or hard-code them into the APK.
- Do not claim a video was created, uploaded, scheduled, or published until the provider confirms it.

## Milestones
1. M1 — Planning UI: implemented; test build required.
2. M2 — Rendering proof of concept: compare CapCut's available AI features with services that expose supported automation APIs; test one short and one long output without paying.
3. M3 — Secure backend: job queue, storage, secret management, logs, retries, pause switch.
4. M4 — YouTube publishing: OAuth, upload, schedule, and status confirmation; complete any required API audit.
5. M5 — TikTok publishing: OAuth, approved scopes, API audit, and status confirmation.
6. M6 — End-to-end autonomy: scheduled research, production, quality checks, publishing, analytics, and daily reports.

## First test
Create one 30–60 second faceless video and one 5–8 minute video using only free-tier features. Confirm the exported files are usable, watermark/licensing conditions are acceptable, and any automation/API route is permitted before selecting a production provider.
