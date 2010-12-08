package ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import launch.LaunchManager;

import util.IChangedListener;
import util.Logger;

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
		Configuration configuration = application.getConfiguration();
		optionEditor.setOptionContainer(configuration.getOptionContainer(), configuration);
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
		
		// handle logger settings
		Logger.VERBOSE = configuration.isVerbose();
		
		// handle scheduler settings
		LaunchManager launchManager = application.getLaunchManager();
		if(configuration.isScheduler()){
			launchManager.startScheduler(0);
		}else{
			launchManager.stopScheduler();
		}
		
		// handle history settings
		application.getHistory().update();
	}
}
