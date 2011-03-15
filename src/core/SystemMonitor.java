package core;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import util.SystemTools;
import util.UiTools;

public class SystemMonitor {

	private final static long SYSTEM_DELAY = 500;
	
	private ProgressMonitor monitor;
	private int max;
	private int progress;
	
	public SystemMonitor() {
		
		monitor = null;
		max = 0;
		progress = 0;
	}

	public void startProgress(AbstractSystem system, String message) {
		
		if(monitor == null){
			max = system.getComponentCount(true);
			progress = 0;
			JFrame frame = new JFrame(); 
			UiTools.setLookAndFeel(frame, Constants.APP_STYLE);
			monitor = new ProgressMonitor(frame,  message, "", 0, max);
			monitor.setMillisToDecideToPopup(0);
			monitor.setMillisToPopup(0);
		}
	}
	
	public void progress(String message) throws Exception {
		
		if(monitor != null){
			if(monitor.isCanceled()){
				throw AbstractSystem.ABOARDING;
			}else{
				monitor.setProgress(++progress);
				monitor.setNote(message);
				SystemTools.sleep(SYSTEM_DELAY / max);
			}
		}
	}
	
	public void stopProgress() {
		
		if(monitor != null){
			monitor.close();
			monitor = null;
		}
	}
}
