package ui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import core.Application;

import util.FileTools;
import util.ILoggingListener;
import util.ILoggingProvider;

public class LoggingConsole extends JPanel implements ILoggingListener {

	public static final int UNLIMITED = 0;
	public static final int MAX_LINES = 1000;
	
	private static final long serialVersionUID = 1L;

	private Application application;
	private ILoggingProvider provider;
	private JTextArea console;
	
	private int maxLines;
	private boolean autoScrolling;
	
	public void setMaxLines(int lines){ maxLines = lines; }
	public void setAutoScrolling(boolean scrolling){ autoScrolling = scrolling; }
	
	public LoggingConsole(){
		
		application = Application.getInstance();
		provider = null;
		console = new JTextArea();
		console.setEditable(false);
		
		maxLines = MAX_LINES;
		autoScrolling = true;
		
		setLayout(new BorderLayout());
		add(new JScrollPane(console), BorderLayout.CENTER);
	}
	
	@Override
	public void setProvider(ILoggingProvider provider){ this.provider = provider; }
	@Override
	public ILoggingProvider getProvider(){ return provider; }
	
	@Override
	public void deregister(){
		if(provider != null){
			provider.removeListener(this);
			clearConsole();
		}
	}
	
	@Override
	public void logged(String log) {
		
		if(maxLines > 0){
			if(console.getLineCount() > maxLines){
				console.setSelectionStart(0);
				try{
					console.setSelectionEnd(console.getLineEndOffset(maxLines / 2));
				}catch(BadLocationException e){
					console.setSelectionEnd(console.getText().length()/2);
				}
				console.replaceSelection("...\n");
			}
		}
		console.append(log);
		if(autoScrolling){
			console.setCaretPosition(console.getText().length());
		}
	}
	
	public void clearConsole(){
		console.setText("");
	}

	public void initConsole(File logfile) {
		
		if(logfile.isFile()){
			try{
				console.setText(FileTools.readFile(logfile.getAbsolutePath()));
			}catch(Exception e){
				application.handleException(e);
			}
		}
	}
}
