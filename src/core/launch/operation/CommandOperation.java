package core.launch.operation;

import java.io.File;
import java.util.ArrayList;

import core.launch.LaunchAgent;
import core.launch.data.Artifact;
import core.launch.data.StatusManager.Status;
import core.persistence.Cache;
import core.runtime.TaskManager;

import util.CommandTask;

public class CommandOperation extends AbstractOperation {

	private CommandOperationConfig config;
	
	public CommandOperation(
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent, 
			CommandOperationConfig config)
	{
		super(cache, taskManager, parent, config);
		this.config = (CommandOperationConfig) super.config;
	}

	@Override
	public String getRuntimeDescription() {
		return config.getCommand() + (!config.getArguments().isEmpty() ? " "+config.getArguments() : "");
	}
	
	@Override
	protected void execute() throws Exception {
		 
		CommandTask task = new CommandTask(
				config.getCommand(), 
				config.getArguments(),
				config.getDirectory().isEmpty() ? 
						parent.getFolder() : 
						parent.getFolder()+File.separator+config.getDirectory(), 
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
			handleOutput(output);
		}
		super.finish();
	}
}
