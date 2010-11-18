package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import util.IChangeListener;


import launch.LaunchConfig;
import core.Application;

public class LaunchConfigPanel extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private ConfigPanel parentPanel;
	private OptionEditor optionEditor;
	
	public LaunchConfigPanel(ConfigPanel parentPanel){
		
		this.parentPanel = parentPanel;
		application = Application.getInstance();
		optionEditor = new OptionEditor();

		setLayout(new BorderLayout());
		add(optionEditor, BorderLayout.CENTER);
		
		parentPanel.addListener(this);
		optionEditor.addListener(this);
	}

	public void init() {
		initUI();
	}
	
	@Override
	public void changed(Object object) {
		
		if(object == parentPanel){
			initUI();
		}
		
		if(object == optionEditor){
			parentPanel.getCurrentConfig().setDirty(true);
			application.getConfiguration().notifyListeners();
		}
	}

	private void initUI() {

		LaunchConfig config = parentPanel.getCurrentConfig();
		if(config != null){
			optionEditor.setOptionContainer(config.getOptionContainer());
		}else{
			optionEditor.setOptionContainer(null);
		}
	}
}
