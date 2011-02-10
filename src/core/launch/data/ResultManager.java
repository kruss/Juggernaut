package core.launch.data;

import java.util.ArrayList;

import core.Result;
import core.Result.Resolution;
import core.launch.data.StatusManager.Status;

public class ResultManager {

	public static final String OUTPUT_FILE = "result.xml";

	private StatusManager statusManager;
	private ArrayList<Result> results;
	
	public ResultManager(StatusManager statusManager) {
		
		this.statusManager = statusManager;
		results = new ArrayList<Result>();
	}
	
	public ArrayList<Result> getResults(){ return results; }
	
	public void addResult(Result result){
		
		results.add(result);
		if(result.resolution == Resolution.ERROR){
			statusManager.setStatus(Status.ERROR);
		}
	}
	
	public static String getResultHtml(Result result){
		
		StringBuilder html = new StringBuilder();
		if(result.resolution != Resolution.UNDEFINED){
			html.append("<b>"+result.name+"</b> - "+ResultManager.getResolutionHtml(result.resolution)+"\n");
		}else{
			html.append("<b>"+result.name+"</b>\n");
		}
		for(String key : result.values.keySet()){
			String value = result.values.get(key);
			html.append("<br>- "+key+": "+value+"\n");
		}
		if(!result.message.isEmpty()){
			html.append("<br><i>"+result.message.replaceAll("\\n", "<br>")+"</i>\n");
		}
		if(result.results.size() > 0){
			html.append("<ul>\n");
			for(Result child : result.results){
				html.append("<li>\n");
				html.append(getResultHtml(child));
				html.append("</li>\n");
			}
			html.append("</ul>\n");
		}
		return html.toString();
	}
	
	public static String getResolutionHtml(Resolution resolution) {
		return "<font color='"+getResolutionColor(resolution)+"'>"+resolution.toString()+"</font>";
	}
	
	public static String getResolutionColor(Resolution resolution) {

		String color = "black";
		if(resolution == Resolution.UNDEFINED){
			color = "orange";
		}else if(resolution == Resolution.SUCCEED){
			color = "green";
		}else if(resolution == Resolution.ERROR){
			color = "red";
		}else if(resolution == Resolution.WARNING){
			color = "blue";
		}
		return color;
	}
}
