package operation;

import java.io.File;

import util.CommandTask;
import lifecycle.LaunchAgent;
import lifecycle.PropertyContainer;
import lifecycle.StatusManager.Status;
import data.AbstractOperation;

public class CommandOperation extends AbstractOperation {

	private CommandOperationConfig config;
	
	public CommandOperation(LaunchAgent parent, CommandOperationConfig config) {
		super(parent, config);
		this.config = config;
	}

	@Override
	protected void execute() throws Exception {
		
		String command = PropertyContainer.expand(parent.getPropertyContainer(), config.getCommand());
		String arguments = PropertyContainer.expand(parent.getPropertyContainer(), config.getArguments());
		String directory = PropertyContainer.expand(parent.getPropertyContainer(), config.getDirectory());
		
		CommandTask commandTask = new CommandTask(
				command, 
				arguments,
				directory.isEmpty() ? parent.getFolder() : parent.getFolder()+File.separator+directory, 
				logger
		);
		commandTask.syncRun(0, 0);
		
		if(commandTask.hasSucceded()){
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
		
		//TODO provide the command-output as artifact
	}
}
