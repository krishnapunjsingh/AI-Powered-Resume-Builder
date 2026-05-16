# Notification-Service

notification-service/
├── pom.xml                                           # Maven configuration with all dependencies
├── FILE_STRUCTURE.md                                 # Project structure documentation (this file)
├── IMPLEMENTATION.md                                 # Comprehensive implementation documentation
│
├── src/main/
│   ├── java/com/resumeai/notification/
│   │   ├── NotificationServiceApplication.java       # Spring Boot Application entry point
│   │   │
│   │   ├── entity/
│   │   │   └── Notification.java                     # JPA Entity for notifications table
│   │   │
│   │   ├── repository/
│   │   │   └── NotificationRepository.java           # Spring Data JPA repository interface
│   │   │
│   │   ├── service/
│   │   │   ├── NotificationService.java              # Service interface (public API)
│   │   │   └── impl/
│   │   │       └── NotificationServiceImpl.java       # Service implementation with business logic
│   │   │
│   │   ├── controller/
│   │   │   └── NotificationResource.java             # REST controller with all endpoints
│   │   │
│   │   ├── dto/
│   │   │   ├── NotificationRequest.java              # Request DTO for sending notifications
│   │   │   ├── NotificationResponse.java             # Response DTO for notification details
│   │   │   └── ErrorResponse.java                    # Standardized error response
│   │   │
│   │   ├── exception/
│   │   │   ├── NotificationException.java            # General exception
│   │   │   ├── NotificationNotFoundException.java    # Notification not found exception
│   │   │   ├── InvalidNotificationException.java     # Invalid notification exception
│   │   │   └── GlobalExceptionHandler.java           # Centralized exception handling
│   │   │
│   │   ├── config/
│   │   │   ├── JwtProperties.java                    # JWT configuration properties
│   │   │   └── SecurityConfig.java                   # Spring Security configuration
│   │   │
│   │   └── security/
│   │       ├── JwtService.java                       # JWT token generation and validation
│   │       └── JwtAuthenticationFilter.java          # JWT authentication filter
│   │
│   └── resources/
│       └── application.properties                    # Application configuration
│
└── src/test/
    └── java/com/resumeai/notification/
        └── controller/
            └── NotificationResourceTest.java         # WebMvc integration tests

═══════════════════════════════════════════════════════════════════════════════

KEY FILES SUMMARY:

1. NotificationServiceApplication.java
   - Entry point for the microservice
   - Enables Eureka discovery client
   - Enables async processing with @EnableAsync
   - Enables scheduled tasks with @EnableScheduling

2. Notification.java (Entity)
   - Maps to notifications table in MySQL
   - Contains all notification metadata
   - Auto timestamps with @PrePersist/@PreUpdate
   - Tracks read status and timestamps

3. NotificationRepository.java (Repository)
   - Extends JpaRepository<Notification, Long>
   - Custom queries for finding notifications by various criteria
   - Query methods for read status, types, and cleanup operations

4. NotificationService.java & NotificationServiceImpl.java (Service)
   - Core business logic for notification operations
   - Async processing for bulk notifications with @Async
   - Scheduled cleanup and retry operations with @Scheduled
   - Validation and error handling

5. NotificationResource.java (REST Controller)
   - @RestController mapped to /notifications
   - 9 endpoints for notification operations
   - JWT authentication required for all endpoints except health
   - Proper HTTP status codes (201, 202, 200, 204, 400, 404, 401, 500)

6. DTOs (Request/Response)
   - NotificationRequest: Submission payload with validation
   - NotificationResponse: Notification details with success/error handling
   - ErrorResponse: Standardized error details

7. Exception Classes
   - NotificationException: General notification errors
   - NotificationNotFoundException: 404 errors for missing notifications
   - InvalidNotificationException: 400 errors for invalid data
   - GlobalExceptionHandler: Centralized exception handling

8. SecurityConfig & JwtAuthenticationFilter
   - JWT token parsing and validation
   - SecurityFilterChain configuration
   - Stateless session management
   - CORS configuration

9. application.properties
   - Port 8085
   - MySQL database configuration
   - JWT secret key
   - Logging configuration

═══════════════════════════════════════════════════════════════════════════════

API ENDPOINTS:

POST /notifications/send
- Send a single notification
- Requires: NotificationRequest body
- Returns: 201 Created

POST /notifications/send-bulk
- Send multiple notifications
- Requires: List<NotificationRequest> body
- Returns: 202 Accepted

PUT /notifications/{notificationId}/mark-read
- Mark a notification as read
- Returns: 200 OK

PUT /notifications/recipient/{recipientId}/mark-all-read
- Mark all notifications as read for a recipient
- Returns: 200 OK

GET /notifications/recipient/{recipientId}
- Get all notifications for a recipient
- Returns: 200 OK with notification list

GET /notifications/recipient/{recipientId}/unread-count
- Get unread notification count for a recipient
- Returns: 200 OK with count

GET /notifications/{notificationId}
- Get a specific notification
- Returns: 200 OK

DELETE /notifications/{notificationId}
- Delete a specific notification
- Returns: 204 No Content

DELETE /notifications/recipient/{recipientId}/delete-all
- Delete all notifications for a recipient
- Returns: 204 No Content

GET /notifications/admin/all
- Get all notifications (admin endpoint)
- Returns: 200 OK with all notifications

GET /notifications/health
- Health check endpoint
- Returns: 200 OK

═══════════════════════════════════════════════════════════════════════════════
