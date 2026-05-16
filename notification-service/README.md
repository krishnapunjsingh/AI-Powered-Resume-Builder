# Notification-Service Microservice

A fully-functional notification microservice for the ResumeAI platform, built following the same architecture, coding patterns, and structure as existing services (auth, resume, section).

## Quick Start

### Build the Service
```bash
cd notification-service
mvn clean package
```

### Run the Service
```bash
# Using Maven
mvn spring-boot:run

# Or with JAR
java -jar target/notification-service-1.0.0.jar --server.port=8085
```

The service will start on port 8085 and connect to Eureka at http://localhost:8761/eureka

## Service Structure

```
notification-service/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── FILE_STRUCTURE.md                # Detailed file structure
├── IMPLEMENTATION.md                # Implementation guide
└── src/
    ├── main/java/com/resumeai/notification/
    │   ├── NotificationServiceApplication.java    # Spring Boot entry point
    │   ├── entity/Notification.java                # JPA entity
    │   ├── repository/NotificationRepository.java  # Data access layer
    │   ├── service/                                # Business logic layer
    │   ├── controller/NotificationResource.java    # REST endpoints
    │   ├── dto/                                    # Request/Response DTOs
    │   ├── exception/                              # Exception handling
    │   ├── config/                                 # Configuration
    │   └── security/                               # JWT security
    ├── main/resources/application.properties
    └── test/java/com/resumeai/notification/controller/NotificationResourceTest.java
```

## Key Features

✅ **Single & Bulk Notifications** - Send one or many notifications  
✅ **Async Processing** - Bulk operations run asynchronously  
✅ **Read State Tracking** - Track read/unread status with timestamps  
✅ **Scheduled Operations** - Automatic cleanup (24h) and retry (1h)  
✅ **JWT Authentication** - Secure endpoints with JWT tokens  
✅ **Exception Handling** - Comprehensive error handling  
✅ **Database Persistence** - MySQL with auto schema creation  
✅ **Health Check** - Service health monitoring  
✅ **CORS Enabled** - Cross-origin request support  
✅ **Eureka Discovery** - Service discovery integration  

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---|
| POST | `/notifications/send` | Send single notification | Yes |
| POST | `/notifications/send-bulk` | Send multiple notifications | Yes |
| PUT | `/notifications/{id}/mark-read` | Mark notification as read | Yes |
| PUT | `/notifications/recipient/{id}/mark-all-read` | Mark all as read | Yes |
| GET | `/notifications/recipient/{id}` | Get all notifications | Yes |
| GET | `/notifications/recipient/{id}/unread-count` | Get unread count | Yes |
| GET | `/notifications/{id}` | Get specific notification | Yes |
| DELETE | `/notifications/{id}` | Delete notification | Yes |
| DELETE | `/notifications/recipient/{id}/delete-all` | Delete all for recipient | Yes |
| GET | `/notifications/admin/all` | Get all notifications | Yes |
| GET | `/notifications/health` | Health check | No |

## Example Requests

### Send a Notification
```bash
curl -X POST http://localhost:8085/notifications/send \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientId": 1,
    "type": "ATS_COMPLETE",
    "title": "Resume Processed",
    "message": "Your resume has been analyzed successfully",
    "channel": "APP",
    "relatedId": 100,
    "relatedType": "RESUME"
  }'
```

### Get User Notifications
```bash
curl -X GET http://localhost:8085/notifications/recipient/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Mark Notification as Read
```bash
curl -X PUT http://localhost:8085/notifications/1/mark-read \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Unread Count
```bash
curl -X GET http://localhost:8085/notifications/recipient/1/unread-count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Notification Types

The service supports the following notification types:
- `ATS_COMPLETE` - Resume ATS analysis complete
- `EXPORT_READY` - Export file ready for download
- `AI_JOB_MATCH` - New job match found
- `PLAN_CHANGE` - Subscription plan changed
- `QUOTA_WARNING` - Usage quota warning

## Notification Channels

- `APP` - In-app notification
- `EMAIL` - Email notification

## Database Configuration

### Environment Variables
```bash
MYSQL_HOST=localhost (default)
MYSQL_PORT=3306 (default)
NOTIFICATION_DB_NAME=resumeai_notification_db (default)
MYSQL_USER=root (default)
MYSQL_PASSWORD=Root@123 (default)
JWT_SECRET=VGhpc0lzQVN0cm9uZ0Jhc2U2NEVuY29kZWRTZWNyZXRLZXlGb3JSZXN1bWVBSTEyMzQ1Njc4OTA= (default)
```

### Auto Schema Creation
The service automatically creates/updates the database schema using Hibernate DDL auto update feature.

## Security

- **JWT Authentication**: All endpoints (except health) require valid JWT tokens
- **Token Validation**: Tokens are validated on every request
- **Stateless Sessions**: No session storage - fully stateless
- **CORS Support**: Cross-origin requests are allowed with proper headers

## Testing

Run the included test suite:
```bash
mvn test
```

The test file includes:
- Health check endpoint test
- Send notification test
- Get notifications test
- Mark as read test
- Delete notification test

## Integration

### Eureka Discovery
The service automatically registers with Eureka. Configure the Eureka server URL in `application.properties`:
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

### Inter-service Communication
Other services can call this service using the service name:
```
http://notification-service/notifications/...
```

## Architecture Patterns

This service follows the same architecture as existing services:

1. **Controller** → Handles HTTP requests
2. **Service** → Contains business logic
3. **Repository** → Manages database access
4. **Entity** → Represents database table
5. **DTO** → Transfer data between layers
6. **Exception Handling** → Centralized with GlobalExceptionHandler
7. **Security** → JWT filter for authentication
8. **Configuration** → Externalized configuration

## Performance Features

- **Connection Pooling**: HikariCP with optimized pool settings
- **Database Indexing**: Optimized indexes on frequently queried columns
- **Async Processing**: Bulk operations run asynchronously
- **Scheduled Cleanup**: Automatic old notification purging
- **Automatic Retry**: Failed notifications are retried hourly

## Dependencies

Key dependencies included:
- Spring Boot 3.5.13
- Spring Data JPA
- Spring Security
- MySQL Connector
- JJWT (JSON Web Token)
- Spring Cloud Eureka
- Lombok
- Spring Boot Actuator

## Development Notes

- **Java Version**: 17
- **Maven Version**: 3.8+
- **IDE**: IntelliJ IDEA / VS Code recommended
- **HTTP Client**: Postman, curl, or REST Client extension

## Troubleshooting

### Port Already in Use
```bash
# Use a different port
java -jar target/notification-service-1.0.0.jar --server.port=8086
```

### Database Connection Issues
- Verify MySQL is running
- Check connection string in application.properties
- Ensure database credentials are correct

### JWT Token Issues
- Verify token format: `Bearer <token>`
- Check token expiration (24 hours by default)
- Ensure JWT secret matches across services

### Eureka Registration Issues
- Verify Eureka server is running on port 8761
- Check network connectivity
- Review logs for detailed errors

## Logging

View logs from the running service:
```bash
# Using Maven
mvn spring-boot:run | grep -i notification

# Or in the JAR output
tail -f <app.log>
```

Configure logging level in `application.properties`:
```properties
logging.level.com.resumeai.notification=DEBUG
logging.level.org.springframework=INFO
```

## Future Enhancements

- WebSocket support for real-time notifications
- Email template engine integration
- SMS notification support
- Advanced filtering and pagination
- Notification delivery receipts
- Priority levels and categories
- Notification scheduling

## Documentation

- **FILE_STRUCTURE.md** - Detailed project structure explanation
- **IMPLEMENTATION.md** - In-depth implementation guide with architecture diagrams

## Support

For questions or issues:
1. Check the documentation files (FILE_STRUCTURE.md, IMPLEMENTATION.md)
2. Review the test file for usage examples
3. Check application logs for error details
4. Verify database and service configurations

---

**Status**: Production Ready ✅  
**Version**: 1.0.0  
**Last Updated**: 2024-04-21
