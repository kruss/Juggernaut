package core.runtime;

import java.util.ArrayList;

import core.ISystemComponent;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;



import util.IChangeable;
import util.IChangeListener;
import util.Task;
import util.Task.State;

/**
 * The task-manager provides timeout control for tasks.
 */
public class TaskManager implements ISystemComponent, IChangeable, IChangeListener {

	private Logger logger;
	private TimeoutTask timeout;
	private ArrayList<Task> tasks;
	private ArrayList<IChangeListener> listeners;
	
	public TaskManager(Logger logger){
		
		this.logger = logger;
		timeout = new TimeoutTask(this);
		tasks = new ArrayList<Task>();
		listeners = new ArrayList<IChangeListener>();
	}
	
	@Override
	public void init() throws Exception {
		
		timeout.asyncRun(0, 0);
	}
	
	@Override
	public void shutdown() throws Exception {

		timeout.syncStop(1000);
		synchronized(tasks){
			for(int i=tasks.size()-1; i>=0; i--){
				Task task = tasks.get(i);
				task.syncStop(1000);
				tasks.remove(task);
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
	
	public void log(String text) {
		logger.debug(Module.TASK, text);
	}
	
	public void error(Exception e) {
		logger.error(Module.TASK, e);
	}
	
	/** register a task for timeout control */
	public void register(Task task) {
		
		synchronized(tasks){;
			tasks.add(task);
			task.addListener(this);
			notifyListeners();
		}
	}

	/** deregister a task for timeout control */
	public void deregister(Task task) {
		
		synchronized(tasks){
			for(Task entry : tasks){
				if(entry == task){
					entry.removeListener(this);
					tasks.remove(entry);
					notifyListeners();
					break;
				}
			}
		}
	}
	
	/** check for timeouts in registered tasks */
	private void checkTimeouts() {
		
		synchronized(tasks){
			for(int i=tasks.size()-1; i>=0; i--){
				Task task = tasks.get(i);
				if(task.isExpired()){
						logger.log(Module.TASK, "Timeout dedected for Task ["+task.getTaskIdentifier()+"]");
						task.asyncStop(1000);
						tasks.remove(task);
						notifyListeners();
				}
			}
		}
	}
	
	public void kill(long id) {
		
		for(Task task : tasks){
			if(task.getTaskId() == id){
				logger.log(Module.TASK, "Killing Task ["+task.getTaskIdentifier()+"]");
				task.asyncStop(1000);
				tasks.remove(task);
				notifyListeners();
				break;
			}
		}
	}
	
	public ArrayList<TaskInfo> getInfo(){
		
		ArrayList<TaskInfo> info = new ArrayList<TaskInfo>();
		synchronized(tasks){
			for(Task task : tasks){
				info.add(new TaskInfo(task));
			}
		}
		return info;
	}
	
	public class TaskInfo {
		
		public long id;
		public String name;
		public State state;
		
		public TaskInfo(Task task){
			id = task.getTaskId();
			name = task.getTaskName();
			state = task.getState();
		}
	}
	
	private class TimeoutTask extends Task {
		public static final long CYCLE = 10 * 1000; // 10 sec
		
		public TimeoutTask(TaskManager taskManager) {
			super("Timeout", taskManager);
			setCyclic(CYCLE);
		}

		@Override
		protected void runTask() {
			checkTimeouts();
		}
	}

	@Override
	public void changed(Object object) {
		
		if(object instanceof Task){
			update((Task) object);
		}
	}
	
	private void update(Task task) {
		
		synchronized(tasks){
			for(Task entry : tasks){
				if(entry == task){
					notifyListeners();
					break;
				}
			}
		}
	}
}
