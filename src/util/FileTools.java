package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileTools {

	/** writes a string to a file */
	public static void writeFile(String path, String string, boolean append) throws IOException { 

		int i=0; int c=0;
		FileWriter fileWriter = new FileWriter(path, append);
		while(i<string.length()){ c=string.charAt(i++); fileWriter.write(c); }
		fileWriter.close();
	}
	
	/** reads a string from a file */
	public static String readFile(String path) throws IOException {

		File file = new File(path);
		StringBuffer sb = new StringBuffer((int)file.length()); // create buffer of given file length
		String line;
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
		while((line = bufferedReader.readLine()) != null){ sb.append(line+"\n"); }
		bufferedReader.close();
		
		return sb.toString();
	}
	
	/** delete a file */
	public static void deleteFile(String path) throws IOException {

		File file = new File(path);
		if(file.isFile()){ 
			if(!file.delete()){ throw new IOException("unable to delete file: "+path); }
		}else{ throw new IOException("not a file: "+path); }
	}
	
	/** recursive delete a folder */
	public static void deleteFolder(String path) throws IOException {
		
		File folder = new File(path);
		if(folder.isDirectory()){
			File[] files = folder.listFiles();
			for(int i=0; i<files.length; i++){
				if(files[i].isDirectory()){ deleteFolder(files[i].getAbsolutePath()); }
				else{ deleteFile(files[i].getAbsolutePath()); }
			}
			if(!folder.delete()){ throw new IOException("unable to delete directory: "+path); }
		}else{ throw new IOException("not a directory: "+path); }
	}

	/** copy a folder from org to dest path */
	public static void copyFolder(String inPath, String outPath) throws IOException {
		
		File inFolder = new File(inPath);
		if(inFolder.isDirectory()){
			if(!(new File(outPath)).exists()){ createFolder(outPath); }
			File[] files = inFolder.listFiles();
			for(int i=0; i<files.length; i++){
				if(files[i].isDirectory()){
					copyFolder(files[i].getAbsolutePath(), outPath+File.separator+files[i].getName());
				}else{
					copyFile(files[i].getAbsolutePath(), outPath+File.separator+files[i].getName());
				}
			}
		}
	}
	
	/** copy a file from org to dest path */
	public static void copyFile(String inPath, String outPath) throws IOException {
		
		FileInputStream fis  = new FileInputStream(new File(inPath));
		FileOutputStream fos = new FileOutputStream(new File(outPath));
		try{
			byte[] buf = new byte[1024];
			int i = 0;
			while((i = fis.read(buf)) != -1){
				fos.write(buf, 0, i);
			}
			if( (new File(inPath)).length() != (new File(outPath)).length() ){
				throw new IOException("error on copy: "+inPath+" > "+outPath);
			}
		} 
		catch(IOException e){ throw e; }
		finally {
			if(fis != null){ fis.close(); }
			if(fos != null){ fos.close(); }
		}
		
	}
	
	/** recursive create a folder */
	public static void createFolder(String path) throws IOException {
		
		File folder = new File(path);
		if(!folder.isDirectory()){
			if(!folder.mkdirs()){
				throw new IOException("Could not create folder: "+path);
			}
		}
	}
	
	/** path of current working-directory */
	public static String getWorkingDir(){
		return System.getProperty("user.dir");
	}
	
	/** name of current operating-system */
	public static String getOSName(){
		return System.getProperty("os.name");
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
