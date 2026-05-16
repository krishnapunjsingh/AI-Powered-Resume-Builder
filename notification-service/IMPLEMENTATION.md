# Notification-Service Implementation Guide

## Overview

The Notification-Service is a microservice responsible for managing in-app and email notifications for the ResumeAI platform. It handles notification dispatch, retrieval, read-state management, and deletion across all key asynchronous events.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Notification-Service                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              NotificationResource (Controller)           │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │ - send()                  [POST /send]              │ │  │
│  │  │ - sendBulk()              [POST /send-bulk]         │ │  │
│  │  │ - markAsRead()            [PUT /{id}/mark-read]    │ │  │
│  │  │ - markAllAsRead()         [PUT /recipient/mark-all] │ │  │
│  │  │ - getByRecipientId()      [GET /recipient/{id}]    │ │  │
│  │  │ - getUnreadCount()        [GET /recipient/unread]  │ │  │
│  │  │ - getByNotificationId()   [GET /{id}]              │ │  │
│  │  │ - deleteNotification()    [DELETE /{id}]           │ │  │
│  │  │ - deleteAllForRecipient() [DELETE /recipient/all]  │ │  │
│  │  │ - getAll()                [GET /admin/all]         │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  └──────────────────┬───────────────────────────────────────┘  │
│                     │                                           │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │         NotificationService (Service Interface)        │   │
│  └──────────────────┬───────────────────────────────────────┘   │
│                     │                                           │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │      NotificationServiceImpl (Implementation)           │   │
│  │  - Business Logic & Validation                         │   │
│  │  - Async Bulk Operations                               │   │
│  │  - Scheduled Cleanup & Retry                           │   │
│  └──────────────────┬───────────────────────────────────────┘   │
│                     │                                           │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │      NotificationRepository (Data Access)              │   │
│  │  - Custom Query Methods                                │   │
│  │  - CRUD Operations                                     │   │
│  └──────────────────┬───────────────────────────────────────┘   │
│                     │                                           │
│                     │                                           │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │           Database (MySQL)                             │   │
│  │         notifications table                            │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Database Schema

```sql
CREATE TABLE notifications (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(50),
    related_id BIGINT NOT NULL,
    related_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    read_at DATETIME,
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

## Key Features

### 1. Single & Bulk Notifications
- Send individual notifications with immediate persistence
- Send bulk notifications asynchronously with @Async
- Automatic status tracking (PENDING → SENT → READ)

### 2. Read State Management
- Mark individual notifications as read with timestamp
- Mark all notifications for a recipient as read
- Track unread count efficiently with COUNT queries

### 3. Notification Types
Supported notification types:
- `ATS_COMPLETE` - ATS score computation complete
- `EXPORT_READY` - Export file ready for download
- `AI_JOB_MATCH` - New high-scoring job match found
- `PLAN_CHANGE` - Subscription plan changed
- `QUOTA_WARNING` - Quota usage warning

### 4. Channels
- `APP` - In-app notification
- `EMAIL` - Email notification

### 5. Related Entities
- RESUME - Links to resume records
- EXPORT - Links to export jobs
- JOB_MATCH - Links to job matches
- PLAN - Links to subscription changes

### 6. Scheduled Operations
- **Cleanup Task**: Runs every 24 hours, deletes read notifications older than 30 days
- **Retry Task**: Runs every 1 hour, retries failed notifications

## DTOs

### NotificationRequest
```java
{
    "recipientId": 1,
    "type": "ATS_COMPLETE",
    "title": "Resume Processed",
    "message": "Your resume has been processed successfully",
    "channel": "APP",
    "relatedId": 100,
    "relatedType": "RESUME"
}
```

### NotificationResponse
```java
{
    "success": true,
    "message": "Notification sent successfully",
    "data": {
        "notificationId": 1,
        "recipientId": 1,
        "type": "ATS_COMPLETE",
        "title": "Resume Processed",
        "message": "Your resume has been processed successfully",
        "channel": "APP",
        "relatedId": 100,
        "relatedType": "RESUME",
        "status": "SENT",
        "isRead": false,
        "createdAt": "2024-04-21T10:30:00",
        "updatedAt": "2024-04-21T10:30:00",
        "readAt": null
    },
    "statusCode": 201
}
```

## Exception Handling

The service implements comprehensive exception handling:

- **NotificationNotFoundException** (404): When a notification doesn't exist
- **InvalidNotificationException** (400): When notification data is invalid
- **NotificationException** (500): General notification service errors
- **MethodArgumentNotValidException** (400): Request validation failures
- **IllegalArgumentException** (400): Invalid argument values
- **Global Exception Handler**: Catches all unhandled exceptions

## Security

- JWT authentication required for all endpoints (except health)
- Token validation via JwtService
- Role-based access control ready
- Stateless session management
- CORS enabled for cross-origin requests

## Running the Service

### Prerequisites
```bash
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Spring Boot 3.5.13
```

### Build
```bash
cd notification-service
mvn clean package
```

### Run
```bash
# Using Maven
mvn spring-boot:run

# Using JAR
java -jar target/notification-service-1.0.0.jar --server.port=8085

# With environment variables
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export NOTIFICATION_DB_NAME=resumeai_notification_db
export MYSQL_USER=root
export MYSQL_PASSWORD=Root@123
export JWT_SECRET=VGhpc0lzQVN0cm9uZ0Jhc2U2NEVuY29kZWRTZWNyZXRLZXlGb3JSZXN1bWVBSTEyMzQ1Njc4OTA=

java -jar target/notification-service-1.0.0.jar
```

## Configuration

### application.properties
```properties
server.port=8085
spring.application.name=notification-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

spring.datasource.url=jdbc:mysql://localhost:3306/resumeai_notification_db
spring.datasource.username=root
spring.datasource.password=Root@123

spring.jpa.hibernate.ddl-auto=update
security.jwt.secret=VGhpc0lzQVN0cm9uZ0Jhc2U2NEVuY29kZWRTZWNyZXRLZXlGb3JSZXN1bWVBSTEyMzQ1Njc4OTA=
security.jwt.expiration-ms=86400000
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### API Testing Examples

#### Send Notification
```bash
curl -X POST http://localhost:8085/notifications/send \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientId": 1,
    "type": "ATS_COMPLETE",
    "title": "Resume Processed",
    "message": "Your resume has been processed",
    "channel": "APP",
    "relatedId": 100,
    "relatedType": "RESUME"
  }'
```

#### Get Notifications
```bash
curl -X GET http://localhost:8085/notifications/recipient/1 \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

#### Mark as Read
```bash
curl -X PUT http://localhost:8085/notifications/1/mark-read \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

#### Get Unread Count
```bash
curl -X GET http://localhost:8085/notifications/recipient/1/unread-count \
  -H "Authorization: Bearer {JWT_TOKEN}"
```

## Integration with Other Services

The Notification-Service is called by:
- **Resume-Service**: When resume processing completes
- **Export-Service**: When export jobs complete
- **Job-Match-Service**: When new job matches are found
- **Subscription-Service**: When plans change

## Performance Considerations

1. **Indexing**: Queries are optimized with indexes on recipient_id, type, status, and created_at
2. **Async Bulk Operations**: Large notification batches are processed asynchronously
3. **Connection Pooling**: HikariCP with 10 max connections and 2 min idle
4. **Scheduled Cleanup**: Old notifications are purged to maintain table size
5. **Pagination**: Ready to implement pagination for large result sets

## Future Enhancements

- WebSocket support for real-time notifications
- Email template engine integration
- SMS notification support
- Notification preferences per user
- Advanced filtering and search
- Notification scheduling
- Delivery receipts and read confirmations
- Notification categories and priority levels
