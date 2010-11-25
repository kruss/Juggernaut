package operation;

import java.io.File;
import java.util.ArrayList;

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
		ArrayList<String> arguments = new ArrayList<String>();
		for(String argument : config.getArguments()){
			arguments.add(parent.getPropertyManager().expand(argument));
		}
		String directory = parent.getPropertyManager().expand(config.getDirectory());
		
		CommandTask commandTask = new CommandTask(
				command, 
				arguments,
				parent.getFolder()+File.separator+directory, 
				logger
		);
		commandTask.syncRun(0);
		
		if(commandTask.hasSucceded()){
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
	}
}
