package ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import logger.Logger.Module;

import util.IChangedListener;
import util.StringTools;
import util.UiTools;

import core.Application;
import core.Configuration;
import core.Constants;
import core.ISystemComponent;
import core.HeapManager.HeapStatus;

public class Window extends JFrame implements ISystemComponent, IStatusClient, IChangedListener {

	private static final long serialVersionUID = 1L;

	private Application application;
	
	private JMenuBar menuBar;
	private ConfigPanel configPanel;
	private SchedulerPanel schedulerPanel;
	private HistoryPanel historyPanel;
	private PreferencePanel preferencePanel;
	private JLabel statusInfo;
	private JLabel heapInfo;
	
	public Window(Application application){
		
		this.application = application;
		
		menuBar = new JMenuBar();
		menuBar.add(new ProjectMenu(this, application.getConfiguration()));
		menuBar.add(new ToolsMenu(this, application.getConfiguration(), application.getFileManager(), application.getHeapManager()));
		setJMenuBar(menuBar);
		
		statusInfo = new JLabel();
		statusInfo.setEnabled(false);
		heapInfo = new JLabel();
		heapInfo.setEnabled(false);
		
		configPanel = new ConfigPanel(this, application.getConfiguration(), application.getLaunchManager());
		schedulerPanel = new SchedulerPanel(application.getLaunchManager(), application.getScheduleManager(), application.getLogger());
		historyPanel = new HistoryPanel(application.getHistory(), application.getLogger());
		preferencePanel = new PreferencePanel(application.getConfiguration(), application.getScheduleManager(), application.getHistory());
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.TOP);
		centerPanel.add(configPanel, "Configuration");
		centerPanel.add(schedulerPanel, "Scheduler");
		centerPanel.add(historyPanel, "History");
		centerPanel.add(preferencePanel, "Preferences");
		
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(statusInfo, BorderLayout.CENTER);
		infoPanel.add(heapInfo, BorderLayout.EAST);
		
		Container pane = getContentPane();
		pane.add(centerPanel, BorderLayout.CENTER);
		pane.add(infoPanel, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		setTitle(Constants.APP_FULL_NAME);
		
		application.getConfiguration().addListener(this);
		application.getHeapManager().addListener(this);
		application.getLaunchManager().addClient(this);
	}
	
	@Override
	public void init() throws Exception {
		
		configPanel.init();
		schedulerPanel.init();
		historyPanel.init();
		preferencePanel.init();
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				quit(); 
			}
		});
		
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
		SwingUtilities.updateComponentTreeUI(this);
		
		setStatus(Constants.APP_NAME+" started at "+StringTools.getTextDate(new Date()));
		setHeap(application.getHeapManager().getHeapStatus());
		setVisible(true);
	}
	
	@Override
	public void shutdown() throws Exception {
		
		if(application.getConfiguration().isDirty() && UiTools.confirmDialog("Save changes ?")){
			application.getConfiguration().save();
		}
		dispose();
	}
	
	/** quit the application */
	public void quit() {
		
		if(!application.getLaunchManager().isBusy() || UiTools.confirmDialog("Aboard running launches ?")){
			application.getLogger().info(Module.COMMON, "Shutdown");
			try{
				application.shutdown();
			}catch(Exception e){
				popupError(e);
				System.exit(Constants.PROCESS_NOK);
			}
			System.exit(Constants.PROCESS_OK);
		}
	}
	
	public void popupError(Exception e){
		
		application.getLogger().error(Module.COMMON, e);
		UiTools.errorDialog(e.getClass().getSimpleName()+"\n\n"+e.getMessage());
	}
	
	public void setStatus(String text){
		
		statusInfo.setText(text);
		application.getLogger().log(Module.COMMON, text);
	}
	
	@Override
	public void status(String text) {
		setStatus(text);
	}
	
	public void setHeap(HeapStatus status){
		
		long MB = 1024 * 1024;
		String info = Math.round(status.usedMemory / MB)+" / "+Math.round(status.maxMemory / MB)+" MB";
		heapInfo.setText(info);
	}

	@Override
	public void changed(Object object) {
		
		if(object == application.getConfiguration()){
			Configuration.State state = application.getConfiguration().getState();
			if(state == Configuration.State.CLEAN){
				setTitle(Constants.APP_FULL_NAME);
			}else if(state == Configuration.State.DIRTY){
				setTitle(Constants.APP_FULL_NAME+" *");
			}
		}else if(object == application.getHeapManager()){
			setHeap(application.getHeapManager().getHeapStatus());
		}
	}
}
