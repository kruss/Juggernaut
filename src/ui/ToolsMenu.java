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

	private JMenuItem printConfig;
	
	public ToolsMenu(){
		super("Tools");
		
		application = Application.getInstance();
		
		JMenu configuration = new JMenu("Configuration");
		add(configuration);
		
		printConfig = new JMenuItem("Print");
		printConfig.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ printConfiguration(); }
		});
		configuration.add(printConfig);
		
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
	
	private void printConfiguration(){
		
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

	@Override
	public void changed(Object object) {}
}
