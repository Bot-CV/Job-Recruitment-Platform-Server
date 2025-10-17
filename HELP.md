## Job Recruitment Platform - API Documentation

### Base URL
- **Local**: `http://localhost:8080`

### Authentication
- **Scheme**: Bearer JWT in `Authorization` header: `Authorization: Bearer <accessToken>`
- Public endpoints are explicitly marked. Others require authentication and role-based access enforced by the backend.

### Standard Response Envelope
All endpoints (except `GET /api/job`) return a common envelope:

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

Pagination responses use:

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

### Auth APIs (`/api/auth`)

- POST `/api/auth/register/candidate`
  - Body:
    ```json
    { "fullName": "string", "email": "string", "password": "string(min 8)" }
    ```
  - Response `data` (AccountResponse):
    ```json
    { "id": 0, "email": "string", "roleName": "string", "status": "enum", "provider": "enum", "verifiedAt": "ISO8601", "dateCreated": "ISO8601" }
    ```

- POST `/api/auth/register/recruiter`
  - Body:
    ```json
    { "fullName": "string", "email": "string", "password": "string(min 8)", "companyName": "string" }
    ```
  - Response: AccountResponse (as above)

- POST `/api/auth/login`
  - Body:
    ```json
    { "email": "string", "password": "string" }
    ```
  - Response `data` (AuthenticationResponse):
    ```json
    { "accessToken": "string", "refreshToken": "string" }
    ```

- POST `/api/auth/login/google`
  - Body:
    ```json
    { "idToken": "string" }
    ```
  - Response: AuthenticationResponse

- POST `/api/auth/refresh`
  - Body:
    ```json
    { "refreshToken": "string" }
    ```
  - Response: AuthenticationResponse

- POST `/api/auth/logout`
  - Body:
    ```json
    { "refreshToken": "string" }
    ```
  - Response: `{ "code": 1000, "data": "Logout successfully" }`

---

### Account APIs (`/api/accounts`)

- POST `/api/accounts/resend-verification`
  - Body:
    ```json
    { "email": "string" }
    ```
  - Response: `"Verify email has been sent to your email"`

- GET `/api/accounts/verify`
  - Query: `token=string`
  - Response: `"Verify email successfully"`

- PATCH `/api/accounts/{accountId}/status/{status}`
  - Path: `accountId: number`, `status: AccountStatus enum`
  - Response: `"Change account status successfully"`

- POST `/api/accounts/forgot-password`
  - Body:
    ```json
    { "email": "string" }
    ```
  - Response: `"Password reset instructions have been sent to your email"`

- POST `/api/accounts/reset-password`
  - Body:
    ```json
    { "token": "string", "newPassword": "string(min 8)" }
    ```
  - Response: `"Password has been reset successfully"`

---

### Candidate APIs (`/api/candidates`) [Auth required]

- GET `/api/candidates/profile`
  - Response `data` (CandidateResponse):
    ```json
    {
      "id": 0,
      "accountId": 0,
      "fullName": "string",
      "location": { /* LocationResponse */ },
      "seniority": "enum(SeniorityLevel)",
      "salaryExpectMin": 0,
      "salaryExpectMax": 0,
      "currency": "string",
      "remotePref": true,
      "relocationPref": false,
      "avatarResourceId": 0,
      "bio": "string",
      "dateCreated": "ISO8601",
      "dateUpdated": "ISO8601",
      "skills": [ /* CandidateSkillResponse */ ]
    }
    ```

- PUT `/api/candidates/profile`
  - Body (CandidateRequest):
    ```json
    {
      "fullName": "string",
      "location": { /* LocationRequest */ },
      "seniority": "enum(SeniorityLevel)",
      "salaryExpectMin": 0,
      "salaryExpectMax": 0,
      "currency": "string",
      "remotePref": true,
      "relocationPref": false,
      "bio": "string",
      "skills": [ /* CandidateSkillRequest */ ]
    }
    ```
  - Response: CandidateResponse

- POST `/api/candidates/save/{jobId}`
  - Path: `jobId: number`
  - Response: SavedJobResponse

- DELETE `/api/candidates/save/{jobId}`
  - Path: `jobId: number`
  - Response: `"Remove saved job successfully"`

- POST `/api/candidates/avatar`
  - Form-Data: `file` (MultipartFile)
  - Response: ResourceResponse

- POST `/api/candidates/applications/{jobId}`
  - Path: `jobId: number`
  - Form-Data: `file` (MultipartFile)
  - Response: JobApplicationResponse

- GET `/api/candidates/applications`
  - Query: `page, size, sortBy, sortDir`
  - Response: DataResponse<JobApplicationResponse>

---

### Recruiter APIs (`/api/recruiters`) [Auth required]

- GET `/api/recruiters/profile`
  - Response: RecruiterResponse

- PUT `/api/recruiters/profile`
  - Body: `{"fullName": "string"}`
  - Response: RecruiterResponse

- PUT `/api/recruiters/company`
  - Body (CompanyRequest):
    ```json
    {
      "name": "string",
      "website": "string",
      "size": "string",
      "companyLocations": [ /* CompanyLocationRequest */ ]
    }
    ```
  - Response: CompanyResponse

- POST `/api/recruiters/avatar`
  - Form-Data: `file` (MultipartFile)
  - Response: ResourceResponse

---

### Job Category APIs

- GET `/api/public/job/category` [Public]
  - Query: `page`(default 0), `size`(default 10), `sortBy`(default `id`), `sortDir`(`asc|desc`)
  - Response `data`: PageResult of JobFamily

- POST `/api/job/category` [Auth required]
  - Body:
    ```json
    { "name": "string" }
    ```
  - Response `data`: JobFamily

- POST `/api/job/category/{jobFamilyId}` [Auth required]
  - Path: `jobFamilyId: number`
  - Body: `{ "name": "string" }`
  - Response `data`: SubFamily

- POST `/api/job/category/{jobFamilyId}/{subFamilyId}` [Auth required]
  - Path: `jobFamilyId: number`, `subFamilyId: number`
  - Body: `{ "name": "string" }`
  - Response `data`: JobRole

Note: `JobFamily`, `SubFamily`, `JobRole` are domain models; fields depend on domain definitions.

---

### Job APIs (`/api/job`)

- GET `/api/job` [Public]
  - Query: `page`(0), `size`(10), `sortBy`(`id`), `sortDir`(`asc|desc`)
  - Response: PageResult of JobResponse (returned directly, not wrapped in DataResponse)

- POST `/api/job` [Auth required]
  - Body (CreateJobRequest)
    ```json
    {
      "title": "string",
      "jobRoleId": 0,
      "seniorityLevel": "enum(SeniorityLevel)",
      "employmentType": "enum(EmploymentType)",
      "minExperienceYears": 0,
      "locationId": 0,
      "workMode": "enum(WorkMode)",
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
  - Response: JobResponse

- PUT `/api/job/{jobId}` [Auth required]
  - Path: `jobId: number`
  - Body (UpdateJobRequest): similar to CreateJobRequest (all fields optional)
  - Response: JobResponse

- PATCH `/api/job/{jobId}` [Auth required]
  - Action: Cancel job posting
  - Response: JobResponse

- PATCH `/api/job/{jobId}/moderate` [Auth required]
  - Query: `action=string` (e.g., approve/reject)
  - Response: JobResponse

---

### Errors
Errors are returned with HTTP status from a central handler and the envelope:

```json
{
  "code": 4xxx,
  "message": "error description"
}
```

Common cases (non-exhaustive):
- AUTH_UNAUTHORIZED, INVALID_CREDENTIALS
- ENUM_INVALID_VALUE (invalid enum in request)
- Validation errors: field-level constraints map to specific codes
- Database violations: duplicate key, foreign key, not-null, unique constraint

---

### Notes
- Multipart endpoints expect `Content-Type: multipart/form-data` with `file` field.
- Enum values: check domain enums `SeniorityLevel`, `EmploymentType`, `WorkMode`, `AccountStatus`, `AuthProvider`.
- All timestamps use ISO-8601 with offset.
