package ui.dialog;

import core.launch.data.StatusManager.Status;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;
import ui.option.IOptionDelegate;
import util.UiTools;

public abstract class AbstractUITest implements IOptionDelegate {
	
	protected Logger logger;
	
	public AbstractUITest(Logger logger){
		this.logger = logger;
	}

	@Override
	public String getName(){ return "Test"; }
	
	@Override
	public void perform(String content) {

		logger.log(Module.COMMON, getClass().getSimpleName()+(!content.isEmpty() ? " ("+content+")" : ""));
		TestStatus result = null;
		try{
			result = performTest(content);
		}catch(Exception e){
			result = new TestStatus(Status.ERROR, e.getClass().getSimpleName()+": "+e.getMessage());
			logger.error(Module.COMMON, e);
		}finally{
			String info = 
				getClass().getSimpleName()+" - "+
				result.status.toString()+"\n\n"+
				result.message;
			if(result.status != Status.CANCEL){
				if(result.status == Status.SUCCEED){
					UiTools.infoDialog(info);
				}else{
					UiTools.errorDialog(info);
				}
			}
		}
	}

	protected abstract TestStatus performTest(String content) throws Exception;
	
	protected class TestStatus {
		
		public Status status;
		public String message;
		
		public TestStatus(Status status, String message){
			
			this.status = status;
			this.message = message;
		}
	}
}
