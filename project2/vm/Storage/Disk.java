/* $Id: Disk.java,v 1.17 2006/11/09 20:42:29 solomon Exp $ */
/* Modified by Dr. Jun Yuan, 10/08/2020*/
/*DO NOT CHANGE THE FILE*/
package Storage;
import java.io.*;
import static java.lang.System.*;

/** A software simulation of a Disk.
 * You may not change this class.
 * This disk is very slow
 * It contains a number of blocks, all BLOCK_SIZE bytes long.
 * All operations occur on individual blocks.
 * You can't modify any more or any less data at a time.
 * To read or write from the disk, call beginRead() or beginWrite().
 * In a real OS, each of these functions will start the action and
 * return immediately. Only when the action has been completed, the Disk
 * notifies let you know the Disk is ready for more.
 * But in this project, because we haven't learned concurrency, so we
 * will just do sync read/write.
 * It may take a while for the disk to seek from one block to another.
 * Seek time is proportional to the difference in block numbers of the
 * blocks.
 * This disk saves its contents in the Unix file DISK between runs.
 * Since the file can be large, you should get in the habit of removing it
 * before logging off.
 *
 */
 class Disk  {
    /////////////////////////////////////////// Disk geometry parameters

    /** The size of a disk block in bytes. */
    public static final int BLOCK_SIZE = 64;

    /** Total size of this disk, in blocks. */
    public int diskSIZE;

    /////////////////////////////////////////// Transient internal state

    /** Current location of the read/write head. */
    protected int currentBlock = 0;

    /** The data stored on the disk. */
    protected byte[] data;

    /** Memory buffer to/from which current I/O operation is transferring.
     * Only meaningful if busy == true.
     */
    private byte[] buffer;

    /** A count of read operations performed, for statistics. */
    protected int readCount;

    /** A count of write operations performed, for statistics. */
    protected int writeCount;

    /** toggle on/off the delay */
    private boolean delay = false;
    /////////////////////////////////////////// Inner classes

    /** The exception thrown when an illegal operation is attempted on the
     * disk.
     */
    static protected class DiskException extends RuntimeException {
        static final long serialVersionUID = 0;
        /** Creates a new exception.
         *
         * @param s the detail string for the exception.
         */
        public DiskException(String s) {
            super("*** YOU CRASHED THE DISK: " + s);
        }
    }

    /////////////////////////////////////////// Constructors

    /** Creates a new Disk.
     * If a Unix file named DISK exists in the local Unix directory, the
     * simulated disk contents are initialized from the Unix file.
     * It is an error if the DISK file exists but its size does not match
     * "size".
     * If there is no DISK file, the first block of the simulated disk is
     * cleared to nulls and the rest is filled with random junk.
     *
     * @param size the total size of this disk, in blocks.
     */
    public Disk(int size) {
        this.diskSIZE = size;
        File diskName = new File("DISK");
        if (diskName.exists()) {
            if (diskName.length() != size * BLOCK_SIZE) {
                throw new DiskException(
                        "File DISK exists but is the wrong size");
            }
        }
        if (size < 1) {
            throw new DiskException("A disk must have at least one block!");
        }
        // NOTE:  the "new" operator always clears the result object to nulls
        data = new byte[diskSIZE * BLOCK_SIZE];
        int count = BLOCK_SIZE*diskSIZE;
        try {
            FileInputStream is = new FileInputStream("DISK");
            is.read(data);
            out.printf("Restored %d bytes from file DISK\n", count);
            is.close();
            return;
        } catch (FileNotFoundException e) {
            out.println("Creating new disk");
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
        byte[] junk = new byte[BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; ) {
            junk[i++] = 74;
            junk[i++] = 85;
            junk[i++] = 78;
            junk[i++] = 75;
        }
        for (int i = 1; i < diskSIZE; i++) {
            arraycopy(
                    junk, 0,
                    data, i * BLOCK_SIZE,
                    BLOCK_SIZE);
        }
    } // Disk(int)

    public Disk() {
        this(128*1024/BLOCK_SIZE);//default 128k
    }
    /////////////////////////////////////////// Methods
    /*
    * enable the delay
    * */
    public void enable_delay() {
        this.delay = true;
    }

    /*
    * disable the delay
    * */
    public void disable_delay() {
        this.delay = false;
    }

    /** Saves the contents of this Disk.
     * The contents of this disk will be forced out to a file named
     * DISK so that they can be restored on the next run of this program.
     * This file could be quite big, so delete it before you log out.
     * Also prints some statistics on disk operations.
     */
    public void flush() {
        try {
            out.println("Saving contents to DISK file...");
            FileOutputStream os = new FileOutputStream("DISK");
            os.write(data);
            os.close();
            out.printf(
                    "%d read operations and %d write operations performed\n",
                    readCount, writeCount);
        } catch(Exception e) {
            e.printStackTrace();
            exit(1);
        }
    } // flush()

    /** Sleeps for a while to simulate the delay in seeking and transferring
     * data.
     * @param targetBlock the block number to which we have to seek.
     */
    private void delay(int targetBlock) {
        if(this.delay == false) return;
        int sleepTime = 10 + Math.abs(targetBlock - currentBlock) / 5;
        try {
            Thread.sleep(sleepTime*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read(int blockNumber, byte buffer[]) {
        delay(blockNumber);
        System.arraycopy(
                data, blockNumber * BLOCK_SIZE,
                buffer, 0,
                BLOCK_SIZE);
        readCount++;
        this.currentBlock = blockNumber;
    }

    public void write(int blockNumber, byte buffer[]) {
        delay(blockNumber);
        System.arraycopy(
                buffer, 0,
                data, blockNumber * BLOCK_SIZE,
                BLOCK_SIZE);
        writeCount++;
        this.currentBlock = blockNumber;
    }

} // Disk
