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

import util.UiTools;

import launch.LaunchConfig;
import launch.LaunchManager;

import core.Application;
import core.ConfigStore;

public class ConfigPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private ConfigStore configStore;
	
	private JComboBox launchCombo;
	private SelectionListener selectionListener;
	private JButton addLaunch;
	private JButton removeLaunch;
	private JButton renameLaunch;
	private JButton runLaunch;
	
	public ConfigPane(){
		
		configStore = Application.getInstance().getConfigStore();
		
		launchCombo = new JComboBox();
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
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JLabel(" Launch "), BorderLayout.WEST);
		topPanel.add(launchCombo, BorderLayout.CENTER);
		
		JPanel centerPanel = new JPanel();
		
		JPanel launchButtonPanel = new JPanel();
		launchButtonPanel.setLayout(new BoxLayout(launchButtonPanel, BoxLayout.Y_AXIS));
		launchButtonPanel.add(addLaunch); 
		launchButtonPanel.add(removeLaunch); 
		launchButtonPanel.add(renameLaunch); 
		launchButtonPanel.add(runLaunch);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(topPanel, BorderLayout.NORTH);
		contentPanel.add(centerPanel, BorderLayout.CENTER);
		contentPanel.add(launchButtonPanel, BorderLayout.EAST);
		
		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
		
		initUI();
		adjustLaunchSelection();
	}
	
	private class SelectionListener implements ItemListener { 
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(e.getSource() == launchCombo){
					adjustLaunchSelection();
				}
			}
		}
	}
	
	void initUI(){
		
		ArrayList<LaunchConfig> configs = configStore.getConfigs();
		for(LaunchConfig config : configs){
			launchCombo.addItem(config);
		}
		launchCombo.addItemListener(selectionListener);
	}
	
	void clearUI(){
		
		launchCombo.removeItemListener(selectionListener);
		launchCombo.removeAllItems();
	}
	
	void refreshUI(LaunchConfig selected){

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
		adjustLaunchSelection();
	}
	
	private void adjustLaunchSelection() {
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			
		}
	}
	
	private void addLaunch(){
		
		String name = UiTools.inputDialog("New Launch", "");
		if(name != null && !name.equals("")){
			LaunchConfig config = new LaunchConfig(name);
			configStore.getConfigs().add(config);
			configStore.setDirty(true);
			configStore.notifyListeners();
			refreshUI(config);
		}
	}
	
	private void removeLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0 && UiTools.confirmDialog("Remove Launch ?")){
			configStore.getConfigs().remove(index);
			configStore.setDirty(true);
			configStore.notifyListeners();
			refreshUI(null);
		}
	}
	
	private void renameLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configStore.getConfigs().get(index);
			String name = UiTools.inputDialog("Rename Launch", config.getName());
			if(name != null && !name.equals("")){
				config.setName(name);
				configStore.setDirty(true);
				configStore.notifyListeners();
				refreshUI(config);
			}
		}
	}
	
	private void runLaunch(){
		
		int index = launchCombo.getSelectedIndex();
		if(index >= 0){
			LaunchConfig config = configStore.getConfigs().get(index);
			LaunchManager.getInstance().runLaunch(config.createLaunch());
		}
	}
}
