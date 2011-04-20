package core.backup;

import java.util.ArrayList;

import core.launch.LaunchConfig;
import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.persistence.Configuration;

import ui.option.OptionContainer;

public class Backup {
	
	public String version;
	public OptionContainer preferences;
	public OptionContainer maintenance;
	public OptionContainer logging;
	public ArrayList<LaunchBackup> launches;
	
	public Backup(Configuration configuration){
		
		version = configuration.getVersion();
		preferences = configuration.getOptionContainer();
		maintenance = configuration.getMaintenanceConfig().getOptionContainer();
		logging = configuration.getLogConfig().getOptionContainer();
		launches = new ArrayList<LaunchBackup>();
	}
	
	public class LaunchBackup {
		
		public String id;
		public String name;
		public OptionContainer container;
		
		public ArrayList<OperationBackup> operations;
		public ArrayList<TriggerBackup> triggers;
		
		public LaunchBackup(LaunchConfig launchConfig){
			
			id = launchConfig.getId();
			name = launchConfig.getName();
			container = launchConfig.getOptionContainer();
			operations = new ArrayList<OperationBackup>();
			triggers = new ArrayList<TriggerBackup>();
		}
	}
	
	public class OperationBackup {
		
		public String id;
		public String name;
		public OptionContainer container;
		
		public OperationBackup(AbstractOperationConfig operationConfig){
			
			id = operationConfig.getId();
			name = operationConfig.getName();
			container = operationConfig.getOptionContainer();
		}
	}
	
	public class TriggerBackup {
		
		public String id;
		public String name;
		public OptionContainer container;
		
		public TriggerBackup(AbstractTriggerConfig triggerConfig){
			
			id = triggerConfig.getId();
			name = triggerConfig.getName();
			container = triggerConfig.getOptionContainer();
		}
	}
}

