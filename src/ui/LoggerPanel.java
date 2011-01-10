package ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.IChangeListener;

import logger.Logger;

import core.Configuration;
import core.ISystemComponent;

public class LoggerPanel extends JPanel implements ISystemComponent, IChangeListener {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;
	private LoggingConsole loggingConsole;
	private OptionEditor optionEditor;
	
	public LoggerPanel(Configuration configuration, Logger logger){
		
		this.configuration = configuration;
		
		loggingConsole = new LoggingConsole();
		optionEditor = new OptionEditor();
		
		setLayout(new BorderLayout());
		add(loggingConsole, BorderLayout.CENTER);
		add(new JScrollPane(optionEditor), BorderLayout.EAST);

		logger.addListener(loggingConsole);
		optionEditor.addListener(this);
	}
	
	@Override
	public void init() throws Exception {
		
		initUI();
	}
	
	@Override
	public void shutdown() throws Exception {}
	
	private void initUI() {
		optionEditor.setOptionContainer(configuration.getLogConfig().getOptionContainer(), configuration.getLogConfig());
	}

	@Override
	public void changed(Object object) {
		
		if(object == optionEditor){
			applyChanges();
			configuration.getLogConfig().setDirty(true);
			configuration.notifyListeners();
		}
	}
	
	private void applyChanges() {}
}
