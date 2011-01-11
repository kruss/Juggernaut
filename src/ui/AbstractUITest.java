package ui;

import util.UiTools;
import launch.StatusManager.Status;
import logger.Logger;
import logger.ILogConfig.Module;
import data.IOptionDelegate;

public abstract class AbstractUITest implements IOptionDelegate {
	
	protected static final Exception CANCEL = new Exception("canceled");
	
	protected Logger logger;
	protected Status status;
	
	public AbstractUITest(Logger logger){
		
		this.logger = logger;
		status = Status.UNDEFINED;
	}

	@Override
	public String getName(){ return "Test"; }
	
	@Override
	public void perform(String content) {

		logger.log(Module.COMMON, getClass().getSimpleName()+(!content.isEmpty() ? " ("+content+")" : ""));
		String message = "";
		try{
			message = performTest(content);
			status = Status.SUCCEED;
		}catch(Exception e){
			if(e == CANCEL){
				message = e.getMessage();
				status = Status.CANCEL;
			}else{
				message = e.getClass().getSimpleName()+": "+e.getMessage();
				status = Status.ERROR;
				logger.error(Module.COMMON, e);
			}
		}finally{
			String info = getClass().getSimpleName()+" - "+status.toString()+"\n\n"+message;
			if(status != Status.CANCEL){
				if(status == Status.SUCCEED){
					UiTools.infoDialog(info);
				}else{
					UiTools.errorDialog(info);
				}
			}
		}
	}

	/** perform test returning a feedback on success or throwing exception */
	protected abstract String performTest(String content) throws Exception;
}
