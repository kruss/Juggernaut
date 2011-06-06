package core.launch.operation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import core.launch.LaunchAgent;
import core.launch.data.Artifact;
import core.launch.data.StatusManager.Status;
import core.persistence.Cache;
import core.runtime.TaskManager;
import core.runtime.logger.ILogConfig.Level;
import core.runtime.logger.ILogConfig.Module;

import util.CommandTask;
import util.StringTools;
import util.SystemTools;


public class EclipseOperation extends AbstractOperation {
	
	private EclipseOperationConfig config;
	
	public EclipseOperation(
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent, 
			EclipseOperationConfig config)
	{
		super(cache, taskManager, parent, config);
		this.config = (EclipseOperationConfig) super.config;
	}
	
	@Override
	public String getRuntimeDescription() {
		return StringTools.join(config.getBuildPattern(), ", ");
	}

	@Override
	protected void execute() throws Exception {
		
		File eclipse = new File(config.getEclipsePath());
		String command = null;
		String directory = null;
		HashMap<String, String> environment = null;
		
		if(eclipse.isFile()){
			command = eclipse.getName();
			if(SystemTools.isWindowsOS()){
				directory = eclipse.getParentFile().getAbsolutePath();
			}else{
				directory = parent.getFolder();
			}
		}else{
			throw new Exception("invalid path: "+eclipse.getAbsolutePath());
		}
		
		String compiler = config.getCompilerPath();
		if(compiler != null && !compiler.isEmpty()){
			environment = new HashMap<String, String>();
			environment.put("PATH", compiler+";"+System.getenv("PATH"));
		}
		
		ArrayList<String> arguments = getArguments();
		CommandTask task = new CommandTask(
				command, 
				StringTools.join(arguments, " "),
				directory, 
				environment,
				taskManager,
				logger
		);
		
		try{
			task.syncRun(0, timeout);
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

	private ArrayList<String> getArguments() throws Exception {
		
		ArrayList<String> arguments = new ArrayList<String>();
		// eclipse args
		arguments.add("-data \""+parent.getFolder()+"\"");
		arguments.add("-nosplash");
		arguments.add("-showlocation");
		// cdt-builder args
		arguments.add("-cdt.builder");
		arguments.add("-cdt.import");
		if(!config.getPreferencePath().isEmpty()){
			File preferences = new File(config.getPreferencePath());
			if(preferences.isFile()){
				arguments.add("-cdt.preferences \""+preferences.getCanonicalPath()+"\"");
			}else{
				throw new Exception("invalid path: "+preferences.getAbsolutePath());
			}
		}
		arguments.add("-cdt.noindex");
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
		if(logger.getConfig().getLogLevel(Module.COMMAND) == Level.DEBUG){
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
