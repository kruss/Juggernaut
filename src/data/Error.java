package data;

import java.util.ArrayList;

public class Error {
	
	public String id;			// id of origin
	public String origin;		// name of origin
	public String message;
	
	public Error(AbstractOperation operation, String message){
		this.id = operation.getConfig().getId();
		this.origin = operation.getIdentifier();
		this.message = message;	
	}
	
	public String getHtml() {
		return "<font color='red'>"+message+"</font>";	
	}
	
	public long getHash(){
		return (id+message).hashCode();	
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
