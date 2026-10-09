# ETHER — Initial Project Plan

## Product goal
Create a new Android-first, voice-first personal AI assistant for Emperor Lucian (Lucian / Alexander Ntow refer to the same person in this project). The app should feel like a polished JARVIS-style assistant while remaining honest about its capabilities and connection status.

## Scope for the first usable version

### 1. App foundation
- A new app structure, independent of previous repositories.
- A dark, high-contrast interface with a readable home screen.
- Clear loading, empty, offline, success, and error states.
- Accessible text sizes and touch targets.

### 2. Assistant interaction
- Text chat first, with speech input/output added through platform-supported services.
- Controls to pause/resume speech, skip/forward where supported, and stop speech.
- A copy action for assistant responses.
- Greetings based on the device's actual local time, not a hard-coded time of day.
- Honest disclosure when an action is unavailable or needs a connected account.

### 3. Connections centre
- List integrations with states such as Not connected, Connecting, Connected, and Error.
- Begin with one integration at a time after the stack and provider requirements are confirmed.
- Use official OAuth flows; never ask users to paste passwords or commit secrets.
- Provide disconnect and error-recovery paths.

### 4. Autonomous business operations
- Track multiple business concepts, offers, customer requests, jobs, proposals, deadlines, deliverables, invoices, and reports.
- Begin with zero-cost services: short-form scripts, captions, research summaries, content calendars, editing briefs, and other digital deliverables.
- Add an opportunity pipeline: discover through permitted public sources/APIs, score fit and fraud risk, draft proposals, track applications, and track delivery.
- Do not scrape websites or automate marketplace interactions unless the platform explicitly permits the exact use case and grants required API access. Never spam proposals, fake credentials, or bypass platform controls.
- Automate drafting, quality checks, reminders, reporting, and other reversible work. Require human confirmation for contracts, material commitments, external submissions where not pre-authorised, and any action with financial/legal consequences.
- Require explicit approval before purchases, payments, subscriptions, ad spend, inventory commitments, or transfers. Never hold or request a user's payment credentials.
- Never claim a business, social account, application, delivery, or payout succeeded unless the provider confirms it.

### 5. Earnings and payout tracking
- Record expected and received income separately in GHS and USD without silently converting currencies.
- Record client/platform source, fees when known, and intended payout route.
- Connect only payment services that support the user's country and verified account. Payout credentials and identity checks must be completed by the user with the provider.
- Keep a clear ledger; never present ledger entries as actual bank or mobile-money balances.

## Suggested delivery sequence
1. **Foundation:** select Kotlin/Jetpack Compose or another explicitly chosen framework; document supported Android versions.
2. **UI prototype:** home screen, chat view, settings, connection centre.
3. **Core interaction:** text chat, speech controls, local-time-aware greeting, copy response.
4. **AI provider:** initial Gemini REST integration with Android Keystore-encrypted local key, explicit send/test actions, and understandable HTTP error handling. Production deployment should use a trusted backend for credentials.
5. **Business operations foundation:** persistent workspace, Work Finder, AI proposal drafting, local opportunity pipeline, and GHS/USD earnings ledger.
6. **Approved integrations:** obtain official API access and implement one permitted integration at a time; begin with a provider that supports public discovery or user-authorised work workflows.
7. **Automation engine:** add scheduled jobs, retries, logs, pause/stop controls, daily budgets, and approval gates after the backend and integrations exist.
8. **Quality:** unit/UI tests, accessibility pass, build workflow, release notes, debug APK artifact.

## Acceptance criteria for the first prototype
- The app launches to a coherent dark home screen.
- Navigation and controls work; no decorative button falsely implies a completed action.
- Time-sensitive greetings use actual device-local time.
- Voice controls visibly reflect real playback/listening state.
- Errors are understandable and recoverable.
- No credentials or tokens are present in the repository.
- README includes reproducible build/run instructions once the stack is selected.

## Decisions still needed
- App framework and build environment.
- Minimum Android version and whether iOS is in scope.
- Gmail, Calendar, YouTube, and TikTok developer setup and user authorisation.
- A trusted backend for production AI credential management.
- Which single OAuth/API integration to implement first.
- Which payment method is available on the user's own verified accounts; the app cannot determine that without provider setup.
- A backend and permitted discovery APIs for dependable background opportunity monitoring; the current Android-only implementation cannot safely perform full unattended business operations by itself.

## Out of scope for the initial prototype
- Autonomous spending or financial commitments.
- Claiming that third-party accounts or businesses exist without verified provider responses.
- Importing old ETHER-OS code, architecture, bugs, or migration history.
