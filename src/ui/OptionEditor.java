package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import data.Option;
import data.OptionContainer;


import util.IChangedListener;
import util.KeyInput;

public class OptionEditor extends JPanel {

	private static final long serialVersionUID = 1L;

	private ArrayList<IChangedListener> listeners;
	private OptionContainer container;
	
	public OptionEditor(){
		
		listeners = new ArrayList<IChangedListener>();
		container = null;
		
		setLayout(new BorderLayout());
	}
	
	public void setOptionContainer(OptionContainer container) {
		
		removeAll();
		this.container = container;
		if(container != null){
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			for(Option option : container.getOptions()){
				panel.add(createPanel(option));
			}
			add(panel, BorderLayout.NORTH);
			setToolTipText(container.getDescription());
		}else{
			setToolTipText("");
		}
	}

	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	private Component createPanel(Option option) {
		
		switch(option.getType()){
			case TEXT:
				return createTextFieldPanel(option); 
			case TEXTAREA:
				return createTextAreaPanel(option); 
			case DATE:
				return createTextFieldPanel(option); 
			case INTEGER:
				return createIntegerSpinnerPanel(option);
			case BOOLEAN:
				return createCheckBoxPanel(option); 
		}
		return null;
	}
	
	private Component createPanel(String name, Component component, String orinetation) {
		
		JPanel panel = new JPanel(new BorderLayout());
		if(name != null){
			panel.add(new JLabel(" "+name+": "), BorderLayout.NORTH);
		}
		panel.add(component, orinetation);
		return panel;
	}

	private Component createTextFieldPanel(final Option option) {

		final JTextField component = new JTextField();
		component.setToolTipText(option.getDescription());
		component.setText(option.getStringValue());
		component.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(KeyInput.isModifyingKeyEvent(arg0)){
					String value = component.getText();
					container.getOption(option.getName()).setStringValue(value);
					notifyListeners();
				}
			}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		return createPanel(option.getName(), component, BorderLayout.CENTER);
	}

	private Component createTextAreaPanel(final Option option) {

		final JTextArea component = new JTextArea(5, 20);
		component.setToolTipText(option.getDescription());
		component.setText(option.getStringValue());
		component.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(KeyInput.isModifyingKeyEvent(arg0)){
					String value = component.getText();
					container.getOption(option.getName()).setStringValue(value);
					notifyListeners();
				}
			}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		return createPanel(option.getName(), new JScrollPane(component), BorderLayout.CENTER);
	}

	private Component createIntegerSpinnerPanel(final Option option) {
		
		SpinnerModel model = new SpinnerNumberModel(
				option.getIntegerValue(),
				option.getIntegerMinimum(),
				option.getIntegerMaximum(),
                1);  
		final JSpinner component = new JSpinner(model);
		((JSpinner.DefaultEditor)component.getEditor()).getTextField().setColumns(8);
		component.setToolTipText(
				option.getDescription()+" <"+option.getIntegerMinimum()+" - "+option.getIntegerMaximum()+">"
		);
		component.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = ((Integer)component.getValue()).intValue();
				container.getOption(option.getName()).setIntegerValue(value);
				notifyListeners();
			}
		});
		return createPanel(option.getName(), component, BorderLayout.WEST);
	}
	
	private Component createCheckBoxPanel(final Option option) {

		final JCheckBox component = new JCheckBox(option.getName());
		component.setToolTipText(option.getDescription());
		component.setSelected(option.getBooleanValue());
		component.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				boolean value = component.isSelected();
				container.getOption(option.getName()).setBooleanValue(value);
				notifyListeners();
			}
		});
		return createPanel(null, component, BorderLayout.WEST);
	}
}
