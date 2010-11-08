package util;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.Option.Type;

import core.IChangeListener;


public class OptionEditor extends JPanel {

	private static final long serialVersionUID = 1L;

	private ArrayList<IChangeListener> listeners;
	private OptionContainer container;
	
	public OptionEditor(){
		
		listeners = new ArrayList<IChangeListener>();
		container = null;
	}
	
	public void setOptionContainer(OptionContainer container) {
		
		removeAll();
		this.container = container;
		
		if(container != null){
			JPanel optionPanel = new JPanel();
			optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
			for(Option option : container.getOptions()){
				optionPanel.add(createPanel(option));
			}
			
			setLayout(new BorderLayout());
			add(new JScrollPane(optionPanel), BorderLayout.CENTER);
		}
	}

	public void addListener(IChangeListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangeListener listener : listeners){
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
				return createTextFieldPanel(option);
			case BOOLEAN:
				return createCheckBoxPanel(option); 
		}
		return null;
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
				String value = component.getText();
				container.getOption(option.getName()).setStringValue(value);
				notifyListeners();
			}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		return createComponentPanel(option, component);
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
				String value = component.getText();
				container.getOption(option.getName()).setStringValue(value);
				notifyListeners();
			}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		return createComponentPanel(option, new JScrollPane(component));
	}

	private Component createCheckBoxPanel(final Option option) {

		final JCheckBox component = new JCheckBox();
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
		return createComponentPanel(option, component);
	}
	
	private Component createComponentPanel(Option option, Component component) {
		
		JPanel panel = new JPanel(new BorderLayout());
		if(
				option.getType() == Type.TEXT ||
				option.getType() == Type.TEXTAREA
		){
			panel.add(new JLabel(" "+option.getName()+": "), BorderLayout.NORTH);
		}else{
			panel.add(new JLabel(" "+option.getName()+": "), BorderLayout.WEST);
		}
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}
}
