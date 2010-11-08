package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import util.UiTools;

import launch.LaunchConfig;
import launch.LaunchManager;

import core.Application;
import core.ConfigStore;
import core.IChangeListener;

public class ConfigPanel extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private ConfigStore configStore;
	
	private ArrayList<IChangeListener> listeners;
	private JComboBox launchCombo;
	private SelectionListener selectionListener;
	private JButton addLaunch;
	private JButton removeLaunch;
	private JButton renameLaunch;
	private JButton runLaunch;
	private LaunchConfig launchConfig;
	
	public ConfigPanel(){
		
		listeners = new ArrayList<IChangeListener>();
		configStore = Application.getInstance().getConfigStore();
		configStore.addListener(this);
		
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
		runLaunch = new JButton(" Run ");
		runLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ runLaunch(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addLaunch); 
		buttonPanel.add(removeLaunch); 
		buttonPanel.add(renameLaunch); 
		buttonPanel.add(runLaunch);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JLabel(" Launch "), BorderLayout.WEST);
		topPanel.add(launchCombo, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.LEFT);
		centerPanel.add(new ConfigPanelLaunch(this), "Launch");
		centerPanel.add(new ConfigPanelOperation(this), "Operation");
		centerPanel.add(new ConfigPanelTrigger(this), "Trigger");

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		
		initUI();
		adjustSelection();
	}
	
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangeListener listener : listeners){
			listener.changed(this);
		}
	}
	
	public LaunchConfig getLaunchConfig(){ return launchConfig; }
	
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
			launchConfig = configStore.getLaunchConfigs().get(index);
		}else{
			launchConfig = null;
		}
		notifyListeners();
	}
	
	private void initUI(){
		
		ArrayList<LaunchConfig> configs = configStore.getLaunchConfigs();
		for(LaunchConfig config : configs){
			launchCombo.addItem(config);
		}
		launchCombo.addItemListener(selectionListener);
	}
	
	private void clearUI(){
		
		launchCombo.removeItemListener(selectionListener);
		launchCombo.removeAllItems();
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
		
		if(object instanceof ConfigStore){
			launchCombo.repaint();
		}
	}
	
	private void addLaunch(){
		
		String name = UiTools.inputDialog("New Launch", "");
		if(name != null && !name.equals("")){
			LaunchConfig config = new LaunchConfig(name);
			configStore.getLaunchConfigs().add(config);
			configStore.notifyListeners();
			refreshUI(config);
		}
	}
	
	private void removeLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0 && UiTools.confirmDialog("Remove Launch ?")){
			configStore.getLaunchConfigs().remove(index);
			configStore.notifyListeners();
			refreshUI(null);
		}
	}
	
	private void renameLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configStore.getLaunchConfigs().get(index);
			String name = UiTools.inputDialog("Rename Launch", config.getName());
			if(name != null && !name.equals("")){
				config.setName(name);
				config.setDirty(true);
				configStore.notifyListeners();
				refreshUI(config);
			}
		}
	}
	
	private void runLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configStore.getLaunchConfigs().get(index);
			LaunchManager.getInstance().runLaunch(config.createLaunch());
		}
	}
}
