package core.ressources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Ressources {

	public static final String HELP = "help.htm";
	
	public String getRessource(String name) throws Exception {
		
		StringBuilder text = new StringBuilder();
		InputStream stream = null;
		BufferedReader reader = null;
		try{
			stream = Ressources.class.getResourceAsStream(name);
			if(stream != null){
				reader = new BufferedReader(new InputStreamReader(stream));
				String line = null;
				while (null != (line = reader.readLine())) {
					text.append(line);
				}
			}else{
				throw new Exception("Unable to open: "+name);
			}
		}finally{
			if(reader != null){ reader.close(); }
			if(stream != null){ stream.close(); }
		}
		return text.toString();
	}
}
