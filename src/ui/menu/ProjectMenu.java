package ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;



import util.IChangeListener;
import util.UiTools;

import core.AbstractSystem;
import core.Constants;
import core.Application;
import core.ISystemComponent;
import core.persistence.Configuration;
import core.runtime.LaunchManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class ProjectMenu extends JMenu implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Application application; 
	private Configuration configuration;
	private LaunchManager launchManager;
	private Logger logger;

	private JMenuItem revert;
	private JMenuItem save;
	private JMenuItem quit;
	
	public ProjectMenu(
			Application application, 
			Configuration configuration,
			LaunchManager launchManager,
			Logger logger)
	{
		super("Project");
		
		this.application = application;
		this.configuration = configuration;
		this.launchManager = launchManager;
		this.logger = logger;
		
		revert = new JMenuItem("Revert");
		revert.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ revert(); }
		});
		add(revert);
		
		save = new JMenuItem("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ save(); }
		});
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		add(save);
		
		quit = new JMenuItem("Quit");
		quit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ quit(); }
		});
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		add(quit);
		
		configuration.addListener(this);
		toggleConfigurationUI();
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
	
	private void revert(){
		
		try{
			application.revert();
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	private void save(){
		
		try{
			configuration.save();
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
	
	public void quit(){
		
		if(!launchManager.isBusy() || UiTools.confirmDialog("Aboard running launches ?")){
			logger.info(Module.COMMON, "Shutdown");
			if(configuration.isDirty() && UiTools.confirmDialog("Save changes ?")){
				save();
			}
			Thread thread = new Thread(new Runnable(){
				@Override
				public void run() {
					try{
						application.shutdown();
					}catch(Exception e){
						if(e != AbstractSystem.ABOARDING){
							UiTools.errorDialog(e);
						}
						System.exit(Constants.PROCESS_NOK);
					}
					System.exit(Constants.PROCESS_OK);
				}
			});
			thread.start();
		}
	}

	@Override
	public void changed(Object object) {
		
		if(object == configuration){
			toggleConfigurationUI();
		}
	}

	private void toggleConfigurationUI() {
		if(configuration.isDirty()){
			revert.setEnabled(true);
			save.setEnabled(true);
		}else{
			revert.setEnabled(false);
			save.setEnabled(false);
		}
	}
}
