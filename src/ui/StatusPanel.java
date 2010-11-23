package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import lifecycle.LaunchAgent;

import util.IChangedListener;
import util.StringTools;
import util.UiTools;

import core.Application;

public class StatusPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JTable launchTable;
	private DefaultTableModel tableModel;
	private JTextArea loggingConsole;
	private JButton stopLaunch;
	
	public StatusPanel()
	{
		application = Application.getInstance();
		
		tableModel = new DefaultTableModel();
		tableModel.addColumn("Launch");
		tableModel.addColumn("Start");
		tableModel.addColumn("Progress");
		tableModel.addColumn("Status");
		
		launchTable = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){ 
					return false;
			}
		};
		launchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		launchTable.setRowSelectionAllowed(true);
		launchTable.setColumnSelectionAllowed(false);
		
		TableColumnModel columnModel = launchTable.getColumnModel();
		columnModel.getColumn(0).setMinWidth(200);
		columnModel.getColumn(1).setMinWidth(150);
		columnModel.getColumn(1).setMaxWidth(150);
		columnModel.getColumn(2).setMinWidth(100);
		columnModel.getColumn(2).setMaxWidth(100);
		columnModel.getColumn(3).setMinWidth(150);
		columnModel.getColumn(3).setMaxWidth(150);
		
		loggingConsole = new JTextArea();
		loggingConsole.setEditable(false);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(launchTable), 
				new JScrollPane(loggingConsole));
		centerPanel.setDividerLocation(150);
		
		stopLaunch = new JButton(" Stop ");
		stopLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ stopLaunch(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(stopLaunch); 
		
		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		application.getLaunchManager().addListener(this);
	}
	
	public void init() {
		initUI();
	}

	private void clearUI() {
	
		for(int i=tableModel.getRowCount()-1; i>=0; i--){
			tableModel.removeRow(i);
		}
	}
	
	private void initUI() {
		
		for(LaunchAgent agent : application.getLaunchManager().getAgents()){
			
			Date start = agent.getStatusManager().getStart();
			Object[] rowData = {
				agent.getConfig().getName(),
				start != null ? StringTools.getTextDate(start) : "Starting",
				agent.getStatusManager().getProgress()+" %",
				agent.getStatusManager().getStatus().toString()
			};
			tableModel.addRow(rowData);
		}
	}

	private void refreshUI(LaunchAgent selected) {
		
		clearUI();
		initUI();
		if(selected != null){			
			for(int i=0; i<tableModel.getRowCount(); i++){
				LaunchAgent agent = application.getLaunchManager().getAgents().get(i);
				if(agent != null && agent.getConfig().getId().equals(selected.getConfig().getId())){
					launchTable.changeSelection(i, -1, false, false);
					break;
				}
			}
		}
	}
	
	@Override
	public void changed(Object object) {
		
		if(object == application.getLaunchManager()){
			int index = launchTable.getSelectedRow();
			if(index >=0 && index < application.getLaunchManager().getAgents().size()){				
				LaunchAgent agent = application.getLaunchManager().getAgents().get(index);
				refreshUI(agent);
			}else{
				refreshUI(null);
			}
		}
	}
	
	public void stopLaunch(){
		
		int index = launchTable.getSelectedRow();
		LaunchAgent agent = application.getLaunchManager().getAgents().get(index);
		if(agent != null && UiTools.confirmDialog("Stop launch ["+agent.getConfig().getName()+"]?")){
			agent.interrupt();
		}
	}
}
