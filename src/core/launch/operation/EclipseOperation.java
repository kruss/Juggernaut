package core.launch.operation;

import java.io.File;
import java.util.ArrayList;

import core.launch.LaunchAgent;
import core.launch.data.Artifact;
import core.launch.data.StatusManager.Status;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.TaskManager;
import core.runtime.logger.ILogConfig.Level;
import core.runtime.logger.ILogConfig.Module;

import util.CommandTask;
import util.StringTools;
import util.SystemTools;


public class EclipseOperation extends AbstractOperation {

	private EclipseOperationConfig config;
	
	public EclipseOperation(
			Configuration configuration, 
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent, 
			EclipseOperationConfig config)
	{
		super(configuration, cache, taskManager, parent, config);
		this.config = (EclipseOperationConfig) super.config;
	}
	
	@Override
	public String getDescription() {
		return StringTools.join(config.getBuildPattern(), ", ");
	}

	@Override
	protected void execute() throws Exception {
		
		File eclipse = new File(config.getEclipsePath());
		String command = null;
		String directory = null;
		
		if(eclipse.isFile()){
			command = eclipse.getName();
			if(SystemTools.isWindowsOS()){
				directory = eclipse.getParentFile().getAbsolutePath();
			}else{
				directory = parent.getFolder(); // TODO make working on linux
			}
		}else{
			throw new Exception("invalid path: "+eclipse.getAbsolutePath());
		}
		
		ArrayList<String> arguments = getArguments();
		CommandTask task = new CommandTask(
				command, 
				StringTools.join(arguments, " "),
				directory, 
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
			//TODO evaluate results from cdt-builder
		}
	}

	private ArrayList<String> getArguments() {
		
		ArrayList<String> arguments = new ArrayList<String>();
		// eclipse args
		arguments.add("-data \""+parent.getFolder()+"\"");
		arguments.add("-nosplash");
		arguments.add("-showlocation");
		// cdt-builder args
		arguments.add("-cdt.builder");
		arguments.add("-cdt.import");
		arguments.add("-cdt.refresh");
		if(config.isCleanBuild()){
			arguments.add("-cdt.clean");
		}
		if(!config.isStrictBuild()){
			arguments.add("-cdt.tolerant");
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
		if(configuration.getLogConfig().getLogLevel(Module.COMMAND) == Level.DEBUG){
			arguments.add("-cdt.verbose");
		}
		// vm args
		int heap = config.getHeapSize();
		arguments.add("-vmargs -Xms"+heap+"M -Xmx"+heap+"M");
		return arguments;
	}
	
	@Override
	protected void finish() {		
		handleOutput(".build");
		super.finish();
	}
}
