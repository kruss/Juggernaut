package core;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import util.SystemTools;
import util.UiTools;

public class AbstractSystem implements ISystemComponent {

	public static final Exception ABOARDING = new Exception("aboarding");
	
	private ArrayList<ISystemComponent> components;
	private ProgressMonitor monitor;

	private boolean init;
	private boolean progress;
	
	public boolean isInitialized(){ return init; }
	public void showProgress(boolean progress){ this.progress = progress; }
	
	public AbstractSystem(){
		
		components = new ArrayList<ISystemComponent>();
		monitor = null;

		init = false;
		progress = false;
	}

	public void clear(){
		components.clear();
	}
	
	public void add(ISystemComponent component){
		components.add(component);
	}
	
	@Override
	public void init() throws Exception {
		
		String name = getClass().getSimpleName();
		if(progress){
			startProgress(name+" <INIT>");
		}
		try{
			for(int i=0; i<components.size(); i++){
				ISystemComponent component = components.get(i);
				String componentName = component.getClass().getSimpleName();
				print("INIT: "+name+"::"+componentName);
				try{
					component.init();
					if(progress){
						setProgress((i+1), componentName);
					}
				}catch(Exception e){
					if(e != ABOARDING){
						error(e);
						if(!UiTools.confirmDialog(name+" Error on INIT!\nContinue anyway ?", e)){
							throw ABOARDING;
						}
					}else{
						throw e;
					}
				}
			}
			init = true;
		}finally{
			if(progress){
				stopProgress();
			}
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		
		init = false;
		String name = getClass().getSimpleName();
		if(progress){
			startProgress(name+" <SHUTDOWN>");
		}
		try{
			for(int i=components.size()-1; i>=0; i--){
				ISystemComponent component = components.get(i);
				String componentName = component.getClass().getSimpleName();
				print("SHUTDOWN: "+name+"::"+componentName);
				try{
					component.shutdown();
					if(progress){
						setProgress((components.size() - i), componentName);
					}
				}catch(Exception e){
					if(e != ABOARDING){
						error(e);
						UiTools.errorDialog(name+" Error on SHUTDOWN!", e);
						throw ABOARDING;
					}else{
						throw e;
					}
				}
			}
		}finally{
			if(progress){
				stopProgress();
			}
		}
	}
	
	private void startProgress(String message) {
		
		JFrame frame = new JFrame(); 
		UiTools.setLookAndFeel(frame, Constants.APP_STYLE);
		monitor = new ProgressMonitor(frame,  message, "", 0, components.size());
		monitor.setMillisToDecideToPopup(0);
		monitor.setMillisToPopup(0);
	}
	
	private void setProgress(int count, String message) throws Exception {
		
		if(monitor.isCanceled()){
			throw ABOARDING;
		}else{
			monitor.setProgress(count);
			monitor.setNote(message);
			SystemTools.sleep(300);
		}
	}
	
	private void stopProgress() {
		monitor.close();
		monitor = null;
	}
	
	private void print(String text){
		System.out.println(text);
	}
	
	private void error(Exception e) {
		e.printStackTrace();
	}
}
