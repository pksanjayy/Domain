# Hyundai Dealer Management System (DMS)

A full-stack, role-based Dealer Management System built with Angular 17 + Spring Boot 3.3 as a college project showcase. Features a premium UI design system with coral accent palette, real-time WebSocket notifications, multi-tier caching, and comprehensive audit logging.

| Layer     | Stack                                                      |
|-----------|-------------------------------------------------------------|
| Frontend  | Angular 17, Angular Material, NgRx, Chart.js, WebSocket    |
| Backend   | Spring Boot 3.3, Java 21, Spring Security (JWT), JPA       |
| Database  | MySQL 8.0, Liquibase migrations                            |
| Cache     | Caffeine (L1) + Redis 7.2 (L2) + HTTP ETag (browser)      |
| DevOps    | Docker Compose                                             |

---

## Prerequisites

| Tool               | Version      | Purpose                    |
|--------------------|--------------|----------------------------|
| Docker Desktop     | ≥ 4.x       | Run MySQL + Redis          |
| Node.js            | ≥ 18.x      | Frontend dev server        |
| npm                | ≥ 9.x       | Package manager            |
| Java JDK           | ≥ 21        | Backend runtime            |
| Maven              | ≥ 3.9        | Backend build tool         |
| Angular CLI        | ≥ 17.x      | `npm install -g @angular/cli` |

---

## Quick Start (Full Stack)

### Step 1 — Start Infrastructure

```powershell
# From project root
docker-compose up -d mysql redis
```

This starts:
- **MySQL 8.0** on `localhost:3306` (DB: `dms_db`, user: `dms_user`, pass: `dms_password`)
- **Redis 7.2** on `localhost:6379`

### Step 2 — Start Backend

```powershell
cd backend
mvn clean spring-boot:run "-Dspring-boot.run.profiles=dev" -e
```

- API: **http://localhost:8080**
- Swagger UI: **http://localhost:8080/swagger-ui.html**
- Liquibase will auto-create all tables and seed demo data on first run

> **Tip:** Use `mvn clean spring-boot:run "-Dspring-boot.run.profiles=dev" -DskipTests -e` to skip tests for faster startup.

### Step 3 — Start Frontend

```powershell
cd frontend
npm install          # First time only
ng serve --proxy-config proxy.conf.json
```

- App: **http://localhost:4200**
- The proxy routes `/api/**` and `/ws/**` to `http://localhost:8080`

> **Important:** The backend **must** be running before the frontend can function. The proxy config in `proxy.conf.json` forwards all API calls to the backend.

---

## Default Login Credentials

| Username      | Password       | Role            | Access                    |
|---------------|----------------|-----------------|---------------------------|
| `admin`       | `Admin@123`    | SUPER_ADMIN     | Full access to everything |

After login, you can create additional users with different roles via Admin → User Management.

---

## Design System

The frontend implements a custom design system defined in [`design.json`](design.json). Key design decisions:

| Token                | Value                                          |
|----------------------|------------------------------------------------|
| **Font Family**      | DM Sans (UI), DM Mono (VINs, codes, IDs)       |
| **Primary Accent**   | `#E85D3A` (Coral)                              |
| **Page Background**  | `#F4F3F0` (Warm off-white)                     |
| **Card Radius**      | `20px`                                         |
| **Sidenav**          | `#111110` (Dark, creates contrast with cards)  |
| **Card Surface**     | `#FFFFFF` with `rgba(0,0,0,0.06)` border       |

All colors use CSS custom properties (e.g., `var(--dms-accent)`) — never hardcode hex values in components.

---

## Project Structure

```
hyundai-dms/
├── backend/                          # Spring Boot 3.3 + Java 21
│   ├── src/main/java/com/hyundai/dms/
│   │   ├── config/                   # Security, Cache, WebSocket, Rate Limit
│   │   ├── common/                   # BaseEntity, ApiResponse, filters
│   │   ├── exception/                # Global exception handler
│   │   ├── security/                 # JWT, Auth, UserDetails
│   │   ├── audit/                    # AOP audit logging
│   │   └── module/
│   │       ├── user/                 # User, Role, Menu, Branch, Permission
│   │       ├── inventory/            # Vehicle, GRN, PDI, Stock Transfer
│   │       ├── sales/                # Customer, Lead, Quotation, Booking
│   │       └── notification/         # Real-time WebSocket notifications
│   ├── src/main/resources/
│   │   ├── application.yml           # Main config
│   │   ├── application-dev.yml       # Dev profile (Docker services)
│   │   └── db/changelog/             # Liquibase migrations
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                         # Angular 17 + MaterialUI
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/                 # Auth store (NgRx), interceptors, guards
│   │   │   ├── shared/               # DataTable, FilterPanel, directives, pipes
│   │   │   ├── layout/               # Topbar, Sidenav, Layout shell
│   │   │   └── modules/
│   │   │       ├── auth/             # Login (split-screen coral gradient)
│   │   │       ├── dashboard/        # KPI overview dashboard
│   │   │       ├── inventory/        # 8 components (dashboard, vehicles, GRN, PDI)
│   │   │       ├── sales/            # Leads, Customers, Quotations, Bookings
│   │   │       └── admin/            # Users, Roles, Menus, Codes, Audit Log
│   │   ├── styles.scss               # Global design system (CSS custom properties)
│   │   └── index.html                # DM Sans + DM Mono fonts
│   ├── angular.json
│   ├── proxy.conf.json               # Dev proxy → backend:8080
│   └── package.json
│
├── docker-compose.yml                # MySQL 8 + Redis 7.2
├── design.json                       # Complete UI design system specification
└── README.md
```

---

## Architecture Highlights

### Authentication & Authorization
- **JWT tokens**: 15-min access tokens + long-lived refresh tokens (stored in DB)
- **5 roles**: `SUPER_ADMIN`, `MASTER_USER`, `SALES_CRM_EXEC`, `WORKSHOP_EXEC`, `MANAGER_VIEWER`
- **Permission matrix**: Module × CRUD granularity
- **Frontend**: NgRx auth state, `JwtInterceptor` (auto-attach), `ErrorInterceptor` (auto-refresh)
- **Backend**: `@PreAuthorize` method-level security

### Data Layer
- **Query DSL**: `SpecificationBuilder` converts `FilterCriteria[]` to JPA `Specification<T>` at runtime
- **Multi-tier cache**: Caffeine (L1) → Redis (L2) → HTTP ETag (L3)
- **Audit logging**: `@Audited` AOP annotation captures before/after state with correlation IDs

### Real-Time Features
- **WebSocket**: STOMP over SockJS for push notifications
- **Notification bell**: Live unread count badge, priority-based styling

### Security Hardening
- CSP headers, X-Frame-Options: DENY, X-Content-Type-Options: nosniff
- Rate limiting: 5 req/min token-bucket on login endpoint
- CORS configured for development proxy

---

## Running Tests

```powershell
# Backend unit + integration tests
cd backend
mvn test

# Frontend unit tests
cd frontend
ng test
```

---

## Building for Production

```powershell
# Frontend production build
cd frontend
ng build --configuration production
# Output → frontend/dist/dms-frontend/

# Backend production JAR
cd backend
mvn clean package -DskipTests
# Output → backend/target/dms-*.jar

# Full stack via Docker
docker-compose up --build
```

---

## API Documentation

Swagger UI: **http://localhost:8080/swagger-ui.html**

Endpoints are grouped by module: Auth, Users, Roles, Menus, Branches, Codes, Vehicles, GRN, PDI, Stock Transfers, Customers, Leads, Quotations, Bookings, Notifications, Audit Logs.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `ECONNREFUSED` on frontend | Make sure backend is running on port 8080 |
| Liquibase lock error | Run `DELETE FROM dms_db.DATABASECHANGELOGLOCK;` in MySQL |
| `npm install` fails | Delete `node_modules/` and `package-lock.json`, retry |
| Maven compilation errors | Run `mvn clean` first, ensure Java 21 is on PATH |
| Redis connection refused | Run `docker-compose up -d redis` |
