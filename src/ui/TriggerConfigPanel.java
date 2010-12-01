package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import util.IChangedListener;
import util.UiTools;
import core.Application;
import data.AbstractTriggerConfig;
import data.LaunchConfig;

public class TriggerConfigPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private ConfigPanel parentPanel;
	private OptionEditor optionEditor;
	private SelectionListener selectionListener;
	private JComboBox triggerCombo;
	private JList triggerList;
	private JButton addTrigger;
	private JButton removeTrigger;
	private JButton moveTriggerUp;
	private JButton moveTriggerDown;
	private AbstractTriggerConfig currentConfig;
	
	public AbstractTriggerConfig getCurrentConfig(){ return currentConfig; }
	
	public TriggerConfigPanel(ConfigPanel parentPanel){
		
		this.parentPanel = parentPanel;
		application = Application.getInstance();
		optionEditor = new OptionEditor();		
		selectionListener = new SelectionListener();
		
		triggerList = new JList();
		triggerList.setToolTipText("Triggers of the Launch");
		triggerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		triggerCombo = new JComboBox();
		triggerCombo.setToolTipText("Available Triggers");
		
		addTrigger = new JButton(" Add ");
		addTrigger.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ addTrigger(); }
		});
		removeTrigger = new JButton(" Remove ");
		removeTrigger.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ removeTrigger(); }
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
		buttonPanel.add(removeTrigger); 
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
		
		parentPanel.addListener(this);
		optionEditor.addListener(this);
	}
	
	public void init() {
		
		initTriggerCombo();
		initUI();
		adjustSelection();
	}
	
	private void initTriggerCombo() {
		
		for(String triggerName : application.getRegistry().getTriggerNames()){
			triggerCombo.addItem(triggerName);
		}
	}

	@Override
	public void changed(Object object) {
		
		if(object == parentPanel){
			refreshUI(null);
		}
		
		if(object == optionEditor){
			parentPanel.getCurrentConfig().setDirty(true);
			application.getConfiguration().notifyListeners();
			triggerList.repaint();
		}
	}
	
	private class SelectionListener implements ListSelectionListener { 

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getSource() == triggerList){
				adjustSelection();
			}
		}
	}
	
	private void adjustSelection() {
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex >= 0){
			currentConfig = parentPanel.getCurrentConfig().getTriggerConfigs().get(listIndex);
			optionEditor.setOptionContainer(currentConfig.getOptionContainer());
		}else{
			currentConfig = null;
			optionEditor.setOptionContainer(null);
		}
		adjustButtons();
		parentPanel.repaint();
	}
	
	private void adjustButtons() {
		
		int listIndex = triggerList.getSelectedIndex();
		int listSize = triggerList.getModel().getSize();
		if(listIndex >= 0){
			removeTrigger.setEnabled(true);
		}else{
			removeTrigger.setEnabled(false);
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
		triggerList.removeAll();
	}
	
	private void initUI(){
		
		DefaultListModel listModel = new DefaultListModel();
		LaunchConfig config = parentPanel.getCurrentConfig();
		if(config != null){
			for(AbstractTriggerConfig triggerConfig : config.getTriggerConfigs()){
				listModel.addElement(triggerConfig);
			}
		}
		triggerList.setModel(listModel);
		triggerList.addListSelectionListener(selectionListener);
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
				AbstractTriggerConfig triggerConfig = application.getRegistry().createTriggerConfig(triggerName);
				LaunchConfig launchConfig = parentPanel.getCurrentConfig();
				launchConfig.getTriggerConfigs().add(listIndex >=0 ? listIndex+1 : 0, triggerConfig);
				launchConfig.setDirty(true);
				application.getConfiguration().notifyListeners();
				refreshUI(triggerConfig);
			}catch(Exception e){
				application.error(e);
			}
		}
	}
	
	private void removeTrigger(){
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex >= 0 && UiTools.confirmDialog("Remove Trigger ?")){
			LaunchConfig launchConfig = parentPanel.getCurrentConfig();
			launchConfig.getTriggerConfigs().remove(listIndex);
			launchConfig.setDirty(true);
			application.getConfiguration().notifyListeners();
			refreshUI(null);
		}
	}
	
	private void moveTriggerUp(){
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex > 0){
			LaunchConfig launchConfig = parentPanel.getCurrentConfig();
			AbstractTriggerConfig triggerConfig = launchConfig.getTriggerConfigs().remove(listIndex);
			launchConfig.getTriggerConfigs().add(listIndex-1, triggerConfig);
			launchConfig.setDirty(true);
			application.getConfiguration().notifyListeners();
			refreshUI(triggerConfig);
		}
	}
	
	private void moveTriggerDown(){
		
		int listIndex = triggerList.getSelectedIndex();
		if(listIndex >= 0 && listIndex < triggerList.getModel().getSize()-1){
			LaunchConfig launchConfig = parentPanel.getCurrentConfig();
			AbstractTriggerConfig triggerConfig = launchConfig.getTriggerConfigs().remove(listIndex);
			launchConfig.getTriggerConfigs().add(listIndex+1, triggerConfig);
			launchConfig.setDirty(true);
			application.getConfiguration().notifyListeners();
			refreshUI(triggerConfig);
		}
	}
}
