export-service/
├── pom.xml                                           # Maven configuration with all dependencies
├── IMPLEMENTATION.md                                  # Comprehensive implementation documentation
│
├── src/main/
│   ├── java/com/resumeai/export/
│   │   ├── ExportServiceApplication.java              # Spring Boot Application entry point
│   │   │
│   │   ├── entity/
│   │   │   └── ExportJob.java                          # JPA Entity for export_jobs table
│   │   │
│   │   ├── repository/
│   │   │   └── ExportRepository.java                   # Spring Data JPA repository interface
│   │   │
│   │   ├── service/
│   │   │   ├── ExportService.java                      # Service interface (public API)
│   │   │   └── impl/
│   │   │       └── ExportServiceImpl.java               # Service implementation with business logic
│   │   │
│   │   ├── controller/
│   │   │   └── ExportResource.java                     # REST controller with all endpoints
│   │   │
│   │   ├── dto/
│   │   │   ├── ExportRequest.java                      # Request DTO for export submission
│   │   │   ├── ExportResponse.java                     # Response DTO for job details
│   │   │   ├── ExportStatsResponse.java                # Statistics response DTO
│   │   │   └── ErrorResponse.java                      # Standardized error response
│   │   │
│   │   ├── exception/
│   │   │   ├── ExportException.java                    # General exception
│   │   │   ├── ExportJobNotFoundException.java          # Job not found exception
│   │   │   ├── InvalidExportFormatException.java        # Invalid format exception
│   │   │   └── GlobalExceptionHandler.java              # Centralized exception handling
│   │   │
│   │   ├── config/
│   │   │   ├── JwtProperties.java                      # JWT configuration properties
│   │   │   └── SecurityConfig.java                     # Spring Security configuration
│   │   │
│   │   └── security/
│   │       └── JwtAuthenticationFilter.java             # JWT authentication filter
│   │
│   └── resources/
│       └── application.properties                      # Application configuration
│
└── src/test/
    └── java/com/resumeai/export/
        └── controller/
            └── ExportResourceTest.java                 # WebMvc integration tests

═══════════════════════════════════════════════════════════════════════════════

KEY FILES SUMMARY:

1. ExportServiceApplication.java
   - Entry point for the microservice
   - Enables Eureka discovery client
   - Enables configuration properties

2. ExportJob.java (Entity)
   - Maps to export_jobs table in MySQL
   - Contains all job metadata and status info
   - Auto timestamps with @PrePersist/@PreUpdate

3. ExportRepository.java (Repository)
   - Extends JpaRepository<ExportJob, Long>
   - Custom queries for finding jobs by various criteria
   - Count queries for statistics

4. ExportService.java & ExportServiceImpl.java (Service)
   - Core business logic for export operations
   - Async processing with @Async
   - Scheduled cleanup with @Scheduled
   - Validation and error handling

5. ExportResource.java (REST Controller)
   - @RestController mapped to /exports
   - 7 endpoints for export operations
   - JWT authentication required (except actuator endpoints)
   - Proper HTTP status codes (202, 200, 204, 400, 404, 401)

6. DTOs (Request/Response)
   - ExportRequest: Submission payload
   - ExportResponse: Job details
   - ExportStatsResponse: Statistics
   - ErrorResponse: Error details

7. Exception Classes
   - ExportException: General errors
   - ExportJobNotFoundException: 404 errors
   - InvalidExportFormatException: Format validation errors

8. SecurityConfig & JwtAuthenticationFilter
   - JWT token parsing and validation
   - SecurityFilterChain configuration
   - Stateless session management

9. application.properties
   - Port 8084
   - MySQL database configuration
   - JWT secret key
   - RabbitMQ configuration
   - AWS S3 configuration
   - Management endpoints

10. ExportResourceTest.java (Tests)
    - 9 WebMvc integration tests
    - Tests all endpoints with mocked service
    - Covers success cases and error scenarios

═══════════════════════════════════════════════════════════════════════════════

BUILD OUTPUT:

target/
├── export-service-1.0.0.jar                   # Packaged JAR file (runnable)
├── classes/
│   └── com/resumeai/export/...                # Compiled classes
└── test-classes/
    └── com/resumeai/export/controller/        # Compiled test classes

═══════════════════════════════════════════════════════════════════════════════
