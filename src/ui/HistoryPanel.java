package ui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import core.Application;
import core.History;
import data.LaunchHistory;

import util.FileTools;
import util.IChangedListener;
import util.StringTools;

// TODO history selection must be guranteed by some id
public class HistoryPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JTable historyTable;
	private DefaultTableModel tableModel;
	private JTextArea historyOutput;
	
	public HistoryPanel(){
		
		application = Application.getInstance();
		
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
		tableModel.addColumn("End");
		tableModel.addColumn("Time (min)");
		tableModel.addColumn("Status");
		
		historyTable = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){ 
					return false;
			}
		};
		historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		historyTable.setRowSelectionAllowed(true);
		historyTable.setColumnSelectionAllowed(false);
		
		TableColumnModel columnModel = historyTable.getColumnModel();
		columnModel.getColumn(0).setMinWidth(150);
		columnModel.getColumn(1).setMinWidth(150);
		columnModel.getColumn(2).setMinWidth(150);
			columnModel.getColumn(2).setMaxWidth(150);
		columnModel.getColumn(3).setMinWidth(150);
			columnModel.getColumn(3).setMaxWidth(150);
		columnModel.getColumn(4).setMinWidth(75);
			columnModel.getColumn(4).setMaxWidth(75);
		columnModel.getColumn(5).setMinWidth(150);
			columnModel.getColumn(5).setMaxWidth(150);
		
		historyOutput = new JTextArea();
		historyOutput.setEditable(false);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(historyTable), 
				new JScrollPane(historyOutput));
		centerPanel.setDividerLocation(150);
		
		setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
		
		historyTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				adjustSelection();
			}
		});
		
		application.getHistory().addListener(this);
	}
	
	public void init() {
		initUI();
		adjustSelection();
	}

	private void clearUI() {
	
		historyTable.clearSelection();
		for(int i=tableModel.getRowCount()-1; i>=0; i--){
			tableModel.removeRow(i);
		}
	}
	
	private void initUI() {
		
		History history = application.getHistory();
		int size = history.getEntries().size();
		
		for(int i=0; i<size; i++){
			LaunchHistory entry = history.getEntries().get(i);
			Object[] rowData = {
				entry.name,
				entry.trigger,
				entry.start != null ? StringTools.getTextDate(entry.start) : "",
				entry.end != null ? StringTools.getTextDate(entry.end) : "",
				(entry.start != null && entry.end != null) ? 
						StringTools.getTimeDiff(entry.start, entry.end) : "",
				entry.status.toString()
			};
			tableModel.addRow(rowData);
		}
	}

	private void refreshUI(LaunchHistory selected) {
		
		clearUI();
		initUI();
		if(selected != null){	
			History history = application.getHistory();
			int size = history.getEntries().size();
			for(int i=0; i<size; i++){
				LaunchHistory entry = history.getEntries().get(i);
				if(entry.id.equals(selected.id)){
					historyTable.changeSelection(i, -1, false, false);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private void adjustSelection() {
		
		LaunchHistory entry = getSelectedHistory();
		if(entry != null){
			File logfile = new File(entry.artifacts.get(0).attachments.get(0).path); // TODO temp
			if(logfile.isFile()){
				try{
					historyOutput.setText(FileTools.readFile(logfile.getAbsolutePath()));
					historyOutput.setCaretPosition(0);
				}catch(Exception e){
					application.getLogger().error(e);
				}
			}else{
				historyOutput.setText("");
			}
		}else{
			historyOutput.setText("");
		}
	}
	
	private LaunchHistory getSelectedHistory() {
		
		LaunchHistory selected = null;
		int index = historyTable.getSelectedRow();
		if(index >=0){
			History history = application.getHistory();
			selected = history.getEntries().get(index);
		}
		return selected;
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getHistory()){
			refreshUI(getSelectedHistory());
		}
	}
}
