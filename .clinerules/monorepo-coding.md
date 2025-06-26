# Cline's Rules for the KITT Framework Monorepo

These are the core, non-negotiable principles for developing within the KITT Framework. They are derived from our Product Requirements and Coding Guidelines to ensure we build a high-quality, scalable, and legally compliant system. Adherence is mandatory.

---
## Generic rules

### Rule 1: Performance is a Feature, Not an Afterthought

- **The 200ms Rule:** All voice interactions must have an end-to-end latency of less than 200ms on our target hardware (Crosscall Core-Z5).
- **Optimize Relentlessly:** Code must be written with hardware constraints (4GB RAM, Adreno 643 GPU) in mind. Every function, especially in the `voice-engine` and `ai-models` packages, must be optimized for performance.

### Rule 2: Offline-First is Our Foundation

- **No Internet, No Problem:** Every feature must be fully functional without an internet connection. The `data-sync` engine is our source of truth for state management.
- **Assume Disconnection:** Design all user flows, from voice commands to data updates, to work seamlessly offline. Online connectivity is an enhancement, not a requirement.

### Rule 3: We Do Not Break the Law

- **Zero "Knight Rider" IP:** Avoid any and all references to "KITT," "Knight Industries," or specific catchphrases from the show. Use generic terms like "voice assistant."
- **License Compliance:** Strictly adhere to the licenses of our AI models (Kyutai Moshi: CC-BY, Gemma: Apache 2.0). Provide proper attribution in all distributions.

### Rule 4: The Monorepo Structure is Sacred

- **Stay in Your Lane:** All code must reside in its designated package or app directory. No cross-dependencies that violate our modular design.
- **Atomic Commits:** Changes that span multiple packages (e.g., updating `shared-core` and the Android/iOS apps) must be contained within a single, atomic commit.

### Rule 5: The Trunk is the Only Source of Truth

- **Short-Lived Branches Only:** All work must be done in branches off `main` that are merged back within a few days at most.
- **No Direct Pushes to `main`:** All changes must go through a pull request with at least one review.
- **Feature Flags for In-Progress Work:** Long-running features must be hidden behind feature flags, not kept in long-lived branches.

### Rule 6: Automation is Our Quality Gatekeeper

- **If It's Not Tested, It's Broken:** Every pull request must pass all automated tests, linters, and formatters before it can be merged. No exceptions.
- **Selective Builds are Mandatory:** Our CI/CD pipeline must be configured to only build and test the components affected by a change.

### Rule 7: Dependency Management is Centralized

- **One Lockfile to Rule Them All:** All dependencies are managed through the root `package.json` and its associated lockfile. No project-specific lockfiles.
- **Internal Packages are First-Class Citizens:** Shared code in `/packages/` must be treated as a versioned, internal dependency.

### Rule 8: Utilize Screenshot Features for Analysis When Available

- **Capture and Analyze Visual Output:** Whenever possible, use ADB or Android CLI tools to capture screenshots of the app for testing and verification purposes. When image analysis models or tools become available, integrate them to programmatically analyze screenshots and validate expected UI or functional results.
- **Organize Screenshots:** Store captured screenshots in a designated directory (e.g., `apps/android/screenshots/`) with timestamps or relevant identifiers to maintain organization and traceability.

---

These rules are the blueprint for our success. They ensure we build a framework that is fast, reliable, legally sound, and maintainable.

## PRDs above all

In **all** projects, every decision must be in line with the monorepo PRD.md and the PRD.md of the project being worked on.

### PRD status

If PRD status is draft, suggest enhancement to the PRD based on the context and you can modify the PRD with permission.

If PRD status is dev, don't modify the PRD, folllow instruction first and PRD second especially if need to mock data or validate an hypothesis with temporary code. If explicitly asked in the prompt to not mock follow that instruction. Ask the user if unsure. 

If PRD status is prod, follow strictly this PRD, no modfication. Ask questions to clarify when there is uncertainty in what to do next.
