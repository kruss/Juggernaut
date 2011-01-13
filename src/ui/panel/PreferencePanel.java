package ui.panel;


import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


import ui.option.OptionEditor;
import util.IChangeListener;
import util.UiTools;

import core.ISystemComponent;
import core.persistence.Configuration;
import core.runtime.ScheduleManager;
import core.runtime.http.IHttpServer;

public class PreferencePanel extends JPanel implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;
	private ScheduleManager scheduleManager;
	private IHttpServer httpServer;
	
	private OptionEditor optionEditor;
	
	public PreferencePanel(
			Configuration configuration,
			ScheduleManager scheduleManager,
			IHttpServer httpServer)
	{
		this.configuration = configuration;
		this.scheduleManager = scheduleManager;
		this.httpServer = httpServer;
		
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
			applyChanges();
			configuration.setDirty(true);
			configuration.notifyListeners();
		}
	}
	
	private void applyChanges() {
		
		try{
			if(configuration.isScheduler()){
				scheduleManager.startScheduler(0);
			}else{
				scheduleManager.stopScheduler();
			}
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
		
		try{
			if(configuration.isHttpServer()){
				httpServer.startServer();
			}else{
				httpServer.stopServer();
			}
		}catch(Exception e){
			UiTools.errorDialog(e);
		}
	}
}
