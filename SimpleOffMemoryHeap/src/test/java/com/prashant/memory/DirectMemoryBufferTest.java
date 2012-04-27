package com.prashant.memory;

import com.prashant.exception.DMBufferOverFlowException;
import com.prashant.exception.DMFrameLimitExcededException;

import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.TestCase;

public class DirectMemoryBufferTest extends TestCase {

    private MemoryBuffer buffer;

    @BeforeClass
    public void setUp() throws Exception {
        buffer = DirectMemoryBuffer.createNew(1024, 1);
    }

    /**
     * Test a simple store and retrieve.
     */
    @Test
    public void testStoreAndRetrieve() throws DMBufferOverFlowException, DMFrameLimitExcededException {
        String expected = "TestString";
        Pointer pointer = buffer.store("TestString".getBytes());
        String actual = new String(buffer.retrieve(pointer));
        assertEquals(expected, actual);
    }

    /**
     * Test if after removing a pointer do we get back our memory buffer for
     * reuse.
     */
    @Test
    public void testfree() throws DMBufferOverFlowException, DMFrameLimitExcededException {
        // Creating a buffer of limited capacity.
        MemoryBuffer tempBuffer = DirectMemoryBuffer.createNew(15, 2);
        String expected = "TestString";
        Pointer pointer = tempBuffer.store(expected.getBytes());
        String actual = new String(tempBuffer.retrieve(pointer));
        assertEquals(expected, actual);
        tempBuffer.free(pointer);
        // Now adding 2 different string keeping total length same+1.
        String expected1 = "Test";
        String expected2 = "String";
        Pointer p1 = tempBuffer.store(expected1.getBytes());
        Pointer p2 = tempBuffer.store(expected2.getBytes());
        String actual1 = new String(tempBuffer.retrieve(p1));
        String actual2 = new String(tempBuffer.retrieve(p2));
        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    @Test
    public void testClearTrivial() {
        buffer.clear();
        assertEquals(0, buffer.used());
    }

    @Test
    public void testUsedTrivial() throws DMBufferOverFlowException {
        MemoryBuffer tempBuffer = DirectMemoryBuffer.createNew(150, 4);
        tempBuffer.store("TestString".getBytes());
        assertEquals(10, tempBuffer.used());

        tempBuffer.store("TestString".getBytes());
        tempBuffer.store("TestString".getBytes());
        tempBuffer.store("TestString".getBytes());
        assertEquals(40, tempBuffer.used());

    }

    @Test
    public void testUsedNonTrivial() throws DMBufferOverFlowException {
        MemoryBuffer tempBuffer = DirectMemoryBuffer.createNew(330, 5);
        int expectedUsed = 0;
        tempBuffer.store("TestString".getBytes());
        expectedUsed += 10;
        assertEquals(expectedUsed, tempBuffer.used());
        for (int i = 0; i < 10; i++) {
            Pointer temp = tempBuffer.store("TestString2".getBytes());
            expectedUsed += 11;

            tempBuffer.free(temp);
            expectedUsed -= 11;

            tempBuffer.store("TestString3".getBytes());
            expectedUsed += 11;
        }
        assertEquals(expectedUsed, tempBuffer.used());
    }

    /**
     * Test storing multiple values.
     */
    @Test
    public void testMultipleStore() throws DMBufferOverFlowException, DMFrameLimitExcededException {
        String expected1 = "Test";
        String expected2 = "String";
        Pointer p1 = buffer.store(expected1.getBytes());
        Pointer p2 = buffer.store(expected2.getBytes());
        String actual1 = new String(buffer.retrieve(p1));
        String actual2 = new String(buffer.retrieve(p2));
        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    /**
     * Negative test to check handling of buffer Overflow, By trying to store
     * more than capacity.
     */
    @Test
    public void testBufferOverFlow() {

        MemoryBuffer tempBuffer = buffer = DirectMemoryBuffer.createNew(5, 3);
        boolean flagCaught = false;
        try {
            tempBuffer.store("TestString".getBytes());
        } catch (DMBufferOverFlowException e) {
            flagCaught = true;
        }
        assertTrue(flagCaught);
    }

    /**
     * Run the Pointer update(Pointer, byte[]) method test
     */
    @Test
    public void testUpdate() throws DMBufferOverFlowException, DMFrameLimitExcededException {
        String expected1 = "Test";
        String expected2 = "String";
        Pointer p1 = buffer.store(expected1.getBytes());
        // After adding
        String actual1 = new String(buffer.retrieve(p1));
        // After updating
        Pointer p2 = buffer.update(p1, expected2.getBytes());
        String actual2 = new String(buffer.retrieve(p2));
        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    /**
     * Run the Pointer update(Pointer, byte[],offset) method test
     */
    @Test
    public void testUpdate2() throws DMBufferOverFlowException, DMFrameLimitExcededException {
        /* Both string of same length should overwrite the previous position */
        String expected1 = "Testin";
        String expected2 = "String";
        Pointer p1 = buffer.store(expected1.getBytes());
        // After adding
        String actual1 = new String(buffer.retrieve(p1));
        // After updating
        buffer.update(p1, expected2.getBytes(), 0);
        String actual2 = new String(buffer.retrieve(p1));
        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }
}
