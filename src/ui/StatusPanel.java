package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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

import lifecycle.LaunchManager.LaunchInfo;

import util.IChangedListener;
import util.ILoggingProvider;
import util.StringTools;
import util.UiTools;

import core.Application;

public class StatusPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JTable launchTable;
	private DefaultTableModel tableModel;
	private JTabbedPane loggingPanel;
	private LoggingConsole applicationConsole;
	private LoggingConsole launchConsole;
	private JButton stopLaunch;
	
	private ArrayList<LaunchInfo> launches;
	
	public StatusPanel()
	{
		application = Application.getInstance();
		launches = new ArrayList<LaunchInfo>();
		
		tableModel = new DefaultTableModel();
		tableModel.addColumn("Launch");
		tableModel.addColumn("Description");
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
		
		stopLaunch = new JButton(" Stop ");
		stopLaunch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ stopLaunch(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(stopLaunch); stopLaunch.setAlignmentY(Component.TOP_ALIGNMENT);

		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JScrollPane(launchTable), BorderLayout.CENTER);
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
		
		application.getLogger().addListener(applicationConsole);
		application.getLaunchManager().addListener(this);
	}
	
	public void init() {
		initUI();
		adjustSelection();
	}

	private void clearUI() {
	
		launchTable.changeSelection(-1, -1, false, false);
		for(int i=tableModel.getRowCount()-1; i>=0; i--){
			tableModel.removeRow(i);
		}
	}
	
	private void initUI() {
		
		for(LaunchInfo launch : launches){
			Object[] rowData = {
				launch.name,
				launch.description,
				launch.start != null ? StringTools.getTextDate(launch.start) : "",
				launch.progress+" %",
				launch.status.toString()
			};
			tableModel.addRow(rowData);
		}
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
			ILoggingProvider provider = launchConsole.getProvider();
			launchConsole.deregister();
			application.getLaunchManager().addListener(launchConsole, selected.id);
			if(launchConsole.getProvider() != provider){
				launchConsole.clearConsole();
			}
			loggingPanel.setSelectedIndex(1);
		}else{
			stopLaunch.setEnabled(false);
			loggingPanel.setSelectedIndex(0);
			launchConsole.deregister();
			launchConsole.clearConsole();
		}		
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getLaunchManager()){
			
			LaunchInfo selected = getSelectedLaunch();
			launches = application.getLaunchManager().getLaunchInfo();
			refreshUI(selected);
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
	
	public void stopLaunch(){
		
		LaunchInfo selected = getSelectedLaunch();
		if(selected != null && UiTools.confirmDialog("Stop launch ["+selected.name+"]?")){
			application.getLaunchManager().stopLaunch(selected.id);
		}
	}
}
