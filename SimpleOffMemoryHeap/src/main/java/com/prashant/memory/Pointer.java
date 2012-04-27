package com.prashant.memory;

public class Pointer {
	private int start;
	private int end;
	private boolean free;
	private int blockNumber;
	// To enable traversing as a Doubly linked list
	private Pointer next;
	private Pointer prev;
	private Class<? extends Object> clazz;

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public int getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}

	public Pointer getNext() {
		return next;
	}

	public void setNext(Pointer next) {
		this.next = next;
	}

	public Pointer getPrev() {
		return prev;
	}

	public void setPrev(Pointer prev) {
		this.prev = prev;
	}

	public Class<? extends Object> getClazz() {
		return clazz;
	}

	public void setClazz(Class<? extends Object> clazz) {
		this.clazz = clazz;
	}

}