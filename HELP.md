# Job Recruitment Platform - API Documentation

## Overview
This is a Spring Boot-based job recruitment platform server built with Java 21. The platform provides APIs for job posting, candidate management, recruiter operations, and authentication.

## Technology Stack
- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL 18.0
- **Cache**: Redis 7.4
- **Authentication**: JWT with OAuth2
- **File Storage**: Cloudinary
- **Email**: SMTP (Gmail)
- **Database Migration**: Flyway
- **Build Tool**: Maven

## Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- Spring Boot Starter Data Redis
- Spring Boot Starter OAuth2 Client & Resource Server
- Spring Boot Starter Validation
- Spring Boot Starter Mail
- PostgreSQL Driver
- Flyway Database Migration
- Lombok
- Google API Client
- Cloudinary HTTP Client
- Apache HTTP Client 5

## Base URL
- **Local**: `http://localhost:8080`
- **Production**: Configure via environment variables

## Authentication
- **Scheme**: Bearer JWT in `Authorization` header: `Authorization: Bearer <accessToken>`
- **Token Expiration**: 7 days (604800000 ms)
- **Refresh Token**: Available for token renewal
- **Public endpoints** are explicitly marked. Others require authentication and role-based access.

## Standard Response Envelope
All endpoints return a common envelope:

```json
{
  "code": 1000,
  "message": "optional message",
  "data": { /* payload varies per endpoint */ }
}
```

- **code**: business status code (1000 for success; see Errors section for failure codes)
- **message**: optional human-readable message
- **data**: the actual response payload

### Pagination Response Format
```json
{
  "content": [ /* items */ ],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true,
  "hasNext": false,
  "hasPrevious": false
}
```

---

## Public Endpoints

### Authentication APIs (`/api/auth`) [Public]

#### POST `/api/auth/register/candidate`
Register a new candidate account.

**Request Body:**
```json
{
  "fullName": "string",
  "email": "string",
  "password": "string(min 8)"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "id": 0,
    "email": "string",
    "roleName": "string",
    "status": "ACTIVE|SUSPENDED",
    "provider": "LOCAL|GOOGLE",
    "verifiedAt": "ISO8601",
    "dateCreated": "ISO8601"
  }
}
```

#### POST `/api/auth/register/recruiter`
Register a new recruiter account.

**Request Body:**
```json
{
  "fullName": "string",
  "email": "string",
  "password": "string(min 8)",
  "companyName": "string"
}
```

**Response:** AccountResponse (same as candidate registration)

#### POST `/api/auth/login`
Login with email and password.

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": {
    "accessToken": "string",
    "refreshToken": "string"
  }
}
```

#### POST `/api/auth/login/google`
Login with Google OAuth.

**Request Body:**
```json
{
  "idToken": "string"
}
```

**Response:** AuthenticationResponse (same as regular login)

#### POST `/api/auth/refresh`
Refresh access token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response:** AuthenticationResponse

#### POST `/api/auth/logout`
Logout and invalidate refresh token.

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": "Logout successfully"
}
```

### Account Management APIs (`/api/accounts`) [Public]

#### POST `/api/accounts/resend-verification`
Resend email verification.

**Request Body:**
```json
{
  "email": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": "Verify email has been sent to your email"
}
```

#### GET `/api/accounts/verify`
Verify email with token.

**Query Parameters:**
- `token`: string (required)

**Response:**
```json
{
  "code": 1000,
  "data": "Verify email successfully"
}
```

#### POST `/api/accounts/forgot-password`
Request password reset.

**Request Body:**
```json
{
  "email": "string"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": "Password reset instructions have been sent to your email"
}
```

#### POST `/api/accounts/reset-password`
Reset password with token.

**Request Body:**
```json
{
  "token": "string",
  "newPassword": "string(min 8)"
}
```

**Response:**
```json
{
  "code": 1000,
  "data": "Password has been reset successfully"
}
```

#### PATCH `/api/accounts/{accountId}/status/{status}` [Admin Only]
Change account status.

**Path Parameters:**
- `accountId`: number
- `status`: "ACTIVE" | "SUSPENDED"

**Response:**
```json
{
  "code": 1000,
  "data": "Change account status successfully"
}
```

### Job APIs (`/api/job`) [Mixed Public/Auth]

#### GET `/api/job` [Public]
Get all published jobs with pagination.

**Query Parameters:**
- `page`: number (default: 0)
- `size`: number (default: 10)
- `sortBy`: string (default: "id")
- `sortDir`: "asc" | "desc" (default: "asc")

**Response:**
```json
{
  "code": 1000,
  "data": {
    "content": [
      {
        "id": 0,
        "title": "string",
        "company": "string",
        "jobRole": "string",
        "seniority": "INTERN|FRESHER|JUNIOR|MID|SENIOR|MANAGER",
        "minExperienceYears": 0,
        "location": "string",
        "workMode": "ONSITE|REMOTE|HYBRID",
        "salaryMin": 0,
        "salaryMax": 0,
        "currency": "string",
        "maxCandidates": 0,
        "datePosted": "ISO8601",
        "dateExpires": "ISO8601",
        "status": "DRAFT|PENDING|PUBLISHED|EXPIRED|CANCELED"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

#### GET `/api/job/public/detail/{jobId}` [Public]
Get detailed job information.

**Path Parameters:**
- `jobId`: number

**Response:**
```json
{
  "code": 1000,
  "data": {
    "id": 0,
    "title": "string",
    "company": "string",
    "jobRole": "string",
    "seniority": "enum",
    "minExperienceYears": 0,
    "location": "string",
    "workMode": "enum",
    "salaryMin": 0,
    "salaryMax": 0,
    "currency": "string",
    "maxCandidates": 0,
    "datePosted": "ISO8601",
    "dateExpires": "ISO8601",
    "status": "enum",
    "summary": "string",
    "responsibilities": "string",
    "requirements": "string",
    "niceToHave": "string",
    "benefits": "string",
    "hiringProcess": "string",
    "notes": "string",
    "skills": ["string"]
  }
}
```

#### POST `/api/job` [Auth Required - Recruiter]
Create a new job posting.

**Request Body:**
```json
{
  "title": "string",
  "jobRoleId": 0,
  "seniorityLevel": "INTERN|FRESHER|JUNIOR|MID|SENIOR|MANAGER",
  "employmentType": "FULL_TIME|PART_TIME|CONTRACT|INTERNSHIP|FREELANCE|TEMPORARY|VOLUNTEER",
  "minExperienceYears": 0,
  "locationId": 0,
  "workMode": "ONSITE|REMOTE|HYBRID",
  "salaryMin": 0,
  "salaryMax": 0,
  "currency": "string",
  "maxCandidates": 0,
  "dateExpires": "ISO8601",
  "summary": "string",
  "responsibilities": "string",
  "requirements": "string",
  "niceToHave": "string",
  "benefits": "string",
  "hiringProcess": "string",
  "notes": "string",
  "saveAsDraft": false,
  "skillIds": [0]
}
```

**Response:** JobResponse

#### PUT `/api/job/{jobId}` [Auth Required - Recruiter]
Update an existing job posting.

**Path Parameters:**
- `jobId`: number

**Request Body:** Same as CreateJobRequest (all fields optional)

**Response:** JobResponse

#### PATCH `/api/job/{jobId}` [Auth Required - Recruiter]
Cancel a job posting.

**Path Parameters:**
- `jobId`: number

**Response:** JobResponse

#### PATCH `/api/job/{jobId}/moderate` [Auth Required - Admin]
Moderate job posting (approve/reject).

**Path Parameters:**
- `jobId`: number

**Query Parameters:**
- `action`: string (e.g., "approve", "reject")

**Response:** JobResponse

### Job Category APIs (`/api/job/category`) [Mixed Public/Auth]

#### GET `/api/job/category` [Public]
Get job categories (JobFamily) with pagination.

**Query Parameters:**
- `page`: number (default: 0)
- `size`: number (default: 10)
- `sortBy`: string (default: "id")
- `sortDir`: "asc" | "desc" (default: "asc")

**Response:**
```json
{
  "code": 1000,
  "data": {
    "content": [
      {
        "id": 0,
        "name": "string",
        "dateCreated": "ISO8601",
        "dateUpdated": "ISO8601"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

#### POST `/api/job/category` [Auth Required]
Create a new job family.

**Request Body:**
```json
{
  "name": "string"
}
```

**Response:** JobFamily object

#### POST `/api/job/category/{jobFamilyId}` [Auth Required]
Create a sub-family under a job family.

**Path Parameters:**
- `jobFamilyId`: number

**Request Body:**
```json
{
  "name": "string"
}
```

**Response:** SubFamily object

#### POST `/api/job/category/{jobFamilyId}/{subFamilyId}` [Auth Required]
Create a job role under a sub-family.

**Path Parameters:**
- `jobFamilyId`: number
- `subFamilyId`: number

**Request Body:**
```json
{
  "name": "string"
}
```

**Response:** JobRole object

---

## Authenticated Endpoints

### Candidate APIs (`/api/candidates`) [Auth Required - Candidate]

#### GET `/api/candidates/profile`
Get candidate profile information.

**Response:**
```json
{
  "code": 1000,
  "data": {
    "id": 0,
    "accountId": 0,
    "fullName": "string",
    "phone": "string",
    "location": {
      "id": 0,
      "city": "string",
      "country": "string"
    },
    "seniority": "INTERN|FRESHER|JUNIOR|MID|SENIOR|MANAGER",
    "salaryExpectMin": 0,
    "salaryExpectMax": 0,
    "currency": "string",
    "remotePref": true,
    "relocationPref": false,
    "avatarResourceId": 0,
    "bio": "string",
    "dateCreated": "ISO8601",
    "dateUpdated": "ISO8601",
    "skills": [
      {
        "skillId": 0,
        "skillName": "string",
        "proficiency": "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT"
      }
    ]
  }
}
```

#### PUT `/api/candidates/profile`
Update candidate profile.

**Request Body:**
```json
{
  "fullName": "string",
  "phone": "string",
  "location": {
    "city": "string",
    "country": "string"
  },
  "seniority": "INTERN|FRESHER|JUNIOR|MID|SENIOR|MANAGER",
  "salaryExpectMin": 0,
  "salaryExpectMax": 0,
  "currency": "string",
  "remotePref": true,
  "relocationPref": false,
  "bio": "string",
  "skills": [
    {
      "skillId": 0,
      "proficiency": "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT"
    }
  ]
}
```

**Response:** CandidateResponse

#### POST `/api/candidates/save/{jobId}`
Save a job for later.

**Path Parameters:**
- `jobId`: number

**Response:** SavedJobResponse

#### DELETE `/api/candidates/save/{jobId}`
Remove a saved job.

**Path Parameters:**
- `jobId`: number

**Response:**
```json
{
  "code": 1000,
  "data": "Remove saved job successfully"
}
```

#### GET `/api/candidates/saved-jobs`
Get all saved jobs with pagination.

**Query Parameters:**
- `page`: number (default: 1)
- `size`: number (default: 10)
- `sortBy`: string (default: "savedAt")
- `sortDir`: "asc" | "desc" (default: "desc")

**Response:** PageResult<SavedJobResponse>

#### POST `/api/candidates/avatar`
Upload candidate avatar.

**Request:** Multipart form data
- `file`: MultipartFile (image)

**Response:** ResourceResponse

#### POST `/api/candidates/applications/{jobId}`
Apply for a job.

**Path Parameters:**
- `jobId`: number

**Request:** Multipart form data
- `file`: MultipartFile (CV/resume)

**Response:** JobApplicationResponse

#### GET `/api/candidates/applications`
Get all job applications with pagination.

**Query Parameters:**
- `page`: number (default: 1)
- `size`: number (default: 10)
- `sortBy`: string (default: "appliedAt")
- `sortDir`: "asc" | "desc" (default: "desc")

**Response:** PageResult<JobApplicationResponse>

### Recruiter APIs (`/api/recruiters`) [Auth Required - Recruiter]

#### GET `/api/recruiters/profile`
Get recruiter profile information.

**Response:**
```json
{
  "code": 1000,
  "data": {
    "id": 0,
    "accountId": 0,
    "fullName": "string",
    "phone": "string",
    "avatarResourceId": 0,
    "company": {
      "id": 0,
      "name": "string",
      "website": "string",
      "size": "string",
      "companyLocations": [
        {
          "id": 0,
          "location": {
            "id": 0,
            "city": "string",
            "country": "string"
          }
        }
      ]
    },
    "dateCreated": "ISO8601",
    "dateUpdated": "ISO8601"
  }
}
```

#### PUT `/api/recruiters/profile`
Update recruiter profile.

**Request Body:**
```json
{
  "fullName": "string",
  "phone": "string"
}
```

**Response:** RecruiterResponse

#### PUT `/api/recruiters/company`
Update company information.

**Request Body:**
```json
{
  "name": "string",
  "website": "string",
  "size": "string",
  "companyLocations": [
    {
      "location": {
        "city": "string",
        "country": "string"
      }
    }
  ]
}
```

**Response:** CompanyResponse

#### POST `/api/recruiters/avatar`
Upload recruiter avatar.

**Request:** Multipart form data
- `file`: MultipartFile (image)

**Response:** ResourceResponse

#### GET `/api/recruiters/company/jobs`
Get company's job postings with pagination.

**Query Parameters:**
- `jobStatus`: string (required)
- `page`: number (default: 0)
- `size`: number (default: 10)
- `sortBy`: string (default: "datePosted")
- `sortDir`: "asc" | "desc" (default: "DESC")

**Response:** PageResult<JobResponse>

#### GET `/api/recruiters/company/{jobId}/applicants`
Get job applicants for a specific job.

**Path Parameters:**
- `jobId`: number

**Query Parameters:**
- `page`: number (default: 0)
- `size`: number (default: 10)
- `sortBy`: string (default: "appliedAt")
- `sortDir`: "asc" | "desc" (default: "DESC")

**Response:** PageResult<JobApplicantResponse>

#### PATCH `/api/recruiters/company/{jobId}/applicants/{jobApplicationId}`
Process a job application (review, interview, offer, reject).

**Path Parameters:**
- `jobId`: number
- `jobApplicationId`: number

**Query Parameters:**
- `action`: string (e.g., "review", "interview", "offer", "reject")

**Response:** JobApplicantResponse

---

## Data Models

### Enums

#### AccountStatus
- `ACTIVE`
- `SUSPENDED`

#### AuthProvider
- `LOCAL`
- `GOOGLE`

#### SeniorityLevel
- `INTERN`
- `FRESHER`
- `JUNIOR`
- `MID`
- `SENIOR`
- `MANAGER`

#### EmploymentType
- `FULL_TIME`
- `PART_TIME`
- `CONTRACT`
- `INTERNSHIP`
- `FREELANCE`
- `TEMPORARY`
- `VOLUNTEER`

#### WorkMode
- `ONSITE`
- `REMOTE`
- `HYBRID`

#### JobStatus
- `DRAFT`
- `PENDING`
- `PUBLISHED`
- `EXPIRED`
- `CANCELED`

#### ApplicationStatus
- `SUBMITTED`
- `REVIEWED`
- `INTERVIEW`
- `OFFERED`
- `REJECTED`

#### ResourceType
- `AVATAR`
- `CV`
- `COMPANY_LOGO`

---

## Error Handling

Errors are returned with HTTP status codes and the standard envelope:

```json
{
  "code": 4xxx,
  "message": "error description"
}
```

### Common Error Codes
- `4000`: AUTH_UNAUTHORIZED
- `4001`: INVALID_CREDENTIALS
- `4002`: ENUM_INVALID_VALUE
- `4003`: Validation errors (field-level constraints)
- `4004`: Database violations (duplicate key, foreign key, not-null, unique constraint)

### HTTP Status Codes
- `200`: Success
- `400`: Bad Request (validation errors)
- `401`: Unauthorized (authentication required)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found
- `500`: Internal Server Error

---

## Configuration

### Environment Variables
- `DATABASE_URL`: PostgreSQL connection URL
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `REDIS_HOST`: Redis host (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)
- `JWT_SECRET`: JWT signing secret
- `CLIENT_ID`: Google OAuth client ID
- `CLIENT_SECRET`: Google OAuth client secret
- `MAIL_USERNAME`: SMTP username
- `MAIL_PASSWORD`: SMTP password
- `CLOUDINARY_NAME`: Cloudinary cloud name
- `CLOUDINARY_API_KEY`: Cloudinary API key
- `CLOUDINARY_API_SECRET`: Cloudinary API secret
- `FRONTEND_URL`: Frontend application URL

### Database Configuration
- **Database**: PostgreSQL 18.0
- **Migration**: Flyway
- **Timezone**: Asia/Ho_Chi_Minh
- **Connection Pool**: HikariCP (Spring Boot default)

### Cache Configuration
- **Cache**: Redis 7.4
- **Persistence**: AOF (Append Only File) enabled
- **Default TTL**: Configured per cache key

### Security Configuration
- **CORS**: Enabled for all origins
- **CSRF**: Disabled (stateless API)
- **Session**: Stateless
- **JWT Algorithm**: HS512
- **Token Expiration**: 7 days

---

## File Upload

### Supported File Types
- **Images**: For avatars and company logos
- **Documents**: For CVs and resumes

### File Storage
- **Provider**: Cloudinary
- **Max File Size**: Configured in application properties
- **Supported Formats**: Common image and document formats

### Upload Endpoints
- `POST /api/candidates/avatar` - Candidate avatar
- `POST /api/recruiters/avatar` - Recruiter avatar
- `POST /api/candidates/applications/{jobId}` - Job application CV

---

## Rate Limiting & Security

### Authentication
- JWT tokens with 7-day expiration
- Refresh token mechanism
- Token blacklisting on logout
- OAuth2 integration with Google

### Authorization
- Role-based access control (RBAC)
- Method-level security annotations
- Custom security filters

### Data Validation
- Bean validation annotations
- Custom validation rules
- Input sanitization

---

## Development & Deployment

### Local Development
1. Start PostgreSQL and Redis using Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Set environment variables in `application-dev.yml`

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Database Migration
- Flyway handles database schema migrations
- Migration files located in `src/main/resources/db/migration/`
- Automatic migration on application startup

### Testing
- Unit tests with Spring Boot Test
- Security tests with Spring Security Test
- Integration tests for API endpoints

---

## API Usage Examples

### Authentication Flow
1. Register account: `POST /api/auth/register/candidate`
2. Verify email: `GET /api/accounts/verify?token=...`
3. Login: `POST /api/auth/login`
4. Use access token in subsequent requests: `Authorization: Bearer <token>`

### Job Application Flow
1. Browse jobs: `GET /api/job` (public)
2. View job details: `GET /api/job/public/detail/{jobId}` (public)
3. Apply for job: `POST /api/candidates/applications/{jobId}` (auth required)
4. Track applications: `GET /api/candidates/applications` (auth required)

### Recruiter Workflow
1. Create job posting: `POST /api/job` (auth required)
2. View applications: `GET /api/recruiters/company/{jobId}/applicants` (auth required)
3. Process applications: `PATCH /api/recruiters/company/{jobId}/applicants/{applicationId}` (auth required)

---

## Notes
- All timestamps use ISO-8601 format with timezone offset
- Multipart endpoints expect `Content-Type: multipart/form-data`
- Pagination is 0-indexed for most endpoints
- File uploads are handled asynchronously
- Email notifications are sent for verification and password reset
- The platform supports both local and Google OAuth authentication