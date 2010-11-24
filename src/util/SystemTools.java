package util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemTools {

	/** path of current working-directory */
	public static String getWorkingDir(){
		return System.getProperty("user.dir");
	}
	
	/** name of current operating-system */
	public static String getOSName(){
		return System.getProperty("os.name");
	}
	
	/** answers if current os is windows */
	public static boolean isWindowsOS(){
		return getOSName().toLowerCase().contains("windows");
	}
	
	/** answers if current os is linux */
	public static boolean isLinuxOS(){
		return getOSName().toLowerCase().contains("linux");
	}
	
	/** get the local host-name */
	public static String getHostName() throws UnknownHostException {
		
		InetAddress addr = InetAddress.getLocalHost();
		return addr.getHostName();
	}
	
	/** thread time-out */
	public static void sleep(long millis){
		try{ Thread.sleep(millis); }catch(InterruptedException e){ }
	}
	
	/** open system-browser on url */
	public static void openBrowser(String url) throws IOException {
		
		if(getOSName().toLowerCase().contains("windows")){
			openWindowsBrowser(url);
		}else if(getOSName().toLowerCase().contains("linux")){
			openLinuxBrowser(url);
		}else{
			throw new IOException("Unsuported OS: "+getOSName());
		}
	}
	
	private static void openWindowsBrowser(String target) throws IOException {
		Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \""+target+"\"");
	}
	
	private static void openLinuxBrowser(String target) throws IOException {
		Runtime.getRuntime().exec("firefox "+target); // the most probably one
	}
}
