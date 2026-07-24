package com.library.exception;

/**
 * Thrown when an operation violates a business rule (for example borrowing over the limit
 * or renewing an overdue loan). Maps to HTTP 400.
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
