package ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import launch.ScheduleManager;

import util.IChangedListener;

import core.Configuration;
import core.History;

public class PreferencePanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;
	private ScheduleManager scheduleManager;
	private History history;
	
	private OptionEditor optionEditor;
	
	public PreferencePanel(
			Configuration configuration,
			ScheduleManager scheduleManager,
			History history)
	{
		this.configuration = configuration;
		this.scheduleManager = scheduleManager;
		this.history = history;
		
		optionEditor = new OptionEditor();
		
		setLayout(new BorderLayout());
		add(new JScrollPane(optionEditor), BorderLayout.CENTER);

		optionEditor.addListener(this);
	}
	
	public void init() {
		initUI();
	}
	
	private void initUI() {
		optionEditor.setOptionContainer(configuration.getOptionContainer(), configuration);
	}

	@Override
	public void changed(Object object) {
		
		if(object == optionEditor){
			applyChanges(configuration);
			configuration.setDirty(true);
			configuration.notifyListeners();
		}
	}
	
	private void applyChanges(Configuration configuration) {
		
		if(configuration.isScheduler()){
			scheduleManager.startScheduler(0);
		}else{
			scheduleManager.stopScheduler();
		}
		
		history.createIndex();
	}
}
