package ui.menu;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;


import util.SystemTools;
import util.UiTools;

import core.Constants;
import core.ISystemComponent;
import core.Logistic;
import core.html.AbstractHtmlPage;
import core.ressources.Ressources;
import core.runtime.FileManager;

public class HelpMenu extends JMenu implements ISystemComponent {

	private static final long serialVersionUID = 1L;

	private FileManager fileManager;
	
	private JMenuItem showInfo;
	private JMenuItem showHelp;
	
	public HelpMenu(FileManager fileManager){
		super("Help");

		this.fileManager = fileManager;
		
		showInfo = new JMenuItem("Info");
		showInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ showInfo(); }
		});
		add(showInfo);
		
		showHelp = new JMenuItem("Tutorial");
		showHelp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ showHelp(); }
		});
		add(showHelp);
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	private void showInfo(){
		
		String info = 
			"Build: "+Logistic.BUILD_DATE+
			"\nVersion: "+Constants.APP_VERSION;
		UiTools.infoDialog(info);
	}
	
	private void showHelp(){
		
		try{
			Ressources ressources = new Ressources();
			String help = ressources.getRessource(Ressources.HELP);
			
			String path = 
				fileManager.getTempFolderPath()+File.separator+
				Ressources.HELP;
			HelpPage page = new HelpPage(help, path);
			page.create();
			
			SystemTools.openBrowser(path);
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	private class HelpPage extends AbstractHtmlPage {
		
		private String help;
		
		public HelpPage(String help, String path) {
			super(Constants.APP_NAME+" [ Tutorial ]", path, null);
			this.help = help;
		}
		
		@Override
		public String getBody() {
			return help;
		}
	}
}
