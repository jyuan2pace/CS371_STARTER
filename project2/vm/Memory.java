import java.util.TimerTask;
import Storage.PhyMemory;
public abstract class Memory {
    protected PhyMemory ram; //inherited and accessible to subclass
    /*
     * The constructor creates memory with specified size mem_size and a prepared disk
     */
    public Memory(int memsize, PhyMemory ram) {
        this.ram = ram;
    }

    public Memory(){
    }

    protected PhyMemory getPhyMemory(){
        return ram;
    }
    /*
     * writes specified value into memory at specified position - addr
     */
    abstract public void write(int addr, byte value);

    /*
     * returns value that was stored at address addr
     */
    abstract public byte read(int addr);
    /*
     * write back dirty pages periodically
     */
    abstract void write_back();

    /*
     * start the background thread to write back dirty pages
     * at the interval of frequency.
     */
    void startup() {
    //func stub for future
    }

    void shutdown() {
        write_back();
        ram.flush();
    }
}
