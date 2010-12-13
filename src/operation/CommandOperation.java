package operation;

import java.io.File;

import util.CommandTask;
import launch.LaunchAgent;
import launch.PropertyContainer;
import launch.StatusManager.Status;
import data.AbstractOperation;
import data.Artifact;

public class CommandOperation extends AbstractOperation {

	private CommandOperationConfig config;
	
	public CommandOperation(LaunchAgent parent, CommandOperationConfig config) {
		super(parent, config);
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
				logger
		);
		task.syncRun(0, 0);
		
		if(task.hasSucceded()){
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
		
		if(!task.getOutput().isEmpty()){
			artifacts.add(new Artifact("Output", task.getOutput()));
		}
	}
}
