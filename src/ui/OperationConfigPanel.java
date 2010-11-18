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

import launch.LaunchConfig;

import operation.AbstractOperationConfig;
import util.UiTools;

import core.Application;
import core.ConfigStore;
import core.IChangeListener;

public class OperationConfigPanel extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private ConfigPanel parent;
	private JComboBox operationCombo;
	private JList operationList;
	private SelectionListener selectionListener;
	private JButton addOperation;
	private JButton removeOperation;
	private JButton moveOperationUp;
	private JButton moveOperationDown;
	private AbstractOperationConfig currentConfig;
	private OptionEditor optionEditor;
	
	public OperationConfigPanel(ConfigPanel parent){
		
		this.parent = parent;
		
		operationList = new JList();
		operationList.setToolTipText("Operations of the Launch");
		operationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		operationCombo = new JComboBox();
		operationCombo.setToolTipText("Available Operations");
		optionEditor = new OptionEditor();

		selectionListener = new SelectionListener();
		
		addOperation = new JButton(" Add ");
		addOperation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ addOperation(); }
		});
		removeOperation = new JButton(" Remove ");
		removeOperation.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ removeOperation(); }
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
		buttonPanel.add(removeOperation); 
		buttonPanel.add(moveOperationUp); 
		buttonPanel.add(moveOperationDown);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(operationCombo, BorderLayout.CENTER);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(operationList), 
				optionEditor);
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
		
		for(AbstractOperationConfig config : Application.getInstance().getOperationRegistry().getOperationConfigs()){
			operationCombo.addItem(config);
		}
	}

	@Override
	public void changed(Object object) {
		
		if(object == parent){
			refreshUI(null);
		}
		
		if(object == optionEditor){
			ConfigStore store = Application.getInstance().getConfigStore();
			parent.getCurrentConfig().setDirty(true);
			store.notifyListeners();
			operationList.repaint();
		}
	}
	
	public AbstractOperationConfig getCurrentConfig(){ return currentConfig; }
	
	private class SelectionListener implements ListSelectionListener { 

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getSource() == operationList){
				adjustSelection();
			}
		}
	}
	
	private void adjustSelection() {
		
		int listIndex = operationList.getSelectedIndex();
		if(listIndex >= 0){
			currentConfig = parent.getCurrentConfig().getOperationConfigs().get(listIndex);
			optionEditor.setOptionContainer(currentConfig.getOptionContainer());
			Application.getInstance().getFrame().setStatus("Operation ["+currentConfig.getId()+"]");
		}else{
			currentConfig = null;
			optionEditor.setOptionContainer(null);
		}
		parent.repaint();
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
	}
	
	private void clearUI(){
		
		operationList.removeListSelectionListener(selectionListener);
		operationList.removeAll();
	}
	
	private void refreshUI(AbstractOperationConfig selected){
		
		clearUI();
		initUI();
		if(selected != null){
			for(int i=0; i<operationList.getModel().getSize(); i++){
				AbstractOperationConfig config = (AbstractOperationConfig)operationList.getModel().getElementAt(i);
				if(config.getId().equals(config.getId())){
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
			AbstractOperationConfig operationConfig = (AbstractOperationConfig)operationCombo.getItemAt(comboIndex);
			LaunchConfig launchConfig = parent.getCurrentConfig();
			launchConfig.getOperationConfigs().add(listIndex >=0 ? listIndex : 0, operationConfig);
			launchConfig.setDirty(true);
			Application.getInstance().getConfigStore().notifyListeners();
			refreshUI(operationConfig);
		}
	}
	
	private void removeOperation(){
		
		int listIndex = operationList.getSelectedIndex();
		if(listIndex >= 0 && UiTools.confirmDialog("Remove Operation ?")){
			LaunchConfig launchConfig = parent.getCurrentConfig();
			launchConfig.getOperationConfigs().remove(listIndex);
			launchConfig.setDirty(true);
			Application.getInstance().getConfigStore().notifyListeners();
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
			Application.getInstance().getConfigStore().notifyListeners();
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
			Application.getInstance().getConfigStore().notifyListeners();
			refreshUI(operationConfig);
		}
	}
}
