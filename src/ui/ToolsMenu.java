package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import util.FileTools;
import util.IChangedListener;
import util.SystemTools;


import core.Application;

public class ToolsMenu extends JMenu implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;

	private JMenuItem printConfiguration;
	
	public ToolsMenu(){
		super("Tools");
		
		application = Application.getInstance();
		
		JMenu configuration = new JMenu("Configuration");
		add(configuration);
		
		printConfiguration = new JMenuItem("Print");
		printConfiguration.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ printConfiguration(); }
		});
		configuration.add(printConfiguration);
		
		application.getConfig().addListener(this);
	}
	
	private void printConfiguration(){
		
		String configuration = application.getConfig().toHtml();
		String path = 
			application.getPersistence().getTempFolderPath()+
			File.separator+"print.htm";
		try{
			FileTools.writeFile(path, configuration, false);
			SystemTools.openBrowser(path);
		}catch(Exception e){
			application.getWindow().popupError(e);
		}
	}

	@Override
	public void changed(Object object) {}
}
