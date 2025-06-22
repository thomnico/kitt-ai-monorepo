# KITT Framework Monorepo

A cross-platform monorepo for the KITT framework - an intelligent voice assistant inspired by Knight Rider that operates on-device using Kyutai and Gemma AI models. This monorepo supports Android native, iOS native, and a Supabase backend with offline-first capabilities.

## Structure

- **apps/**: Platform-specific applications
  - **android/**: Native Android app using Kotlin
  - **ios/**: Native iOS app using Swift and Core ML
  - **api/**: API functions for backend services
- **packages/**: Shared code and logic across platforms
  - **shared/**: Cross-platform shared logic
  - **ai-models/**: AI model integration layer for Kyutai Moshi and Gemma
  - **ui-components/**: Shared UI components where applicable
  - **data-sync/**: Offline-first data sync implementation with Supabase
  - **voice/**: Voice processing modules
- **tools/**: Build and development tools
- **.github/**: GitHub Actions for CI/CD
- **docs/**: Documentation for architecture, implementation, and guidelines

## Development Workflow

1. Set up the monorepo using Yarn workspaces for dependency management.
2. Implement shared code packages first, then platform-specific applications.
3. Create unit and integration tests for each package.
4. Configure CI/CD pipelines for automated testing and deployment.

## Core Requirements

- **Cross-Platform Support**: Native implementations for Android (Kotlin) and iOS (Swift), with shared business logic.
- **AI Model Integration**: On-device Kyutai Moshi for voice interactions and Gemma models with quantization for mobile constraints.
- **Offline-First Architecture**: Local-first data approach using SQLite, connected to a Supabase backend for synchronization.

For detailed implementation guidelines, security considerations, performance optimizations, and documentation requirements, refer to the full project prompt and documentation in the `docs/` directory.
