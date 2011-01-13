package ui.option;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



import util.IChangeListener;
import util.StringTools;

public class OptionEditor extends JPanel {

	private static final long serialVersionUID = 1L;

	private ArrayList<IChangeListener> listeners;
	private OptionContainer container;
	private boolean groups;
	
	public OptionEditor(){
		
		listeners = new ArrayList<IChangeListener>();
		container = null;
		groups = true;
		
		setLayout(new BorderLayout());
	}
	
	public void setGroups(boolean groups){ this.groups = groups; }
	
	public void setOptionContainer(OptionContainer container, IOptionInitializer initializer) {
		
		removeAll();
		this.container = container;
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		if(container != null){
			String groupName = "";
			JPanel groupPanel = null;
			for(Option option : container.getOptions()){
				if(!groupName.equals(option.getGroup())){
					groupName = option.getGroup();
					if(groupPanel != null){
						centerPanel.add(groupPanel);
						groupPanel = null;
					}
					groupPanel = new JPanel();
					groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
					if(groups){
						groupPanel.setBorder(BorderFactory.createTitledBorder(groupName));
					}
				}
				JPanel panel = createPanel(option);
				option.parent = panel;
				groupPanel.add(panel);
			}
			if(groupPanel != null){
				centerPanel.add(groupPanel);
				groupPanel = null;
			}
			add(centerPanel, BorderLayout.NORTH);
			setToolTipText(container.getDescription());
			if(initializer != null){
				initializer.initOptions(container);
			}
		}else{
			setToolTipText(null);
		}
	}

	public void addListener(IChangeListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangeListener listener : listeners){
			listener.changed(this);
		}
	}
	
	private JPanel createPanel(Option option) {
		
		switch(option.getType()){
			case TEXT:
				return createTextFieldPanel(option); 
			case TEXT_SMALL:
				return createSmallTextFieldPanel(option);
			case TEXT_AREA:
				return createTextAreaPanel(option); 
			case TEXT_LIST:
				return createTextListPanel(option);
			case DATE:
				return createTextFieldPanel(option); 
			case INTEGER:
				return createIntegerSpinnerPanel(option);
			case BOOLEAN:
				return createCheckBoxPanel(option); 
		}
		return null;
	}
	
	private JPanel createTextFieldPanel(final Option option) {

		final JTextField component = new JTextField();
		option.component = component;
		component.setColumns(25);
		component.setToolTipText(option.getDescription());
		component.setText(option.getStringValue());
		component.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				if(StringTools.isModifyingKey(e)){
					String value = component.getText();
					container.getOption(option.getName()).setStringValue(value);
					notifyListeners();
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(option.getUIName()+":"), BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createSmallTextFieldPanel(final Option option) {

		final JTextField component = new JTextField();
		option.component = component;
		component.setColumns(25);
		component.setToolTipText(option.getDescription());
		component.setText(option.getStringValue());
		component.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				if(StringTools.isModifyingKey(e)){
					String value = component.getText();
					container.getOption(option.getName()).setStringValue(value);
					notifyListeners();
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(option.getUIName()+":"), BorderLayout.NORTH);
		panel.add(component, BorderLayout.WEST);
		return panel;
	}
	
	private JPanel createTextAreaPanel(final Option option) {

		final JTextArea component = new JTextArea(5, 25);
		option.component = component;
		component.setToolTipText(option.getDescription());
		component.setText(option.getStringValue());
		component.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				if(StringTools.isModifyingKey(e)){
					String value = component.getText();
					container.getOption(option.getName()).setStringValue(value);
					notifyListeners();
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(option.getUIName()+":"), BorderLayout.NORTH);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createTextListPanel(final Option option) {

		final JComboBox component = new JComboBox();
		option.component = component;
		component.setToolTipText(option.getDescription());
		for(int i=0; i<option.getListSize(); i++){
			component.addItem(option.getListItem(i));
		}
		component.setSelectedItem(option.getStringValue());
		component.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					int index = component.getSelectedIndex();
					String value = (String)component.getItemAt(index);
					container.getOption(option.getName()).setStringValue(value);
					notifyListeners();
				}
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(option.getUIName()+":"), BorderLayout.NORTH);
		panel.add(component, BorderLayout.WEST);
		return panel;
	}
	
	private JPanel createIntegerSpinnerPanel(final Option option) {
		
		SpinnerModel model = new SpinnerNumberModel(
				option.getIntegerValue(),
				option.getIntegerMinimum(),
				option.getIntegerMaximum(),
                1);  
		final JSpinner component = new JSpinner(model);
		option.component = component;
		((JSpinner.DefaultEditor)component.getEditor()).getTextField().setColumns(8);
		component.setToolTipText(
				option.getDescription()+" : "+option.getIntegerMinimum()+" - "+option.getIntegerMaximum()
		);
		component.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				int value = ((Integer)component.getValue()).intValue();
				container.getOption(option.getName()).setIntegerValue(value);
				notifyListeners();
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(option.getUIName()+":"), BorderLayout.NORTH);
		panel.add(component, BorderLayout.WEST);
		return panel;
	}
	
	private JPanel createCheckBoxPanel(final Option option) {

		final JCheckBox component = new JCheckBox(option.getUIName());
		option.component = component;
		component.setToolTipText(option.getDescription());
		component.setSelected(option.getBooleanValue());
		component.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean value = component.isSelected();
				container.getOption(option.getName()).setBooleanValue(value);
				notifyListeners();
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(component, BorderLayout.WEST);
		return panel;
	}
	
	public static void setOptionDelegate(final Option option, final IOptionDelegate delegate){
		
		JButton button = new JButton(" "+delegate.getName()+" ");
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				delegate.perform(option.getStringValue());
			}
		});
		option.parent.add(button, BorderLayout.EAST);
	}
}