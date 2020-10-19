/*DO NOT CHANGE THE FILE*/
package Storage;
import java.util.*;

public class PhyMemory {
    private Disk disk;
    private int addrSpaceSize = 16*1024; //16K default
    private static final int FRAME_SIZE = 64; //must be the same as page size
    private byte[] data;
    private boolean delay =  false;
    //Constructor of physical memory
    public PhyMemory(int addrSpaceSize, Disk disk) {
        this.addrSpaceSize = addrSpaceSize;
        assert(this.addrSpaceSize % FRAME_SIZE == 0);
        this.disk = disk;
        data = new byte[this.addrSpaceSize];
        Arrays.fill(data, (byte) 0);
    }

    //Default constructor of physical memory
    //by default disk is 128KB, physical memory is 16K and virtual memory is 64K
    public PhyMemory() { //default memory size default disk size
        this(16*1024,new Disk());
    }
    //enable the delay
    public void enable_delay() {
        this.delay =  true;
    }
    //disable the delay
    public void disable_delay() {
        this.delay = false;
    }
    //in real simulation, there is a delay for a read/write to complete in physical
    //memory. This delay is order of magnitude smaller than the delay in disk.
    private void delay() {
        if(this.delay == false) return;
        int sleepTime = 1;
        try {
            Thread.sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //read a page at given physical address
    public byte read(int pAddr) {
        delay();
        return data[pAddr];
    }

    //write a page at given physical address
    public void write(int pAddr, byte b) {
        delay();
        data[pAddr] = b;
    }

    //write a page worth data from StartAddr of physical memory
    //to disk starting at blockNum
    public void store(int blockNum, int startAddr) {
        byte[] buffer = new byte[FRAME_SIZE]; //for simplicity FRAME_SIZE=BLOCK_SIZE
        System.arraycopy(data, startAddr, buffer,0,FRAME_SIZE);
        disk.write(blockNum, buffer);
    }
    //read a page worth data from disk starting at blockNum to
    //the startAddr of physical memory
    public void load(int blockNum, int startAddr) {
        byte[] buffer = new byte[FRAME_SIZE];
        disk.read(blockNum, buffer);
        System.arraycopy(buffer,0, data, startAddr, FRAME_SIZE);
    }
    //returns how many physical frames are there.
    public int num_frames() {
        return this.addrSpaceSize /FRAME_SIZE;
    }

    //only used in the shutdown of memory to notify disk
    public void flush() {
        disk.flush();
    }

    //collect stats of read/count on disk
    public int writeCountDisk() {
        return disk.writeCount;
    }
    //collect stats of read/count on disk
    public int readCountDisk() {
        return disk.readCount;
    }

}
