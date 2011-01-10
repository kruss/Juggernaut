package ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import logger.Logger;

import core.ISystemComponent;

public class LoggerPanel extends JPanel implements ISystemComponent {

	private static final long serialVersionUID = 1L;

	private LoggingConsole console;
	
	public LoggerPanel(Logger logger){
		
		console = new LoggingConsole();
		
		setLayout(new BorderLayout());
		add(console, BorderLayout.CENTER);

		logger.addListener(console);
	}
	
	@Override
	public void init() throws Exception {}
	
	@Override
	public void shutdown() throws Exception {}
}
