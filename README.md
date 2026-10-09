# ETHER

A fresh, voice-first personal AI assistant project with a JARVIS-inspired Android interface.

## Principles
- Build from a clean start; do not import code from any previous ETHER project.
- Support text and voice with truthful listening, playback, pause, and stop states.
- Use actual device-local time for greetings.
- Never claim an integration or business action succeeded unless confirmed by the provider.
- Require explicit approval for spending, purchases, subscriptions, ad spend, or inventory commitments.
- Never commit API keys, OAuth secrets, passwords, or tokens.

## Current build (Android)
- Dark home screen with device-local time and time-aware greeting.
- Voice input using Android speech recognition, plus spoken status using Android text-to-speech.
- Assistant chat can call Gemini after the user supplies an API key in **AI Setup**; recent turns are sent as conversation context.
- Messages can be copied to the phone clipboard, and Speak reads the latest ETHER response.
- The local Business Workspace saves ideas/tasks on the phone and supports marking complete or deleting them.
- Content Studio can draft short-form video scripts/captions with Gemini, then copy or save drafts locally; it does not publish to social accounts.
- The About screen identifies the creator as Emperor Lucian / Lucian / Alexander Ntow (one person) and Dark Empire Leadership.
- The API key is encrypted at rest with Android Keystore. A key stored on a mobile device is still less protected than a backend-held secret.
- AI requests occur only after the user taps Send or Test connection. ETHER does not automatically fall back to paid providers.
- Capability and connection screens honestly show what is configured versus not yet integrated.
- Purchases, payments, subscriptions, ads, and inventory commitments remain approval-gated.

## Before using Gemini
1. Open **AI Setup** in ETHER and tap **Get a Gemini API key**.
2. Create a key in Google AI Studio, then return to ETHER and save it.
3. Check the project's free-tier eligibility, limits, and billing settings. Availability and terms can change. If billing is enabled, usage beyond free quotas may be charged; for strict zero-cost use, do not enable billing.
4. Tap **Test connection**. Only then try a normal assistant message.

Do not paste API keys into chat or commit them to GitHub. For a public production app, move provider credentials behind a trusted backend rather than embedding them in a mobile client.

## Still not integrated
- Gmail and Google Calendar OAuth.
- YouTube and TikTok OAuth/publishing.
- Full autonomous business execution, scheduling, and cross-provider fallback.
- Gmail/Calendar/YouTube/TikTok account linking is not implemented yet and will require official app configuration and user authorisation.
- A trusted backend for production AI credential management.

## Current status
Early Android prototype. Gemini chat requires the user's own configured API key and a successful connection test. This is not yet a production release.

See [docs/PROJECT_PLAN.md](docs/PROJECT_PLAN.md) for the initial scope.
