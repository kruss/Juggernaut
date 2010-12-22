package ui;

import html.AbstractHtmlPage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import util.IChangedListener;
import util.SystemTools;


import core.Configuration;
import core.Constants;
import core.FileManager;
import core.HeapManager;

public class ToolsMenu extends JMenu implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Window window; 
	private Configuration configuration;
	private FileManager fileManager;
	private HeapManager heapManager;
	
	private JMenuItem exportConfig;
	private JMenuItem collectGarbage;
	
	public ToolsMenu(
			Window window, 
			Configuration configuration,
			FileManager fileManager,
			HeapManager heapManager){
		super("Tools");
		
		this.window = window;
		this.configuration = configuration;
		this.fileManager = fileManager;
		this.heapManager = heapManager;
		
		JMenu configMenu = new JMenu("Configuration");
		add(configMenu);
		
		exportConfig = new JMenuItem("Export");
		exportConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ exportConfiguration(); }
		});
		configMenu.add(exportConfig);
		
		collectGarbage = new JMenuItem("Garbage Collector");
		collectGarbage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ collectGarbage(); }
		});
		add(collectGarbage);
		
		configuration.addListener(this);
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
	
	private void exportConfiguration(){
		
		String path = 
			fileManager.getTempFolderPath()+
			File.separator+"Configuration.htm";
		ConfigPage page = new ConfigPage(path);
		try{
			page.create();
			SystemTools.openBrowser(path);
		}catch(Exception e){
			window.popupError(e);
		}
	}
	
	private void collectGarbage(){
		
		heapManager.cleanup();
	}

	@Override
	public void changed(Object object) {}
}
