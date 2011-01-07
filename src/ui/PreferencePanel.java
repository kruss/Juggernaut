package ui;

import http.IHttpServer;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import launch.ScheduleManager;

import util.IChangedListener;
import util.UiTools;

import core.Configuration;
import core.History;
import core.ISystemComponent;

public class PreferencePanel extends JPanel implements ISystemComponent, IChangedListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;
	private ScheduleManager scheduleManager;
	private IHttpServer httpServer;
	private History history;
	
	private OptionEditor optionEditor;
	
	public PreferencePanel(
			Configuration configuration,
			ScheduleManager scheduleManager,
			IHttpServer httpServer,
			History history)
	{
		this.configuration = configuration;
		this.scheduleManager = scheduleManager;
		this.httpServer = httpServer;
		this.history = history;
		
		optionEditor = new OptionEditor();
		
		setLayout(new BorderLayout());
		add(new JScrollPane(optionEditor), BorderLayout.CENTER);

		optionEditor.addListener(this);
	}
	
	@Override
	public void init() throws Exception {
		
		initUI();
	}
	
	@Override
	public void shutdown() throws Exception {}
	
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
		
		try{
			if(configuration.isWebserver()){
				httpServer.startServer();
			}else{
				httpServer.stopServer();
			}
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
		
		history.createIndex();
	}
}
