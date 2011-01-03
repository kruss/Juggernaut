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

import launch.LaunchManager;

import logger.Logger;
import logger.ILogConfig.Module;

import util.IChangedListener;
import util.StringTools;

import core.Configuration;
import core.Constants;
import core.HeapManager;
import core.ISystemComponent;
import core.HeapManager.HeapStatus;

public class Window extends JFrame implements ISystemComponent, IStatusClient, IChangedListener {

	private static final long serialVersionUID = 1L;
	
	private Configuration configuration;
	private HeapManager heapManager;
	private Logger logger;
	private ProjectMenu projectMenu;
	
	private JLabel statusLabel;
	private JLabel heapLabel;
	
	public Window(
			Configuration configuration,
			LaunchManager launchManager,
			HeapManager heapManager,
			Logger logger,
			ProjectMenu projectMenu,
			ToolsMenu toolsMenu,
			ConfigPanel configPanel,
			SchedulerPanel schedulerPanel,
			HistoryPanel historyPanel,
			PreferencePanel preferencePanel)
	{
		this.configuration = configuration;
		this.heapManager = heapManager;
		this.logger = logger;
		this.projectMenu = projectMenu;
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(projectMenu);
		menuBar.add(toolsMenu);
		setJMenuBar(menuBar);
		
		JTabbedPane centerPanel = new JTabbedPane();
		centerPanel.setTabPlacement(JTabbedPane.TOP);
		centerPanel.add(configPanel, "Configuration");
		centerPanel.add(schedulerPanel, "Scheduler");
		centerPanel.add(historyPanel, "History");
		centerPanel.add(preferencePanel, "Preferences");
		
		statusLabel = new JLabel();
		statusLabel.setEnabled(false);
		heapLabel = new JLabel();
		heapLabel.setEnabled(false);
		
		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.add(statusLabel, BorderLayout.CENTER);
		infoPanel.add(heapLabel, BorderLayout.EAST);
		
		Container pane = getContentPane();
		pane.add(centerPanel, BorderLayout.CENTER);
		pane.add(infoPanel, BorderLayout.SOUTH);
		pack();

		setSize(Constants.APP_WIDTH, Constants.APP_HEIGHT);
		setLocation(100, 100);
		setTitle(Constants.APP_FULL_NAME);
		
		configuration.addListener(this);
		heapManager.addListener(this);
		launchManager.addClient(this);
	}
	
	@Override
	public void init() throws Exception {
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){ 
				projectMenu.quit(); 
			}
		});
		
		UIManager.LookAndFeelInfo styles[] = UIManager.getInstalledLookAndFeels();
		UIManager.setLookAndFeel(styles[Constants.APP_STYLE].getClassName()); 
		SwingUtilities.updateComponentTreeUI(this);
		
		setStatus(Constants.APP_NAME+" started at "+StringTools.getTextDate(new Date()));
		setHeap(heapManager.getHeapStatus());
		setVisible(true);
	}
	
	@Override
	public void shutdown() throws Exception {
		dispose();
	}
	
	public void setStatus(String text){
		
		statusLabel.setText(text);
		logger.log(Module.COMMON, text);
	}
	
	@Override
	public void status(String text) {
		setStatus(text);
	}
	
	public void setHeap(HeapStatus status){
		
		long MB = 1024 * 1024;
		String info = Math.round(status.usedMemory / MB)+" / "+Math.round(status.maxMemory / MB)+" MB";
		heapLabel.setText(info);
	}

	@Override
	public void changed(Object object) {
		
		if(object == configuration){
			Configuration.State state = configuration.getState();
			if(state == Configuration.State.CLEAN){
				setTitle(Constants.APP_FULL_NAME);
			}else if(state == Configuration.State.DIRTY){
				setTitle(Constants.APP_FULL_NAME+" *");
			}
		}else if(object == heapManager){
			setHeap(heapManager.getHeapStatus());
		}
	}
}
