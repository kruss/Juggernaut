package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import util.IChangedListener;


import core.Application;

public class ProjectMenu extends JMenu implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JMenuItem revert;
	private JMenuItem save;
	private JMenuItem quit;
	
	public ProjectMenu(){
		super("Project");
		
		application = Application.getInstance();
		
		revert = new JMenuItem("Revert");
		revert.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ revert(); }
		});
		add(revert);
		
		save = new JMenuItem("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ save(); }
		});
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		add(save);
		
		quit = new JMenuItem("Quit");
		quit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ quit(); }
		});
		quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		add(quit);
		
		application.getConfiguration().addListener(this);
	}
	
	private void revert(){
		
		try{
			Application.getInstance().revert();
		}catch(Exception e){
			Application.getInstance().handleException(e);
		}
	}
	
	private void save(){
		
		try{
			Application.getInstance().getConfiguration().save();
		}catch(Exception e){
			Application.getInstance().handleException(e);
		}
	}
	
	private void quit(){
		
		Application.getInstance().shutdown();
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getConfiguration()){
			if(application.getConfiguration().isDirty()){
				revert.setEnabled(true);
				save.setEnabled(true);
			}else{
				revert.setEnabled(false);
				save.setEnabled(false);
			}
		}
	}
}
