package core.backup;

import java.util.ArrayList;

import ui.option.OptionContainer;

public class Backup {
	
	public OptionContainer preferences;
	public ArrayList<LaunchBackup> launches = new ArrayList<LaunchBackup>();
	
	public class LaunchBackup {
		
		public OptionContainer launch;
		public ArrayList<OptionContainer> operations = new ArrayList<OptionContainer>();
		public ArrayList<OptionContainer> triggers = new ArrayList<OptionContainer>();
	}
}

