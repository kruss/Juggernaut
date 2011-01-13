package ui.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import core.runtime.logger.ILogListener;
import core.runtime.logger.ILogProvider;

public class LoggerConsole extends JPanel implements ILogListener {

	public static final int UNLIMITED = 0;
	public static final int MAX_LINES = 500;
	
	private static final long serialVersionUID = 1L;

	private ILogProvider provider;
	private JPopupMenu popup;
	private JTextArea console;
	
	private int maxLines;
	private boolean autoScrolling;
	
	public void setMaxLines(int lines){ maxLines = lines; }
	public void setAutoScrolling(boolean scrolling){ autoScrolling = scrolling; }
	
	public LoggerConsole(){
		
		provider = null;
		maxLines = MAX_LINES;
		autoScrolling = true;
		
		popup = new JPopupMenu();
		JMenuItem clearConsole = new JMenuItem("Clear");
		clearConsole.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				console.setText(""); 
			}
		});
		popup.add(clearConsole);
		JCheckBoxMenuItem pinConsole = new JCheckBoxMenuItem("Pin");
		pinConsole.setSelected(!autoScrolling);
		pinConsole.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
				autoScrolling = !item.isSelected();
			}
		});
		popup.add(pinConsole);
		
		console = new JTextArea();
		console.setEditable(false);
		console.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON3){
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		
		setLayout(new BorderLayout());
		add(new JScrollPane(console), BorderLayout.CENTER);
	}
	
	@Override
	public void setProvider(ILogProvider provider){ this.provider = provider; }
	@Override
	public ILogProvider getProvider(){ return provider; }
	
	@Override
	public void deregister(){
		if(provider != null){
			provider.removeListener(this);
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

	public void initConsole(String buffer) {
		console.setText(buffer);
	}
}
