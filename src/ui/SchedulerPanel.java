package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import launch.LaunchManager;
import launch.ScheduleManager;
import launch.LaunchManager.LaunchInfo;
import logger.ILogProvider;
import logger.Logger;

import util.IChangedListener;
import util.StringTools;
import util.UiTools;

public class SchedulerPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private LaunchManager launchManager;
	private ScheduleManager scheduleManager;
	
	private JScrollPane launchPanel;
	private JTable launchTable;
	private DefaultTableModel tableModel;
	private JTabbedPane loggingPanel;
	private LoggingConsole applicationConsole;
	private LoggingConsole launchConsole;
	private JButton triggerScheduler;
	private JButton stopLaunch;
	
	private ArrayList<LaunchInfo> launches;
	
	public SchedulerPanel(
			LaunchManager launchManager,
			ScheduleManager scheduleManager,
			Logger logger)
	{
		this.launchManager = launchManager;
		this.scheduleManager = scheduleManager;
		
		launches = new ArrayList<LaunchInfo>();
		
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
		tableModel.addColumn("Launch");
		tableModel.addColumn("Trigger");
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
		columnModel.getColumn(0).setMinWidth(150);
		columnModel.getColumn(1).setMinWidth(200);
		columnModel.getColumn(2).setMinWidth(150);
			columnModel.getColumn(2).setMaxWidth(150);
		columnModel.getColumn(3).setMinWidth(100);
			columnModel.getColumn(3).setMaxWidth(100);
		columnModel.getColumn(4).setMinWidth(150);
			columnModel.getColumn(4).setMaxWidth(150);
		
		triggerScheduler = new JButton(" Trigger ");
		triggerScheduler.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ triggerScheduler(); }
		});
			
		stopLaunch = new JButton(" Stop ");
		stopLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ stopLaunch(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(triggerScheduler);
		buttonPanel.add(stopLaunch);
		
		launchPanel = new JScrollPane(launchTable);
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(launchPanel, BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.EAST);
		
		applicationConsole = new LoggingConsole();
		launchConsole = new LoggingConsole();
		
		loggingPanel = new JTabbedPane(JTabbedPane.TOP);
		loggingPanel.add("Application", applicationConsole);
		loggingPanel.add("Launch", launchConsole);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				topPanel, 
				loggingPanel);
		centerPanel.setDividerLocation(150);
		
		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		
		launchTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				adjustSelection();
			}
		});
		launchTable.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				adjustSelection();
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		
		launchManager.addListener(this);
		scheduleManager.addListener(this);
		logger.addListener(applicationConsole);
	}
	
	public void init() {
		initUI();
		adjustSelection();
	}

	private void clearUI() {
	
		launchTable.clearSelection();
		for(int i=tableModel.getRowCount()-1; i>=0; i--){
			tableModel.removeRow(i);
		}
	}
	
	private void initUI() {
		
		for(LaunchInfo launch : launches){
			Object[] rowData = {
				launch.name,
				launch.trigger,
				launch.start != null ? StringTools.getTextDate(launch.start) : "",
				launch.progress+" %",
				launch.status.toString()
			};
			tableModel.addRow(rowData);
		}
		setSchedulerUpdate();
	}

	private void setSchedulerUpdate() {
		Date updated = scheduleManager.getUpdated();
		String info = updated != null ? 
				"Scheduler: "+StringTools.getTextDate(updated) : "Scheduler: idle";
		launchPanel.setToolTipText(info);
		launchTable.setToolTipText(info);
	}

	private void refreshUI(LaunchInfo selected) {
		
		clearUI();
		initUI();
		if(selected != null){	
			for(int i=0; i<launches.size(); i++){
				if(launches.get(i).id.equals(selected.id)){
					launchTable.changeSelection(i, -1, false, false);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private void adjustSelection() {
		
		LaunchInfo selected = getSelectedLaunch();
		if(selected != null){
			stopLaunch.setEnabled(true);
			ILogProvider provider = launchManager.getLoggingProvider(selected.id); 
			if(provider != launchConsole.getProvider()){
				launchConsole.deregister();
				launchConsole.clearConsole();
				launchConsole.initConsole(provider.getBuffer());
				provider.addListener(launchConsole);
				loggingPanel.setSelectedIndex(1);
			}
		}else{
			stopLaunch.setEnabled(false);
			launchConsole.deregister();
			launchConsole.clearConsole();
		}		
	}

	@Override
	public void changed(Object object) {
		
		if(object == launchManager){
			LaunchInfo selected = getSelectedLaunch();
			launches = launchManager.getLaunchInfo();
			refreshUI(selected);
		}else if(object == scheduleManager){
			setSchedulerUpdate();
		}
	}

	private LaunchInfo getSelectedLaunch() {
		
		LaunchInfo selected = null;
		int index = launchTable.getSelectedRow();
		if(index >=0){				
			selected = launches.get(index);
		}
		return selected;
	}
	
	public void triggerScheduler(){
		
		scheduleManager.triggerScheduler(0);
	}
	
	public void stopLaunch(){
		
		LaunchInfo selected = getSelectedLaunch();
		if(selected != null && UiTools.confirmDialog("Stop launch ["+selected.name+"]?")){
			launchManager.stopLaunch(selected.id);
		}
	}
}
