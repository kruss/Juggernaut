package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;


import core.Application;

public class ProjectMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	private JMenuItem save;
	private JMenuItem close;
	
	public ProjectMenu(){
		
		super("Project");
		
		save = new JMenuItem("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ save(); }
		});
		add(save);
		
		close = new JMenuItem("Close");
		close.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ close(); }
		});
		add(close);
	}
	
	private void save(){
		
		try{
			Application.getInstance().getConfigStore().save();
		}catch(Exception e){
			Application.getInstance().handleException(e);
		}
	}
	
	private void close(){
		
		Application.getInstance().shutdown();
	}
}
