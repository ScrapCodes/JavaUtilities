package com.prashant.memory;

import com.prashant.exception.DMBufferOverFlowException;
import com.prashant.exception.DMFrameLimitExcededException;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 
 * Inspired and adapted from <em>Apache DirectMemory</em> according to our needs
 * 
 * <p>
 * Off heap memory buffer management. Includes utilities to create, store and retrieve memory blocks pointed to by <code>Pointer</code>.
 * </p>
 * <p>
 * We would need this as some applications requires frequent memory allocation and deallocation which can cause a significant overhead and thus become
 * a bottle neck while scaling.
 * </p>
 * 
 * 
 * @see <a href="http://incubator.apache.org/projects/directmemory.html">Apache DirectMemory</a>
 */
public class DirectMemoryBuffer implements MemoryBuffer {
    private static final Logger logger = Logger.getAnonymousLogger();
    protected ByteBuffer buffer;

    /** List of pointers to blocks of memory */
    // public List<Pointer> pointers = new ArrayList<Pointer>();
    public Queue<Pointer> pointers = new LinkedBlockingQueue<Pointer>();

    AtomicInteger used = new AtomicInteger();
    public int bufferNumber;

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#used()
     */
    public int used() {
        return used.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#capacity()
     */
    public int capacity() {
        return buffer.capacity();
    }

    /**
     * Creates new instance of OffHeapMemoryBuffer with the specified capacity
     * and bufferNumber
     * 
     */
    public static MemoryBuffer createNew(int capacity, int bufferNumber) {
        logger.info("Creating Off heap memory buffer of capacity :" + capacity + " bufferNumber:" + bufferNumber);
        return new DirectMemoryBuffer(ByteBuffer.allocateDirect(capacity), bufferNumber);
    }

    private DirectMemoryBuffer(ByteBuffer buffer, int bufferNumber) {
        this.buffer = buffer;
        this.bufferNumber = bufferNumber;
        buffer.order(); // Please configure ByteOrder.BIG_ENDIAN :
                        // ByteOrder.LITTLE_ENDIAN
        createAndAddFirstPointer();
    }

    private Pointer createAndAddFirstPointer() {
        Pointer first = new Pointer();
        first.setBlockNumber(0);
        first.setStart(0);
        first.setFree(true);
        first.setEnd(buffer.capacity() - 1);
        first.setNext(null);
        first.setPrev(null);
        pointers.add(first);
        return first;
    }

    /**
     * Resize an existing block to new capacity by slicing the <code>Pointer</code> pointed to by existing pointer. The new <code>Pointer</code> is a
     * slice present before the existing.
     * 
     * @param existing
     *            points to existing Pointer(referring existing block).
     * @param capacity
     *            to be resized to.
     * @return Pointer to resized block.
     */
    private Pointer slice(Pointer existing, int capacity) {
        if ((existing.getEnd() - existing.getStart()) == capacity + 1 && existing.isFree()) {
            return existing;
        }
        Pointer fresh = new Pointer();
        fresh.setBlockNumber(existing.getBlockNumber());
        fresh.setStart(existing.getStart());
        fresh.setEnd(fresh.getStart() + capacity);
        fresh.setFree(true);
        fresh.setPrev(existing.getPrev());
        existing.setPrev(fresh);
        fresh.setNext(existing);
        existing.setStart(existing.getStart() + (capacity + 1));
        return fresh;
    }

    /**
     * Returns the block of size equal or greater than current size for
     * reallocation which is marked as free.
     * 
     * @param capacity
     * @return null when it fails to search a valid sized block
     */
    private Pointer firstMatch(int capacity) {
        synchronized (pointers) {
            
            for (Pointer ptr : pointers) {
                // Find a frame with right size. Comapaction problems to be dealt.
                if (ptr.isFree() && (ptr.getEnd() - ptr.getStart()) > capacity) {
                    return ptr;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#retrieve(com.prashant.memory.Pointer, int, int)
     */
    public byte[] retrieve(Pointer pointer, int offset, int len) throws DMFrameLimitExcededException {
        int checkLength = pointer.getStart() + offset + len;
        ByteBuffer buf = null;
        if (checkLength <= pointer.getEnd()) {
            synchronized (buffer) {
                buf = buffer.duplicate();
            }
            buf.position(pointer.getStart() + offset);
            final byte[] swp = new byte[len];
            buf.get(swp);
            return swp;
        }
        throw new DMFrameLimitExcededException("Trying to read to a position out of bounds for this pointer.", null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#retrieve(com.prashant.memory.Pointer)
     */
    public byte[] retrieve(Pointer pointer) throws DMFrameLimitExcededException {
        return retrieve(pointer, 0, pointer.getEnd() - pointer.getStart());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#free(com.prashant.memory.Pointer)
     */
    public long free(Pointer pointer2free) {
        used.addAndGet(-(pointer2free.getEnd() - pointer2free.getStart()));
        freeAndMerge(pointer2free);
        return pointer2free.getEnd() - pointer2free.getStart();
    }

    /**
     * It is synchronized as it does merging of adjacent blocks. An easiest way
     * to somewhat address fragmentation problem.
     * 
     * @param pointer2free
     */
    private void freeAndMerge(Pointer pointer2free) {
        synchronized (pointers) {
            
            pointers.remove(pointer2free);
            // merge adjacent blocks
            if (null != pointer2free.getPrev() && pointer2free.getPrev().isFree()) {
                // Merge previous
                pointers.remove(pointer2free.getPrev());
                pointer2free.setStart(pointer2free.getPrev().getStart());
                pointer2free.setPrev(pointer2free.getPrev().getPrev());
                // Recursive call
                freeAndMerge(pointer2free);
            }

            if (null != pointer2free.getNext() && pointer2free.getNext().isFree()) {
                // Merge Next
                pointers.remove(pointer2free.getNext());
                pointer2free.setEnd(pointer2free.getNext().getEnd());
                pointer2free.setNext(pointer2free.getNext().getNext());
                // Recursive call
                freeAndMerge(pointer2free);
            }
            if (!pointer2free.isFree()) {
                pointer2free.setFree(true);
                pointer2free.setClazz(null);
                pointers.add(pointer2free);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#clear()
     */
    public void clear() {
        pointers.clear();
        createAndAddFirstPointer();
        buffer.clear();
        used.set(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#store(byte[], int)
     */
    public synchronized Pointer store(byte[] payload, int capacity) throws DMBufferOverFlowException {
        // First good match is page that has capacity equal or greater than
        // payload.
        Pointer goodOne = firstMatch(capacity);
        if (goodOne == null) {
            RuntimeException e = new NullPointerException();
            // logger.error("Did not find a suitable buffer");
            throw new DMBufferOverFlowException("did not find a suitable buffer", e.getCause());
        }

        Pointer fresh = slice(goodOne, capacity);

        fresh.setFree(false);
        used.addAndGet(payload.length);
        ByteBuffer buf = buffer.slice();
        buf.position(fresh.getStart());
        try {
            buf.put(payload);
        } catch (BufferOverflowException e) {
            goodOne.setStart(fresh.getStart());
            goodOne.setEnd(buffer.limit());
            // return null; //Uncomment incase we want to ignore this exception.
            throw new DMBufferOverFlowException("An attempt to store more than the configured capacity", e.getCause());
        }
        pointers.add(fresh);
        return fresh;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#store(byte[])
     */
    public synchronized Pointer store(byte[] payload) throws DMBufferOverFlowException {
        return store(payload, payload.length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#update(com.prashant.memory.Pointer, byte[], int)
     */
    public void update(Pointer pointer, byte[] payload, int offset) throws DMFrameLimitExcededException {
        if (pointer.getStart() + offset + payload.length - 1 <= pointer.getEnd()) {
            ByteBuffer buf;
            synchronized (buffer) {
                buf = buffer.duplicate();
            }
            buf.position(pointer.getStart() + offset);
            buf.put(payload);
            return;
        }
        throw new DMFrameLimitExcededException("Trying to write to a position out of bounds for this pointer.", null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.prashant.memory.MemoryBuffer#update(com.prashant.memory.Pointer, byte[])
     */
    public Pointer update(Pointer pointer, byte[] payload) throws DMBufferOverFlowException {
        free(pointer);
        return store(payload);
    }
}