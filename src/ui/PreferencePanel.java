package ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
		Configuration configuration = application.getConfig();
		optionEditor.setOptionContainer(configuration.getOptionContainer(), configuration);
	}

	@Override
	public void changed(Object object) {
		
		if(object == optionEditor){
			Configuration configuration = application.getConfig();
			applyChanges(configuration);
			configuration.setDirty(true);
			configuration.notifyListeners();
		}
	}
	
	private void applyChanges(Configuration configuration) {
		
		if(configuration.isScheduler()){
			application.getScheduleManager().startScheduler(0);
		}else{
			application.getScheduleManager().stopScheduler();
		}
		
		application.getHistory().createIndex();
	}
}
