package ui.menu;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JMenu;
import javax.swing.JMenuItem;



import ui.dialog.CacheMonitor;
import ui.dialog.TaskMonitor;
import util.DateTools;
import util.FileTools;
import util.IChangeListener;
import util.SystemTools;
import util.UiTools;


import core.Juggernaut;
import core.Constants;
import core.ISystemComponent;
import core.backup.BackupManager;
import core.html.AbstractHtmlPage;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.runtime.FileManager;
import core.runtime.HeapManager;
import core.runtime.Registry;
import core.runtime.TaskManager;
import core.runtime.logger.ErrorManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ErrorManager.ErrorInfo;

public class ToolsMenu extends JMenu implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Juggernaut juggernaut; 
	private ErrorManager errorManager;
	private Configuration configuration;
	private Registry registry;
	private FileManager fileManager;
	private HeapManager heapManager;
	
	private JMenuItem backupConfig;
	private JMenuItem restoreConfig;
	private JMenuItem printConfig;
	private JMenuItem showTaskMonitor;
	private JMenuItem showCacheMonitor;
	private JMenuItem showErrors;
	private JMenuItem clearErrors;
	private JMenuItem cleanupHeap;
	private Logger logger;
	
	private TaskMonitor taskMonitor;
	private CacheMonitor cacheMonitor;
	
	public ToolsMenu(
			Juggernaut juggernaut,
			ErrorManager errorManager,
			Configuration configuration,
			Cache cache,
			Registry registry,
			TaskManager taskManager,
			FileManager fileManager,
			HeapManager heapManager,
			Logger logger)
	{
		super("Tools");
		
		this.juggernaut = juggernaut;
		this.errorManager = errorManager;
		this.configuration = configuration;
		this.registry = registry;
		this.fileManager = fileManager;
		this.heapManager = heapManager;
		this.logger = logger;
		
		taskMonitor = new TaskMonitor(taskManager);
		cacheMonitor = new CacheMonitor(cache);
		
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
		
		JMenu monitorMenu = new JMenu("Monitor");
		add(monitorMenu);
		
		showTaskMonitor = new JMenuItem("Tasks");
		showTaskMonitor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ showTaskMonitor(); }
		});
		monitorMenu.add(showTaskMonitor);
		
		showCacheMonitor = new JMenuItem("Cache");
		showCacheMonitor.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ showCacheMonitor(); }
		});
		monitorMenu.add(showCacheMonitor);
		
		JMenu errorMenu = new JMenu("Error");
		add(errorMenu);
		
		showErrors = new JMenuItem("Show");
		showErrors.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ showErrors(); }
		});
		errorMenu.add(showErrors);
		
		clearErrors = new JMenuItem("Clear");
		clearErrors.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ clearErrors(); }
		});
		errorMenu.add(clearErrors);
		
		JMenu cleanupMenu = new JMenu("Cleanup");
		add(cleanupMenu);
		
		cleanupHeap = new JMenuItem("Heap");
		cleanupHeap.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ cleanupHeap(); }
		});
		cleanupMenu.add(cleanupHeap);
		
		errorManager.addListener(this);
		toggleErrorUI();
	}
	
	@Override
	public void init() throws Exception {
		taskMonitor.init();
		cacheMonitor.init();
	}
	
	@Override
	public void shutdown() throws Exception {
		taskMonitor.shutdown();
		cacheMonitor.shutdown();
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
				juggernaut.revert();
				
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
	
	private void showTaskMonitor(){
		taskMonitor.showDialog(true);
	}
	
	private void showCacheMonitor(){
		cacheMonitor.showDialog(true);
	}
	
	private void showErrors(){
		
		try{
			String path = fileManager.getTempFolder()+File.separator+"errors.txt";
			StringBuilder info = new StringBuilder();
			ArrayList<ErrorInfo> errors = errorManager.getInfo();
			for(ErrorInfo error : errors){
				info.append("### "+error.title+" ###\n\n"+error.message+"\n");
			}
			FileTools.writeFile(path, info.toString().replaceAll("\n", "\r\n"), false);
			SystemTools.openBrowser(path);
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	private void clearErrors(){
		errorManager.clear();
	}
	
	private void cleanupHeap(){
		heapManager.cleanup();
	}
	
	@Override
	public void changed(Object object) {
		
		if(object == errorManager){
			toggleErrorUI();
		}
	}

	private void toggleErrorUI() {
		if(errorManager.getErrorCount() > 0){
			showErrors.setEnabled(true);
			clearErrors.setEnabled(true);
		}else{
			showErrors.setEnabled(false);
			clearErrors.setEnabled(false);
		}
	}
}
