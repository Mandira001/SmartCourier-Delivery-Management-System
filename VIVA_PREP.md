# SmartCourier Delivery Management System - Viva Preparation

## 1. One-Minute Project Introduction

SmartCourier Delivery Management System is a microservices-based courier management platform. It allows users to register, login, create courier deliveries, generate tracking numbers, view delivery history, and track shipment status. Admin users can monitor all deliveries, update delivery status, view dashboard statistics, and generate reports. The system uses a React frontend, Spring Boot backend microservices, PostgreSQL for persistent data, RabbitMQ for asynchronous tracking updates, Redis for token/session storage, Eureka for service discovery, Config Server for centralized configuration, API Gateway as the single entry point, and Zipkin for distributed tracing.

## 2. Main Technologies Used and Why

| Technology | Where Used | Why Used |
|---|---|---|
| React 18 + Vite | `frontend` | Fast single-page frontend with component-based UI and quick development server. |
| Spring Boot | All backend services | Simplifies REST APIs, dependency injection, security, JPA, and service setup. |
| Spring Cloud Gateway | `api-gateway` | Single entry point, routing, JWT validation, circuit breaker fallback. |
| Eureka Server | `Eureka-Server` | Service discovery so services can find each other by name. |
| Spring Cloud Config Server | `config-server` | Centralized external configuration from GitHub config repo. |
| PostgreSQL | Auth, delivery, tracking services | Relational persistent storage for users, deliveries, tracking events, documents, proofs. |
| Spring Data JPA | Entity/repository layers | Reduces boilerplate CRUD and maps Java entities to database tables. |
| Spring Security | Gateway and services | Role-based access control with USER and ADMIN roles. |
| JWT | Auth + gateway | Stateless authentication token carrying email and role. |
| Redis | Auth service | Stores issued JWT against user email for 1 hour, matching token expiry. |
| RabbitMQ | Delivery -> Tracking communication | Asynchronous event-driven status updates. Delivery service publishes events; tracking service consumes them. |
| OpenFeign | Admin service | Admin service calls delivery service by service name through Eureka/load balancing. |
| Zipkin + Micrometer Tracing | Services | Tracks requests across microservices for observability/debugging. |
| Docker Compose | Root `docker-compose.yml` | Runs all services and infrastructure together. |
| Swagger/OpenAPI | Backend services | API documentation and testing endpoints from browser. |

## 3. Microservices and Responsibilities

### API Gateway - Port 8080

This is the entry point of the whole backend. The frontend calls `/gateway/...` routes. The gateway validates JWT tokens in `GatewayJwtFilter`, extracts `email` and `role`, then forwards them as headers:

- `X-User-Email`
- `X-User-Role`

It also has a Resilience4j circuit breaker and `/fallback` endpoint, so if a service is down, the gateway returns a clean service-unavailable response.

Important viva line: "The gateway centralizes authentication validation and prevents every service from separately parsing the JWT."

### Auth Service - Port 8081

Responsible for signup and login.

Endpoints:

- `POST /auth/signup`
- `POST /auth/login`
- `GET /auth/test`

Flow:

1. Signup checks if email already exists.
2. Password is encrypted using `PasswordEncoder`.
3. Normal users get role `USER`.
4. Admin users require admin key `SECRET123`.
5. Login verifies email and password.
6. JWT is generated with email as subject and role as a claim.
7. Token is stored in Redis for 1 hour.

Main classes:

- `AuthController`
- `AuthService`
- `User`
- `UserRepository`
- `JwtUtil`

### Delivery Service - Port 8082

Responsible for booking deliveries and managing delivery lifecycle.

Endpoints:

- `POST /deliveries` - USER creates a delivery.
- `GET /deliveries/my` - USER views own deliveries.
- `GET /deliveries/{id}` - USER gets a delivery by id.
- `PUT /deliveries/{id}?status=...` - ADMIN updates status.
- `GET /deliveries` - USER or ADMIN gets all deliveries.

Entities:

- `Delivery`
- `Address`
- `DeliveryStatus`
- `PackageDetails` exists, but current create flow mainly uses sender/receiver addresses.

Delivery lifecycle:

`DRAFT -> BOOKED -> PICKED_UP -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED`

Important code behavior:

- New delivery starts as `BOOKED`.
- A unique tracking number is generated using `UUID.randomUUID().toString()`.
- After creation or status update, delivery service sends a message to RabbitMQ queue `tracking_queue`.

### Tracking Service - Port 8083

Responsible for tracking history, documents, and proof of delivery.

Endpoints:

- `POST /tracking/events` - ADMIN manually adds tracking event.
- `GET /tracking/{trackingNumber}` - USER or ADMIN gets tracking history.
- `POST /tracking/documents/upload` - USER uploads document.
- `GET /tracking/documents/{trackingNumber}` - gets uploaded documents.
- `GET /tracking/documents/download/{id}` - downloads document.
- `POST /tracking/proof` - USER saves delivery proof.
- `GET /tracking/{trackingNumber}/proof` - gets latest proof.

RabbitMQ flow:

1. Delivery service publishes message like `trackingNumber|BOOKED`.
2. Tracking service listener consumes from `tracking_queue`.
3. It parses tracking number and status.
4. It saves a `TrackingEvent` with location `"AUTO UPDATE"` and remarks `"Updated via Delivery Service"`.

Main classes:

- `TrackingController`
- `TrackingService`
- `TrackingListener`
- `TrackingEvent`
- `Document`
- `DeliveryProof`

### Admin Service - Port 8084

Responsible for dashboard, reports, monitoring, and resolving delivery status.

Endpoints:

- `GET /admin/dashboard`
- `GET /admin/deliveries`
- `GET /admin/exceptions`
- `PUT /admin/deliveries/{id}/resolve?status=...`
- `GET /admin/reports`
- `GET /admin/users`
- `GET /admin/hubs`

It uses OpenFeign:

```java
@FeignClient(name = "delivery-service")
```

This means the admin service does not hardcode the delivery service URL. Eureka resolves `delivery-service` dynamically.

### Config Server - Port 8888

Loads centralized configuration from:

`https://github.com/Mandira001/Cloud-Config-for-project`

This is useful because service-specific configuration can be changed in one place instead of inside each service.

### Eureka Server - Port 8761

Acts as service registry. Services register themselves with Eureka, and other services/gateway can discover them by service name.

## 4. Frontend Functionality

The frontend is a React single-page app.

Important files:

- `src/App.jsx` - top-level navigation, token storage, role-based menu.
- `src/api.js` - all backend API calls.
- `LoginPage.jsx` - login and token handling.
- `SignupPage.jsx` - user/admin registration UI.
- `DeliveriesPage.jsx` - create shipment, view deliveries, generate QR shipping label PDF.
- `TrackingPage.jsx` - search tracking number and display timeline.
- `AdminPage.jsx` - dashboard, charts, CSV export, status update.
- `NotificationsPage.jsx` - simulated email/SMS notification inbox.

Frontend storage:

- JWT token is stored in localStorage as `scdm_token`.
- Role is stored as `scdm_role`.
- Notifications are stored as `scdm_notifications`.

Libraries:

- `lucide-react` for icons.
- `framer-motion` for animations.
- `react-qr-code` for QR shipping labels.
- `jspdf` and `html2canvas` for PDF export.
- `recharts` for dashboard charts.

## 5. Complete User Flow

### User Signup/Login Flow

1. User signs up from React frontend.
2. Frontend calls `/gateway/auth/auth/signup`.
3. Gateway allows auth APIs without JWT.
4. Auth service saves user with encoded password.
5. User logs in.
6. Auth service returns JWT.
7. Frontend stores JWT in localStorage.
8. Further requests send `Authorization: Bearer <token>`.

### Create Delivery Flow

1. User opens My Deliveries and submits sender/receiver address.
2. Frontend calls `/gateway/deliveries/deliveries`.
3. Gateway validates token and adds user headers.
4. Delivery service creates delivery with logged-in user's email.
5. Delivery status is set to `BOOKED`.
6. Tracking number is generated.
7. Delivery is saved in PostgreSQL.
8. Delivery service publishes `trackingNumber|BOOKED` to RabbitMQ.
9. Tracking service consumes the event and saves tracking history.

### Tracking Flow

1. User enters tracking number.
2. Frontend calls `/gateway/tracking/tracking/{trackingNumber}`.
3. Tracking service fetches all tracking events ordered by timestamp.
4. Frontend displays timeline and current status.

### Admin Status Update Flow

1. Admin changes status in dashboard.
2. Frontend calls `/gateway/admin/admin/deliveries/{id}/resolve?status=...`.
3. Admin service uses Feign to call delivery service.
4. Delivery service validates status transition.
5. Delivery service saves new status.
6. Delivery service publishes RabbitMQ tracking update.
7. Tracking service stores the new tracking event.
8. Frontend also creates a simulated SMS notification.

## 6. Security Explanation

Authentication:

- User logs in and receives JWT.
- JWT contains email and role.
- Token expires after 1 hour.

Authorization:

- Gateway validates JWT.
- Gateway forwards role/email headers.
- Services use `@PreAuthorize` annotations.

Examples:

- `@PreAuthorize("hasRole('USER')")` allows only users.
- `@PreAuthorize("hasRole('ADMIN')")` allows only admins.
- `@PreAuthorize("hasAnyRole('USER','ADMIN')")` allows both.

Password security:

- Passwords are not stored in plain text.
- They are encoded using Spring Security `PasswordEncoder`.

## 7. Database Design

Important tables/entities:

- `users`: stores name, email, encoded password, role.
- `delivery`: stores delivery id, sender/receiver address references, status, customer email, tracking number, created date.
- `address`: stores sender/receiver details.
- `tracking_event`: stores tracking number, status, location, remarks, timestamp.
- `document`: stores uploaded document metadata and local file path.
- `delivery_proof`: stores receiver name, proof image path, and delivered time.

Relationships:

- `Delivery` has one sender `Address`.
- `Delivery` has one receiver `Address`.
- Tracking events are linked by `trackingNumber`, not foreign key.

## 8. Why Microservices?

This project uses microservices because each major business responsibility is separated:

- Auth handles users and tokens.
- Delivery handles booking and lifecycle.
- Tracking handles shipment history.
- Admin handles monitoring and reporting.

Benefits:

- Independent development.
- Better separation of concerns.
- Easier scaling of busy services.
- Fault isolation.
- Service discovery and gateway make the architecture manageable.

Trade-off:

- More configuration and infrastructure is needed: gateway, Eureka, config server, tracing, message broker.

## 9. Important Viva Questions and Answers

### Q1. What problem does your project solve?

It solves courier delivery management by providing user registration, delivery booking, tracking number generation, live shipment tracking, admin monitoring, status updates, reports, and notification simulation in one system.

### Q2. Why did you use Spring Boot?

Spring Boot reduces setup effort and provides production-ready modules for REST APIs, security, JPA, validation, configuration, actuator, and integration with Spring Cloud.

### Q3. Why did you use API Gateway?

The API gateway gives a single entry point to the system. It routes requests to services, validates JWT tokens, forwards user identity headers, and provides fallback behavior using circuit breaker.

### Q4. Why is Eureka used?

Eureka is used for service discovery. Instead of hardcoding service URLs, services register with Eureka and can communicate using service names like `delivery-service`.

### Q5. Why RabbitMQ instead of direct API call for tracking update?

RabbitMQ makes tracking updates asynchronous and decoupled. Delivery service does not need to wait for tracking service. If tracking service is temporarily slow, messages can still be queued.

### Q6. What is JWT and how is it used?

JWT is a signed token used for stateless authentication. In this project, it stores the user's email as subject and role as a claim. The gateway validates the token and forwards user identity to services.

### Q7. What is Redis used for?

Redis stores the generated token against the user's email for 1 hour. It acts as a fast in-memory store for token/session data.

### Q8. How is role-based access implemented?

The gateway extracts the role from JWT and sends it as `X-User-Role`. Service filters convert that role into Spring Security authority like `ROLE_USER` or `ROLE_ADMIN`. Then methods are protected using `@PreAuthorize`.

### Q9. How is a tracking number generated?

When a delivery is created, the delivery service generates a unique tracking number using Java UUID.

### Q10. What happens when admin updates a delivery status?

Admin service calls delivery service through Feign. Delivery service validates the status transition, saves the new status, and publishes an event to RabbitMQ. Tracking service consumes that event and saves it in tracking history.

### Q11. What is the use of Config Server?

Config Server centralizes configuration. Instead of keeping all environment settings inside every service, services can fetch config from a shared GitHub config repository.

### Q12. What is Zipkin used for?

Zipkin is used for distributed tracing. It helps trace a request as it moves through gateway, admin, delivery, tracking, and message flow, making debugging easier.

### Q13. What is the difference between authentication and authorization?

Authentication verifies who the user is, usually by login credentials and JWT. Authorization checks what the user is allowed to do, such as USER creating delivery and ADMIN updating status.

### Q14. How does the frontend know whether the user is admin?

After login, the frontend decodes the JWT payload and reads the `role` claim. It stores the role in localStorage and shows admin dashboard only when role is `ADMIN`.

### Q15. What are the main limitations of the current project?

- Admin signup UI does not currently send `adminKey`, while backend requires it for admin registration.
- Notification system is simulated in frontend localStorage, not real email/SMS.
- File uploads are stored on local disk, not cloud storage.
- JWT secret is hardcoded and should be moved to secure configuration in production.
- Some gateway route definitions are expected from external config server, not visible in local application properties.

## 10. Best Way to Explain Architecture in Viva

Use this exact sequence:

"The frontend communicates only with the API Gateway. The gateway validates JWT tokens and routes requests to the proper microservice. Auth service manages users and JWT generation. Delivery service manages booking and lifecycle of deliveries. Whenever delivery status changes, it sends an event to RabbitMQ. Tracking service consumes that event and stores tracking history. Admin service uses Feign to call delivery service and generate dashboards and reports. Eureka enables service discovery, Config Server centralizes configuration, PostgreSQL stores data, Redis stores token/session data, and Zipkin helps trace requests."

## 11. Demo Script

1. Open frontend.
2. Signup or login as user.
3. Create a new shipment in My Deliveries.
4. Show generated tracking number and shipping label with QR code.
5. Search the tracking number in Tracking page.
6. Login as admin.
7. Open dashboard and show total deliveries/charts.
8. Update delivery status.
9. Return to tracking page and show updated timeline.
10. Show notifications page for simulated email/SMS alerts.

## 12. Short Definitions to Memorize

- Microservice: A small independent service focused on one business capability.
- API Gateway: A single entry point that routes and secures requests.
- Eureka: Service registry for discovering services dynamically.
- Config Server: Central configuration provider for microservices.
- JWT: Signed token containing user identity and claims.
- RabbitMQ: Message broker for asynchronous communication.
- Redis: Fast in-memory key-value store.
- Feign: Declarative REST client used for service-to-service calls.
- JPA: Java API for object-relational mapping.
- Zipkin: Tool for distributed request tracing.

