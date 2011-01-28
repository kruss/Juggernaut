package ui.panel;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


import ui.IStatusClient;
import ui.IStatusProvider;
import util.FileTools;
import util.IChangeListener;
import util.IChangeable;
import util.SystemTools;
import util.UiTools;

import core.ISystemComponent;
import core.launch.LaunchAgent;
import core.launch.LaunchConfig;
import core.launch.trigger.AbstractTrigger;
import core.persistence.Cache;
import core.persistence.Configuration;
import core.persistence.History;
import core.runtime.FileManager;
import core.runtime.LaunchManager;
import core.runtime.Registry;
import core.runtime.TaskManager;
import core.runtime.LaunchManager.LaunchStatus;
import core.runtime.http.IHttpServer;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import core.runtime.smtp.ISmtpClient;

public class ConfigPanel extends JPanel implements ISystemComponent, IChangeListener, IChangeable, IStatusProvider {

	private static final long serialVersionUID = 1L;

	private Configuration configuration; 
	private Cache cache;
	private History history;
	private FileManager fileManager;
	private TaskManager taskManager;
	private ISmtpClient smtpClient;
	private IHttpServer httpServer;
	private LaunchManager launchManager;
	private Logger logger;
	
	private ArrayList<IChangeListener> listeners;
	private IStatusClient client;
	private JComboBox launchCombo;
	private SelectionListener selectionListener;
	private JButton addLaunch;
	private JButton deleteLaunch;
	private JButton renameLaunch;
	private JButton cloneLaunch;
	private JButton launchFolder;
	private JButton triggerLaunch;
	private JTabbedPane tabPanel;
	private LaunchConfigPanel launchPanel;
	private OperationConfigPanel operationPanel;
	private TriggerConfigPanel triggerPanel;
	private LaunchConfig currentConfig;
		
	public LaunchConfig getCurrentConfig(){ return currentConfig; }
	
	public ConfigPanel(
			Configuration configuration, 
			Cache cache,
			History history,
			FileManager fileManager,
			TaskManager taskManager,
			ISmtpClient smtpClient,
			IHttpServer httpServer,
			LaunchManager launchManager,
			Registry registry,
			Logger logger)
	{
		this.configuration = configuration;
		this.cache = cache;
		this.history = history;
		this.fileManager = fileManager;
		this.taskManager = taskManager;
		this.smtpClient = smtpClient;
		this.httpServer = httpServer;
		this.launchManager = launchManager;
		this.logger = logger;
		
		listeners = new ArrayList<IChangeListener>();
		client = null;
		
		launchCombo = new JComboBox();
		launchCombo.setToolTipText("Configured Launches");
		selectionListener = new SelectionListener();
		
		addLaunch = new JButton(" Add ");
		addLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ addLaunch(); }
		});
		deleteLaunch = new JButton(" Delete ");
		deleteLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ deleteLaunch(); }
		});
		renameLaunch = new JButton(" Rename ");
		renameLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ renameLaunch(); }
		});
		cloneLaunch = new JButton(" Clone ");
		cloneLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ cloneLaunch(); }
		});
		launchFolder = new JButton(" Folder ");
		launchFolder.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ launchFolder(); }
		});
		triggerLaunch = new JButton(" Trigger ");
		triggerLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ triggerLaunch(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addLaunch); 
		buttonPanel.add(deleteLaunch); 
		buttonPanel.add(renameLaunch);
		buttonPanel.add(cloneLaunch);
		buttonPanel.add(launchFolder);
		buttonPanel.add(triggerLaunch); 
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JLabel(" Launch "), BorderLayout.WEST);
		topPanel.add(launchCombo, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		launchPanel =  new LaunchConfigPanel(this, configuration);
		operationPanel = new OperationConfigPanel(this, configuration, registry, logger);
		triggerPanel = new TriggerConfigPanel(this, configuration, registry, logger);
		
		tabPanel = new JTabbedPane();
		tabPanel.setTabPlacement(JTabbedPane.LEFT);
		tabPanel.add(launchPanel, "Launch");
		tabPanel.add(operationPanel, "Operation");
		tabPanel.add(triggerPanel, "Trigger");

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(tabPanel, BorderLayout.CENTER);
		
		configuration.addListener(this);
		launchManager.addListener(this);
	}
	
	@Override
	public void init() throws Exception {
		
		launchPanel.init();
		operationPanel.init();
		triggerPanel.init();
		initUI();
		adjustSelection();
	}
	
	@Override
	public void shutdown() throws Exception {}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	@Override
	public void setClient(IStatusClient client){ 
		this.client = client; 
		operationPanel.setClient(client);
		triggerPanel.setClient(client);
	}
	@Override
	public void status(String text){
		if(client != null){
			client.status(text);
		}
	}
	
	private class SelectionListener implements ItemListener { 
		
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(e.getSource() == launchCombo){
					adjustSelection();
				}
			}
		}
	}
	
	private void adjustSelection() {
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			currentConfig = configuration.getLaunchConfigs().get(index);
			status("Launch "+(index+1)+"/"+launchCombo.getItemCount()+" ["+currentConfig.getId()+"]");
		}else{
			currentConfig = null;
		}
		tabPanel.setSelectedIndex(0);
		adjustButtons();
		notifyListeners();
	}
	
	private void adjustButtons() {
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig launchConfig = configuration.getLaunchConfigs().get(index);
			
			if(!launchManager.isRunning(launchConfig.getId())){
				deleteLaunch.setEnabled(true);
				
			}else{
				deleteLaunch.setEnabled(false);
			}
			renameLaunch.setEnabled(true);
			cloneLaunch.setEnabled(true);
			File folder = fileManager.getLaunchFolder(launchConfig.getId());
			if(folder.isDirectory()){
				launchFolder.setEnabled(true);
			}else{
				launchFolder.setEnabled(false);
			}
			if(launchConfig.isReady() && !launchManager.isRunning(launchConfig.getId())){
				triggerLaunch.setEnabled(true);
			}else{
				triggerLaunch.setEnabled(false);
			}
			tabPanel.setEnabled(true);
		}else{
			deleteLaunch.setEnabled(false);
			renameLaunch.setEnabled(false);
			cloneLaunch.setEnabled(false);
			launchFolder.setEnabled(false);
			triggerLaunch.setEnabled(false);
			tabPanel.setEnabled(false);
		}		
	}
	
	private void clearUI(){
		
		launchCombo.removeItemListener(selectionListener);
		launchCombo.removeAllItems();
	}
	
	private void initUI(){
		
		ArrayList<LaunchConfig> configs = configuration.getLaunchConfigs();
		for(LaunchConfig config : configs){
			launchCombo.addItem(config);
		}
		launchCombo.addItemListener(selectionListener);
	}
	
	private void refreshUI(LaunchConfig selected){

		clearUI();
		initUI();
		if(selected != null){
			for(int i=0; i<launchCombo.getItemCount(); i++){
				if(selected == launchCombo.getItemAt(i)){
					launchCombo.setSelectedIndex(i);
				}
			}
		}else{
			launchCombo.setSelectedIndex(-1);
		}
		adjustSelection();
	}
	
	@Override
	public void changed(Object object) {
		
		if(object == configuration){
			launchCombo.repaint();
			adjustButtons();
		}
		
		if(object == launchManager){
			adjustButtons();
		}
	}

	private void addLaunch(){
		
		String name = UiTools.inputDialog("New Launch", "");
		if(name != null && !name.equals("")){
			LaunchConfig config = new LaunchConfig(name);
			configuration.getLaunchConfigs().add(config);
			Collections.sort(configuration.getLaunchConfigs());
			refreshUI(config);
			configuration.notifyListeners();
		}
	}
	
	private void deleteLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0 && UiTools.confirmDialog("Delete Launch ?")){
			
			LaunchConfig config = configuration.getLaunchConfigs().get(index);
			File folder = fileManager.getLaunchFolder(config.getId());
			if(folder.isDirectory()){
				removeLaunchFolder(folder.getAbsolutePath());
			}
			configuration.getLaunchConfigs().remove(index);
			configuration.setDirty(true);
			refreshUI(null);
			configuration.notifyListeners();
		}
	}
	
	private void removeLaunchFolder(final String path){
		
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					logger.log(Module.COMMON, "delete: "+path);
					FileTools.deleteFolder(path);
				}catch(Exception e){
					UiTools.errorDialog(e);
				}
			}
		});
		thread.start();
	}
	
	private void renameLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configuration.getLaunchConfigs().get(index);
			String name = UiTools.inputDialog("Rename Launch", config.getName());
			if(name != null && !name.equals("")){
				config.setName(name);
				config.setDirty(true);
				Collections.sort(configuration.getLaunchConfigs());
				refreshUI(config);
				configuration.notifyListeners();
			}
		}
	}
	
	private void cloneLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configuration.getLaunchConfigs().get(index);
			LaunchConfig clone = config.duplicate();
			configuration.getLaunchConfigs().add(clone);
			Collections.sort(configuration.getLaunchConfigs());
			refreshUI(clone);
			configuration.notifyListeners();
		}
	}
	
	private void triggerLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			if(UiTools.confirmDialog("Trigger Launch")){
				try{
					LaunchConfig config = configuration.getLaunchConfigs().get(index);
					LaunchAgent launch = config.createLaunch(
							configuration, cache, history, fileManager, taskManager, smtpClient, httpServer, AbstractTrigger.USER_TRIGGER
					);
					LaunchStatus status = launchManager.runLaunch(launch);
					if(!status.launched){
						UiTools.infoDialog(status.message);
					}
				}catch(Exception e){
					UiTools.errorDialog(e);
				}
			}
		}
	}
	
	private void launchFolder(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configuration.getLaunchConfigs().get(index);
			File folder = fileManager.getLaunchFolder(config.getId());
			if(folder.isDirectory()){
				try{
					String path = folder.getAbsolutePath();
					logger.debug(Module.COMMON, "open: "+path);
					SystemTools.openBrowser(path);
				}catch(Exception e){
					UiTools.errorDialog(e);
				}
			}
		}
	}
}
