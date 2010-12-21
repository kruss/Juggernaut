package core;

import java.util.ArrayList;

import logger.Logger.Module;

import util.Task;

/**
 * The task-manager provides timeout control for tasks.
 */
public class TaskManager extends Task {

	private static final long CYCLE = 15 * 1000; // 15 sec
	
	private ArrayList<Task> tasks;
	
	public TaskManager(){
		super("TaskManager", Application.getInstance().getLogger());
		tasks = new ArrayList<Task>();
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
	
	public void init() {
		
		setCycle(CYCLE);
		asyncRun(0, 0);
	}
	
	public void shutdown() {

		syncKill();
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

	@Override
	protected void runTask() {
		checkTimeout();
	}

	/** check for timeouts in registered tasks */
	private void checkTimeout() {
		
		synchronized(tasks){
			for(int i=tasks.size()-1; i>=0; i--){
				Task task = tasks.get(i);
				if(task != null && task.isExpired()){
						tasks.remove(task);
						task.getObserver().log(Module.COMMON, "Task Timeout ["+task.getName()+"]");
						task.asyncKill();
				}
			}
		}
	}
}
