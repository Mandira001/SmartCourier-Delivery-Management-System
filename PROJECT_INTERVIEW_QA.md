# SmartCourier Project Interview Questions and Answers

Use these answers in viva/interview. Keep your tone confident, but do not memorize word-for-word. The strongest style is: answer shortly, then point to the exact code area if asked.

---

## 1. Project Introduction

**Q1. Explain your project in short.**  
SmartCourier Delivery Management System is a courier management platform built using React frontend and Spring Boot microservices. Users can register, login, book deliveries, generate tracking numbers, view delivery history, track shipment status, and download shipping labels. Admins can view dashboard statistics, monitor deliveries, update status, generate reports, and analyze revenue trends.

**Q2. What problem does this project solve?**  
It digitizes courier booking and tracking. Instead of manually managing shipment records, users get online booking and tracking, while admins get centralized monitoring, status control, and reporting.

**Q3. What is the architecture of your project?**  
It follows microservices architecture. The frontend calls an API Gateway, and the gateway routes requests to Auth, Delivery, Tracking, and Admin services. Eureka handles service discovery, PostgreSQL stores data, RabbitMQ handles asynchronous tracking updates, Redis stores login tokens, and Config Server manages centralized configuration.

**Q4. Why did you choose microservices?**  
Because each major feature has a separate responsibility. Auth handles users, Delivery handles shipments, Tracking handles tracking events, and Admin handles dashboard operations. This makes the system easier to scale, maintain, and debug compared to one large monolithic backend.

**Q5. What is the main highlight of your project?**  
The main highlight is the complete end-to-end delivery flow: login with JWT, booking a shipment, generating a tracking number, publishing a RabbitMQ event, storing tracking history, and allowing admin status updates that again update the tracking timeline.

---

## 2. Frontend and Backend Connection

**Q6. Where have you written the code to connect frontend with backend?**  
The main connection code is in `frontend/src/api.js`. It contains a common `request()` helper that uses `fetch()` to call backend APIs through `/gateway`. The Vite proxy in `frontend/vite.config.js` forwards `/gateway` requests to the backend API Gateway running on port `8080`.

**Q7. Why does the frontend use `/gateway` instead of direct service URLs?**  
Because the API Gateway is the single entry point for all backend services. The frontend does not need to know separate ports like `8081`, `8082`, or `8083`. It calls `/gateway`, and the gateway routes the request to the correct microservice.

**Q8. Which file contains the frontend API methods?**  
`frontend/src/api.js` contains methods like `login()`, `signup()`, `createDelivery()`, `getMyDeliveries()`, `getTracking()`, `getAdminDashboard()`, `getAdminDeliveries()`, and `updateDeliveryStatus()`.

**Q9. How is JWT sent from frontend to backend?**  
After login, the token is stored in `localStorage` as `scdm_token`. For protected APIs, `api.js` adds it in the request header as `Authorization: Bearer <token>`.

**Q10. What happens when the frontend calls `createDelivery()`?**  
The frontend sends sender address, receiver address, weight, distance, delivery type, and service type to `/gateway/deliveries/deliveries`. The gateway validates the JWT, adds user headers, and forwards the request to Delivery Service.
        
**Q11. How did you handle CORS during frontend development?**  
I used the Vite proxy in `frontend/vite.config.js`. The frontend runs on port `5173`, while the gateway runs on port `8080`. The proxy forwards `/gateway` calls to `http://localhost:8080`, so the browser does not directly deal with cross-origin backend URLs.

**Q12. Why did you centralize API calls in `api.js`?**  
To avoid repeating `fetch()`, headers, token logic, JSON parsing, and error handling in every page. It makes the frontend cleaner and easier to maintain.

---

## 3. Complete Project Flow

**Q13. Explain the login flow.**  
The user enters email and password on the React login page. The frontend calls `/gateway/auth/auth/login`. The gateway allows auth APIs publicly. Auth Service checks the user, verifies the encoded password, generates a JWT, stores it in Redis for one hour, and returns the token to the frontend.

**Q14. Explain the signup flow.**  
The signup page sends user details to Auth Service through the gateway. Auth Service checks duplicate email, encodes the password, assigns role `USER` by default, or `ADMIN` only if the correct admin key is provided, then saves the user in PostgreSQL.

**Q15. Explain the delivery booking flow.**  
The user fills the delivery form in `DeliveriesPage.jsx`. The frontend builds a nested payload with sender and receiver addresses and calls `createDelivery()` from `api.js`. Delivery Service calculates price, generates a UUID tracking number, saves the delivery, and publishes a `trackingNumber|BOOKED` message to RabbitMQ.

**Q16. Explain the tracking flow.**  
When a delivery is created or updated, Delivery Service publishes a message to RabbitMQ. Tracking Service listens to `tracking_queue`, reads the tracking number and status, creates a `TrackingEvent`, and stores it. The user can search the tracking number from the frontend to see the timeline.

**Q17. Explain the admin status update flow.**  
Admin updates a delivery status from `AdminPage.jsx`. The frontend calls `/gateway/admin/admin/deliveries/{id}/resolve?status=...`. Admin Service uses Feign client to call Delivery Service. Delivery Service validates the status transition, saves the new status, and publishes another RabbitMQ tracking event.

**Q18. What is the role of API Gateway?**  
API Gateway is the backend entry point. It validates JWT, extracts email and role, adds `X-User-Email` and `X-User-Role` headers, routes requests to services, and provides fallback behavior if a service is unavailable.

**Q19. What is the role of Eureka Server?**  
Eureka is used for service discovery. Services register themselves with Eureka, and other services can call them by service name instead of hardcoded URLs.

**Q20. What is the role of Config Server?**  
Config Server centralizes application configuration. Instead of managing configuration separately in every service, common and service-specific settings can be maintained from one config source.

---

## 4. Internal Code Functioning

**Q21. How does `App.jsx` work internally?**  
`App.jsx` controls global frontend state such as token, user role, selected page, dark mode, toast messages, and unread notification count. It also handles login/logout and role-based navigation.

**Q22. How is role-based navigation handled in frontend?**  
In `App.jsx`, the navigation items are filtered based on `token` and `userRole`. Admin users see the dashboard, normal users see delivery options, and logged-out users see login/signup. Actual security is still enforced in the backend.

**Q23. Why do you say frontend role checks are not real security?**  
Because frontend code can be modified by the user in the browser. Frontend role checks improve user experience, but real security is done by Gateway JWT validation and backend `@PreAuthorize` annotations.

**Q24. How does `api.js` parse responses?**  
The backend returns mixed responses: some JSON and some plain text. So `parseJson()` first reads the response as text, then tries to convert it to JSON. If parsing fails, it returns the original text.

**Q25. How does the delivery price calculation work?**  
Delivery Service calculates price using base fee, weight, distance, delivery type, and service type. National and international deliveries have different base fees and rates, and express service costs more than standard service.

**Q26. How does the frontend estimate price before submission?**  
`DeliveriesPage.jsx` has an `estimatedPrice` calculation using `useMemo()`. It mirrors the backend pricing logic so users can see an estimated price before booking.

**Q27. How is the tracking number generated?**  
In `DeliveryService.java`, a UUID is generated using `UUID.randomUUID().toString()` and saved as the delivery tracking number.

**Q28. How are tracking events created automatically?**  
Delivery Service sends a RabbitMQ message containing `trackingNumber|status`. `TrackingListener.java` consumes the message, splits it, converts the status, creates a tracking event, and saves it in the tracking database.

**Q29. How is the shipping label generated?**  
In `DeliveriesPage.jsx`, the label is rendered as HTML with delivery details and a QR code. `html2canvas` captures it as an image, and `jsPDF` converts that image into a downloadable PDF.

**Q30. How is the admin dashboard data calculated?**  
`AdminService.java` fetches all deliveries using Feign client, then calculates total deliveries, delivered count, in-transit count, pending count, and total revenue using Java streams.

---

## 5. Security Questions

**Q31. How did you implement authentication?**  
Authentication is implemented using JWT. Auth Service verifies login credentials and generates a token containing email and role. The frontend stores the token and sends it with protected requests.

**Q32. How did you implement authorization?**  
Authorization is implemented using roles. Gateway extracts the role from JWT, and services use annotations like `@PreAuthorize("hasRole('USER')")` and `@PreAuthorize("hasRole('ADMIN')")`.

**Q33. Where is the JWT validated?**  
JWT is validated in the API Gateway inside `GatewayJwtFilter.java`. If the token is missing or invalid, the gateway returns `401 Unauthorized`.

**Q34. Why are passwords secure in your project?**  
Passwords are not stored directly. During signup, Auth Service encodes passwords using Spring Security `PasswordEncoder`. During login, it matches the raw password with the encoded password.

**Q35. What is the use of Redis in authentication?**  
Redis stores the issued JWT against the user's email for one hour. This matches the token expiry time and can support session/token validation logic.

**Q36. Can a user access another user's deliveries by changing email in frontend?**  
No. The frontend does not send the customer email for "my deliveries". The gateway extracts email from the validated JWT and passes it as `X-User-Email`, so the backend uses the authenticated user's email.

**Q37. How is admin signup protected?**  
If someone selects role `ADMIN`, Auth Service requires a correct admin key. Without the key, it throws an error and does not create an admin account.

---

## 6. Microservices Questions

**Q38. What are the main microservices in your project?**  
The main services are Auth Service, Delivery Service, Tracking Service, Admin Service, API Gateway, Eureka Server, and Config Server.

**Q39. Why is Auth Service separate?**  
Because authentication and user management are independent from delivery and tracking. Separating it makes security easier to manage and avoids mixing login logic with business modules.

**Q40. Why is Tracking Service separate from Delivery Service?**  
Delivery Service manages shipment booking and status. Tracking Service manages event history, documents, and proof. Keeping them separate allows asynchronous updates and a clearer responsibility boundary.

**Q41. How do Delivery Service and Tracking Service communicate?**  
They communicate asynchronously through RabbitMQ. Delivery Service publishes status events, and Tracking Service consumes them from `tracking_queue`.

**Q42. Why did you use RabbitMQ instead of direct REST call for tracking update?**  
RabbitMQ decouples the services. Delivery creation does not need to wait for Tracking Service to respond. Even if tracking is slow or temporarily down, the event-based design is more flexible and scalable.

**Q43. How does Admin Service communicate with Delivery Service?**  
Admin Service uses OpenFeign with `@FeignClient(name = "delivery-service")`. The actual service instance is resolved through Eureka.

**Q44. What happens if a service is down?**  
The API Gateway has circuit breaker configuration and fallback support. Instead of failing with an unclear error, it can return a controlled service-unavailable response.

**Q45. What is distributed tracing in your project?**  
Micrometer/Zipkin tracing is used to track requests across services. Delivery Service also forwards trace headers in RabbitMQ messages, and Tracking Listener reads them for visibility.

---

## 7. Database and Data Flow

**Q46. Which database did you use?**  
PostgreSQL is used for persistent data storage across services like users, deliveries, tracking events, documents, and delivery proofs.

**Q47. Why did you use JPA repositories?**  
Spring Data JPA reduces boilerplate code. Instead of manually writing common SQL operations, repository interfaces provide CRUD methods and custom finder methods like `findByEmail()` and `findByCustomerEmail()`.

**Q48. What are the important entities?**  
Important entities include `User`, `Delivery`, `Address`, `DeliveryStatus`, `TrackingEvent`, `Document`, and `DeliveryProof`.

**Q49. What status lifecycle did you implement?**  
The normal lifecycle is `BOOKED -> PICKED_UP -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED`. There is also `EXCEPTION`, which can be raised from active states.

**Q50. How do you prevent invalid status changes?**  
`DeliveryService.java` has `isValidTransition()` which checks whether the current status can move to the requested next status. Invalid transitions throw an error.

---

## 8. Scenario-Based Questions

**Q51. Scenario: User tries to open admin dashboard. What happens?**  
Frontend does not show the dashboard option for normal users. Even if the user manually calls the API, backend authorization rejects the request because admin endpoints require role `ADMIN`.

**Q52. Scenario: User creates a delivery but tracking event is not visible immediately. What could be the reason?**  
Since tracking update uses RabbitMQ asynchronously, possible reasons include RabbitMQ not running, Tracking Service not consuming messages, queue misconfiguration, or a delay in event processing.

**Q53. Scenario: JWT token is expired. What happens?**  
The gateway fails token validation and returns `401 Unauthorized`. The user should login again to get a fresh token.

**Q54. Scenario: Admin tries to directly change status from `BOOKED` to `DELIVERED`. What happens?**  
Delivery Service rejects it because the lifecycle only allows `BOOKED` to move to `PICKED_UP`. Direct jump to `DELIVERED` is an invalid transition.

**Q55. Scenario: Delivery Service is working but Admin Dashboard fails. What could be checked?**  
Check Admin Service logs, Feign client configuration, Eureka registration, gateway route, and whether Delivery Service endpoint `/deliveries` is accessible with proper authorization headers.

**Q56. Scenario: Login succeeds but protected APIs fail. What would you debug?**  
I would check whether the frontend stored the token correctly, whether `api.js` is sending `Authorization: Bearer <token>`, whether gateway JWT validation is passing, and whether role headers are reaching downstream services.

**Q57. Scenario: Shipping label PDF is blank. What can be the reason?**  
The label DOM element may not be rendered, the `shipping-label` id may be missing, or `html2canvas` may fail to capture the element before PDF generation.

**Q58. Scenario: User says "my delivery list is showing other users' deliveries." What is the risk area?**  
The risk area is backend filtering. `getMyDeliveries()` must use the authenticated email from `X-User-Email`, not an email supplied by the frontend.

**Q59. Scenario: RabbitMQ message format changes. What breaks?**  
`TrackingListener.java` expects `trackingNumber|status`. If the format changes, the listener parsing logic must also be updated, otherwise tracking events may fail.

**Q60. Scenario: Frontend dashboard chart has no data. What would you check?**  
I would check the admin API response, `getRevenueTrend()` in `api.js`, Admin Service revenue endpoint, Delivery Service data, and whether `createdAt` and `price` fields are present.

---

## 9. Contribution and Ownership Questions

**Q61. What percentage of the project was done by you?**  
The safest interview answer is: "I worked on the major implementation and integration parts of the project, especially the frontend flow, API integration, delivery booking, tracking flow, admin dashboard understanding, and microservice connection. Frameworks, libraries, and standard setup contributed the base structure, but the project-specific logic and integration were mainly done by me."

**Q62. If they insist on a number, what should you say?**  
"Approximately 70-80% of the project-specific implementation and integration was done by me. The remaining part includes framework boilerplate, library behavior, generated setup, and standard configuration support."

**Q63. Which parts can you confidently say you implemented or understood deeply?**  
I can confidently explain frontend routing, login state, token storage, API connection, delivery booking form, tracking timeline, shipping label generation, admin dashboard flow, JWT security flow, RabbitMQ tracking update, and service-to-service communication.

**Q64. What was the most challenging part?**  
The most challenging part was connecting multiple services in one flow: frontend to gateway, gateway to delivery service, delivery service to RabbitMQ, and tracking service consuming that event to update tracking history.

**Q65. What did you learn from this project?**  
I learned how a real backend system is divided into services, how JWT-based security works through a gateway, how asynchronous communication works with RabbitMQ, and how a React frontend connects cleanly with a microservices backend.

**Q66. What would you improve if you had more time?**  
I would add real payment integration, real email/SMS service, stronger test coverage, refresh token support, better admin user management, deployment pipeline, and role-based audit logs.

---

## 10. Important Code Location Questions

**Q67. Where is frontend routing/navigation handled?**  
In `frontend/src/App.jsx`, using React state instead of React Router. The `page` state decides which page component is rendered.

**Q68. Where is login API called?**  
The API method is in `frontend/src/api.js` as `login()`, and it is used from `frontend/src/pages/LoginPage.jsx`.

**Q69. Where is delivery creation handled in frontend?**  
In `frontend/src/pages/DeliveriesPage.jsx`, especially the `handleSubmit()` function. It converts form fields into the backend DTO structure and calls `createDelivery()`.

**Q70. Where is delivery creation handled in backend?**  
In `delivery-service/src/main/java/com/lpu/delivery_service/controller/DeliveryController.java` and `DeliveryService.java`.

**Q71. Where is JWT validation done in gateway?**  
In `api-gateway/src/main/java/com/lpu/api_gateway/security/GatewayJwtFilter.java`.

**Q72. Where is password encoding done?**  
In `auth-service/src/main/java/com/lpu/auth_service/service/AuthService.java` during signup.

**Q73. Where is RabbitMQ message published?**  
In `delivery-service/src/main/java/com/lpu/delivery_service/service/DeliveryService.java`, after delivery creation and status update.

**Q74. Where is RabbitMQ message consumed?**  
In `tracking-service/src/main/java/com/lpu/tracking/service/listener/TrackingListener.java`.

**Q75. Where is admin dashboard logic written?**  
In `admin-service/src/main/java/com/lpu/admin/service/service/AdminService.java`, and endpoints are exposed from `AdminController.java`.

---

## 11. Short Technical Answers

**Q76. Why React?**  
React makes the UI component-based, reusable, and state-driven, which fits pages like login, deliveries, tracking, and admin dashboard.

**Q77. Why Vite?**  
Vite gives a fast development server, hot reload, and optimized production build.

**Q78. Why Spring Boot?**  
Spring Boot simplifies REST APIs, dependency injection, security, database integration, and microservice setup.

**Q79. Why JWT?**  
JWT supports stateless authentication. The token carries user identity and role, so protected services can authorize requests efficiently.

**Q80. Why PostgreSQL?**  
PostgreSQL is reliable for structured relational data like users, deliveries, addresses, and tracking records.

**Q81. Why Redis?**  
Redis is fast in-memory storage. Here it stores issued tokens with expiry.

**Q82. Why RabbitMQ?**  
RabbitMQ enables asynchronous communication between Delivery Service and Tracking Service.

**Q83. Why Feign Client?**  
Feign makes service-to-service REST calls easier using interfaces, and it works well with Eureka service discovery.

**Q84. Why Recharts?**  
Recharts provides ready React chart components for dashboard visualizations.

**Q85. Why QR code in shipping label?**  
QR code makes the tracking number quickly scannable and improves the practical courier label experience.

---

## 12. Strong Closing Answers

**Q86. How is your project different from a basic CRUD project?**  
It is not only CRUD. It includes authentication, role-based access, microservices, API Gateway routing, service discovery, async RabbitMQ messaging, tracking timeline, PDF label generation, charts, reports, and distributed tracing.

**Q87. What is the end-to-end value of your project?**  
It covers the real courier lifecycle from user registration to shipment booking, tracking, admin monitoring, status update, and report generation.

**Q88. What is one line that summarizes your technical contribution?**  
I built and integrated a React frontend with a Spring Boot microservices backend using JWT security, API Gateway routing, RabbitMQ-based tracking updates, and admin analytics.

**Q89. If interviewer asks, "Can you explain any one feature deeply?", which feature should you choose?**  
Choose delivery booking with tracking update. It shows frontend form handling, API Gateway, JWT headers, Delivery Service business logic, database save, RabbitMQ event publishing, and Tracking Service consumption.

**Q90. Final impressive answer: explain delivery booking in 30 seconds.**  
When a user books a delivery, the React form sends a structured payload through `api.js` to the API Gateway. The gateway validates JWT and forwards the user's email to Delivery Service. Delivery Service calculates price, generates a UUID tracking number, stores the delivery, and publishes a RabbitMQ event. Tracking Service consumes that event and creates the first tracking timeline entry. So one user action triggers database persistence and asynchronous tracking creation across services.

