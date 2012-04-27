package com.prashant.memory;

import com.prashant.exception.DMBufferOverFlowException;
import com.prashant.exception.DMFrameLimitExcededException;

public interface MemoryBuffer {

	/**
	 * @return Total used bytes
	 */
	public abstract int used();

	/**
	 * @return Total capacity configured
	 */
	public abstract int capacity();

	/**
	 * Retrieve bytes with offset and length where offset is relative position.
	 * 
	 * @throws DMFrameLimitExcededException
	 */
	public abstract byte[] retrieve(Pointer pointer, int offset, int len) throws DMFrameLimitExcededException;

	/**
	 * Retrieves the bytes pointed to by <code>Pointer</code>.
	 * 
	 * @throws DMFrameLimitExcededException
	 */
	public abstract byte[] retrieve(Pointer pointer) throws DMFrameLimitExcededException;

	/**
	 * Marks the pointer as free and reduces the total <code>used</code> bytes.
	 * 
	 * @param pointer2free
	 * @return bytes removed
	 */
	public abstract long free(Pointer pointer2free);

	/**
	 * Resets the entire Memory buffer, clears pointer list and sets total
	 * <code>used</code> bytes to zero
	 * 
	 */
	public abstract void clear();

	/**
	 * @param payload
	 *            to be stored as byte array.
	 * 
	 * @return The <code>Pointer</code> object which points to stored bytes.
	 * @throws DMBufferOverFlowException
	 */
	public abstract Pointer store(byte[] payload, int capacity) throws DMBufferOverFlowException;

	/**
	 * @param payload
	 *            to be stored as byte array.
	 * 
	 * @return The <code>Pointer</code> object which points to stored bytes.
	 * @throws DMBufferOverFlowException
	 */
	public abstract Pointer store(byte[] payload) throws DMBufferOverFlowException;

	/**
	 * Faster
	 * 
	 * @throws DMFrameLimitExcededException
	 */
	public abstract void update(Pointer pointer, byte[] payload, int offset) throws DMFrameLimitExcededException;

	/**
	 * Costly operation
	 * 
	 * @throws DMBufferOverFlowException
	 */
	public abstract Pointer update(Pointer pointer, byte[] payload) throws DMBufferOverFlowException;

}