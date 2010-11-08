package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import launch.LaunchConfig;

import operation.AbstractOperationConfig;
import operation.OperationRegistry;

import core.Application;
import core.ConfigStore;
import core.IChangeListener;

public class ConfigPanelOperation extends JPanel implements IChangeListener {

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
	
	public ConfigPanelOperation(ConfigPanel parent){
		
		this.parent = parent;
		
		operationList = new JList();
		operationList.setToolTipText("Operations of the Launch");
		operationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		operationCombo = new JComboBox();
		operationCombo.setToolTipText("Available Operations");

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
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(operationList), BorderLayout.WEST);

		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		parent.addListener(this);
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
		
		if(object instanceof ConfigPanel){
			refreshUI(null);
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
		
		int index = operationList.getSelectedIndex();
		if(index >= 0){
			currentConfig = parent.getLaunchConfig().getOperationConfigs().get(index);
		}else{
			currentConfig = null;
		}
	}
	
	private void initUI(){
		
		DefaultListModel listModel = new DefaultListModel();
		LaunchConfig config = parent.getLaunchConfig();
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
		if(selected != null){
			for(int i=0; i<operationList.getModel().getSize(); i++){
				AbstractOperationConfig config = (AbstractOperationConfig)operationList.getModel().getElementAt(i);
				if(config.getId().equals(config.getId())){
					operationList.setSelectedIndex(i);
					break;
				}
			}
		}
		initUI();
	}
	
	private void addOperation(){
		
		int index = operationCombo.getSelectedIndex();
		if(index >= 0){
			AbstractOperationConfig operationConfig = (AbstractOperationConfig)operationCombo.getItemAt(index);
			ConfigStore store = Application.getInstance().getConfigStore();
			LaunchConfig launchConfig = parent.getLaunchConfig();
			launchConfig.getOperationConfigs().add(operationConfig);
			launchConfig.setDirty(true);
			store.notifyListeners();
			refreshUI(null);
		}
	}
	
	private void removeOperation(){
		
	}
	
	private void moveOperationUp(){
		
	}
	
	private void moveOperationDown(){
		
	}
}
