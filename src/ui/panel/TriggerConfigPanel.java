package ui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import ui.option.OptionEditor;
import util.IChangeListener;
import util.UiTools;
import core.launch.LaunchConfig;
import core.launch.trigger.AbstractTriggerConfig;
import core.persistence.Configuration;
import core.runtime.Registry;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class TriggerConfigPanel extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private ConfigPanel parent;
	private Configuration configuration;
	private Registry registry;
	private Logger logger;
	
	private OptionEditor optionEditor;
	private SelectionListener selectionListener;
	private KeyListener keySelectionListener;
	private JComboBox triggerCombo;
	private JList triggerList;
	private JButton addTrigger;
	private JButton deleteTrigger;
	private JButton moveTriggerUp;
	private JButton moveTriggerDown;
	private AbstractTriggerConfig currentConfig;
	
	public AbstractTriggerConfig getCurrentConfig(){ return currentConfig; }
	
	public TriggerConfigPanel(
			ConfigPanel parent, 
			Configuration configuration, 
			Registry registry,
			Logger logger)
	{
		this.parent = parent;
		this.configuration = configuration;
		this.registry = registry;
		this.logger = logger;

		optionEditor = new OptionEditor();		
		selectionListener = new SelectionListener();
		keySelectionListener = new KeySelectionListener();
		
		triggerList = new JList();
		triggerList.setToolTipText("Triggers of the Launch");
		triggerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		triggerCombo = new JComboBox();
		triggerCombo.setToolTipText("Available Triggers");
		
		addTrigger = new JButton(" Add ");
		addTrigger.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ addTrigger(); }
		});
		deleteTrigger = new JButton(" Remove ");
		deleteTrigger.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ deleteTrigger(); }
		});
		moveTriggerUp = new JButton(" Up ");
		moveTriggerUp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ moveTriggerUp(); }
		});
		moveTriggerDown = new JButton(" Down ");
		moveTriggerDown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ moveTriggerDown(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addTrigger); 
		buttonPanel.add(deleteTrigger); 
		buttonPanel.add(moveTriggerUp); 
		buttonPanel.add(moveTriggerDown);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(triggerCombo, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(triggerList), 
				new JScrollPane(optionEditor));
		centerPanel.setDividerLocation(150);

		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		parent.addListener(this);
		optionEditor.addListener(this);
	}
	
	public void init() {
		
		initTriggerCombo();
		initUI();
		adjustSelection();
	}
	
	private void initTriggerCombo() {
		
		try{
			for(String triggerName : registry.getTriggerNames()){
				triggerCombo.addItem(triggerName);
			}
		}catch(Exception e){
			logger.error(Module.COMMON, e);
		}
	}

	@Override
	public void changed(Object object) {
		
		if(object == parent){
			refreshUI(null);
		}
		
		if(object == optionEditor){
			parent.getCurrentConfig().setDirty(true);
			configuration.notifyListeners();
			triggerList.repaint();
		}
	}
	
	private class SelectionListener implements ListSelectionListener { 

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getSource() == triggerList && e.getValueIsAdjusting()){
				adjustSelection();
			}
		}
	}
	
	private class KeySelectionListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getSource() == triggerList){
				adjustSelection();
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {}
	}
	
	private void adjustSelection() {
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex >= 0){
			currentConfig = parent.getCurrentConfig().getTriggerConfigs().get(listIndex);
			optionEditor.setOptionContainer(currentConfig.getOptionContainer(), currentConfig);
		}else{
			currentConfig = null;
			optionEditor.setOptionContainer(null, null);
		}
		adjustButtons();
		parent.repaint();
	}
	
	private void adjustButtons() {
		
		int listIndex = triggerList.getSelectedIndex();
		int listSize = triggerList.getModel().getSize();
		if(listIndex >= 0){
			deleteTrigger.setEnabled(true);
		}else{
			deleteTrigger.setEnabled(false);
		}
		if(listIndex >=1){
			moveTriggerUp.setEnabled(true);
		}else{
			moveTriggerUp.setEnabled(false);
		}
		if(listIndex >=0 && listIndex < listSize-1 && listSize > 0){
			moveTriggerDown.setEnabled(true);
		}else{
			moveTriggerDown.setEnabled(false);
		}
	}
	
	private void clearUI(){
		
		triggerList.removeListSelectionListener(selectionListener);
		triggerList.removeKeyListener(keySelectionListener);
		triggerList.removeAll();
	}
	
	private void initUI(){
		
		DefaultListModel listModel = new DefaultListModel();
		LaunchConfig config = parent.getCurrentConfig();
		if(config != null){
			for(AbstractTriggerConfig triggerConfig : config.getTriggerConfigs()){
				listModel.addElement(triggerConfig);
			}
		}
		triggerList.setModel(listModel);
		triggerList.addListSelectionListener(selectionListener);
		triggerList.addKeyListener(keySelectionListener);
	}
	
	private void refreshUI(AbstractTriggerConfig selected){
		
		clearUI();
		initUI();
		if(selected != null){
			for(int i=0; i<triggerList.getModel().getSize(); i++){
				AbstractTriggerConfig config = (AbstractTriggerConfig)triggerList.getModel().getElementAt(i);
				if(selected.getId().equals(config.getId())){
					triggerList.setSelectedIndex(i);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private void addTrigger(){
		
		int listIndex = triggerList.getSelectedIndex();
		int comboIndex = triggerCombo.getSelectedIndex();
		if(comboIndex >= 0){
			String triggerName = (String)triggerCombo.getItemAt(comboIndex);
			try{
				AbstractTriggerConfig triggerConfig = registry.createTriggerConfig(triggerName);
				if(triggerConfig != null){
					LaunchConfig launchConfig = parent.getCurrentConfig();
					launchConfig.getTriggerConfigs().add(listIndex >=0 ? listIndex+1 : 0, triggerConfig);
					launchConfig.setDirty(true);
					configuration.notifyListeners();
					refreshUI(triggerConfig);
				}
			}catch(Exception e){
				UiTools.errorDialog(e);
			}
		}
	}
	
	private void deleteTrigger(){
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex >= 0 && UiTools.confirmDialog("Delete Trigger ?")){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			launchConfig.getTriggerConfigs().remove(listIndex);
			launchConfig.setDirty(true);
			configuration.notifyListeners();
			refreshUI(null);
		}
	}
	
	private void moveTriggerUp(){
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex > 0){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			AbstractTriggerConfig triggerConfig = launchConfig.getTriggerConfigs().remove(listIndex);
			launchConfig.getTriggerConfigs().add(listIndex-1, triggerConfig);
			launchConfig.setDirty(true);
			configuration.notifyListeners();
			refreshUI(triggerConfig);
		}
	}
	
	private void moveTriggerDown(){
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex >= 0 && listIndex < triggerList.getModel().getSize()-1){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			AbstractTriggerConfig triggerConfig = launchConfig.getTriggerConfigs().remove(listIndex);
			launchConfig.getTriggerConfigs().add(listIndex+1, triggerConfig);
			launchConfig.setDirty(true);
			configuration.notifyListeners();
			refreshUI(triggerConfig);
		}
	}
}
