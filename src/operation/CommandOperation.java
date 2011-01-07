package operation;

import java.io.File;
import java.util.ArrayList;

import core.Cache;
import core.Configuration;
import core.TaskManager;

import util.CommandTask;
import launch.LaunchAgent;
import launch.PropertyContainer;
import launch.StatusManager.Status;
import data.AbstractOperation;
import data.Artifact;

public class CommandOperation extends AbstractOperation {

	private CommandOperationConfig config;
	
	public CommandOperation(Configuration configuration, Cache cache, TaskManager taskManager, LaunchAgent parent, CommandOperationConfig config) {
		super(configuration, cache, taskManager, parent, config);
		this.config = config;
	}

	@Override
	public String getDescription() {
		return PropertyContainer.expand(parent.getPropertyContainer(), config.getCommand());
	}
	
	@Override
	protected void execute() throws Exception {
		
		String command = PropertyContainer.expand(parent.getPropertyContainer(), config.getCommand());
		String arguments = PropertyContainer.expand(parent.getPropertyContainer(), config.getArguments());
		String directory = PropertyContainer.expand(parent.getPropertyContainer(), config.getDirectory());
		
		CommandTask task = new CommandTask(
				command, 
				arguments,
				directory.isEmpty() ? parent.getFolder() : parent.getFolder()+File.separator+directory, 
				taskManager,
				logger
		);
		try{
			task.syncRun(0, 0);
		}finally{
			if(task.hasSucceded()){
				statusManager.setStatus(Status.SUCCEED);
			}else{
				statusManager.setStatus(Status.ERROR);
			}
			if(!task.getOutput().isEmpty()){
				artifacts.add(new Artifact("Command", task.getOutput(), "txt"));
			}
		}
	}
	
	@Override
	protected void finish() {
		ArrayList<String> outputs = config.getOutputs();
		for(String output : outputs){
			collectOuttput(output);
		}
		super.finish();
	}
}
