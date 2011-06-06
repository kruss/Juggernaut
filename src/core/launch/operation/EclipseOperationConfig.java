package core.launch.operation;

import java.util.ArrayList;

import ui.option.Option;
import ui.option.Option.Type;
import util.StringTools;

import core.launch.LaunchAgent;
import core.persistence.Cache;
import core.runtime.TaskManager;


public class EclipseOperationConfig extends AbstractOperationConfig {
	
	public static final String OPERATION_NAME = "Eclipse";

	public enum OPTIONS {
		ECLIPSE, BUILD, EXCLUDE, CLEAN, STRICT, HEAP, PREFERENCES, COMPILER
	}
	
	public EclipseOperationConfig(){
		
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.ECLIPSE.toString(), "The path to Eclipse", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.BUILD.toString(), "[project|configuration] pattern to include in build (linewise, commented with '//')", 
				Type.TEXT_AREA, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.EXCLUDE.toString(), "[project|configuration] pattern to exclude from build (comma seperated)",
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.CLEAN.toString(), "Perform a clean build", 
				Type.BOOLEAN, true
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.STRICT.toString(), "Build in strict mode", 
				Type.BOOLEAN, true
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.HEAP.toString(), "Set the HEAP size for the Eclipse VM (MB)", 
				Type.INTEGER, 128, 64, 640
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.PREFERENCES.toString(), "Optional path to a preference file (epf)", 
				Type.TEXT, ""
		));
		optionContainer.setOption(new Option(
				GROUPS.SETTINGS.toString(),
				OPTIONS.COMPILER.toString(), "Optional compiler-path (bin-Folder)", 
				Type.TEXT, ""
		));
	}
	
	@Override
	public String getName(){ return OPERATION_NAME; }
	
	@Override
	public String getUIName(){ return OPERATION_NAME; }
	
	@Override
	public String getDescription(){
		return "An operation to run Eclipse with CDT-Builder Plugin";
	}
	
	public String getEclipsePath(){ 
		return optionContainer.getOption(OPTIONS.ECLIPSE.toString()).getStringValue(); 
	}
	
	public ArrayList<String> getBuildPattern(){ 
		
		String value = optionContainer.getOption(OPTIONS.BUILD.toString()).getStringValue();
		return StringTools.split(value, "\\n", "//");
	}
	
	public ArrayList<String> getExcludePattern(){ 
		
		String value = optionContainer.getOption(OPTIONS.EXCLUDE.toString()).getStringValue();
		return StringTools.split(value, ", ");
	}
	
	public boolean isCleanBuild(){ 
		return optionContainer.getOption(OPTIONS.CLEAN.toString()).getBooleanValue(); 
	}
	
	public boolean isStrictBuild(){ 
		return optionContainer.getOption(OPTIONS.STRICT.toString()).getBooleanValue(); 
	}
	
	/** get the HEAP size for the Eclipse VM (MB) */
	public int getHeapSize(){
		return optionContainer.getOption(OPTIONS.HEAP.toString()).getIntegerValue();
	}
	
	public String getPreferencePath(){ 
		return optionContainer.getOption(OPTIONS.PREFERENCES.toString()).getStringValue(); 
	}
	
	public String getCompilerPath() {
		return optionContainer.getOption(OPTIONS.COMPILER.toString()).getStringValue(); 
	}
	
	@Override
	public boolean isValid(){
		return !getEclipsePath().isEmpty();
	}
	
	@Override
	public AbstractOperation createOperation(
			Cache cache, 
			TaskManager taskManager, 
			LaunchAgent parent)
	{
		return new EclipseOperation(cache, taskManager, parent, this);
	}
}
