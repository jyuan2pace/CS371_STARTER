import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class VirtMemoryTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Test
    public void testWriteBackToSameBlock() {
    //every 32 writes triggers a write-back to disk.
        Memory m = new VirtMemory();
        m.startup();
        for(int i=0; i<32; i++) {
            m.write(i, Byte.parseByte("-1"));
        }
        m.shutdown();
        //same block, so there should be only one write
        int writeCount = m.getPhyMemory().writeCountDisk();
        int readCount = m.getPhyMemory().readCountDisk();
        assertEquals(1, writeCount);
        assertEquals(1, readCount);
    }

    @Test
    public void testWriteBackToMultiBlocks() {
        //every 32 writes triggers a write-back to disk.
        Memory m = new VirtMemory();
        m.startup();
        for(int i=0; i<32; i++) {
            m.write(i*64, Byte.parseByte("-1"));
        }

        m.shutdown();
        int writeCount = m.getPhyMemory().writeCountDisk();
        int readCount = m.getPhyMemory().readCountDisk();
        //dirtied 32 blocks, so there should be 32 writes counted on disk
        assertEquals(32, writeCount);
        //32 blocks loaded in because of poor locality
        assertEquals(32, readCount);
    }

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    @Test
    public void testOutOfRange() {
        Memory m = new VirtMemory();
        m.startup();
        m.write(0xFFFFFF, Byte.parseByte("-1"));
        assertNotEquals(0, errContent.toString().length());
        byte x = m.read(0xFFFFFF);
        assertNotEquals(0,errContent.toString().length());
        m.shutdown();
    }
    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    @Test
    public void testSingleWrite() {
        Memory m = new VirtMemory();
        m.startup();
        m.write(0x8000, Byte.parseByte("-1")); //write it to somewhere way beyond 16K
        m.shutdown();
        //now the disk should have persisted your write, reboot
        m = new VirtMemory();
        m.startup();
        byte data = m.read(0x8000);
        m.shutdown();
        assertEquals(Byte.parseByte("-1"), data);
    }

    //the following are more releastic workloads
    static final int TEST_SIZE = 64*1024;// 64K, test on max address space!
    static byte fce(int adr) {
        return (byte) ((adr * 5 + 6) % 256 - 128);
    }
    static byte fce2(int adr) {
        return (byte) ((adr * 7 + 5) % 256 - 128);
    }
    @Test
    public void testEnd2EndForward() {
        Memory m = new VirtMemory();
        m.startup();
        boolean result = true;
        for (int i = 0; i < TEST_SIZE; i++)
            m.write(i, fce(i));
        for (int i = 0; i < TEST_SIZE; i++)
            if (m.read(i) != fce(i))
                result = false;
        assertEquals(true, result);
        m.shutdown();
        assertEquals(2048, m.getPhyMemory().writeCountDisk());
        assertEquals(2048, m.getPhyMemory().readCountDisk());

    }
    @Test
    public void TestEnd2EndBackward() {
        Memory m = new VirtMemory();
        m.startup();
        boolean result = true;
        for (int i = 0; i < TEST_SIZE; i++)
            m.write(i, fce(i));
        for (int i = TEST_SIZE-1; i >= 0; i--)
            if (m.read(i) != fce(i))
                result = false;
        assertEquals(true, result);
        m.shutdown();
        assertEquals(2048, m.getPhyMemory().writeCountDisk());
        assertEquals(1792, m.getPhyMemory().readCountDisk());
    }
    @Test
    public void TestEnd2EndMix() {
       Memory m = new VirtMemory();
        m.startup();
        boolean result = true;
        for (int i = TEST_SIZE-1; i >= 0; i--)
            m.write(i, fce(i));
        for (int posun = 0; posun < TEST_SIZE; posun += 100) {
            for (int i = 0; i < TEST_SIZE; i++) {
                int adr = (i+posun)%TEST_SIZE;
                if (m.read(adr) != fce(adr))
                    result = false;
            }
        }
        int posun_zapis=55;
        for (int i = 0; i < TEST_SIZE; i++) {
            int adr = (i+posun_zapis)%TEST_SIZE;
            m.write(adr, fce2(adr));
        }
        for (int posun = 20; posun < TEST_SIZE; posun += 100) {
            for (int i = 0; i < TEST_SIZE; i++) {
                int adr = (i+posun)%TEST_SIZE;
                if (m.read(adr) != fce2(adr))
                    result = false;
            }
        }
        assertEquals(true, result);
        m.shutdown();
    }

}
