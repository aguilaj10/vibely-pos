package com.vibely.pos.shared.domain.exception

/**
 * Base sealed class for all domain-level exceptions.
 *
 * Domain exceptions represent business rule violations and expected failure scenarios
 * within the domain layer. They carry a human-readable [message] and an optional
 * machine-readable [code] for programmatic error handling.
 */
sealed class DomainException(override val message: String, val code: String? = null, override val cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Thrown when input validation fails.
 *
 * @param field The name of the field that failed validation.
 * @param message A description of the validation failure.
 */
class ValidationException(val field: String, override val message: String, code: String? = "VALIDATION_ERROR", cause: Throwable? = null) :
    DomainException(
        message = message,
        code = code,
        cause = cause,
    ) {
    override fun toString(): String = "ValidationException(field='$field', message='$message', code=$code)"
}

/**
 * Thrown when a requested entity or resource cannot be found.
 *
 * @param entityType The type of entity that was not found (e.g., "Product", "Order").
 * @param identifier The identifier used in the lookup.
 */
class NotFoundException(val entityType: String, val identifier: String, code: String? = "NOT_FOUND", cause: Throwable? = null) :
    DomainException(
        message = "$entityType not found with identifier: $identifier",
        code = code,
        cause = cause,
    ) {
    override fun toString(): String = "NotFoundException(entityType='$entityType', identifier='$identifier', code=$code)"
}

/**
 * Thrown when an operation is attempted without proper authorization.
 *
 * @param message A description of the authorization failure.
 */
class UnauthorizedException(override val message: String = "Unauthorized access", code: String? = "UNAUTHORIZED", cause: Throwable? = null) :
    DomainException(
        message = message,
        code = code,
        cause = cause,
    ) {
    override fun toString(): String = "UnauthorizedException(message='$message', code=$code)"
}

/**
 * Thrown when a business rule or invariant is violated.
 *
 * @param rule A description of the business rule that was violated.
 */
class BusinessRuleException(val rule: String, code: String? = "BUSINESS_RULE_VIOLATION", cause: Throwable? = null) :
    DomainException(
        message = "Business rule violated: $rule",
        code = code,
        cause = cause,
    ) {
    override fun toString(): String = "BusinessRuleException(rule='$rule', code=$code)"
}
