# Superior Email

## What this is

Superior Email is an email *client* — not a mail server. It connects to existing mailboxes
(Gmail, TUM mail, etc.) over standard protocols (IMAP for reading, SMTP for sending, OAuth 2.0
for modern auth) and fixes UX pain points that traditional email clients get wrong.

This is a personal learning project by a TUM M&T Computer Engineering student, built to be
genuinely understood, not vibe-coded. See "How to work with me on this project" at the bottom —
it matters more than usual for this repo.

Pain points this project targets (pick 1–2 to build well before adding more — see Roadmap):

- Threading that actually works — group by conversation properly
- Snooze / send-later / follow-up reminders
- A rules engine the user controls ("if from X and contains invoice → tag + archive")
- Unified inbox across multiple accounts
- Fast local full-text search
- Tracking-pixel stripping / privacy
- Clean, keyboard-driven UX

## Architecture

```
┌─────────────────────┐        REST / JSON        ┌──────────────────────┐
│   Frontend (UI)      │  ◄──────────────────────► │   Backend (Java)      │
│   React + TS          │                           │   Spring Boot          │
│   (not started yet)   │                           │   Jakarta Mail (planned)│
└─────────────────────┘                           └──────────┬───────────┘
                                                             │ IMAP/SMTP/OAuth
                                                  ┌──────────▼───────────┐
                                                  │  Real mail servers    │
                                                  │  (Gmail, TUM, ...)    │
                                                  └──────────────────────┘
                                        + PostgreSQL (via Docker, not yet wired up)
                                          for message metadata, search index, snooze, rules
```

Backend does all mail-protocol work, auth, storage, business logic. Frontend (React + TS,
built later) is a thin client calling the backend's REST API. Alternative considered: JavaFX
desktop UI instead of React, if a browser frontend turns out to be more friction than it's
worth — not decided yet, revisit after Milestone 4.

## Stack

- **Language:** Java 21 (Temurin, via SDKMAN)
- **Framework:** Spring Boot 4.1.1
- **Build tool:** Gradle (Groovy DSL), via the `./gradlew` wrapper — do not assume Maven
- **Package root:** `com.thilorichtberg.superior_email`
- **Database:** PostgreSQL, intended to run via Docker locally — not yet connected as of
  Milestone 0
- **Mail protocol library:** Jakarta Mail (formerly JavaMail) — not yet added as a dependency
- **Frontend (future):** React + TypeScript
- **Dev environment:** WSL2 (Ubuntu 24.04), VS Code with the WSL + Java + Spring Boot
  extensions

### Important version note

This project was generated with **Spring Boot 4.1.1**, which restructured some autoconfigure
package paths compared to Spring Boot 3.x. For example, Hibernate/JPA autoconfiguration lives
under `org.springframework.boot.hibernate.autoconfigure.*` and
`org.springframework.boot.jdbc.autoconfigure.*` in this version — NOT under
`org.springframework.boot.autoconfigure.orm.jpa.*` like most tutorials and Stack Overflow
answers (which target Boot 3.x) will tell you. If something related to autoconfiguration
exclusion, conditional beans, or package paths doesn't behave as expected, check the actual
installed Spring Boot version's package structure before assuming a tutorial's package path is
correct.

## Commands

- Run the app: `./gradlew bootRun`
- Build without running: `./gradlew build`
- Run tests: `./gradlew test`
- Kill a stuck/stale Gradle daemon: `./gradlew --stop`
- Check compiled output for a class: `find build/classes -name 'ClassName*'`
- Test an endpoint without a browser: `curl http://localhost:8080/<path>`

The app runs on `http://localhost:8080` by default.

## Current status

- **Milestone 0 — Skeleton: COMPLETE.** Spring Boot app starts cleanly. `GET /health` returns
  `{"status":"ok"}` via `HealthController.java`. Committed and pushed.
- **Milestone 1 — Connect to a real mailbox: COMPLETE.**
  - Day 1: throwaway Gmail account created, 2FA + app password set up, Jakarta Mail
    dependency added, real credentials isolated in gitignored
    `application-local.properties`.
  - Day 2: verified real IMAP connection via temporary `MailDemoRunner` (deleted
    once confirmed).
  - Day 3: `GET /accounts/{id}/messages` implemented, returning a list of
    `EmailSummary` records (`from`, `subject`, `receivedAt`) as JSON, backed by
    `MailService.fetchRecent(...)`. Covered by a `@WebMvcTest` with `MailService`
- **Milestone 2 — Persist & model:** not started. Will introduce `Account`, `Message`,
  `Thread`, `Label` entities and real Postgres via Docker (currently disabled via
  `spring.autoconfigure.exclude` in `application.properties` — that line needs to be removed
  once Postgres is actually wired up).
- **Milestone 3 — First "superior" feature:** not decided yet. Candidates: threading, snooze,
  or a rules engine. Pick one, do it well, don't start the others yet.
- **Milestones 4–7** (sending, OAuth, frontend, polish): not started.

## Conventions

- Everything currently lives flat under `com.thilorichtberg.superior_email` — no subpackages
  yet. This is fine at the current size; revisit and split into `controller` / `service` /
  `model` / `repository` packages once the flat structure starts feeling cluttered (roughly
  once there are 5+ classes).
- Controllers are thin — they should call into a service, not contain business logic directly,
  even though `HealthController` (being trivial) doesn't yet demonstrate this.
- Prefer constructor injection over field injection for any future `@Autowired` dependencies.
- Config values (URLs, credentials, feature flags) belong in `application.properties`, never
  hardcoded in Java files.

## Known environment quirks (don't "fix" these without understanding why they're there)

- The `spring.autoconfigure.exclude=...` line in `application.properties` deliberately
  disables JPA/DataSource autoconfiguration. This is temporary scaffolding to let the app run
  without a database before Milestone 2 wires up real Postgres. It should be **removed**, not
  extended, once a real `DataSource` is configured.
- Development happens inside WSL2 (Ubuntu), not native Windows. File paths, line endings, and
  permissions (`chmod +x gradlew`) can behave unexpectedly if files pass through Windows
  Explorer (e.g. zip extraction) rather than staying inside the Linux filesystem the whole
  time. If a file mysteriously won't execute or a text file's content looks truncated in an
  editor, check for Windows-side interference before assuming the code itself is wrong.
- Files named `*:Zone.Identifier` are harmless Windows metadata (marks a file as
  downloaded-from-internet) and are not part of the project — safe to delete, should never be
  committed.

## Rules

- **Never commit real credentials** (mailbox passwords, OAuth client secrets, API tokens).
  These belong in a gitignored properties file or environment variables once real accounts are
  wired up in Milestone 1 onward.
- **Never run migrations or destructive database operations without explicit confirmation** —
  ask first once Postgres is in the picture.
- **Don't silently fix things outside the scope of what was asked.** If something adjacent
  looks broken or suboptimal, flag it and ask before changing it.

## How to work with me on this project

This is explicitly a learning project, not a "get it done fast" project. Please follow this
discipline:

1. **I design, you implement.** Before building a feature, I'll describe what the
   classes/endpoints/data model should look like. Implement that design rather than
   substituting your own architecture, unless you flag a concern with mine first.
2. **Always be ready to explain.** If asked "explain this file line by line" or "why did you
   choose this approach," give a real, specific explanation — not a generic summary of what
   the code obviously does.
3. **Small, reviewable changes.** Prefer one feature or fix per change, not several bundled
   together, so each commit stays understandable and reviewable.
4. **Teach, don't just deliver.** When implementing something non-trivial, briefly note the
   key concept involved (e.g. "this uses IMAP UIDs because they're stable across sessions,
   unlike message sequence numbers") so understanding builds alongside the code.
5. **Write tests alongside logic**, especially anything involving mail parsing or protocol
   edge cases — that's where correctness actually gets verified.
6. **If Spring Boot 4.x behavior differs from what's commonly documented for 3.x**, prefer
   checking the actual installed version's behavior/package structure over pattern-matching to
   older tutorials.
