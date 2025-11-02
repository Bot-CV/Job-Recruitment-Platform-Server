package org.toanehihi.jobrecruitmentplatformserver.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Credentials (1001 - 1100)
    EMAIL_ALREADY_EXISTED(1001, "Email already existed", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1002, "Username or password is not correct", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD(1003, "New password is as same as old", HttpStatus.FORBIDDEN),
    FULLNAME_BLANK(1004, "Fullname must not be blanked", HttpStatus.FORBIDDEN),
    EMAIL_BLANK(1005, "Email must not be blanked", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD(1006, "You password must be at least 8 characters", HttpStatus.FORBIDDEN),
    INVALID_EMAIL(1007, "Your email must comply with email rules", HttpStatus.FORBIDDEN),
    COMPANY_NAME_BLANK(1008, "Company name must not be blank", HttpStatus.FORBIDDEN),
    ID_TOKEN_BLANK(1009, "ID token is required", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_REQUIRED(1010, "Refresh token is required", HttpStatus.FORBIDDEN),
    LEVEL_INVALID(1011, "Level must be in range 0 - 5", HttpStatus.BAD_REQUEST),
    ACCOUNT_DOES_NOT_SUPPORT(1012, "This account does not support this operation", HttpStatus.FORBIDDEN),

    // Auth (1101 - 1200)
    AUTH_UNAUTHENTICATED(1101, "Unauthenticated", HttpStatus.FORBIDDEN),
    AUTH_UNAUTHORIZED(1102, "Unauthorized request", HttpStatus.FORBIDDEN),
    AUTH_ACCOUNT_SUSPENDED(1103, "Your account has been suspended", HttpStatus.FORBIDDEN),
    AUTH_RESET_TOKEN_INVALID(1104, "Reset token invalid", HttpStatus.FORBIDDEN),

    // JWT (1201 - 1300)
    JWT_INVALID_TOKEN(1201, "Token is invalid or expired", HttpStatus.FORBIDDEN),
    JWT_GENERATION_ERROR(1202, "Jwt generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_TOKEN_BLACKLISTED(1203, "Your token has been blacklisted", HttpStatus.BAD_REQUEST),
    JWT_EXPIRED_TOKEN(1204, "You token has been expired", HttpStatus.BAD_REQUEST),

    // Account (1301 - 1400)
    ACCOUNT_NOT_FOUND(1301, "Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_LINKED_GOOGLE(1302, "Your account already linked to a google account", HttpStatus.BAD_REQUEST),
    ACCOUNT_GOOGLE_EMAIL_MISMATCH(1303, "Your google accounts does not match", HttpStatus.BAD_REQUEST),
    ACCOUNT_GOOGLE_ALREADY_USED(1304, "Google account already used", HttpStatus.BAD_REQUEST),
    ACCOUNT_CANDIDATE_NOT_FOUND(1305, "Candidate not found for this account", HttpStatus.CONFLICT),
    ACCOUNT_VERIFY_TOKEN_INVALID(1306, "Verify email token invalid", HttpStatus.FORBIDDEN),
    ACCOUNT_ALREADY_VERIFIED(1307, "Account already verify", HttpStatus.FORBIDDEN),
    ACCOUNT_RECRUITER_NOT_FOUND(1308, "Recruiter not found for this account", HttpStatus.CONFLICT),

    // Recruiter(1401 - 1500)
    RECRUITER_COMPANY_NOT_FOUND(1401, "There is no company found for recruiter account", HttpStatus.NOT_FOUND),

    // Role (1401 - 1500)
    ROLE_NOT_FOUND(1401, "Role not found", HttpStatus.NOT_FOUND),
    ACCESS_FORBIDDEN(1403, "Access forbidden", HttpStatus.FORBIDDEN),

    // Job (1501 - 1600)
    JOB_NOT_FOUND(1501, "Job not found", HttpStatus.NOT_FOUND),
    JOB_ROLE_NOT_FOUND(1502, "Job role not found", HttpStatus.NOT_FOUND),
    JOB_CLOSED_CANNOT_UPDATE(1503, "Job is closed, cannot update", HttpStatus.BAD_REQUEST),
    JOB_HAS_APPLICANTS_CANNOT_UPDATE(1504, "Job has applicants, cannot update", HttpStatus.BAD_REQUEST),
    JOB_EXPIRED_CANNOT_APPLY(1505, "Job is expired, cannot apply", HttpStatus.BAD_REQUEST),
    JOB_NOT_IN_PENDING_STATUS(1506, "Job is not in pending status", HttpStatus.BAD_REQUEST),
    JOB_APPLICATION_NOT_FOUND(1507, "Job application not found", HttpStatus.NOT_FOUND),
    RECRUITER_UNAUTHORIZED_ACCESS_JOB_APPLICANTS(1508, "Recruiter unauthorized to access job applicants",
            HttpStatus.FORBIDDEN),
    JOB_ALREADY_PROCESSED(1509, "Job already processed", HttpStatus.CONFLICT),
    JOB_ALREADY_SAVED_TO_THIS_ACCOUNT(1510, "Job already saved to this account", HttpStatus.BAD_REQUEST),

    // Location (1601 - 1700)
    LOCATION_NOT_FOUND(1601, "Location not found", HttpStatus.NOT_FOUND),

    // Skill (1701 - 1800)
    SKILL_NOT_FOUND(1701, "Skill not found", HttpStatus.NOT_FOUND),
    INTERVIEW_NOT_FOUND(1702, "Interview not found", HttpStatus.NOT_FOUND),
    RECRUITER_UNAUTHORIZED_ACCESS_INTERVIEW(1703, "Recruiter unauthorized to access this interview",
            HttpStatus.FORBIDDEN),

    // Company (1801 - 1900)
    COMPANY_NOT_FOUND(1801, "Company not found", HttpStatus.NOT_FOUND),
    COMPANY_HAS_BEEN_VERIFIED(1802, "Company has been verified", HttpStatus.FORBIDDEN),

    // Email (9601 - 9650)
    EMAIL_SEND_FAILED(9601, "Failed to send email", HttpStatus.BAD_GATEWAY),

    // Resource (9651 - 9700)
    RESOURCE_NOT_FOUND(9651, "Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_TYPE_MISMATCH(9652, "Resource type mismatch", HttpStatus.BAD_REQUEST),
    RESOURCE_UPLOAD_FAILED(9653, "Resource upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_DOWNLOAD_FAILED(9654, "Resource download failed", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_ATTESTATION_HAS_BEEN_SENT(9655, "Your company's attestations has been sent", HttpStatus.FORBIDDEN),

    // Database constraint violations(9701 - 9800)
    DATABASE_CONSTRAINT_VIOLATION(9701, "Database constraint violated", HttpStatus.BAD_REQUEST),
    DATABASE_DUPLICATE_KEY(9702, "This key already exists. Please choose a different one", HttpStatus.CONFLICT),
    DATABASE_FOREIGN_KEY_VIOLATION(9703, "Foreign key constraint violated", HttpStatus.CONFLICT),
    DATABASE_UNIQUE_CONSTRAINT_VIOLATION(9704, "Unique constraint violated", HttpStatus.CONFLICT),
    DATABASE_NOT_NULL_VIOLATION(9705, "Null value violates not-null constraint", HttpStatus.BAD_REQUEST),

    // Data errors(9801 - 9900)
    ENUM_INVALID_VALUE(9801, "Invalid enum value", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_DATA(9802, "Invalid request data", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(9803, "Input file must not empty", HttpStatus.FORBIDDEN),
    FILE_TYPE_NOT_SUPPORTED(9804, "File type is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_UPLOAD_FAILED(9805, "Fail to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    AVATAR_RESOURCE_NOT_FOUND(9806, "Avatar resource not found", HttpStatus.NOT_FOUND),

    // System errors (9901 - 9999)
    SYSTEM_UNKNOWN_ERROR(9998, "System unknow error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus status;
}
