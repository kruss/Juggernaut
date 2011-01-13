package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import util.UiTools;

import core.Constants;
import core.FileManager;
import core.ISystemComponent;
import core.Logistic;

// TODO more help
public class HelpMenu extends JMenu implements ISystemComponent {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private FileManager fileManager;
	
	private JMenuItem helpInfo;
	
	public HelpMenu(FileManager fileManager){
		super("Help");

		this.fileManager = fileManager;
		
		helpInfo = new JMenuItem("Info");
		helpInfo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ helpInfo(); }
		});
		add(helpInfo);
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	private void helpInfo(){
		
		String info = 
			"Build: "+Logistic.BUILD_DATE+
			"\nVersion: "+Constants.APP_VERSION;
		UiTools.infoDialog(info);
	}
}
