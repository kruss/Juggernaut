package core.runtime.logger;

import java.util.ArrayList;
import java.util.Date;

import util.DateTools;
import util.IChangeListener;
import util.IChangeable;

import core.ISystemComponent;
import core.runtime.logger.ILogConfig.Module;

public class ErrorManager implements ISystemComponent, IChangeable {

	private static final int ERROR_MAX = 100;
	
	private ArrayList<Error> errors;
	private ArrayList<IChangeListener> listeners;
	
	public ErrorManager() {
		errors = new ArrayList<Error>();
		listeners = new ArrayList<IChangeListener>();
	}

	@Override
	public void init() throws Exception {}

	@Override
	public void shutdown() throws Exception {
		clear();
	}

	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	public int getErrorCount() {
		
		synchronized(errors){
			return errors.size();
		}
	}
	
	public void add(Date date, Module module, String message) {
		
		synchronized(errors){
			if(errors.size() > ERROR_MAX){
				errors.remove(errors.size() - 1);
			}
			Error error = new Error(date, module, message);
			errors.add(error);
			notifyListeners();
		}
	}
	
	public void clear(){
		
		synchronized(errors){
			errors.clear();
			notifyListeners();
		}
	}
	
	class Error {
		public Date date; 
		public Module module; 
		public String message;
		
		public Error(Date date, Module module, String message){
			this.date = date;
			this.module = module;
			this.message = message;
		}
	}
	
	public ArrayList<ErrorInfo> getInfo(){
		
		ArrayList<ErrorInfo> info = new ArrayList<ErrorInfo>();
		synchronized(errors){
			for(Error error : errors){
				info.add(0, new ErrorInfo(error));
			}
		}
		return info;
	}
	
	public class ErrorInfo {
		
		public String title;
		public String message;
		
		public ErrorInfo(Error error){
			title = DateTools.getTextDate(error.date)+" - "+error.module.toString();
			message = error.message;
		}
	}
}
