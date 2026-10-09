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

### 4. Business workspace (later milestone)
- Track business ideas, tasks, drafts, and reports.
- Start with zero-cost/non-financial actions where possible.
- Require a clear confirmation step before any purchase, payment, subscription, ad spend, or inventory commitment.
- Never claim a business or social account has been created or published unless the provider confirms it.

## Suggested delivery sequence
1. **Foundation:** select Kotlin/Jetpack Compose or another explicitly chosen framework; document supported Android versions.
2. **UI prototype:** home screen, chat view, settings, connection centre.
3. **Core interaction:** text chat, speech controls, local-time-aware greeting, copy response.
4. **AI provider:** initial Gemini REST integration with Android Keystore-encrypted local key, explicit send/test actions, and understandable HTTP error handling. Production deployment should use a trusted backend for credentials.
5. **Local business workspace:** add persistent ideas/tasks and report drafts without performing external financial actions.
6. **First OAuth integration:** select one service and complete its developer-console setup with user consent.
7. **Quality:** unit/UI tests, accessibility pass, build workflow, release notes, debug APK artifact.

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
- Which single OAuth integration to implement first.

## Out of scope for the initial prototype
- Autonomous spending or financial commitments.
- Claiming that third-party accounts or businesses exist without verified provider responses.
- Importing old ETHER-OS code, architecture, bugs, or migration history.
