package core.launch.data;

import java.util.ArrayList;
import java.util.Collections;

import core.Result;
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
		
		if(result.status == Result.Status.ERROR){
			if(result.values.size() > 0){
				for(String value : result.values){
					parent.getStatusManager().addError(parent, trace, value);
				}
			}else{
				parent.getStatusManager().addError(parent, trace, null);
			}
		}
		
		for(Result child : result.results){
			addResultErrors(child, trace);
		}
	}

	public static String getResultHtml(Result result){
		
		StringBuilder html = new StringBuilder();
		if(result.status != Result.Status.UNDEFINED){
			html.append("<b>"+result.name+"</b> - "+getResultStatusHtml(result.status)+"\n");
		}else{
			html.append("<b>"+result.name+"</b>\n");
		}
		ArrayList<String> keys = new ArrayList<String>();
		for(String key : result.properties.keySet()){
			keys.add(key);
		}
		Collections.sort(keys);
		for(String key : keys){
			String value = result.properties.get(key);
			html.append("<br>- <i>"+key+": "+value+"</i>\n");
		}
		for(String value : result.values){
			html.append("<br>+ "+value+"\n");
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
	
	public static String getResultStatusHtml(Result.Status status) {
		return "<font color='"+getResultStatusColor(status)+"'>"+status.toString()+"</font>";
	}
	
	public static String getResultStatusColor(Result.Status status) {

		String color = "black";
		if(status == Result.Status.SUCCEED){
			color = "green";
		}else if(status == Result.Status.WARNING){
			color = "blue";
		}else if(status == Result.Status.ERROR){
			color = "red";
		}
		return color;
	}
}
