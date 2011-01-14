package core.backup;

import java.util.ArrayList;

import ui.option.OptionContainer;

public class Backup {
	
	public String version;
	public OptionContainer container;
	public ArrayList<LaunchBackup> launches;
	
	public Backup(String version){
		
		this.version = version;
		container = null;
		launches = new ArrayList<LaunchBackup>();
	}
	
	public class LaunchBackup {
		
		public String name;
		public OptionContainer container;
		
		public ArrayList<OperationBackup> operations;
		public ArrayList<TriggerBackup> triggers;
		
		public LaunchBackup(String name){
			
			this.name = name;
			container = null;
			operations = new ArrayList<OperationBackup>();
			triggers = new ArrayList<TriggerBackup>();
		}
	}
	
	public class OperationBackup {
		
		public String name;
		public OptionContainer container;
		
		public OperationBackup(String name){
			
			this.name = name;
			container = null;
		}
	}
	
	public class TriggerBackup {
		
		public String name;
		public OptionContainer container;
		
		public TriggerBackup(String name){
			
			this.name = name;
			container = null;
		}
	}
}

