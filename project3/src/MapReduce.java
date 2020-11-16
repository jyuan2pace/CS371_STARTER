import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MapReduce {
	// for final output		
	protected PrintWriter pw;
	protected Lock pwLock;
	protected StopWatch stopWatch;
	protected Logger LOGGER;

	abstract void MREmit(Object key, Object value);

	public Object MRGetNext(Object key, int partition_number) {
		throw new UnsupportedOperationException();
	}
	
	public int MRRun(String inputFileName,
		    		  MapperReducerClientAPI mapperReducerObj,
		    		  int num_mappers, 
		    		  int num_reducers)
	{	
		setup(num_mappers, inputFileName);
		MRRunHelper(inputFileName, mapperReducerObj,num_mappers, num_reducers);
		return teardown(inputFileName);
	}

	abstract protected void MRRunHelper(String inputFileName,
							   MapperReducerClientAPI mapperReducerObj,
							   int num_mappers,
							   int num_reducers) ;

	public void MRPostProcess(String key, int value) {
		pwLock.lock();
		pw.printf("%s:%d\n", (String)key, value); 
		pwLock.unlock();
	}
	protected void setup (int nSplits, String inputFile) {
		try {
			pwLock = new ReentrantLock();
			stopWatch = new StopWatch();
			LOGGER = Logger.getLogger(MyMapReduce.class.getName());
			//split input into as many as nSplits files.
			Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh" , "-c", "./split.sh "+inputFile +" " +nSplits});
			p.waitFor();
			int exitVal = p.exitValue();
			assert(exitVal == 0);
			pw = new PrintWriter(new FileWriter("res/out.txt"));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString());
		}
	}
	protected int teardown(String inputFile) {
		pw.close();
        try {
			Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh" , "-c", "./test.sh "+inputFile});
			p.waitFor();
			int exitVal = p.exitValue();
	        if(exitVal == 0) {
	        	LOGGER.log(Level.INFO, "PASSED");
	        } else {
		        LOGGER.log(Level.INFO, "FAILED, process exit value = {0}", exitVal);
	        }
			return exitVal;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString());
			return -1;
        }
	}
	
	protected class StopWatch{
		 private long startTime;
		    public StopWatch() {
		        startTime = System.currentTimeMillis();
		    }
		    public double getElapsedTime() {
		        long endTime = System.currentTimeMillis();
		        return (double) (endTime - startTime) / (1000);
		    }
	}
}