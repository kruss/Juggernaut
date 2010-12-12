package operation;

import java.io.File;
import java.util.ArrayList;

import core.Application;

import util.CommandTask;
import util.StringTools;
import util.SystemTools;
import launch.LaunchAgent;
import launch.PropertyContainer;
import launch.StatusManager.Status;
import logger.Logger.Level;
import logger.Logger.Module;
import data.AbstractOperation;

public class EclipseOperation extends AbstractOperation {

	private EclipseOperationConfig config;
	
	public EclipseOperation(LaunchAgent parent, EclipseOperationConfig config) {
		super(parent, config);
		this.config = config;
	}
	
	@Override
	public String getDescription() {
		return PropertyContainer.expand(parent.getPropertyContainer(), config.getEclipsePath());
	}

	@Override
	protected void execute() throws Exception {
		
		File eclipse = new File(
				PropertyContainer.expand(parent.getPropertyContainer(), config.getEclipsePath())
		);
		String command = null;
		String directory = null;
		if(eclipse.isFile()){
			command = eclipse.getName();
			if(SystemTools.isWindowsOS()){
				directory = eclipse.getParentFile().getAbsolutePath();
			}else{
				directory = parent.getFolder(); // TODO temp
			}
		}else{
			throw new Exception("invalid path: "+eclipse.getAbsolutePath());
		}
		
		ArrayList<String> arguments = new ArrayList<String>();
		arguments.add("-data \""+parent.getFolder()+"\"");
		arguments.add("-cdt.builder");
		if(Application.getInstance().getConfig().getLogLevel(Module.CMD) == Level.DEBUG){
			arguments.add("-cdt.verbose");
		}
		arguments.add("-cdt.import");
		if(config.isCleanBuild()){
			arguments.add("-cdt.clean");
		}
		for(String pattern : config.getBuildPattern()){
			int index = pattern.indexOf("|");
			if(index > 0 && index < pattern.length()-1){
				arguments.add("-cdt.build \""+pattern+"\"");
			}
		}
		for(String pattern : config.getExcludePattern()){
			int index = pattern.indexOf("|");
			if(index > 0 && index < pattern.length()-1){
				arguments.add("-cdt.exclude \""+pattern+"\"");
			}
		}
		
		CommandTask commandTask = new CommandTask(
				command, 
				StringTools.join(arguments, " "),
				directory, 
				logger
		);
		commandTask.syncRun(0, 0);
		
		if(commandTask.hasSucceded()){
			statusManager.setStatus(Status.SUCCEED);
		}else{
			statusManager.setStatus(Status.ERROR);
		}
		
		//TODO provide the command-output as artifact
		//TODO collect results from cdt-builder
	}
}
