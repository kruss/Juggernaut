package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import model.IModelListener;

import core.Application;

public class LaunchMenu extends JMenu implements IModelListener {

	private static final long serialVersionUID = 1L;

	private JMenuItem create;
	private JMenuItem open;
	private JMenuItem save;
	private JMenuItem close;
	
	public LaunchMenu(){
		
		super("Launch");
		
		create = new JMenuItem("Create");
		create.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Application.getInstance().getLogger().log(e.getActionCommand());
				create();
			}
		});
		add(create);
		
		open = new JMenuItem("Open");
		open.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Application.getInstance().getLogger().log(e.getActionCommand());
				open();
			}
		});
		add(open);
		
		
		save = new JMenuItem("Save");
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Application.getInstance().getLogger().log(e.getActionCommand());
				save();
			}
		});
		add(save);
		save.setEnabled(false);
		
		close = new JMenuItem("Close");
		close.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Application.getInstance().getLogger().log(e.getActionCommand());
				close();
			}
		});
		add(close);
		close.setEnabled(false);
	}
	
	@Override
	public void modelChanged(Status status) {
		
		if(status == Status.INIT){
			save.setEnabled(true);
			close.setEnabled(true);
		}
		if(status == Status.CLOSE){
			save.setEnabled(false);
			close.setEnabled(false);
		}
	}
	
	private void create(){
		
	}
	
	private void open(){
		
	}
	
	private void save(){
		
	}
	
	private void close(){
		
	}
}
