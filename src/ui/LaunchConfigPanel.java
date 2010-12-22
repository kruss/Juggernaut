package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.IChangedListener;

import core.Configuration;
import data.LaunchConfig;

public class LaunchConfigPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private ConfigPanel parent;
	private Configuration configuration;
	
	private OptionEditor optionEditor;
	
	public LaunchConfigPanel(ConfigPanel parent, Configuration configuration){
		
		this.parent = parent;
		this.configuration = configuration;
		
		optionEditor = new OptionEditor();

		setLayout(new BorderLayout());
		add(new JScrollPane(optionEditor), BorderLayout.CENTER);
		
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
			parent.getCurrentConfig().setDirty(true);
			configuration.notifyListeners();
		}
	}

	private void initUI() {

		LaunchConfig config = parent.getCurrentConfig();
		if(config != null){
			optionEditor.setOptionContainer(config.getOptionContainer(), config);
		}else{
			optionEditor.setOptionContainer(null, null);
		}
	}
}
