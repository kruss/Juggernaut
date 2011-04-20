package core.backup;

import ui.option.Option;
import ui.option.OptionContainer;
import util.FileTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.backup.Backup.LaunchBackup;
import core.backup.Backup.OperationBackup;
import core.backup.Backup.TriggerBackup;
import core.launch.LaunchConfig;
import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.FileManager;
import core.runtime.Registry;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class BackupManager {

	private Cache cache;
	private Configuration configuration;
	private Registry registry;
	private FileManager fileManager;
	private Logger logger;
	
	public BackupManager(
			Cache cache,
			Configuration configuration, 
			Registry registry, 
			FileManager fileManager, 
			Logger logger)
	{
		this.cache = cache;
		this.configuration = configuration;
		this.registry = registry;
		this.fileManager = fileManager;
		this.logger = logger;
	}
	
	public void backup(String path) throws Exception {
		
		// configuration
		Backup backup = new Backup(configuration);
		// launches
		for(LaunchConfig launchConfig : configuration.getLaunchConfigs()){
			LaunchBackup launchBackup = backup.new LaunchBackup(launchConfig);
			// operations
			for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
				OperationBackup operationBackup = backup.new OperationBackup(operationConfig);
				launchBackup.operations.add(operationBackup);
			}
			// triggers
			for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
				TriggerBackup triggerBackup = backup.new TriggerBackup(triggerConfig);
				launchBackup.triggers.add(triggerBackup);
			}
			backup.launches.add(launchBackup);
		}
		
		logger.log(Module.COMMON, "Backup: "+path);
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(backup);
		FileTools.writeFile(path, xml, false);
	}

	public Configuration restore(String path) throws Exception {
		
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Backup backup = (Backup)xstream.fromXML(xml);
		
		logger.log(Module.COMMON, "Restore "+path);
		Configuration restore = new Configuration(cache, fileManager, logger, configuration.getPath());
		// configuration
		restore(backup.preferences, restore.getOptionContainer());
		restore(backup.maintenance, restore.getMaintenanceConfig().getOptionContainer());
		restore(backup.logging, restore.getLogConfig().getOptionContainer());
		// launches
		for(LaunchBackup launchBackup : backup.launches){
			String launchName = launchBackup.name;
			logger.log(Module.COMMON, "Restore Launch ["+launchName+"]");
			LaunchConfig launchConfig = new LaunchConfig(launchName);
			launchConfig.setId(launchBackup.id);
			restore(launchBackup.container, launchConfig.getOptionContainer());
			// operations
			for(OperationBackup operationBackup : launchBackup.operations){
				String operationName = operationBackup.name;
				AbstractOperationConfig operationConfig = registry.createOperationConfig(operationName);
				if(operationConfig != null){
					logger.log(Module.COMMON, "Restore Operation ["+operationName+"]");
					operationConfig.setId(operationBackup.id);
					restore(operationBackup.container, operationConfig.getOptionContainer());
					launchConfig.getOperationConfigs().add(operationConfig);
				}else{
					logger.log(Module.COMMON, "No Operation ["+operationName+"]");
				}
			}
			// triggers
			for(TriggerBackup triggerBackup : launchBackup.triggers){
				String triggerName = triggerBackup.name;
				AbstractTriggerConfig triggerConfig = registry.createTriggerConfig(triggerName);
				if(triggerConfig != null){
					logger.log(Module.COMMON, "Restore Trigger ["+triggerName+"]");
					triggerConfig.setId(triggerBackup.id);
					restore(triggerBackup.container, triggerConfig.getOptionContainer());
					launchConfig.getTriggerConfigs().add(triggerConfig);
				}else{
					logger.log(Module.COMMON, "No Trigger ["+triggerName+"]");
				}
			}
			restore.getLaunchConfigs().add(launchConfig);
		}
		logger.log(Module.COMMON, "Restore completed (Version: "+backup.version+" -> "+restore.getVersion()+")");
		
		return restore;
	}

	private void restore(OptionContainer source, OptionContainer destination) {
		
		for(Option sourceOption : source.getOptions()){
			String name = sourceOption.getName();
			Option destinationOption = destination.getOption(name);
			if(destinationOption != null){
				destinationOption.setStringValue(sourceOption.getStringValue());
				logger.debug(Module.COMMON, "Restore Option ["+name+"]");
			}else{
				logger.log(Module.COMMON, "No Option ["+name+"]");
			}
		}
	}
}
