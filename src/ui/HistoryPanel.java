package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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

import core.Application;
import core.History.HistoryInfo;

import util.FileTools;
import util.IChangedListener;
import util.StringTools;
import util.UiTools;

public class HistoryPanel extends JPanel implements IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	private JTable historyTable;
	private DefaultTableModel tableModel;
	private JTextArea historyOutput;
	private JButton deleteAllHistory;
	private JButton deleteHistory;
	
	private ArrayList<HistoryInfo> entries;
	
	public HistoryPanel(){
		
		application = Application.getInstance();
		entries = new ArrayList<HistoryInfo>();
		
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
		columnModel.getColumn(1).setMinWidth(200);
		columnModel.getColumn(2).setMinWidth(150);
			columnModel.getColumn(2).setMaxWidth(150);
		columnModel.getColumn(3).setMinWidth(100);
			columnModel.getColumn(3).setMaxWidth(100);
		columnModel.getColumn(4).setMinWidth(150);
			columnModel.getColumn(4).setMaxWidth(150);
		
		deleteAllHistory = new JButton(" Delete All ");
		deleteAllHistory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ deleteAllHistory(); }
		});
		
		deleteHistory = new JButton(" Delete ");
		deleteHistory.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ deleteHistory(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(deleteHistory);
		buttonPanel.add(deleteAllHistory);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
		topPanel.add(buttonPanel, BorderLayout.EAST);
		
		historyOutput = new JTextArea();
		historyOutput.setEditable(false);
		
		JSplitPane centerPanel = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				topPanel, 
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
		
		entries = application.getHistory().getHistoryInfo();
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
		
		for(HistoryInfo entry : entries){
			Object[] rowData = {
				entry.name,
				entry.trigger,
				entry.start != null ? StringTools.getTextDate(entry.start) : "",
				(entry.start != null && entry.end != null) ? 
						StringTools.getTimeDiff(entry.start, entry.end) : "",
				entry.status.toString()
			};
			tableModel.addRow(rowData);
		}
	}

	private void refreshUI(HistoryInfo selected) {
		
		clearUI();
		initUI();
		if(selected != null){	
			for(int i=0; i<entries.size(); i++){
				if(entries.get(i).historyId.equals(selected.historyId)){
					historyTable.changeSelection(i, -1, false, false);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private void adjustSelection() {
		
		HistoryInfo entry = getSelectedHistory();
		if(entry != null){
			File logfile = new File(entry.logfile);
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
			deleteHistory.setEnabled(true);
		}else{
			historyOutput.setText("");
			deleteHistory.setEnabled(false);
		}
	}
	
	@Override
	public void changed(Object object) {
		
		if(object == application.getHistory()){
			HistoryInfo selected = getSelectedHistory();
			entries = application.getHistory().getHistoryInfo();
			refreshUI(selected);
		}
	}
	
	private HistoryInfo getSelectedHistory() {
		
		HistoryInfo selected = null;
		int index = historyTable.getSelectedRow();
		if(index >=0){				
			selected = entries.get(index);
		}
		return selected;
	}
	
	public void deleteAllHistory(){
		
		if(UiTools.confirmDialog("Delete all history ?")){
			application.getHistory().clear();
			refreshUI(null);
		}
	}
	
	public void deleteHistory(){
		
		HistoryInfo entry = getSelectedHistory();
		if(
				entry != null && 
				UiTools.confirmDialog("Delete history ["+entry.name+" ("+StringTools.getTextDate(entry.start)+")"+"] ?")
		){
			application.getHistory().delete(entry.historyId);
			refreshUI(null);
		}
	}
}
