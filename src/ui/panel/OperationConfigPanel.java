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
import core.launch.operation.AbstractOperationConfig;
import core.persistence.Configuration;
import core.runtime.Registry;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class OperationConfigPanel extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private ConfigPanel parent;
	private Configuration configuration;
	private Registry registry;
	private Logger logger;
	
	private OptionEditor optionEditor;
	private SelectionListener selectionListener;
	private KeyListener keySelectionListener;
	private JComboBox operationCombo;
	private JList operationList;
	private JButton addOperation;
	private JButton deleteOperation;
	private JButton moveOperationUp;
	private JButton moveOperationDown;
	private AbstractOperationConfig currentConfig;
	
	public AbstractOperationConfig getCurrentConfig(){ return currentConfig; }
	
	public OperationConfigPanel(
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
		
		operationList = new JList();
		operationList.setToolTipText("Operations of the Launch");
		operationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		operationCombo = new JComboBox();
		operationCombo.setToolTipText("Available Operations");
		
		addOperation = new JButton(" Add ");
		addOperation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ addOperation(); }
		});
		deleteOperation = new JButton(" Delete ");
		deleteOperation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ deleteOperation(); }
		});
		moveOperationUp = new JButton(" Up ");
		moveOperationUp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ moveOperationUp(); }
		});
		moveOperationDown = new JButton(" Down ");
		moveOperationDown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ moveOperationDown(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(addOperation); 
		buttonPanel.add(deleteOperation); 
		buttonPanel.add(moveOperationUp); 
		buttonPanel.add(moveOperationDown);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(operationCombo, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(operationList), 
				new JScrollPane(optionEditor));
		centerPanel.setDividerLocation(150);

		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		parent.addListener(this);
		optionEditor.addListener(this);
	}
	
	public void init() {
		
		initOperationCombo();
		initUI();
		adjustSelection();
	}
	
	private void initOperationCombo() {
		
		try{
			for(String operationName : registry.getOperationNames()){
				operationCombo.addItem(operationName);
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
			operationList.repaint();
		}
	}
	
	private class SelectionListener implements ListSelectionListener { 

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getSource() == operationList && e.getValueIsAdjusting()){
				adjustSelection();
			}
		}
	}
	
	private class KeySelectionListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getSource() == operationList){
				adjustSelection();
			}
		}
		@Override
		public void keyTyped(KeyEvent e) {}
	}
	
	private void adjustSelection() {
		
		int listIndex = operationList.getSelectedIndex();
		if(listIndex >= 0){
			currentConfig = parent.getCurrentConfig().getOperationConfigs().get(listIndex);
			optionEditor.setOptionContainer(currentConfig.getOptionContainer(), currentConfig);
		}else{
			currentConfig = null;
			optionEditor.setOptionContainer(null, null);
		}
		adjustButtons();
		parent.repaint();
	}
	
	private void adjustButtons() {
		
		int listIndex = operationList.getSelectedIndex();
		int listSize = operationList.getModel().getSize();
		if(listIndex >= 0){
			deleteOperation.setEnabled(true);
		}else{
			deleteOperation.setEnabled(false);
		}
		if(listIndex >=1){
			moveOperationUp.setEnabled(true);
		}else{
			moveOperationUp.setEnabled(false);
		}
		if(listIndex >=0 && listIndex < listSize-1 && listSize > 0){
			moveOperationDown.setEnabled(true);
		}else{
			moveOperationDown.setEnabled(false);
		}
	}
	
	private void clearUI(){
		
		operationList.removeListSelectionListener(selectionListener);
		operationList.removeKeyListener(keySelectionListener);
		operationList.removeAll();
	}
	
	private void initUI(){
		
		DefaultListModel listModel = new DefaultListModel();
		LaunchConfig config = parent.getCurrentConfig();
		if(config != null){
			for(AbstractOperationConfig operationConfig : config.getOperationConfigs()){
				listModel.addElement(operationConfig);
			}
		}
		operationList.setModel(listModel);
		operationList.addListSelectionListener(selectionListener);
		operationList.addKeyListener(keySelectionListener);
	}
	
	private void refreshUI(AbstractOperationConfig selected){
		
		clearUI();
		initUI();
		if(selected != null){
			for(int i=0; i<operationList.getModel().getSize(); i++){
				AbstractOperationConfig config = (AbstractOperationConfig)operationList.getModel().getElementAt(i);
				if(selected.getId().equals(config.getId())){
					operationList.setSelectedIndex(i);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private void addOperation(){
		
		int listIndex = operationList.getSelectedIndex();
		int comboIndex = operationCombo.getSelectedIndex();
		if(comboIndex >= 0){
			String operationName = (String)operationCombo.getItemAt(comboIndex);
			try{
				AbstractOperationConfig operationConfig = registry.createOperationConfig(operationName);
				if(operationConfig != null){
					LaunchConfig launchConfig = parent.getCurrentConfig();
					launchConfig.getOperationConfigs().add(listIndex >=0 ? listIndex+1 : 0, operationConfig);
					launchConfig.setDirty(true);
					configuration.notifyListeners();
					refreshUI(operationConfig);
				}
			}catch(Exception e){
				UiTools.errorDialog(e);
			}
		}
	}
	
	private void deleteOperation(){
		
		int listIndex = operationList.getSelectedIndex();
		if(listIndex >= 0 && UiTools.confirmDialog("Delete Operation ?")){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			launchConfig.getOperationConfigs().remove(listIndex);
			launchConfig.setDirty(true);
			configuration.notifyListeners();
			refreshUI(null);
		}
	}
	
	private void moveOperationUp(){
		
		int listIndex = operationList.getSelectedIndex();
		if(listIndex > 0){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			AbstractOperationConfig operationConfig = launchConfig.getOperationConfigs().remove(listIndex);
			launchConfig.getOperationConfigs().add(listIndex-1, operationConfig);
			launchConfig.setDirty(true);
			configuration.notifyListeners();
			refreshUI(operationConfig);
		}
	}
	
	private void moveOperationDown(){
		
		int listIndex = operationList.getSelectedIndex();
		if(listIndex >= 0 && listIndex < operationList.getModel().getSize()-1){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			AbstractOperationConfig operationConfig = launchConfig.getOperationConfigs().remove(listIndex);
			launchConfig.getOperationConfigs().add(listIndex+1, operationConfig);
			launchConfig.setDirty(true);
			configuration.notifyListeners();
			refreshUI(operationConfig);
		}
	}
}
