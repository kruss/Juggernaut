package ui;

import html.AbstractHtmlPage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import util.IChangeListener;
import util.SystemTools;
import util.UiTools;


import core.Configuration;
import core.Constants;
import core.FileManager;
import core.HeapManager;
import core.ISystemComponent;
import core.TaskManager;

public class ToolsMenu extends JMenu implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;
	private FileManager fileManager;
	private HeapManager heapManager;
	
	private JMenuItem exportConfig;
	private JMenuItem garbageCollector;
	private JMenuItem taskMonitor;
	
	private TaskMonitor monitor;
	
	public ToolsMenu(
			Configuration configuration,
			TaskManager taskManager,
			FileManager fileManager,
			HeapManager heapManager)
	{
		super("Tools");
		
		this.configuration = configuration;
		this.fileManager = fileManager;
		this.heapManager = heapManager;
		
		monitor = new TaskMonitor(taskManager);
		
		JMenu configMenu = new JMenu("Configuration");
		add(configMenu);
		
		exportConfig = new JMenuItem("Export");
		exportConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ exportConfiguration(); }
		});
		configMenu.add(exportConfig);
		
		garbageCollector = new JMenuItem("Garbage Collector");
		garbageCollector.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ garbageCollector(); }
		});
		add(garbageCollector);
		
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
	
	private void exportConfiguration(){
		
		String path = 
			fileManager.getTempFolderPath()+
			File.separator+"Configuration.htm";
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
	
	private void garbageCollector(){
		heapManager.cleanup();
	}
	
	private void taskMonitor(){
		monitor.showDialog(true);
	}

	@Override
	public void changed(Object object) {}
}
