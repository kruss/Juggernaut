package operation;

import java.io.File;

import util.CommandTask;
import lifecycle.LaunchAgent;
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
		
		String command = parent.getPropertyManager().expand(config.getCommand());
		String arguments = parent.getPropertyManager().expand(config.getArguments());
		String directory = parent.getPropertyManager().expand(config.getDirectory());
		
		CommandTask commandTask = new CommandTask(
				command, 
				arguments,
				directory.isEmpty() ? parent.getFolder() : parent.getFolder()+File.separator+directory, 
				logger
		);
		commandTask.syncRun(0);
		
		if(commandTask.hasSucceded()){
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
		
		//TODO provide the command-output as artifact
	}
}
