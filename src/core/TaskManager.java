package core;

import java.util.ArrayList;


import logger.Logger;
import logger.ILogConfig.Module;

import util.IChangeable;
import util.IChangeListener;
import util.Task;

/**
 * The task-manager provides timeout control for tasks.
 */
public class TaskManager implements ISystemComponent, IChangeable {

	private Logger logger;
	private TimeoutTask timeout;
	private ArrayList<RegisteredTask> entries;
	private ArrayList<IChangeListener> listeners;
	
	public TaskManager(Logger logger){
		
		this.logger = logger;
		timeout = new TimeoutTask(this);
		entries = new ArrayList<RegisteredTask>();
		listeners = new ArrayList<IChangeListener>();
	}
	
	@Override
	public void init() throws Exception {
		
		timeout.asyncRun(0, 0);
	}
	
	@Override
	public void shutdown() throws Exception {

		timeout.syncKill(1000);
		synchronized(entries){
			for(int i=entries.size()-1; i>=0; i--){
				RegisteredTask entry = entries.get(i);
				entry.task.syncKill(1000);
				entries.remove(entry);
			}
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
	
	public void debug(String text) {
		logger.debug(Module.TASK, text);
	}
	
	public void error(Exception e) {
		logger.error(Module.TASK, e);
	}
	
	/** register a task for timeout control */
	public void register(Task task) {
		
		synchronized(entries){
			RegisteredTask entry = new RegisteredTask(task);
			entries.add(entry);
			notifyListeners();
		}
	}
	
	/** set state for a registered task */
	public void status(Task task, boolean running) {
		
		synchronized(entries){
			for(RegisteredTask entry : entries){
				if(entry.task == task){
					entry.running = running;
					notifyListeners();
					break;
				}
			}
		}
	}

	/** deregister a task for timeout control */
	public void deregister(Task task) {
		
		synchronized(entries){
			for(int i=entries.size()-1; i>=0; i--){
				RegisteredTask entry = entries.get(i);
				if(entry.task == task){
					entries.remove(entry);
					notifyListeners();
					break;
				}
			}
		}
	}
	
	/** check for timeouts in registered tasks */
	private void checkTimeouts() {
		
		synchronized(entries){
			for(int i=entries.size()-1; i>=0; i--){
				RegisteredTask entry = entries.get(i);
				if(entry.task.isExpired()){
						logger.log(Module.TASK, "Task Timeout ["+entry.task.getName()+"]");
						entry.task.asyncKill(1000);
						entries.remove(entry);
						notifyListeners();
				}
			}
		}
	}
	
	public ArrayList<TaskInfo> getInfo(){
		
		ArrayList<TaskInfo> info = new ArrayList<TaskInfo>();
		synchronized(entries){
			for(RegisteredTask entry : entries){
				info.add(new TaskInfo(entry));
			}
		}
		return info;
	}
	
	private class RegisteredTask {
		public Task task;
		public boolean running;
		
		public RegisteredTask(Task task){
			this.task = task;
			running = false;
		}
	}
	
	public class TaskInfo {
		public String name;
		public boolean running;
		
		public TaskInfo(RegisteredTask entry){
			name = entry.task.getName();
			running = entry.running;
		}
	}
	
	private class TimeoutTask extends Task {
		public static final long CYCLE = 15 * 1000; // 15 sec
		
		public TimeoutTask(TaskManager taskManager) {
			super("Timeout", taskManager);
			setCycle(CYCLE);
		}

		@Override
		protected void runTask() {
			checkTimeouts();
		}
	}
}
