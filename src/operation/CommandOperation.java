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
		
		CommandTask command = new CommandTask(
				config.getCommand(), config.getArguments(),
				parent.getOutputFolder()+File.separator+config.getDirectory(), 
				logger
		);
		command.syncRun(0);
		
		if(command.hasSucceded()){
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
	}
}
