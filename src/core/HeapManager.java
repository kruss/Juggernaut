package core;

import util.Task;


import logger.Logger;
import logger.ILogConfig.Module;

/** provides access to the application's heap */
public class HeapManager implements ISystemComponent {

	private TaskManager taskManager;
	private Logger logger;
	private Runtime runtime;
	
	public HeapManager(TaskManager taskManager, Logger logger){
		
		this.taskManager = taskManager;
		this.logger = logger;
		runtime = Runtime.getRuntime();
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}

	public HeapStatus getHeapStatus(){
		
		HeapStatus status = new HeapStatus();
		status.usedMemory = (runtime.totalMemory() - runtime.freeMemory());
		status.freeMemory = runtime.freeMemory();
		status.totalMemory = runtime.totalMemory();
		status.maxMemory = runtime.maxMemory();
		return status;
	}
	
	public class HeapStatus {
		
		public long usedMemory; 		// bytes
		public long freeMemory; 		// bytes
		public long totalMemory; 		// bytes
		public long maxMemory; 			// bytes
		
		public HeapStatus(){
			usedMemory = 0;
			freeMemory = 0;
			totalMemory = 0;
			maxMemory = 0;
		}
		
		public HeapStatus(HeapStatus status){
			this.usedMemory = status.usedMemory;
			this.freeMemory = status.freeMemory;
			this.totalMemory = status.totalMemory;
			this.maxMemory = status.maxMemory;
		}
	}
	
	/** runs the garbage collector */
	public void cleanup(){
		
		GarbageCollector task = new GarbageCollector();
		task.asyncRun(0, GarbageCollector.TIMEOUT);
	}
	
	private class GarbageCollector extends Task {
		
		public static final long TIMEOUT = 10 * 60 * 1000; // 10 min
		
		public GarbageCollector() {
			super("GarbageCollector", taskManager);
		}

		@Override
		protected void runTask() {
			logger.log(Module.COMMON, "Running Garbage Collector");
			runtime.gc();
		}
	}
}
