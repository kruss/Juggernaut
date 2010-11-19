package operation;

import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import util.Option;
import util.OptionContainer;
import util.Option.Type;

/**
 * the configuration of an operation,- will be serialized
 */
public abstract class AbstractOperationConfig {

	public enum OPTIONS {
		ACTIVE, CRITICAL
	}
	
	private String id;
	protected OptionContainer optionContainer;
	
	public AbstractOperationConfig(){
		
		id = UUID.randomUUID().toString();
		
		optionContainer = new OptionContainer();
		optionContainer.setDescription(getDescription());
		optionContainer.getOptions().add(new Option(
				OPTIONS.ACTIVE.toString(), "The operation's active state",
				Type.BOOLEAN, true
		));
		optionContainer.getOptions().add(new Option(
				OPTIONS.CRITICAL.toString(), "Erros will aboard the launch",
				Type.BOOLEAN, true
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
			return (isValid() ? "" : "~ ") + getName();
		}else{
			return (isValid() ? "" : "~ ") + "<"+getName()+">";
		}
	}
	
	public abstract String getName();
	public abstract String getDescription();
	public abstract boolean isValid();
	public abstract AbstractOperation createOperation();
	
	public AbstractOperationConfig clone(){
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(this);
		AbstractOperationConfig config = (AbstractOperationConfig)xstream.fromXML(xml);
		return config;
	}
}
