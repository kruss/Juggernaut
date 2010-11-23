package ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import util.ILoggingListener;
import util.ILoggingProvider;

public class LoggingConsole extends JPanel implements ILoggingListener {

	private static final long serialVersionUID = 1L;

	private ILoggingProvider provider;
	
	private JTextArea console;
	
	public LoggingConsole(){
		
		provider = null;
		
		console = new JTextArea();
		console.setEditable(false);
		
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
		}
	}
	
	@Override
	public void logged(String log) {
		
		if(console.getLineCount() > 100){
			console.setSelectionStart(0);
			try{
				console.setSelectionEnd(console.getLineEndOffset(50));
			}catch(BadLocationException e){
				console.setSelectionEnd(console.getText().length()/2);
			}
			console.replaceSelection("...\n");
		}
		console.append(log);
		console.setCaretPosition(console.getText().length());
	}
	
	public void clearConsole(){
		console.setText("");
	}
}
