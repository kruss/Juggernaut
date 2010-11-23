package ui;

import java.awt.BorderLayout;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;

import lifecycle.LaunchManager.LaunchInfo;

import util.IChangedListener;
import util.ILogListener;
import util.StringTools;
import util.UiTools;

import core.Application;

public class StatusPanel extends JPanel implements IChangedListener, ILogListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JTable launchTable;
	private DefaultTableModel tableModel;
	private JTextArea loggingConsole;
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
		columnModel.getColumn(1).setMinWidth(250);
		columnModel.getColumn(2).setMinWidth(150);
			columnModel.getColumn(2).setMaxWidth(150);
		columnModel.getColumn(3).setMinWidth(100);
			columnModel.getColumn(3).setMaxWidth(100);
		columnModel.getColumn(4).setMinWidth(150);
			columnModel.getColumn(4).setMaxWidth(150);
		
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
		
		launchTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				adjustSelection();
			}
		});
		application.getLaunchManager().addListener(this);
		application.getLogger().addListener(this);
	}
	
	public void init() {
		initUI();
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
					adjustSelection();
					break;
				}
			}
		}
	}
	
	private void adjustSelection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getLaunchManager()){
			
			LaunchInfo selected = getSelectedLaunch();
			launches = application.getLaunchManager().getLaunchInfo();
			refreshUI(selected);
		}
	}
	
	@Override
	public void logged(String log) {
		
		if(loggingConsole.getLineCount() > 100){
			loggingConsole.setSelectionStart(0);
			try{
				loggingConsole.setSelectionEnd(loggingConsole.getLineEndOffset(50));
			}catch(BadLocationException e){
				loggingConsole.setSelectionEnd(loggingConsole.getText().length()/2);
			}
			loggingConsole.replaceSelection("...\n");
		}
		loggingConsole.append(log);
		loggingConsole.setCaretPosition(loggingConsole.getText().length());
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
