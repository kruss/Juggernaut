package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import operation.AbstractOperationConfig;
import operation.OperationRegistry;

import core.Application;
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
		
		operationCombo = new JComboBox();
		operationCombo.setToolTipText("Available Operations");
		operationList = new JList();
		operationList.setToolTipText("Operations of the Launch");
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
		
		JPanel centerPanel = new JPanel();
		// TODO
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(centerPanel, BorderLayout.CENTER);
		contentPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
		
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
	
	private class SelectionListener implements ItemListener { 
		
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED){
				if(e.getSource() == operationList){
					adjustSelection();
				}
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
		
	}
	
	private void clearUI(){
		
	}
	
	private void refreshUI(AbstractOperationConfig selected){
		
	}
	
	private void addOperation(){
		
	}
	
	private void removeOperation(){
		
	}
	
	private void moveOperationUp(){
		
	}
	
	private void moveOperationDown(){
		
	}
}
