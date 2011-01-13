package core.backup;

import java.util.ArrayList;


import ui.option.Option;
import ui.option.OptionContainer;
import util.FileTools;
import util.StringTools;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import core.backup.Backup.LaunchBackup;
import core.launch.LaunchConfig;
import core.launch.operation.AbstractOperationConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.persistence.Configuration;
import core.runtime.FileManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;



public class BackupManager {

	private Configuration configuration;
	private FileManager fileManager;
	private Logger logger;
	
	public BackupManager(Configuration configuration, FileManager fileManager, Logger logger){
		
		this.configuration = configuration;
		this.fileManager = fileManager;
		this.logger = logger;
	}
	
	public void backup(String path) throws Exception {
		
		Backup backup = new Backup();
		
		backup.preferences = configuration.getOptionContainer();
		for(LaunchConfig launchConfig : configuration.getLaunchConfigs()){
			LaunchBackup launchBackup = backup.new LaunchBackup();
			
			launchBackup.launch = launchConfig.getOptionContainer();
			for(AbstractOperationConfig operationConfig : launchConfig.getOperationConfigs()){
				launchBackup.operations.add(operationConfig.getOptionContainer());
			}
			for(AbstractTriggerConfig triggerConfig : launchConfig.getTriggerConfigs()){
				launchBackup.triggers.add(triggerConfig.getOptionContainer());
			}
			
			backup.launches.add(launchBackup);
		}
		
		XStream xstream = new XStream(new DomDriver());
		String xml = xstream.toXML(backup);
		FileTools.writeFile(path, xml, false);
	}

	public Configuration restore(String path) throws Exception {
		
		XStream xstream = new XStream(new DomDriver());
		String xml = FileTools.readFile(path);
		Backup backup = (Backup)xstream.fromXML(xml);
		
		Configuration restore = new Configuration(fileManager, logger, configuration.getPath());
		
		logger.log(Module.COMMON, "(BACKUP) Restoring Configuration");
		restore(backup.preferences, restore.getOptionContainer(), StringTools.enum2strings(Configuration.OPTIONS.class));
		
		return restore;
	}

	private void restore(OptionContainer source, OptionContainer destination, ArrayList<String> options) {
		
		for(String name : options){
			
			Option sourceOption = source.getOption(name);
			if(sourceOption != null){
				Option destinationOption = destination.getOption(name);
				if(destinationOption != null){
					destinationOption.setStringValue(sourceOption.getStringValue());
					logger.debug(Module.COMMON, "(BACKUP) restoring\t: "+name);
				}else{
					logger.log(Module.COMMON, "(BACKUP) missing Destination\t: "+name);
				}
			}else{
				logger.log(Module.COMMON, "(BACKUP) missing Source\t: "+name);
			}
		}
	}
}
