package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import launch.LaunchAgent;
import launch.LaunchManager;
import launch.LaunchManager.LaunchStatus;

import smtp.ISmtpClient;
import util.IChangedListener;
import util.UiTools;

import core.Cache;
import core.Configuration;
import core.FileManager;
import core.History;
import core.ISystemComponent;
import core.Registry;
import core.TaskManager;
import data.AbstractTrigger;
import data.LaunchConfig;

public class ConfigPanel extends JPanel implements ISystemComponent, IChangedListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration; 
	private Cache cache;
	private History history;
	private FileManager fileManager;
	private TaskManager taskManager;
	private ISmtpClient smtpClient;
	private LaunchManager launchManager;
	
	private ArrayList<IChangedListener> listeners;
	private JComboBox launchCombo;
	private SelectionListener selectionListener;
	private JButton addLaunch;
	private JButton removeLaunch;
	private JButton renameLaunch;
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
			LaunchManager launchManager,
			Registry registry)
	{
		this.configuration = configuration;
		this.cache = cache;
		this.history = history;
		this.fileManager = fileManager;
		this.taskManager = taskManager;
		this.smtpClient = smtpClient;
		this.launchManager = launchManager;
		listeners = new ArrayList<IChangedListener>();
		
		launchCombo = new JComboBox();
		launchCombo.setToolTipText("Configured Launches");
		selectionListener = new SelectionListener();
		
		addLaunch = new JButton(" Add ");
		addLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ addLaunch(); }
		});
		removeLaunch = new JButton(" Remove ");
		removeLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ removeLaunch(); }
		});
		renameLaunch = new JButton(" Rename ");
		renameLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ renameLaunch(); }
		});
		triggerLaunch = new JButton(" Trigger ");
		triggerLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ triggerLaunch(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addLaunch); 
		buttonPanel.add(removeLaunch); 
		buttonPanel.add(renameLaunch);
		buttonPanel.add(triggerLaunch); 
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JLabel(" Launch "), BorderLayout.WEST);
		topPanel.add(launchCombo, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		launchPanel =  new LaunchConfigPanel(this, configuration);
		operationPanel = new OperationConfigPanel(this, configuration, registry);
		triggerPanel = new TriggerConfigPanel(this, configuration, registry);
		
		tabPanel = new JTabbedPane();
		tabPanel.setTabPlacement(JTabbedPane.LEFT);
		tabPanel.add(launchPanel, "Launch");
		tabPanel.add(operationPanel, "Operation");
		tabPanel.add(triggerPanel, "Trigger");

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(tabPanel, BorderLayout.CENTER);
		
		configuration.addListener(this);
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
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
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
			
			removeLaunch.setEnabled(true);
			renameLaunch.setEnabled(true);
			if(
					configuration.getLaunchConfigs().size() > 0 &&
					configuration.getLaunchConfigs().get(index).isReady()
			){
				triggerLaunch.setEnabled(true);
			}else{
				triggerLaunch.setEnabled(false);
			}
			tabPanel.setEnabled(true);
		}else{
			removeLaunch.setEnabled(false);
			renameLaunch.setEnabled(false);
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
	}

	private void addLaunch(){
		
		String name = UiTools.inputDialog("New Launch", "");
		if(name != null && !name.equals("")){
			LaunchConfig config = new LaunchConfig(name);
			configuration.getLaunchConfigs().add(config);
			Collections.sort(configuration.getLaunchConfigs());
			configuration.notifyListeners();
			refreshUI(config);
		}
	}
	
	private void removeLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0 && UiTools.confirmDialog("Remove Launch ?")){
			// TODO remove build-folder if existent
			configuration.getLaunchConfigs().remove(index);
			configuration.setDirty(true);
			configuration.notifyListeners();
			refreshUI(null);
		}
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
				configuration.notifyListeners();
				refreshUI(config);
			}
		}
	}
	
	private void triggerLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			if(UiTools.confirmDialog("Trigger Launch"))
			{
				try{
					LaunchConfig config = configuration.getLaunchConfigs().get(index);
					LaunchAgent launch = config.createLaunch(
							configuration, cache, history, fileManager, taskManager, smtpClient, AbstractTrigger.USER_TRIGGER
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
}
