package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lifecycle.LaunchManager;

import util.IChangedListener;

import core.Application;
import core.Configuration;

public class PreferencePanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private OptionEditor optionEditor;
	
	public PreferencePanel(){
		
		application = Application.getInstance();
		optionEditor = new OptionEditor();
		
		setLayout(new BorderLayout());
		add(new JScrollPane(optionEditor), BorderLayout.CENTER);

		optionEditor.addListener(this);
	}
	
	public void init() {
		initUI();
	}
	
	private void initUI() {
		optionEditor.setOptionContainer(application.getConfiguration().getOptionContainer());
	}

	@Override
	public void changed(Object object) {
		
		if(object == optionEditor){
			Configuration configuration = application.getConfiguration();
			applyChanges(configuration);
			configuration.setDirty(true);
			configuration.notifyListeners();
		}
	}
	
	private void applyChanges(Configuration configuration) {
		
		LaunchManager launchManager = application.getLaunchManager();
		if(configuration.isScheduler()){
			launchManager.startScheduler(0);
		}else{
			launchManager.stopScheduler();
		}
		application.getLogger().setVerbose(configuration.isVerbose());
	}
}
