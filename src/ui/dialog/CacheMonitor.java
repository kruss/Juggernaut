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
import core.persistence.Cache;
import core.persistence.Cache.CacheInfo;

public class CacheMonitor extends JDialog implements ISystemComponent, IChangeListener {
	
	private static final long serialVersionUID = 1L;
	
	private Cache cache;
	private DefaultTableModel tableModel;
	private JTable cacheTable;
	private JButton emptyCache;
	private JButton deleteEntry;
	private JButton editEntry;
	
	private ArrayList<CacheInfo> entries;
	
	public CacheMonitor(Cache cache){
		
		this.cache = cache;
		entries = new ArrayList<CacheInfo>();
		
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
		tableModel.addColumn("Item");
		tableModel.addColumn("Key");
		tableModel.addColumn("Value");
		
		cacheTable = new JTable(tableModel){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){ 
					return false;
			}
		};
		cacheTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cacheTable.setRowSelectionAllowed(true);
		cacheTable.setColumnSelectionAllowed(false);
		
		cacheTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				adjustSelection();
			}
		});
		cacheTable.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				adjustSelection();
			}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		
		TableColumnModel columnModel = cacheTable.getColumnModel();
		columnModel.getColumn(0).setMinWidth(200);
		columnModel.getColumn(1).setMinWidth(100);
		columnModel.getColumn(2).setMinWidth(100);
		
		emptyCache = new JButton(" Empty ");
		emptyCache.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ emptyCache(); }
		});
		
		deleteEntry = new JButton(" Delete ");
		deleteEntry.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ deleteEntry(); }
		});
		
		editEntry = new JButton(" Edit ");
		editEntry.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ editEntry(); }
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(emptyCache);
		buttonPanel.add(deleteEntry);
		buttonPanel.add(editEntry);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JScrollPane(cacheTable), BorderLayout.CENTER);
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
		setSize(500, 200); 
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
				cache.addListener(this);
				updateUI();
			}
		}else{
			if(isVisible()){
				cache.removeListener(this);
				clearUI();
			}
		}
		setVisible(visible);
	}

	private void clearUI() {
		
		cacheTable.clearSelection();
		for(int i=tableModel.getRowCount()-1; i>=0; i--){
			tableModel.removeRow(i);
		}
	}
	
	private void initUI() {
		
		for(CacheInfo entry : entries){
			Object[] rowData = {
				entry.identifier,
				entry.key,
				entry.value,
			};
			tableModel.addRow(rowData);
		}
		setTitle("Cache-Monitor ("+entries.size()+")");
	}

	private void refreshUI(CacheInfo selected) {
		
		clearUI();
		initUI();
		if(selected != null){	
			for(int i=0; i<entries.size(); i++){
				if(entries.get(i).id.equals(selected.id) && entries.get(i).key.equals(selected.key)){
					cacheTable.changeSelection(i, -1, false, false);
					break;
				}
			}
		}
		adjustSelection();
	}
	
	private synchronized void updateUI() {
		
		CacheInfo selected = getSelectedEntry();
		entries = cache.getInfo();
		refreshUI(selected);
	}
	
	private void adjustSelection() {
		
		CacheInfo selected = getSelectedEntry();
		if(selected != null){
			deleteEntry.setEnabled(true);
			editEntry.setEnabled(true);
		}else{
			deleteEntry.setEnabled(false);
			editEntry.setEnabled(false);
		}
	}
	
	@Override
	public void changed(Object object) {
		
		if(object instanceof Cache){
			updateUI();
		}
	}
	
	private CacheInfo getSelectedEntry() {
		
		CacheInfo selected = null;
		int index = cacheTable.getSelectedRow();
		if(index >=0){				
			selected = entries.get(index);
		}
		return selected;
	}
	
	private void emptyCache(){
		
		if(UiTools.confirmDialog("Empty cache ?\n\n!!! Complete cache will be deleted !!!")){
			cache.clear();
		}
	}
	
	private void deleteEntry(){
		
		CacheInfo selected = getSelectedEntry();
		if(selected != null && UiTools.confirmDialog("Delete [ "+selected.identifier+" ] Entry ?")){
			cache.removeValue(selected.id, selected.key);
		}
	}
	
	private void editEntry(){
		
		CacheInfo selected = getSelectedEntry();
		if(selected != null){			
			String value = UiTools.inputDialog("Edit Entry", selected.value);
			if(value != null){
				cache.setValue(selected.id, selected.key, value);
			}
		}
	}
}
