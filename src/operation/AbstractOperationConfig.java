package operation;

import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import util.Option;
import util.OptionContainer;
import util.Option.Type;

public abstract class AbstractOperationConfig {

	public enum OPTIONS {
		ACTIVE, DESCRIPTION, CRITICAL, TIMEOUT
	}
	
	private String id;
	protected OptionContainer optionContainer;
	
	public AbstractOperationConfig(){
		
		id = UUID.randomUUID().toString();
		
		optionContainer = new OptionContainer();
		optionContainer.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The item's active state",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.DESCRIPTION.toString(), "The item's description", 
				Type.TEXT, ""
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.CRITICAL.toString(), "Erros will aboard the launch",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.TIMEOUT.toString(), "Timeout in minutes (0 = no timeout)", 
				Type.INTEGER, 0
		));
	}
	
	public String getId(){ return id; }
	
	public OptionContainer getOptionContainer(){ return optionContainer; }
	
	public boolean isActive(){ 
		return optionContainer.getOption(OPTIONS.ACTIVE.toString()).getBooleanValue(); 
	}
	
	@Override
	public String toString(){ 
		if(isActive()){
			return getName();
		}else{
			return "<"+getName()+">";
		}
	}
	
	public abstract String getName();
	public abstract AbstractOperation createOperation();
	
	public AbstractOperationConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		AbstractOperationConfig config = (AbstractOperationConfig)xstream.fromXML(xml);
		return config;
	}
}
