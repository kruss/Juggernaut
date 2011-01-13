package ui.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import util.IChangeListener;
import util.UiTools;
import core.Constants;
import core.ISystemComponent;
import core.runtime.TaskManager;
import core.runtime.TaskManager.TaskInfo;

public class TaskMonitor extends JDialog implements ISystemComponent, IChangeListener {
	
	private static final long serialVersionUID = 1L;
	
	private TaskManager taskManager;
	private DefaultTableModel tableModel;
	private JTable taskTable;
	private JButton killTask;
	
	private ArrayList<TaskInfo> tasks;
	
	public TaskMonitor(TaskManager taskManager){
		
		this.taskManager = taskManager;
		tasks = new ArrayList<TaskInfo>();
		
		tableModel = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public Object getValueAt(int row, int column){
				try{
					return super.getValueAt(row, column);
				}catch(Exception e){
					return null;
				}
			}
		};
		tableModel.addColumn("Task");
		tableModel.addColumn("Id");
		tableModel.addColumn("Status");
		
		taskTable = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){ 
					return false;
			}
		};
		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskTable.setRowSelectionAllowed(true);
		taskTable.setColumnSelectionAllowed(false);
		
		taskTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				adjustSelection();
			}
		});
		taskTable.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				adjustSelection();
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		
		TableColumnModel columnModel = taskTable.getColumnModel();
		columnModel.getColumn(0).setMinWidth(150);
		columnModel.getColumn(1).setMinWidth(50);
			columnModel.getColumn(1).setMaxWidth(50);
		columnModel.getColumn(2).setMinWidth(100);
			columnModel.getColumn(2).setMaxWidth(100);
			
		killTask = new JButton(" Kill ");
		killTask.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ killTask(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(killTask);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
		centerPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		add(centerPanel);
		pack();
					
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				showDialog(false); 
			}
		});
		
		UiTools.setLookAndFeel(this, Constants.APP_STYLE);
		setAlwaysOnTop(true);
		setSize(400, 200); 
		setLocation(75, 75);
		setVisible(false);
	}

	@Override
	public void init() throws Exception {
		initUI();
		adjustSelection();
	}

	@Override
	public void shutdown() throws Exception {
		showDialog(false);
	}
	
	public void showDialog(boolean visible){
		
		if(visible){
			if(!isVisible()){
				taskManager.addListener(this);
				updateUI();
			}
		}else{
			if(isVisible()){
				taskManager.removeListener(this);
				clearUI();
			}
		}
		setVisible(visible);
	}

	private void clearUI() {
		
		taskTable.clearSelection();
		for(int i=tableModel.getRowCount()-1; i>=0; i--){
			tableModel.removeRow(i);
		}
	}
	
	private void initUI() {
		
		int running = 0;
		int idle = 0;
		for(TaskInfo task : tasks){
			Object[] rowData = {
				task.name,
				task.id,
				task.running ? "RUNNING" : "IDLE",
			};
			tableModel.addRow(rowData);
			if(task.running){
				running++;
			}else{
				idle++;
			}
		}
		
		setTitle("Task-Monitor ("+tasks.size()+")");
		taskTable.setToolTipText("Tasks ("+running+"/"+(running+idle)+" running)");
	}

	private void refreshUI(TaskInfo selected) {
		
		clearUI();
		initUI();
		if(selected != null){	
			for(int i=0; i<tasks.size(); i++){
				if(tasks.get(i).name.equals(selected.name)){
					taskTable.changeSelection(i, -1, false, false);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private synchronized void updateUI() {
		
		TaskInfo selected = getSelectedTask();
		tasks = taskManager.getInfo();
		refreshUI(selected);
	}
	
	private void adjustSelection() {
		
		TaskInfo selected = getSelectedTask();
		if(selected != null){
			killTask.setEnabled(true);
		}else{
			killTask.setEnabled(false);
		}
	}
	
	@Override
	public void changed(Object object) {
		
		if(object instanceof TaskManager){
			updateUI();
		}
	}
	
	private TaskInfo getSelectedTask() {
		
		TaskInfo selected = null;
		int index = taskTable.getSelectedRow();
		if(index >=0){				
			selected = tasks.get(index);
		}
		return selected;
	}
	
	private void killTask(){
		
		TaskInfo selected = getSelectedTask();
		if(UiTools.confirmDialog("Kill [ "+selected.name+" ("+selected.id+") ] Task ?")){
			taskManager.kill(selected.id);
		}
	}
}
