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
	
	/** answers if current os is mac */
	public static boolean isMacOS(){
		return getOSName().toLowerCase().contains("mac");
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
		
		if(isWindowsOS()){
			openWindowsBrowser(url);
		}else if(isLinuxOS()){
			openLinuxBrowser(url);
		}else if(isMacOS()){
			openMacBrowser(url);
		}else{
			throw new IOException("Unsuported OS: "+getOSName());
		}
	}
	
	private static void openWindowsBrowser(String target) throws IOException {
		Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \""+target+"\"");
	}
	
	private static void openLinuxBrowser(String target) throws IOException {
		Runtime.getRuntime().exec("firefox "+target);
	}
	
	private static void openMacBrowser(String path) throws IOException {
		Runtime.getRuntime().exec("open file://"+path.replaceAll("\\s", "%20"));
	}
}
