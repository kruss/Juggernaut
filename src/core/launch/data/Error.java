package core.launch.data;

import java.util.ArrayList;

import core.launch.LifecycleObject;

public class Error {
	
	public String id;				
	public String origin;		
	public String component;
	public String message;
	
	public Error(LifecycleObject origin, String component, String message){
		this.id = origin.getId();
		this.origin = origin.getIdentifier();
		this.component = (component != null ? component : "GENERIC");
		this.message = (message != null ? message : "UNDEFINED");
	}
	
	public long getHash(){
		return (id+component+message).hashCode();	
	}
	
	/** return errors which are in list1 but not in list2 */
	public static ArrayList<Error> getDelta(ArrayList<Error> list1, ArrayList<Error> list2) {
		
		ArrayList<Error> delta = new ArrayList<Error>();
		for(Error error1 : list1){
			boolean found = false;
			for(Error error2 : list2){
				if(error1.getHash() == error2.getHash()){
					found = true;
					break;
				}
			}
			if(!found){
				delta.add(error1);
			}
		}
		return delta;
	}

	/** return errors which are in list1 and in list2 */
	public static ArrayList<Error> getMatch(ArrayList<Error> list1, ArrayList<Error> list2) {
		
		ArrayList<Error> match = new ArrayList<Error>();
		for(Error error1 : list1){
			boolean found = false;
			for(Error error2 : list2){
				if(error1.getHash() == error2.getHash()){
					found = true;
					break;
				}
			}
			if(found){
				match.add(error1);
			}
		}
		return match;
	}
}
