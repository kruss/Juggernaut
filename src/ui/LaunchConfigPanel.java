package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.IChangedListener;


import core.Application;
import data.LaunchConfig;

public class LaunchConfigPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private ConfigPanel parentPanel;
	private OptionEditor optionEditor;
	
	public LaunchConfigPanel(ConfigPanel parentPanel){
		
		this.parentPanel = parentPanel;
		application = Application.getInstance();
		optionEditor = new OptionEditor();

		setLayout(new BorderLayout());
		add(new JScrollPane(optionEditor), BorderLayout.CENTER);
		
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
			application.getConfig().notifyListeners();
		}
	}

	private void initUI() {

		LaunchConfig config = parentPanel.getCurrentConfig();
		if(config != null){
			optionEditor.setOptionContainer(config.getOptionContainer(), config);
		}else{
			optionEditor.setOptionContainer(null, null);
		}
	}
}
