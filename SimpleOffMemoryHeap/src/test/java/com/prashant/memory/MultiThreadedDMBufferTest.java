package com.prashant.memory;

import com.prashant.exception.DMBufferOverFlowException;
import com.prashant.exception.DMFrameLimitExcededException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.*;
import org.junit.Test;

public class MultiThreadedDMBufferTest extends TestCase {

    private MemoryBuffer buffer;
    private static final Logger logger = Logger.getAnonymousLogger();

    private final static int THREADS = 2;
    Thread[] thread = new Thread[THREADS];

    public MultiThreadedDMBufferTest(String testName) {
        super(testName);
        buffer = DirectMemoryBuffer.createNew(102400, 1);
    }

//    public static Test suite() {
//        TestSuite suite = new TestSuite(MultiThreadedDMBufferTest.class);
//
//        return suite;
//    }

    /**
     * Stress Test.
     */
    public void stressTestStoreRetrieveAndFree() throws DMBufferOverFlowException, DMFrameLimitExcededException {
        List<Pointer> listOfPointers = new ArrayList<Pointer>();
        for (int j = 0; j < 200; j++) {
            for (int i = 0; i < 10; i++) {
                String expected = "T";
                Pointer pointer = buffer.store("T".getBytes());
                listOfPointers.add(pointer);
                String actual = new String(buffer.retrieve(pointer));
                if (!expected.equalsIgnoreCase(actual)) {
                    logger.fine("Failed to match" + expected + ":" + actual);
                    fail();
                }
            }
            for (Pointer pointer2free : listOfPointers) {
                buffer.free(pointer2free);
            }
        }
    }

    class StoreThread extends Thread {

        StoreThread() {
        }

        public void run() {
            try {
                stressTestStoreRetrieveAndFree();
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }
    }

    @Test
    public void testParallelStore() throws Throwable {
        System.out.println("parallel add");
        try {
            for (int i = 0; i < THREADS; i++) {
                thread[i] = new StoreThread();
            }
            for (int i = 0; i < THREADS; i++) {
                thread[i].start();
            }
            for (int i = 0; i < THREADS; i++) {
                thread[i].join();
            }
        } catch (Throwable t) {
            throw t;
        }
    }

}
