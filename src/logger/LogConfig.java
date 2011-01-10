package logger;

import util.StringTools;

import data.IOptionInitializer;
import data.Option;
import data.OptionContainer;
import data.Option.Type;

/**
 * the configuration of the application,- will be serialized
 */
public class LogConfig implements ILogConfig, IOptionInitializer {

	public enum GROUPS {
		LOGGING
	}
	
	public enum OPTIONS { 
		LOGGING
	}

	private OptionContainer optionContainer;
	private transient boolean dirty;

	public LogConfig(){
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription("The log-level preferences");

		for(Module module : Module.values()){
			optionContainer.getOptions().add(new Option(
					GROUPS.LOGGING.toString(),
					module.toString(), "Set log-level for "+module.toString()+" module",
					Type.TEXT_LIST, StringTools.enum2strings(Level.class), Level.NORMAL.toString()
			));
		}

		dirty = true;
	}
	
	@Override
	public Level getLogLevel(Module module){
		return Level.valueOf(optionContainer.getOption(module.toString()).getStringValue());
	}
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	
	public void setDirty(boolean dirty){ this.dirty = dirty; }
	public boolean isDirty(){ return dirty; }

	@Override
	public void initOptions(OptionContainer container) {}
}
