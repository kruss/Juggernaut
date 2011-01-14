package ui.menu;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;

import javax.swing.JMenu;
import javax.swing.JMenuItem;



import ui.dialog.TaskMonitor;
import util.DateTools;
import util.IChangeListener;
import util.SystemTools;
import util.UiTools;


import core.Application;
import core.Constants;
import core.ISystemComponent;
import core.backup.BackupManager;
import core.html.AbstractHtmlPage;
import core.persistence.Configuration;
import core.runtime.FileManager;
import core.runtime.HeapManager;
import core.runtime.Registry;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;

public class ToolsMenu extends JMenu implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Application application; 
	private Configuration configuration;
	private Registry registry;
	private FileManager fileManager;
	private HeapManager heapManager;
	
	private JMenuItem backupConfig;
	private JMenuItem restoreConfig;
	private JMenuItem printConfig;
	private JMenuItem collectGarbage;
	private JMenuItem taskMonitor;
	private Logger logger;
	
	private TaskMonitor monitor;
	
	public ToolsMenu(
			Application application,
			Configuration configuration,
			Registry registry,
			TaskManager taskManager,
			FileManager fileManager,
			HeapManager heapManager,
			Logger logger)
	{
		super("Tools");
		
		this.application = application;
		this.configuration = configuration;
		this.registry = registry;
		this.fileManager = fileManager;
		this.heapManager = heapManager;
		this.logger = logger;
		
		monitor = new TaskMonitor(taskManager);
		
		JMenu configMenu = new JMenu("Configuration");
		add(configMenu);
		
		backupConfig = new JMenuItem("Backup");
		backupConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ backupConfig(); }
		});
		configMenu.add(backupConfig);
		
		restoreConfig = new JMenuItem("Restore");
		restoreConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ restoreConfig(); }
		});
		configMenu.add(restoreConfig);
		
		printConfig = new JMenuItem("Print");
		printConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ printConfig(); }
		});
		configMenu.add(printConfig);
		
		collectGarbage = new JMenuItem("Garbage Collector");
		collectGarbage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ collectGarbage(); }
		});
		add(collectGarbage);
		
		taskMonitor = new JMenuItem("Task Monitor");
		taskMonitor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ taskMonitor(); }
		});
		add(taskMonitor);
		
		configuration.addListener(this);
	}
	
	@Override
	public void init() throws Exception {
		monitor.init();
	}
	
	@Override
	public void shutdown() throws Exception {
		monitor.shutdown();
	}
	
	private void backupConfig(){
		
		String path = 
			SystemTools.getWorkingDir()+File.separator+
			"Configuration_"+DateTools.getFileSystemDate(new Date())+".xml";
		try{
			BackupManager backup = new BackupManager(configuration, registry, fileManager, logger);
			backup.backup(path);
			
			UiTools.infoDialog("Backup to:\n\n"+path);
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	private void restoreConfig(){
		
		File file = UiTools.fileDialog("Backup File", SystemTools.getWorkingDir());
		if(
				file != null && 
				UiTools.confirmDialog("Restore Configuration from Backup?")
		){
			String path = file.getAbsolutePath();
			try{
				BackupManager backup = new BackupManager(configuration, registry, fileManager, logger);
				Configuration restore = backup.restore(path);
				restore.save();
				
				configuration.setDirty(true);
				application.revert();
				
				UiTools.infoDialog("Restore from:\n\n"+path);
			}catch(Exception e){
				UiTools.errorDialog(e);
			}
		}
	}
	
	private void printConfig(){
		
		String path = 
			fileManager.getTempFolderPath()+File.separator+
			"Configuration.htm";
		ConfigPage page = new ConfigPage(path);
		try{
			page.create();
			SystemTools.openBrowser(path);
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	private class ConfigPage extends AbstractHtmlPage {
		
		public ConfigPage(String path) {
			super(Constants.APP_NAME+" [ Configuration ]", path, null);
		}
		
		@Override
		public String getBody() {
			return configuration.toHtml();
		}
	}
	
	private void collectGarbage(){
		heapManager.cleanup();
	}
	
	private void taskMonitor(){
		monitor.showDialog(true);
	}

	@Override
	public void changed(Object object) {}
}
