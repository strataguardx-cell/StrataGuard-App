# CLAUDE.md — StrataGuard Project Instructions

## What Is StrataGuard?

StrataGuard is a mobile app for the Australian market that empowers renters and apartment buyers with transparency into strata (body corporate) building health and helps them defend bond disputes using strata evidence. It is the first app that sits entirely on the resident's side — independent of any strata manager.

### Current Project Status (as of May 2026)

**COMPLETED:**
- ✅ Phase 1 — Project skeleton (monorepo, Gradle, version catalog, all modules scaffolded)
- ✅ Phase 2 — Auth flow (Firebase Auth with Google Sign-In working on both Android and iOS)
- ✅ Home screen with 4 feature cards: all live (no "Coming Soon" badges)
- ✅ CMP running successfully on both Android and iOS from single codebase
- ✅ Phase 3 app-side — Strata Plan Search screen + Building Detail screen (Firestore-backed, seed data, tested on Android)
- ✅ Phase 3 server-side — Spring Boot server module scaffolded: Flyway migrations (V1–V4), StrataPlanController + StrataPlanService, Firebase token filter, SecurityConfig, entities + JPA repositories, seed SQL data for NSW/VIC
- ✅ Phase 4 app-side — Document Evidence feature: camera + gallery picker, on-device EXIF AI detection, Firestore thumbnail storage, verdict badges, evidence timeline (tested on Android)
- ✅ Phase 5 app-side — Dispute Risk Check: Firestore-backed dispute CRUD, rule-based risk scoring, DisputeListScreen + DisputeViewModel, DI-wired and tested on Android
- ✅ Know Your Rights screen — static content screen, NSW/VIC toggle, 5 expandable sections per state (rights, tribunal scope, filing steps, deadlines, resources), tested on Android

**NOT STARTED / REMAINING:**
- Phase 5 server-side — built and compiles (DisputeController, DisputeService, DisputeRiskService, PdfExportService with PDFBox + S3), but not yet wired end-to-end to the app (app uses Firestore; server uses PostgreSQL)
- PDF export button in the app — "Generate Evidence Pack" trigger in DisputeListScreen calling server /api/v1/disputes/generate-pdf
- Strata document upload + OCR pipeline (Google Cloud Vision)

**Implementation notes:**
- App uses Firestore directly for strata search and evidence (MVP shortcut). Server module will be the authoritative backend once deployed.
- Evidence uses Firestore base64 thumbnails instead of Firebase Storage (avoids Storage quota/auth complexity for MVP).
- Server module requires PostgreSQL running locally (`docker compose up -d postgres` from `infra/`). Run with `./gradlew :server:bootRun --args='--spring.profiles.active=local'`.

### App Design Language

The app uses a consistent design across both platforms:
- **Header:** Navy blue (#1B2A4A) background with white text, "StrataGuard" title left-aligned, "Sign out" button right-aligned
- **Greeting:** "Hi, {user_email} 👋" with subtitle "You're protected by StrataGuard."
- **Beta badge:** Orange/amber pill badge "🚀 Beta — NSW & VIC"
- **Feature cards:** 2x2 grid of white rounded cards with subtle shadow, each containing an icon, bold title, and grey description text
- **Accent color:** Orange/amber (#E8A020 approximate) used for badges, "Coming soon" labels, and interactive elements
- **Typography:** Clean sans-serif, bold headings, regular body text
- **"Coming soon" labels:** Orange text on cards for features not yet available (Dispute Risk Check, Know Your Rights)

When building new screens, maintain this visual language: navy headers, white card surfaces, orange accents, clean spacing.

---

### Core Value Proposition

StrataGuard is a specialist **advocacy and transparency platform** — not a generalist property tech tool. This positions it in a blue ocean within the crowded Australian prop-tech market, where every other player serves strata managers or landlords.

Every existing strata app in Australia (SMATA, Resvu, StrataMax, MYBOS, Stratasphere) is built for strata managers. Resident-facing portals like Stratafy are controlled by the strata manager who decides what residents can see. StrataGuard flips this — it gives residents independent access to building health data, evidence documentation, and AI-powered dispute guidance.

### Strategic Positioning & Competitive Moat

**The Power Asymmetry:** StrataGuard weaponises information currently locked behind paywalls ($300+ professional strata reports) or gatekeepers (strata managers). Providing a "lite" version of that strata report to a renter or prospective buyer for a fraction of the cost creates immediate, tangible value.

**The Bondinator Pivot:** Integration > duplication. By focusing on the cross-referencing of strata defects with bond claims, StrataGuard builds proprietary logic (the dispute risk scoring engine) that is significantly harder to copy than a simple photo-upload tool. The moat is in the intelligence layer, not the data capture layer.

**Regulatory Tailwinds:** The July 2025 NSW strata legislation reforms strengthened consumer protections and transparency. StrataGuard is not a "nice-to-have" — it is a tool for exercising newly granted legal rights. This makes marketing narratives dramatically easier to write and positions the app as riding a regulatory wave, not fighting one.

**The OCR "Magic Moment":** The key UX moment that converts users is uploading a dense, 50-page PDF strata report and having the app instantly highlight critical risks like "Water Ingress" or "Combustible Cladding." This is the moment the app goes from "useful" to "essential." Build toward this experience in every design decision.

### Critical Risks to Monitor

**1. Data Acquisition (The Cold Start Problem)**
The value proposition depends on the app having more data than the user already has. If the user has to manually enter every defect they already know about, the "Transparency" sell weakens. Mitigations: seed data, crowdsourcing strata minutes from users, OCR auto-extraction from uploaded documents, and eventually scraper/partnership strategies for public-record filings. See "Strata Data Strategy" section below.

**2. Willingness to Pay (WTP)**
Renters are often financially squeezed. This is the single biggest validation question. The validation sprint must test concrete price points, not abstract interest. See "Validation Sprint Strategy" section below.

### What It Does (Features by Priority)

**P0 — MVP (Validation Sprint)**
- Strata plan search: user enters a strata plan number or address and sees building health summary (defect history, sinking fund status, levy amounts, meeting decisions)
- Evidence capture: timestamped photo/video documentation that the resident owns and controls
- Incident timeline: chronological log of issues for dispute preparation
- State-specific legal info: plain-English explanation of tenant/owner rights (starting NSW + VIC)
- Tribunal-ready PDF export: generates evidence packs formatted for NCAT (NSW) or VCAT (VIC)

**P1 — Post-Validation**
- AI-powered bond dispute risk assessment: cross-references strata building defects against bond claims to determine if damage is a building defect (strata's responsibility) or tenant damage
- Import from Bondinator: pull in existing condition report photos rather than re-documenting
- Push notifications: deadline reminders for tribunal filing, mediation dates, levy due dates
- Multi-state expansion: QLD (QCAT), WA (SAT), SA (SACAT), then remaining states

**P2 — Growth**
- Community features: anonymised building ratings and reviews from residents
- Strata meeting minute summarisation (OCR + AI)
- Integration with strata report providers for pre-purchase due diligence
- Freemium model: basic search free, evidence packs and AI analysis paid

---

## Tech Stack

### App — Compose Multiplatform (CMP/KMP)

Single Kotlin codebase targeting Android and iOS.

| Layer | Technology |
|-------|-----------|
| UI Framework | Compose Multiplatform |
| Navigation | Decompose or Voyager (evaluate both, prefer Voyager for simplicity) |
| Networking | Ktor Client + kotlinx.serialization |
| Local DB | Room Multiplatform (preferred) or SQLDelight |
| DI | Koin Multiplatform |
| Image Loading | Coil 3 (CMP-compatible) |
| Camera | CameraX (Android) / AVFoundation (iOS) via expect/actual |
| Auth | Firebase Auth via expect/actual wrappers |
| Async | Kotlin Coroutines + Flow |
| Build | Gradle KTS with version catalogs (libs.versions.toml) |

### Backend — Kotlin Spring Boot

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.x (Kotlin) |
| API Style | REST (JSON) — consider GraphQL later if query patterns demand it |
| Auth | Firebase Admin SDK (verify ID tokens from app) |
| Database | PostgreSQL 16 + PostGIS (geospatial address lookup) |
| Cache | Redis (session, rate limiting, strata data cache) |
| File Storage | AWS S3 (ap-southeast-2 Sydney region for AU data residency) |
| OCR | Google Cloud Vision API (extract text from condition reports, strata docs) |
| PDF Generation | Apache PDFBox |
| Migrations | Flyway |
| Testing | JUnit 5 + MockK + Testcontainers |

### ML Service — Python (P1, not needed for MVP)

| Layer | Technology |
|-------|-----------|
| Framework | FastAPI |
| ML | scikit-learn (initial), upgrade to transformer-based if data warrants |
| Task | Bond dispute risk scoring |
| Deployment | Docker container, called by Spring Boot backend via internal HTTP |

**For MVP:** Use rule-based risk scoring inside the Spring Boot server. No Python service needed yet.

---

## Monorepo Structure

```
strataguard/
├── CLAUDE.md                          ← This file
├── settings.gradle.kts                ← Root Gradle config (includes all modules)
├── build.gradle.kts                   ← Root build file (common plugins, versions)
├── gradle/
│   └── libs.versions.toml             ← Version catalog (single source of truth for deps)
│
├── shared/                            ← KMP module: models + DTOs shared by app AND server
│   ├── build.gradle.kts
│   └── src/
│       └── commonMain/kotlin/com/strataguard/shared/
│           ├── models/                ← Domain models (StrataPlan, Defect, Evidence, Dispute)
│           ├── dto/                   ← API request/response DTOs
│           ├── validation/            ← Shared validation rules
│           └── constants/             ← State enums, tribunal types, status codes
│
├── composeApp/                        ← CMP app module (Android + iOS shared UI)
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/strataguard/app/
│       │   ├── ui/
│       │   │   ├── theme/             ← Material 3 theme, colors, typography
│       │   │   ├── navigation/        ← App navigation graph
│       │   │   ├── screens/
│       │   │   │   ├── home/          ← Dashboard / strata plan search
│       │   │   │   ├── building/      ← Building health detail screen
│       │   │   │   ├── evidence/      ← Evidence capture + timeline
│       │   │   │   ├── dispute/       ← Dispute risk assessment + PDF export
│       │   │   │   ├── rights/        ← State-specific legal info
│       │   │   │   ├── auth/          ← Login / signup
│       │   │   │   └── settings/      ← User preferences, state selection
│       │   │   └── components/        ← Reusable composables
│       │   ├── data/
│       │   │   ├── repository/        ← Repository implementations
│       │   │   ├── remote/            ← Ktor API client, API service interfaces
│       │   │   └── local/             ← Room/SQLDelight DAOs, offline cache
│       │   └── domain/
│       │       ├── usecase/           ← Business logic use cases
│       │       └── repository/        ← Repository interfaces
│       ├── androidMain/kotlin/com/strataguard/app/
│       │   ├── MainApplication.kt
│       │   ├── platform/              ← Android-specific: CameraX, file picker
│       │   └── di/                    ← Android Koin modules
│       └── iosMain/kotlin/com/strataguard/app/
│           ├── MainViewController.kt
│           ├── platform/              ← iOS-specific: AVFoundation, PHPicker
│           └── di/                    ← iOS Koin modules
│
├── android/                           ← Android entry point
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/.../MainActivity.kt
│
├── ios/                               ← Xcode project (generated by CMP wizard)
│   ├── iosApp.xcodeproj/
│   └── iosApp/
│
├── server/                            ← Spring Boot backend
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/strataguard/server/
│       │   ├── StrataGuardApplication.kt
│       │   ├── config/                ← Security, CORS, S3, Redis, Firebase config
│       │   ├── controller/            ← REST controllers
│       │   │   ├── StrataPlanController.kt
│       │   │   ├── StrataDocumentController.kt
│       │   │   ├── EvidenceController.kt
│       │   │   ├── DisputeController.kt
│       │   │   └── AuthController.kt
│       │   ├── service/               ← Business logic
│       │   │   ├── StrataPlanService.kt
│       │   │   ├── EvidenceService.kt
│       │   │   ├── AiDetectionService.kt      ← AI-generated media detection pipeline
│       │   │   ├── DisputeRiskService.kt  ← Rule-based for MVP, calls ML service later
│       │   │   ├── PdfExportService.kt
│       │   │   ├── StrataDocumentService.kt
│       │   │   ├── OcrService.kt              ← Google Cloud Vision OCR + risk flag extraction
│       │   ├── repository/            ← Spring Data JPA repos
│       │   ├── entity/                ← JPA entities (maps to PostgreSQL tables)
│       │   ├── security/              ← Firebase token verification filter
│       │   └── exception/             ← Global error handling
│       ├── main/resources/
│       │   ├── application.yml        ← Spring config (profiles: local, dev, prod)
│       │   └── db/migration/          ← Flyway SQL migrations
│       └── test/                      ← JUnit 5 + MockK + Testcontainers
│
├── ml-service/                        ← Python ML (NOT needed for MVP, scaffold only)
│   ├── app/
│   │   ├── main.py
│   │   ├── models/
│   │   └── routers/
│   ├── requirements.txt
│   └── Dockerfile
│
└── infra/
    ├── docker-compose.yml             ← Local dev: PostgreSQL, Redis, ML service
    ├── docker-compose.prod.yml        ← Production config
    ├── Dockerfile.server              ← Spring Boot container
    └── nginx/                         ← Reverse proxy config (prod)
```

---

## Coding Conventions

### Kotlin (App + Server + Shared)

- **Language:** Kotlin everywhere. No Java files.
- **Style:** Follow official Kotlin coding conventions. 4-space indentation.
- **Nullability:** Leverage Kotlin null safety. Avoid `!!` — use `?.`, `?:`, `let`, `require`, or `checkNotNull`.
- **Coroutines:** Use structured concurrency. ViewModels use `viewModelScope`. Repositories expose `Flow`. Never use `GlobalScope`.
- **Naming:** PascalCase for classes/objects, camelCase for functions/properties, SCREAMING_SNAKE_CASE for constants. Packages are lowercase dot-separated.
- **DTOs:** All API DTOs go in `shared/` module and use `@Serializable` (kotlinx.serialization). Never use data classes with default mutable collections.
- **Error Handling:** Use `Result<T>` or sealed class `ApiResult<T>` for repository returns. No raw exceptions propagating to UI layer.

### Compose UI

- **State:** Hoist state up. Screens receive state + event lambdas. No business logic in composables.
- **Previews:** Every screen composable must have at least one `@Preview`.
- **Theme:** Use Material 3 dynamic color. All colors, typography, spacing from theme — no hardcoded values.
- **Components:** Extract any UI used in 2+ places into `components/` package.
- **Accessibility:** All images have `contentDescription`. Interactive elements have minimum 48dp touch targets.

### Spring Boot

- **Layering:** Controller → Service → Repository. Controllers do NO business logic.
- **Validation:** Use `@Valid` + Jakarta validation annotations on DTOs.
- **Error Responses:** Consistent error JSON format via `@ControllerAdvice` global exception handler.
- **Profiles:** `local` (Docker Compose DBs), `dev` (cloud dev environment), `prod` (production).
- **Secrets:** Never commit secrets. Use environment variables, loaded via `application.yml` placeholders.

### Database

- **Migrations:** All schema changes via Flyway. Never manual DDL.
- **Naming:** snake_case for tables and columns. Table names plural (`strata_plans`, `evidence_items`).
- **Indexes:** Add indexes for any column used in WHERE clauses or JOINs.
- **Soft Delete:** Use `deleted_at` timestamp columns, never hard delete user data.

### Git Conventions

- **Branch naming:** `feature/SG-{number}-short-description`, `fix/SG-{number}-short-description`
- **Commits:** Conventional commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
- **PRs:** One feature per PR. Include description of what and why.

---

## State-Specific Legal Modules

StrataGuard must handle different legislation per Australian state/territory. Implement this as a pluggable module system.

```kotlin
// shared/src/commonMain/kotlin/.../models/AustralianState.kt
enum class AustralianState(
    val displayName: String,
    val tribunalName: String,
    val tribunalAcronym: String,
    val legislationName: String
) {
    NSW("New South Wales", "NSW Civil and Administrative Tribunal", "NCAT", "Strata Schemes Management Act 2015"),
    VIC("Victoria", "Victorian Civil and Administrative Tribunal", "VCAT", "Owners Corporations Act 2006"),
    QLD("Queensland", "Queensland Civil and Administrative Tribunal", "QCAT", "Body Corporate and Community Management Act 1997"),
    WA("Western Australia", "State Administrative Tribunal", "SAT", "Strata Titles Act 1985"),
    SA("South Australia", "South Australian Civil and Administrative Tribunal", "SACAT", "Strata Titles Act 1988"),
    TAS("Tasmania", "Magistrates Court of Tasmania", "MagCourt", "Strata Titles Act 1998"),
    ACT("Australian Capital Territory", "ACT Civil and Administrative Tribunal", "ACAT", "Unit Titles (Management) Act 2011"),
    NT("Northern Territory", "Northern Territory Civil and Administrative Tribunal", "NTCAT", "Unit Titles Act 1975")
}
```

For MVP, only implement `NSW` and `VIC` state modules. The architecture must make adding new states trivial — a new implementation of the state interface, not changes to core logic.

---

## Database Schema (MVP Core Tables)

```sql
-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    state AustralianState NOT NULL DEFAULT 'NSW',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- Strata Plans (building-level data)
CREATE TABLE strata_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_number VARCHAR(50) UNIQUE NOT NULL,   -- e.g., SP12345 (NSW), OC12345 (VIC)
    address TEXT NOT NULL,
    suburb VARCHAR(100),
    state VARCHAR(3) NOT NULL,
    postcode VARCHAR(4),
    location GEOGRAPHY(POINT, 4326),           -- PostGIS
    total_lots INT,
    year_built INT,
    sinking_fund_balance DECIMAL(12,2),
    admin_fund_balance DECIMAL(12,2),
    quarterly_levy DECIMAL(10,2),
    last_agm_date DATE,
    data_source VARCHAR(50) NOT NULL DEFAULT 'user', -- 'seed', 'user', 'scrape', 'partner'
    contributed_by UUID REFERENCES users(id),  -- NULL for seed data, user ID for user-contributed
    fetched_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Known Defects (building-level, from strata records)
CREATE TABLE building_defects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strata_plan_id UUID REFERENCES strata_plans(id),
    category VARCHAR(50) NOT NULL,             -- structural, plumbing, electrical, cosmetic, etc.
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,             -- critical, major, minor, cosmetic
    reported_date DATE,
    resolved_date DATE,
    resolution_notes TEXT,
    source_document VARCHAR(255),              -- which strata doc this came from
    source_document_id UUID REFERENCES strata_documents(id), -- link to uploaded doc
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Strata Documents (crowdsourced uploads for OCR extraction)
CREATE TABLE strata_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strata_plan_id UUID REFERENCES strata_plans(id),
    uploaded_by UUID REFERENCES users(id),
    doc_type VARCHAR(50) NOT NULL,             -- minutes, agm_report, defect_notice, strata_report, levy_notice, insurance
    title VARCHAR(255),
    s3_key VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    file_size_bytes BIGINT,
    ocr_status VARCHAR(20) DEFAULT 'pending',  -- pending, processing, completed, failed
    ocr_extracted_text TEXT,                    -- raw OCR output
    ocr_structured_data JSONB,                 -- parsed: defects found, financial figures, dates, risk flags
    ocr_risk_flags JSONB,                      -- highlighted risks: water_ingress, combustible_cladding, etc.
    uploaded_at TIMESTAMPTZ DEFAULT now(),
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Evidence Items (user-captured photos/videos/notes)
CREATE TABLE evidence_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    strata_plan_id UUID REFERENCES strata_plans(id),
    type VARCHAR(20) NOT NULL,                 -- photo, video, note, document
    title VARCHAR(255),
    description TEXT,
    s3_key VARCHAR(500),                       -- S3 object key for media
    s3_url TEXT,                               -- pre-signed URL (generated on read)
    captured_at TIMESTAMPTZ NOT NULL,          -- when the user took the photo/video
    location GEOGRAPHY(POINT, 4326),           -- GPS where captured
    metadata JSONB,                            -- EXIF data, device info
    -- AI-generated media detection
    ai_detection_status VARCHAR(20) DEFAULT 'pending', -- pending, processing, completed, failed
    ai_detection_score DECIMAL(5,4),           -- 0.0000 to 1.0000 (probability of being AI-generated)
    ai_detection_verdict VARCHAR(20),          -- authentic, suspicious, ai_generated
    ai_detection_flags JSONB,                  -- detailed flags: missing_exif, inconsistent_noise, gan_artifacts, etc.
    ai_detection_model VARCHAR(50),            -- which detection model/version was used
    ai_detected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- Incidents (chronological timeline entries)
CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    strata_plan_id UUID REFERENCES strata_plans(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    incident_date DATE NOT NULL,
    category VARCHAR(50),                      -- water_damage, crack, mould, noise, etc.
    status VARCHAR(20) DEFAULT 'open',         -- open, reported, in_dispute, resolved
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- Link evidence to incidents (many-to-many)
CREATE TABLE incident_evidence (
    incident_id UUID REFERENCES incidents(id),
    evidence_id UUID REFERENCES evidence_items(id),
    PRIMARY KEY (incident_id, evidence_id)
);

-- Dispute Cases
CREATE TABLE disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    strata_plan_id UUID REFERENCES strata_plans(id),
    state VARCHAR(3) NOT NULL,
    tribunal VARCHAR(20) NOT NULL,             -- NCAT, VCAT, etc.
    dispute_type VARCHAR(50) NOT NULL,         -- bond, defect_rectification, levy, noise, bylaws
    status VARCHAR(30) DEFAULT 'draft',        -- draft, filed, mediation, hearing, resolved
    risk_score DECIMAL(3,2),                   -- 0.00 to 1.00 (from risk engine)
    risk_factors JSONB,                        -- breakdown of what contributed to score
    filing_deadline DATE,
    hearing_date DATE,
    outcome VARCHAR(30),                       -- won, lost, settled, withdrawn
    notes TEXT,
    pdf_export_s3_key VARCHAR(500),            -- generated tribunal evidence pack
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

-- Link incidents to disputes (many-to-many)
CREATE TABLE dispute_incidents (
    dispute_id UUID REFERENCES disputes(id),
    incident_id UUID REFERENCES incidents(id),
    PRIMARY KEY (dispute_id, incident_id)
);

-- Indexes
CREATE INDEX idx_strata_plans_plan_number ON strata_plans(plan_number);
CREATE INDEX idx_strata_plans_location ON strata_plans USING GIST(location);
CREATE INDEX idx_strata_plans_postcode ON strata_plans(postcode);
CREATE INDEX idx_strata_plans_address_fts ON strata_plans USING GIN(to_tsvector('english', address || ' ' || COALESCE(suburb, '')));
CREATE INDEX idx_strata_plans_state ON strata_plans(state);
CREATE INDEX idx_evidence_user ON evidence_items(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_incidents_user ON incidents(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_incidents_strata ON incidents(strata_plan_id);
CREATE INDEX idx_disputes_user ON disputes(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_building_defects_strata ON building_defects(strata_plan_id);
CREATE INDEX idx_strata_documents_strata ON strata_documents(strata_plan_id);
CREATE INDEX idx_strata_documents_ocr_status ON strata_documents(ocr_status) WHERE ocr_status IN ('pending', 'processing');
```

---

## Strata Search Screen — UI Specification

This is the current feature being built. When the user taps "Search a Strata Plan" from the home screen:

### Search Screen
- **Header:** Same navy header as home, with back arrow + title "Search a Strata Plan"
- **Search bar:** Prominent text field at top with placeholder "Enter strata plan number or address..."
- **Search mode toggle:** Segmented control or tabs: "Plan Number" | "Address" | "Near Me"
  - Plan Number: text field accepts SP##### format (NSW) or OC##### (VIC)
  - Address: text field with autocomplete suggestions as user types
  - Near Me: uses device GPS, shows strata plans within radius on a list/map
- **Results list:** Cards showing: plan number, address, suburb, state badge (NSW/VIC), total lots, year built
- **Empty state (no results):** Friendly illustration + "This plan isn't in our database yet" + CTA button "Add This Building" to create a user-contributed stub
- **Loading state:** Skeleton shimmer cards while searching

### Building Detail Screen (after tapping a result)
- **Header:** Navy header with building address as title
- **Hero section:** Plan number, address, state badge, total lots, year built
- **Health Summary Card:** Traffic-light style (green/amber/red) overall building health score based on:
  - Number of open defects (critical/major/minor)
  - Sinking fund balance vs recommended minimum
  - Time since last AGM
- **Defects Section:** Expandable list of known building defects with severity badges, dates, resolution status
- **Financial Section:** Sinking fund balance, admin fund balance, quarterly levy amount (if known)
- **Actions:** "Document Evidence for This Building" button → navigates to Evidence capture (Phase 4)
- **Add Info CTA:** If data is sparse (user-contributed stub), show prompts to add missing info: "Know the sinking fund balance? Add it here"

### App-Side Search Flow (Ktor Client)
```kotlin
// data/remote/StrataApiService.kt
interface StrataApiService {
    suspend fun searchStrataPlans(query: String, type: SearchType): List<StrataPlanSummaryDto>
    suspend fun getStrataPlanDetail(planId: String): StrataPlanDetailDto
    suspend fun createStrataPlan(request: CreateStrataPlanRequest): StrataPlanDetailDto
}

enum class SearchType { PLAN_NUMBER, ADDRESS, NEARBY }
```

---

## API Endpoints (MVP)

### Auth
- `POST /api/v1/auth/register` — Register with Firebase token
- `POST /api/v1/auth/login` — Verify Firebase token, return session

### Strata Plans
- `GET /api/v1/strata/search?q={query}&type={plan_number|address|nearby}&lat={lat}&lng={lng}` — Search strata plans
- `GET /api/v1/strata/{planId}` — Get building health detail
- `GET /api/v1/strata/{planId}/defects` — List known defects for building
- `POST /api/v1/strata` — Create user-contributed strata plan stub (when plan not found in DB)
- `PATCH /api/v1/strata/{planId}` — Update/enrich strata plan data (user adds missing info like sinking fund balance)

### Strata Documents (Crowdsourced)
- `POST /api/v1/strata/{planId}/documents` — Upload strata document (minutes, AGM report, defect notice) for OCR processing
- `GET /api/v1/strata/{planId}/documents` — List uploaded documents for a building
- `GET /api/v1/strata/{planId}/documents/{docId}/extracted` — Get OCR-extracted data from a document

### Evidence
- `POST /api/v1/evidence` — Upload evidence item (multipart: file + metadata). Triggers async AI detection.
- `GET /api/v1/evidence?strataId={id}` — List user's evidence for a building (includes AI detection verdict)
- `GET /api/v1/evidence/{id}` — Get evidence detail with pre-signed S3 URL + AI detection results
- `GET /api/v1/evidence/{id}/ai-detection` — Get detailed AI detection report (flags, score, model used)
- `DELETE /api/v1/evidence/{id}` — Soft delete

### Incidents
- `POST /api/v1/incidents` — Create incident
- `GET /api/v1/incidents?strataId={id}` — List incidents (timeline)
- `PUT /api/v1/incidents/{id}` — Update incident
- `POST /api/v1/incidents/{id}/evidence` — Link evidence to incident

### Disputes
- `POST /api/v1/disputes` — Create dispute case
- `GET /api/v1/disputes` — List user's disputes
- `GET /api/v1/disputes/{id}` — Get dispute detail with risk assessment
- `POST /api/v1/disputes/{id}/assess` — Run risk assessment (rule-based for MVP)
- `POST /api/v1/disputes/{id}/export-pdf` — Generate tribunal evidence pack PDF
- `POST /api/v1/disputes/{id}/incidents` — Link incidents to dispute

### Legal Info
- `GET /api/v1/legal/{state}/rights` — Get tenant/owner rights for state
- `GET /api/v1/legal/{state}/tribunal-process` — Get tribunal filing steps

---

## Local Development Setup

### Prerequisites
- JDK 17+
- Android Studio with Kotlin Multiplatform plugin
- Docker + Docker Compose
- Xcode 15+ (for iOS builds, macOS only)

### First-time setup

```bash
# Clone and enter repo
cd strataguard

# Start local databases
docker compose up -d postgres redis

# Run Flyway migrations
cd server && ./gradlew flywayMigrate

# Run Spring Boot server
./gradlew bootRun --args='--spring.profiles.active=local'

# In another terminal — run the CMP Android app
cd .. && ./gradlew :android:installDebug
```

### docker-compose.yml (local dev)

```yaml
version: '3.8'
services:
  postgres:
    image: postgis/postgis:16-3.4
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: strataguard
      POSTGRES_USER: strataguard
      POSTGRES_PASSWORD: localdev
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  pgdata:
```

---

## Build Order (What To Scaffold First)

Follow this sequence when setting up the project:

### Phase 1 — Project skeleton ✅ COMPLETED
1. Create root `settings.gradle.kts` and `build.gradle.kts` with version catalog
2. Create `gradle/libs.versions.toml` with all dependency versions
3. Scaffold `shared/` module with domain models and DTOs
4. Scaffold `composeApp/` with CMP wizard defaults + Koin + Ktor setup
5. Scaffold `android/` entry point with `MainActivity`
6. Scaffold `server/` Spring Boot project with `application.yml` profiles
7. Create `infra/docker-compose.yml`

### Phase 2 — Auth flow (vertical slice) ✅ COMPLETED
8. Set up Firebase project, add `google-services.json` (Android) and `GoogleService-Info.plist` (iOS)
9. Implement Firebase Auth in `composeApp/` (login/signup screens with Google Sign-In)
10. Home screen with greeting, beta badge, and 4 feature cards implemented
11. Firebase token verification in `server/` — still needed when backend work begins

### Phase 3 — Strata search (core feature) ✅ COMPLETE
12. ✅ Create Flyway migration for `strata_plans` + `building_defects` tables (V2, V3, V4)
13. ✅ Implement `StrataPlanController` + `StrataPlanService` in server
14. ✅ Seed sample strata data for NSW/VIC (V4 SQL migration + Firestore seed via `seedIfEmpty()`)
15. ✅ Build strata search screen + building detail screen in `composeApp/`

### Phase 4 — Evidence + Incidents 🔨 IN PROGRESS
16. ✅ Implement S3 integration in server for photo uploads (EvidenceController, EvidenceService, AwsConfig)
17. ✅ Implement on-device AI detection (EXIF/metadata analysis) in composeApp before upload
18. ✅ Create evidence capture flow in app (camera + gallery, EXIF detection, Firestore thumbnails, timeline)
19. ✅ Build incident tracking with evidence linking (IncidentController, IncidentService, incident_evidence join table)

### Phase 5 — Disputes + PDF Export
19. Implement rule-based dispute risk scoring in server
20. Build dispute creation and assessment flow in app
21. Implement PDFBox evidence pack generation
22. Test full flow: capture evidence → create incident → file dispute → export PDF

---

## Strata Data Strategy (MVP)

There is no single public API for Australian strata plan data. This is both the biggest risk and the biggest moat — whoever solves data acquisition first wins the market. For MVP, use a hybrid approach:

### Primary: User-Contributed Data
- Users search by strata plan number (e.g., SP12345) or address
- If the plan doesn't exist in our DB, the user can create a stub record and start documenting
- This makes users the data source, validates engagement, and bootstraps the database organically
- Fields the user can fill: plan number, address, suburb, state, postcode, total lots, year built
- **Critical risk:** If users have to manually enter every defect they already know about, the "Transparency" value weakens. Mitigate by making document upload + OCR auto-extraction the primary data input path (not manual forms)

### Secondary: Seed Dataset
- Pre-populate the database with a small set of real strata plans from publicly available sources
- NSW: NSW Land Registry Services publishes some strata plan info
- VIC: Owners Corporation Register has searchable records
- Goal: a few hundred plans across NSW/VIC so the app doesn't feel empty on day one
- Mark these records with `data_source = 'seed'` vs user-contributed records with `data_source = 'user'`

### Tertiary: Crowdsourced Strata Documents
- Encourage users to upload strata minutes, AGM reports, and defect notices
- OCR pipeline (Google Cloud Vision) auto-extracts key data: defect mentions, financial figures, resolution dates
- This is the scalable path — every uploaded document enriches the building's profile for all future users
- Store raw documents in S3, extracted structured data in PostgreSQL

### Future (Post-MVP):
- Scrape public registers (NSW Strata Hub, VIC Consumer Affairs searchable directories) — fragile but gives real data
- Partner with strata report providers for licensed data feeds
- Negotiate data-sharing agreements with Fair Trading / Consumer Affairs departments

### Search Implementation
- Search by plan number: exact match or prefix match on `plan_number` column
- Search by address: full-text search using PostgreSQL `tsvector` on address fields + PostGIS proximity for "near me"
- If no results found: show "This plan isn't in our database yet — would you like to add it?" flow
- Cache recent searches in Redis for fast lookups

---

## AI-Generated Media Detection

All photo and video evidence uploaded to StrataGuard must be checked for AI generation. This is non-negotiable — tribunal evidence must be authentic, and a single fake submission could destroy the platform's credibility.

### Detection Pipeline

When a user uploads a photo or video:
1. **Upload completes** → evidence record created with `ai_detection_status = 'pending'`
2. **Async processing** → backend sends media to detection service
3. **Detection runs** → returns score (0.0 = certainly authentic, 1.0 = certainly AI-generated) + detailed flags
4. **Verdict assigned:**
   - `authentic` (score < 0.3): green checkmark badge on evidence item, eligible for tribunal PDF
   - `suspicious` (score 0.3–0.7): amber warning badge, user notified, evidence still stored but flagged in any exported PDF with a disclaimer
   - `ai_generated` (score > 0.7): red flag badge, user warned, evidence excluded from tribunal PDF exports by default
5. **User can see** the verdict on every evidence item in the timeline

### Detection Signals (Multi-Layered)

Don't rely on a single detection method. Layer these:

**Layer 1 — Metadata Analysis (on-device, instant)**
- Check EXIF data: AI-generated images typically lack camera make/model, GPS, focal length, ISO
- Check C2PA/Content Credentials: if present, verify the provenance chain
- Verify file creation timestamps match claimed capture time
- Flag if image has been round-tripped through known AI tools (DALL-E, Midjourney, Stable Diffusion leave metadata traces)

**Layer 2 — Statistical Analysis (server-side, fast)**
- JPEG compression artifact analysis: AI images have different compression patterns than camera photos
- Noise pattern analysis: real camera sensors leave consistent noise fingerprints; AI-generated images have uniform or inconsistent noise
- Color histogram analysis: GAN-generated images have telltale distribution patterns
- For video: frame-level consistency checks, temporal noise analysis

**Layer 3 — ML-Based Detection (server-side, async)**
- Use a pre-trained AI detection model. Options (evaluate in order of preference):
  1. **Hive Moderation API** — commercial, high accuracy, easy integration via REST API
  2. **Illuminarty API** — commercial, supports both images and video
  3. **Open-source models:** DIRE (Diffusion Reconstruction Error), UniversalFakeDetect, or DE-FAKE — self-hosted in the Python ML service container
- For video: extract keyframes, run image detection on each, aggregate scores
- Track which model version produced each verdict (`ai_detection_model` column) for auditability

### Implementation Strategy

**MVP (Phase 4 — Evidence Capture):**
- Layer 1 only (metadata analysis) — runs on-device before upload, instant feedback
- If EXIF is missing/suspicious, show amber warning: "This image may not be from a camera. For strongest tribunal evidence, use the in-app camera."
- Store metadata check results in `ai_detection_flags` JSONB

**Post-MVP:**
- Add Layer 2 + Layer 3 server-side analysis via the backend
- Process asynchronously — don't block the upload UX
- Update verdict after processing completes, push notification if status changes

### On-Device Detection (CMP Implementation)

```kotlin
// domain/usecase/DetectAiMediaUseCase.kt
class DetectAiMediaUseCase {
    
    data class DetectionResult(
        val verdict: Verdict,
        val score: Float,
        val flags: List<DetectionFlag>
    )
    
    enum class Verdict { AUTHENTIC, SUSPICIOUS, AI_GENERATED }
    
    enum class DetectionFlag {
        MISSING_EXIF,
        NO_CAMERA_MODEL,
        NO_GPS_DATA,
        TIMESTAMP_MISMATCH,
        AI_TOOL_METADATA,        // e.g., "Made with DALL-E" in metadata
        NO_C2PA_CREDENTIALS,
        SUSPICIOUS_COMPRESSION,
        INCONSISTENT_NOISE
    }
    
    // Run on-device before upload
    suspend fun analyzeMedia(filePath: String, type: MediaType): DetectionResult
}
```

### UI Treatment

- **Authentic:** Small green shield icon + "Verified" label on evidence card
- **Suspicious:** Amber triangle icon + "Unverified — metadata incomplete" on evidence card. Tappable to see details + recommendation to re-capture with in-app camera
- **AI Generated:** Red octagon icon + "Flagged as AI-generated" on evidence card. Cannot be included in tribunal PDF export. User can appeal/dispute the flag.

### In-App Camera Advantage

Strongly encourage users to capture evidence using the in-app camera rather than uploading from gallery. In-app captures:
- Embed verified GPS + timestamp directly from device sensors
- Include device fingerprint in metadata
- Skip AI detection entirely (auto-marked as `authentic` since we control the capture pipeline)
- Show a prominent "Capture with StrataGuard Camera" button above the "Upload from Gallery" option

---

## Environment Variables (server/application.yml)

```yaml
spring:
  profiles:
    active: local

# Local profile
---
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:postgresql://localhost:5432/strataguard
    username: strataguard
    password: localdev
  redis:
    host: localhost
    port: 6379

aws:
  s3:
    bucket: strataguard-evidence-local
    region: ap-southeast-2

firebase:
  project-id: ${FIREBASE_PROJECT_ID}

google:
  cloud:
    vision:
      api-key: ${GOOGLE_CLOUD_VISION_KEY}
```

---

## Important Notes

- **Australia-only:** All data stored in ap-southeast-2 (Sydney). Privacy Act 1988 compliance required.
- **No condition reports:** Bondinator already handles this. StrataGuard focuses on strata transparency + dispute defense. Import from Bondinator, don't rebuild.
- **MVP first:** Ship the smallest thing that validates willingness to pay. No ML service, no community features, no multi-state beyond NSW/VIC.
- **Offline-capable:** Evidence capture must work offline. Queue uploads for when connectivity returns using WorkManager (Android) / BGTaskScheduler (iOS).
- **Timestamps are critical:** Every piece of evidence needs immutable, verifiable timestamps — this is what makes it admissible in tribunal proceedings.
- **AI-generated media detection is mandatory:** Every uploaded photo/video must be checked for AI generation. Fake evidence destroys platform credibility. On-device EXIF analysis for MVP, server-side ML detection post-MVP. In-app camera captures bypass detection (auto-verified). See "AI-Generated Media Detection" section.
- **OCR is a key differentiator:** Google Cloud Vision integration for auto-extracting risk signals from uploaded strata documents is what turns StrataGuard from a filing cabinet into an intelligence tool. Prioritise this in the post-MVP roadmap.

---

## Business Model & Monetization Strategy

Three revenue models to validate — they are not mutually exclusive. The validation sprint should test which resonates most.

### Model A: Transaction-Based (Primary for MVP)
- Free: strata plan search, basic building health view, evidence capture
- Paid ($49–$79): Tribunal-ready PDF evidence pack generation (one-time per dispute)
- Rationale: Users pay only when stakes are high (losing $2,500+ bond). Low friction to adoption, clear value-for-money at the moment of need.

### Model B: Subscription (Post-Validation)
- Free tier: search + basic evidence capture
- Pro ($9.99–$19.99/month): unlimited evidence storage, AI dispute risk scoring, priority OCR document processing, push notification reminders for deadlines
- Rationale: Recurring revenue, but harder sell to financially squeezed renters. Test after MVP proves the tool is sticky.

### Model C: Affiliate / Lead Generation (Growth Phase)
- Partner with tenant insurance providers (bond insurance products)
- Partner with "No Win, No Fee" strata lawyers — StrataGuard sends qualified leads (users with strong evidence packs and high dispute risk scores)
- Partner with professional strata report providers for pre-purchase due diligence upsells
- Rationale: User pays nothing, revenue from partners. Only viable at scale with sufficient user base.

**MVP pricing to validate:** Transaction model. Free to use, pay for the PDF evidence pack.

---

## Validation Sprint Strategy

Two-week sprint to validate willingness to pay before writing production backend code.

### What to Build
- Landing page explaining the value proposition with email capture
- Fake door test: "Generate Your Evidence Pack — $49" button that captures intent (email + plan number) before revealing "Coming soon"

### Where to Post
- r/AusProperty
- r/AusFinance
- r/AusPropertyChat (highly recommended — active community)
- r/sydney, r/melbourne (state-specific)
- r/AusVisa (for the immigrant renter angle — many visa holders rent in strata buildings)
- Facebook groups: Strata issues NSW, Melbourne renters, etc.

### The Killer Validation Question
Don't ask "would you use this?" — that's worthless. Instead ask:

> "If you were losing $2,500 of your bond today, would you pay $50 for a generated evidence pack that has a 70% success rate at NCAT/VCAT?"

This tests willingness to pay at a specific price point with a concrete outcome. The answer tells you whether the business works.

### Success Metrics
- 100+ email signups in 2 weeks = strong interest, proceed to MVP
- 30+ "Generate Evidence Pack" fake door clicks = validated WTP
- <20 signups = pivot the messaging or reconsider the market


