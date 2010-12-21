package ui;

import html.AbstractHtmlPage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import util.IChangedListener;
import util.SystemTools;


import core.Application;
import core.Constants;

public class ToolsMenu extends JMenu implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;

	private JMenuItem exportConfig;
	private JMenuItem collectGarbage;
	
	public ToolsMenu(){
		super("Tools");
		
		application = Application.getInstance();
		
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
		
		application.getConfig().addListener(this);
	}
	
	class ConfigPage extends AbstractHtmlPage {
		public ConfigPage(String path) {
			super(Constants.APP_NAME+" [ Configuration ]", path, null);
		}
		@Override
		public String getBody() {
			return application.getConfig().toHtml();
		}
	}
	
	private void exportConfiguration(){
		
		String path = 
			application.getFileManager().getTempFolderPath()+
			File.separator+"Configuration.htm";
		ConfigPage page = new ConfigPage(path);
		try{
			page.create();
			SystemTools.openBrowser(path);
		}catch(Exception e){
			application.popupError(e);
		}
	}
	
	private void collectGarbage(){
		
		application.getHeapManager().cleanup();
	}

	@Override
	public void changed(Object object) {}
}
