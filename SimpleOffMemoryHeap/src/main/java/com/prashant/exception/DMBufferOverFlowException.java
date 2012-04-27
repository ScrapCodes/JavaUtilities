package com.prashant.exception;

/**
 * If an application has its own memory management for buffer allocation. A
 * buffer overflow occurs when an attempt is made to store more items in the
 * DirectMemoryBuffer than it's capacity
 * 
 * @author prashant
 * 
 */
public class DMBufferOverFlowException extends Exception {

	private static final long serialVersionUID = 3855572440545219746L;
	private Integer errorCode;

	/**
	 * Constructs a new exception with the default error code, detail message
	 * and cause.
	 * 
	 */
	public DMBufferOverFlowException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = 123125;
	}

	/**
	 * Constructs a new exception with the specified error code, detail message
	 * and cause.
	 * 
	 */
	public DMBufferOverFlowException(Integer errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public Integer getErrorCode() {
		return this.errorCode;
	}

}
