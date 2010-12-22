package core;

import java.util.ArrayList;

import logger.Logger;
import logger.Logger.Module;

import util.Task;

/**
 * The task-manager provides timeout control for tasks.
 */
public class TaskManager implements ISystemComponent {

	
	private Logger logger;
	private TaskManagerTask timeout;
	private ArrayList<Task> tasks;
	
	public TaskManager(Logger logger){
		
		this.logger = logger;
		timeout = new TaskManagerTask(this);
		tasks = new ArrayList<Task>();
	}
	
	@Override
	public void init() throws Exception {
		
		timeout.asyncRun(0, 0);
	}
	
	@Override
	public void shutdown() throws Exception {

		timeout.syncKill();
		synchronized(tasks){
			for(int i=tasks.size()-1; i>=0; i--){
				Task task = tasks.get(i);
				if(task != null){
					task.syncKill();
				}
			}
		}
		tasks.clear();
	}
	
	/** register a task for timeout control */
	public void register(Task task) {
		
		synchronized(tasks){
			tasks.add(task);
		}
	}

	/** deregister a task for timeout control */
	public void deregister(Task task) {
		
		synchronized(tasks){
			tasks.remove(task);
		}
	}
	
	/** check for timeouts in registered tasks */
	private void checkTimeouts() {
		
		synchronized(tasks){
			for(int i=tasks.size()-1; i>=0; i--){
				Task task = tasks.get(i);
				if(task != null && task.isExpired()){
						tasks.remove(task);
						logger.log(Module.TASK, "Task Timeout ["+task.getName()+"]");
						task.asyncKill();
				}
			}
		}
	}
	
	public void debug(String text) {
		logger.debug(Module.TASK, text);
	}
	
	private class TaskManagerTask extends Task {

		public static final long CYCLE = 15 * 1000; // 15 sec
		
		public TaskManagerTask(TaskManager taskManager) {
			super("TaskManager", taskManager);
			setCycle(CYCLE);
		}

		@Override
		protected void runTask() {
			checkTimeouts();
		}
	}
}
