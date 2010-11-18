package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;


import launch.LaunchConfig;
import core.Application;
import core.ConfigStore;
import core.IChangeListener;

public class LaunchConfigPanel extends JPanel implements IChangeListener {

	private static final long serialVersionUID = 1L;

	private ConfigPanel parent;
	private OptionEditor optionEditor;
	
	public LaunchConfigPanel(ConfigPanel parent){
		
		this.parent = parent;
		optionEditor = new OptionEditor();

		setLayout(new BorderLayout());
		add(optionEditor, BorderLayout.CENTER);
		
		parent.addListener(this);
		optionEditor.addListener(this);
	}

	public void init() {
		initUI();
	}
	
	@Override
	public void changed(Object object) {
		
		if(object == parent){
			initUI();
		}
		
		if(object == optionEditor){
			ConfigStore store = Application.getInstance().getConfigStore();
			parent.getCurrentConfig().setDirty(true);
			store.notifyListeners();
		}
	}

	private void initUI() {

		LaunchConfig config = parent.getCurrentConfig();
		if(config != null){
			optionEditor.setOptionContainer(config.getOptionContainer());
		}else{
			optionEditor.setOptionContainer(null);
		}
	}
}
