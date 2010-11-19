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

import lifecycle.LaunchAgent;
import lifecycle.LaunchManager;
import lifecycle.LaunchManager.LaunchingStatus;

import util.IChangedListener;
import util.UiTools;


import core.Application;
import data.LaunchConfig;

public class ConfigPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
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
	
	public ConfigPanel(){
		
		application = Application.getInstance();
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
		
		launchPanel =  new LaunchConfigPanel(this);
		operationPanel = new OperationConfigPanel(this);
		triggerPanel = new TriggerConfigPanel(this);
		
		tabPanel = new JTabbedPane();
		tabPanel.setTabPlacement(JTabbedPane.LEFT);
		tabPanel.add(launchPanel, "Launch");
		tabPanel.add(operationPanel, "Operation");
		tabPanel.add(triggerPanel, "Trigger");

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(tabPanel, BorderLayout.CENTER);
		
		application.getConfiguration().addListener(this);
	}
	
	public void init() {
		
		launchPanel.init();
		operationPanel.init();
		triggerPanel.init();
		initUI();
		adjustSelection();
	}
	
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
			currentConfig = application.getConfiguration().getLaunchConfigs().get(index);
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
					application.getConfiguration().getLaunchConfigs().size() > 0 &&
					application.getConfiguration().getLaunchConfigs().get(index).isReady()
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
		
		ArrayList<LaunchConfig> configs = application.getConfiguration().getLaunchConfigs();
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
		
		if(object == application.getConfiguration()){
			launchCombo.repaint();
			adjustButtons();
		}
	}

	private void addLaunch(){
		
		String name = UiTools.inputDialog("New Launch", "");
		if(name != null && !name.equals("")){
			LaunchConfig config = new LaunchConfig(name);
			application.getConfiguration().getLaunchConfigs().add(config);
			Collections.sort(application.getConfiguration().getLaunchConfigs());
			application.getConfiguration().notifyListeners();
			refreshUI(config);
		}
	}
	
	private void removeLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0 && UiTools.confirmDialog("Remove Launch ?")){
			application.getConfiguration().getLaunchConfigs().remove(index);
			application.getConfiguration().setDirty(true);
			application.getConfiguration().notifyListeners();
			refreshUI(null);
		}
	}
	
	private void renameLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = application.getConfiguration().getLaunchConfigs().get(index);
			String name = UiTools.inputDialog("Rename Launch", config.getName());
			if(name != null && !name.equals("")){
				config.setName(name);
				config.setDirty(true);
				Collections.sort(application.getConfiguration().getLaunchConfigs());
				application.getConfiguration().notifyListeners();
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
					LaunchConfig config = application.getConfiguration().getLaunchConfigs().get(index);
					LaunchAgent launch = config.createLaunch(LaunchManager.USER_TRIGGER);
					LaunchingStatus status = application.getLaunchManager().runLaunch(launch);
					if(!status.launched){
						UiTools.infoDialog(status.message);
					}
				}catch(Exception e){
					application.handleException(e);
				}
			}
		}
	}
}
