package util;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class StringTools {

	private static final int MAX_EXCEPTION_TRACE = 25;

	/** get stack-trace of an exception */
	public static String trace(Exception e){
		return trace(e, MAX_EXCEPTION_TRACE);
	}
	
	/** get stack-trace of an exception using a given depth */
	public static String trace(Exception e, int depth){

		StringBuilder trace = new StringBuilder();
		trace.append("["+e.getClass().getSimpleName()+"] "+e.getMessage()+"\n\n");
		StackTraceElement[] stack = e.getStackTrace();
		int len = stack.length > depth ? depth : stack.length;
		for(int i=0; i<len; i++){
			if(stack[i].getLineNumber()>0){
				trace.append(stack[i].getClassName()+"::"+stack[i].getMethodName()+" ("+stack[i].getLineNumber()+")\n");
			}else{
				trace.append(stack[i].getClassName()+"::"+stack[i].getMethodName()+"\n");
			}
		}
		if(len < stack.length){ trace.append("...\n"); }
		return trace.toString();
	}
	
	/** check if a key-event was actual a modifying one */
	public static boolean isModifyingKey(KeyEvent e) {

		return 
		( KeyEvent.CHAR_UNDEFINED != e.getKeyChar() && !e.isControlDown() && !e.isMetaDown() ) || 
		( e.isControlDown() && KeyEvent.VK_V == e.getKeyCode() ) ||
		( e.isControlDown() && KeyEvent.VK_X == e.getKeyCode() ); 
	}
	
	/** join a string-list using a separator */
	public static String join(ArrayList<String> list, String sep){
		
		StringBuilder join = new StringBuilder();
		for(int i=0; i<list.size(); i++){
			if(i < list.size()-1){
				join.append(list.get(i)+sep);
			}else{
				join.append(list.get(i));
			}
		}
		return join.toString();
	}
	
	/** split a string by a delimiter */
	public static ArrayList<String> split(String value, String delim){
		
		ArrayList<String> list = new ArrayList<String>();
		String[] strings = value.split(delim);
		for(String string : strings){
				String trim = string.trim();
				if(!trim.isEmpty()){
					list.add(trim);
				}
		}
		return list;
	}
	
	/** split a string by a delimiter using a exclude prefix */
	public static ArrayList<String> split(String value, String delim, String exclude){
		
		ArrayList<String> list = new ArrayList<String>();
		String[] strings = value.split(delim);
		for(String string : strings){
			String trim = string.trim();
			if(!trim.isEmpty() && !trim.startsWith(exclude)){
				list.add(string);
			}
		}
		return list;
	}
	
	/** get string representation of an enumeration */
	public static <T extends Enum<T>> ArrayList<String> enum2strings(Class<T> clazz) {      
		try{         
			ArrayList<String> list = new ArrayList<String>();                
			for(Object obj : clazz.getEnumConstants()){             
				list.add((String) obj.toString());          
			}          
			return list;     
		}catch(Exception e) {         
			throw new RuntimeException(e);     
		} 
	}

	/** add item to list if not already contained */
	public static void addUnique(ArrayList<String> list, String item) {
		if(!list.contains(item)){
			list.add(item);
		}
	}
}
