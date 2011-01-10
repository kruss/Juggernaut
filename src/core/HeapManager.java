package core;

import java.util.ArrayList;

import util.IChangeListener;
import util.IChangeable;
import util.Task;

import logger.Logger;
import logger.ILogConfig.Module;

/** provides access to the application's heap */
public class HeapManager implements ISystemComponent, IChangeable {

	private TaskManager taskManager;
	private Logger logger;
	private Runtime runtime;
	private HeapStatus heap;
	private HeapStatusUpdater updater;
	private ArrayList<IChangeListener> listeners;

	public HeapManager(TaskManager taskManager, Logger logger) {

		this.taskManager = taskManager;
		this.logger = logger;
		runtime = Runtime.getRuntime();
		heap = new HeapStatus();
		listeners = new ArrayList<IChangeListener>();
	}

	@Override
	public void init() throws Exception {
		if (updater == null) {
			updater = new HeapStatusUpdater();
			updater.asyncRun(0, 0);
		}
	}

	@Override
	public void shutdown() throws Exception {
		if (updater != null) {
			updater.syncKill(1000);
			updater = null;
		}
	}

	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	public HeapStatus getHeapStatus() {
		synchronized (heap) {
			return new HeapStatus(heap);
		}
	}

	private void setHeapStatus(HeapStatus status) {
		synchronized (heap) {
			heap = new HeapStatus(status);
		}
		notifyListeners();
	}

	private class HeapStatusUpdater extends Task {

		public static final long CYCLE = 5 * 1000; // 5 sec

		public HeapStatusUpdater() {
			super("HeapStatus", taskManager);
			setCycle(CYCLE);
		}

		@Override
		protected void runTask() {
			HeapStatus status = new HeapStatus();
			status.usedMemory = (runtime.totalMemory() - runtime.freeMemory());
			status.freeMemory = runtime.freeMemory();
			status.totalMemory = runtime.totalMemory();
			status.maxMemory = runtime.maxMemory();
			setHeapStatus(status);
		}
	}

	public class HeapStatus {

		public long usedMemory; // bytes
		public long freeMemory; // bytes
		public long totalMemory; // bytes
		public long maxMemory; // bytes

		public HeapStatus() {
			usedMemory = 0;
			freeMemory = 0;
			totalMemory = 0;
			maxMemory = 0;
		}

		public HeapStatus(HeapStatus status) {
			this.usedMemory = status.usedMemory;
			this.freeMemory = status.freeMemory;
			this.totalMemory = status.totalMemory;
			this.maxMemory = status.maxMemory;
		}
	}

	/** runs the garbage collector */
	public void cleanup() {

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