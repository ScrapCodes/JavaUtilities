package com.prashant.exception;

/**
 * A frame is being pointed to by a pointer. This is thrown when a pointer tries
 * to request bytes beyond its limits.
 * 
 * @author prashant
 * 
 */
public class DMFrameLimitExcededException extends Exception {

	private static final long serialVersionUID = 3855572440545219746L;
	private Integer errorCode;

	/**
	 * Constructs a new exception with the default error code, detail message
	 * and cause.
	 * 
	 */
	public DMFrameLimitExcededException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = 123123;
	}

	/**
	 * Constructs a new exception with the specified error code, detail message
	 * and cause.
	 * 
	 */
	public DMFrameLimitExcededException(Integer errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public Integer getErrorCode() {
		return this.errorCode;
	}

}
