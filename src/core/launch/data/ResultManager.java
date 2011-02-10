package core.launch.data;

import java.util.ArrayList;

import core.Result;
import core.Result.Resolution;
import core.launch.LifecycleObject;

public class ResultManager {

	public static final String OUTPUT_FILE = "result.xml";

	private LifecycleObject parent;
	private ArrayList<Result> results;
	
	public ResultManager(LifecycleObject parent) {
		
		this.parent = parent;
		results = new ArrayList<Result>();
	}
	
	public ArrayList<Result> getResults(){ return results; }
	
	public void addResult(Result result){
		
		addResultErrors(result, null);
		results.add(result);
	}
	
	private void addResultErrors(Result result, String trace) {
		
		if(trace == null){
			trace = result.name;
		}else{
			trace += "::"+result.name;
		}
		
		if(result.resolution == Resolution.ERROR){
			parent.getStatusManager().addError(parent, trace + (result.message.isEmpty() ? "" : ": "+result.message));
		}
		
		for(Result child : result.results){
			addResultErrors(child, trace);
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
