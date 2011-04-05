package ui.dialog;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import core.Constants;
import ui.option.IOptionDelegate;
import ui.option.OptionContainer;
import ui.option.OptionEditor;
import util.IChangeListener;
import util.UiTools;

public class OptionEditorDialog extends JDialog implements IOptionDelegate {

	private static final long serialVersionUID = 1L;
	
	private String name; 
	private OptionEditor optionEditor;
	
	public OptionEditorDialog(
			String name, 
			OptionContainer container, 
			IChangeListener listener) 
	{
		this.name = name; 
		
		optionEditor = new OptionEditor();
		optionEditor.setShowInfo(false);
		optionEditor.setOptionContainer(container, null);
		optionEditor.addListener(listener);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(optionEditor), BorderLayout.CENTER);
		
		add(centerPanel);
		pack();
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				dispose();
			}
		});
		
		UiTools.setLookAndFeel(this, Constants.APP_STYLE);
		setAlwaysOnTop(true); 
		setLocation(150, 150);
		setTitle(name);
		setVisible(false);
	}

	@Override
	public String getDelegateName() {
		return name;
	}

	@Override
	public void perform(String content) {
		setVisible(true);
	}
}
