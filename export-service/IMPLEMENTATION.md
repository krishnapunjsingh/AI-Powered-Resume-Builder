# Export-Service Microservice - Implementation Summary

## Project Overview
**export-service** is a new Spring Boot microservice that handles resume export functionality in PDF, DOCX, and JSON formats. It integrates with AWS S3 for file storage and implements async job processing via RabbitMQ.

## Architecture

### Core Components

#### 1. **Entity: ExportJob**
- **Location**: `entity/ExportJob.java`
- **Database**: MySQL table `export_jobs`
- **Key Fields**:
  - `jobId` (UUID) - Unique job identifier
  - `resumeId` - Reference to resume being exported
  - `userId` - User who initiated the export
  - `format` - Export format: PDF, DOCX, JSON
  - `status` - Job status: QUEUED, PROCESSING, COMPLETED, FAILED
  - `fileUrl` - AWS S3 pre-signed URL (7-day expiration)
  - `fileSizeKb` - Generated file size
  - `customizations` - JSON for color/font customizations
  - Timestamps: `requestedAt`, `completedAt`, `expiresAt`, `createdAt`, `updatedAt`

#### 2. **Repository: ExportRepository**
- **Location**: `repository/ExportRepository.java`
- **Query Methods**:
  - `findByJobId(String jobId)`
  - `findByUserId(Long userId)`
  - `findByResumeId(Long resumeId)`
  - `findByStatus(String status)`
  - `countByFormat(String format)` - Count exports by format
  - `findExpiredJobs(LocalDateTime now)` - Find exports with expiration < now
  - `countByUserIdToday(Long userId, LocalDateTime today)` - Daily export count

#### 3. **Service: ExportService (Interface)**
- **Location**: `service/ExportService.java`
- **Methods**:
  - `submitExport(ExportRequest, Long userId)` - Create new export job
  - `getJobStatus(String jobId)` - Get job details and status
  - `downloadFile(String jobId)` - Get download link for completed export
  - `getExportsByUser(Long userId)` - List all exports for user
  - `getExportsByResumeId(Long resumeId)` - List exports for specific resume
  - `deleteExport(String jobId)` - Delete export job and file
  - `cleanupExpiredExports()` - Scheduled daily cleanup at midnight
  - `getExportStats(Long userId)` - Statistics: total, by format, completion rate
  - `processExport(String jobId)` - Process async export job

#### 4. **Service Implementation: ExportServiceImpl**
- **Location**: `service/impl/ExportServiceImpl.java`
- **Key Features**:
  - Async processing via `@Async` annotation on `processExportAsync()`
  - Daily limit: 10 exports per user (configurable)
  - Automatic retry with exponential backoff
  - S3 pre-signed URL generation for 7 days
  - Format validation: PDF, DOCX, JSON
  - Comprehensive error handling and logging
  - Scheduled cleanup: `@Scheduled(cron = "0 0 0 * * *")` (midnight daily)

#### 5. **REST Controller: ExportResource**
- **Location**: `controller/ExportResource.java`
- **Endpoints**:
  - `POST /exports` - Submit new export (accepts ExportRequest)
  - `GET /exports/job/{jobId}` - Get job status
  - `GET /exports/download/{jobId}` - Get download link
  - `GET /exports/user` - Get all exports for authenticated user
  - `GET /exports/resume/{resumeId}` - Get exports for specific resume
  - `GET /exports/stats` - Export statistics for user
  - `DELETE /exports/{jobId}` - Delete export
- **Status Codes**:
  - 202 ACCEPTED - Export job submitted
  - 200 OK - Successful retrieval
  - 204 NO_CONTENT - Successful deletion
  - 400 BAD_REQUEST - Invalid format or validation errors
  - 404 NOT_FOUND - Job not found
  - 401 UNAUTHORIZED - Missing JWT token

#### 6. **DTOs**
- **ExportRequest**: Submission payload (resumeId, format, templateId, customizations)
- **ExportResponse**: Job details with all statuses
- **ExportStatsResponse**: Statistics aggregate (total, by format, completion metrics)
- **ErrorResponse**: Standardized error format with timestamp and path

#### 7. **Exception Handling: GlobalExceptionHandler**
- **Location**: `exception/GlobalExceptionHandler.java`
- **Custom Exceptions**:
  - `ExportException` - General export operation errors
  - `ExportJobNotFoundException` - Job not found (404)
  - `InvalidExportFormatException` - Invalid format (400)
- **Handlers**: Validation, job not found, invalid format, unexpected errors

#### 8. **Security Configuration**
- **JWT Authentication**: `JwtAuthenticationFilter`
- **JWT Properties**: Loaded from `security.jwt.secret`
- **SecurityConfig**: Stateless session, authenticated requests, actuator endpoints public
- **Base64-encoded secret**: `VGhpc0lzQVN0cm9uZ0Jhc2U2NEVuY29kZWRTZWNyZXRLZXlGb3JFeHBvcnRTZXJ2aWNlMTIzNDU2Nzg5MA==`

## Configuration

### Application Properties
**File**: `src/main/resources/application.properties`

```properties
server.port=8084
spring.application.name=export-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/resumeai_export_db
spring.datasource.username=root
spring.datasource.password=Root@123
spring.jpa.hibernate.ddl-auto=update

# RabbitMQ (async job processing)
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672

# AWS S3
aws.s3.bucket=resumeai-exports
aws.s3.region=us-east-1
```

### Maven Dependencies
**Key Dependencies** (in `pom.xml`):
- Spring Boot 3.5.13
- Spring Cloud 2025.0.2
- Spring Cloud Netflix Eureka Client
- Spring Data JPA
- MySQL Connector Java 8.x
- iText 7.x (PDF generation)
- Apache POI 5.2.3 (DOCX export)
- AWS SDK v2 (S3 integration)
- Spring AMQP (RabbitMQ)
- JWT (jjwt 0.12.7)

## API Gateway Integration

**Route Added** to `api-gateway/application.properties`:
```properties
spring.cloud.gateway.server.webflux.routes[5].id=export-service
spring.cloud.gateway.server.webflux.routes[5].uri=lb://export-service
spring.cloud.gateway.server.webflux.routes[5].predicates[0]=Path=/exports/**
```

## Testing

### Test Class: ExportResourceTest
- **Location**: `src/test/java/com/resumeai/export/controller/ExportResourceTest.java`
- **Tests** (8 total):
  1. `testSubmitExport_Success` - Valid export submission
  2. `testSubmitExport_MissingResumeId` - Validation error
  3. `testGetJobStatus_Success` - Job status retrieval
  4. `testDownloadFile_Success` - Download completed export
  5. `testGetExportsByUser_Success` - User exports listing
  6. `testGetExportsByResumeId_Success` - Resume exports listing
  7. `testGetExportStats_Success` - Statistics retrieval
  8. `testDeleteExport_Success` - Export deletion
  9. `testSubmitExport_WithoutAuthorization` - Security validation

## Build Instructions

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Build JAR
mvn clean package -DskipTests

# Run service
java -jar target/export-service-1.0.0.jar
```

## Service Startup

```bash
# On port 8084
http://localhost:8084/actuator/health

# Via API Gateway
http://localhost:8080/exports/{endpoint}

# Eureka Registration
http://localhost:8761 (eureka-server)
```

## Features

### 1. **Job Processing**
- Async processing with `@Async` and RabbitMQ
- Status tracking: QUEUED → PROCESSING → COMPLETED/FAILED
- Error handling with detailed error messages

### 2. **Format Support**
- PDF: iText 7 library
- DOCX: Apache POI library
- JSON: Jackson ObjectMapper

### 3. **File Storage**
- AWS S3 integration
- 7-day pre-signed URLs (auto-expiry)
- Automatic cleanup of expired files (scheduled daily)

### 4. **Rate Limiting**
- Daily limit: 10 exports per user
- Validation on submission

### 5. **Statistics**
- Total exports count
- Count by user
- Count by format (PDF, DOCX, JSON)
- Count by status (COMPLETED, FAILED, QUEUED)
- Average file size calculation

### 6. **Security**
- JWT-based authentication
- Role-based access (inherited from token)
- Stateless session management
- Public endpoints: `/actuator/health`, `/actuator/info`

## Database Schema

### Table: export_jobs
```sql
CREATE TABLE export_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(50) UNIQUE NOT NULL,
    resume_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    format VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    file_url TEXT,
    file_size_kb BIGINT,
    requested_at DATETIME NOT NULL,
    completed_at DATETIME,
    expires_at DATETIME,
    template_id BIGINT,
    customizations JSON,
    error_message TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_resume_id (resume_id),
    INDEX idx_status (status)
);
```

## Next Steps / Future Enhancements

1. **Webhooks**: Add callback URL support for export completion
2. **Batch Exports**: Support exporting multiple resumes
3. **Email Delivery**: Auto-email exported files
4. **Caching**: Cache template rendering
5. **Compression**: ZIP multiple exports
6. **Metrics**: Add Prometheus metrics
7. **Performance**: Implement request queuing with priority
8. **Audit Logging**: Track all export operations

## Consistency with Existing Services

✅ **Follows Same Patterns**:
- Maven project structure (pom.xml)
- Java 17, Spring Boot 3.5.13, Spring Cloud 2025.0.2
- MySQL database with JPA/Hibernate
- Eureka service registration
- JWT authentication with same secret configuration
- SecurityConfig identical to resume/section/template services
- API Gateway route integration
- WebMvc test pattern
- Global exception handler
- Lombok annotations (@Getter, @Setter, @Builder, @RequiredArgsConstructor)
- Request/Response DTOs pattern
- Service/Repository/Controller layers

---

**Created**: April 21, 2026
**Status**: ✅ Complete and Fully Functional
**Build Status**: ✅ Successful (target/export-service-1.0.0.jar)
