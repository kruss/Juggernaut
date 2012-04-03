package core.runtime.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import util.IChangeListener;
import util.Task;

import core.runtime.FileManager;
import core.runtime.TaskManager;
import core.runtime.logger.Logger;
import core.runtime.logger.ILogConfig.Module;

public class HttpServer implements IHttpServer {

	private IHttpConfig config;
	private TaskManager taskManager;
	private Logger logger;
	private ArrayList<IChangeListener> listeners;
	private ServerSocket socket;
	private ServerThread thread; 
	private File root;
	
	@Override
	public IHttpConfig getConfig(){ return config; }
	
	@Override
	public boolean isRunning(){ return thread != null; }
	
	public HttpServer(
			IHttpConfig config, 
			FileManager fileManager, 
			TaskManager taskManager, 
			Logger logger
	){
		this.config = config;
		this.logger = logger;
		this.taskManager = taskManager;
		
		root = fileManager.getHistoryFolder();
		listeners = new ArrayList<IChangeListener>();
	}
	
	@Override
	public void init() throws Exception {
		if(config.isHttpServer()){
			start();
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		stop();
	}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	@Override
	public void start() throws Exception {
		if(thread == null){
			startServer();
			notifyListeners();
		}
	}

	@Override
	public void stop() throws Exception {
		if(thread != null){
			stopServer();
			notifyListeners();
		}
	}
	
	private void startServer() throws Exception {
		int port = config.getHttpPort();
		logger.debug(Module.HTTP, "Startup HTTP - Port: "+port);
		socket = new ServerSocket(port);
		thread = new ServerThread(this);
		thread.asyncRun(0, 0);
	}
	
	private void stopServer() throws Exception {
		logger.debug(Module.HTTP, "Shutdown HTTP");
		socket.close();
		thread.syncStop(1000);
		thread = null;
	}
	
	private class ServerThread extends Task {

		private HttpServer server;
		
		public ServerThread(HttpServer server) {
			super("HttpServer", taskManager);
			
			this.server = server;
			setCyclic(100);
		}
		
		protected void runTask() {
			try{
				new HttpSession(server, socket.accept());
			}catch(IOException e){
				logger.debug(Module.HTTP, e.getMessage());
			}
		}
	}
	
	public HttpResponse serve(String uri, String method, HashMap<String, String> header, HashMap<String, String> params){
		logger.debug(Module.HTTP, method+" '"+uri+"' ");
		for(String key : header.keySet()){
			logger.debug(Module.HTTP, "  HDR: '"+key+"' = '"+header.get(key)+ "'");
		}
		for(String key : params.keySet()){
			logger.debug(Module.HTTP, "  PRM: '"+key+"' = '"+params.get(key)+ "'");
		}

		if(root.isDirectory()){
			return serveFile(uri, header);
		}else{
			return new HttpResponse( 
					HTTP_INTERNALERROR, MIME_TEXT,
					"not a directory: "+root.getAbsolutePath()
			);
		}
	}

	public HttpResponse serveFile(String uri, HashMap<String, String> header) {
		// cut off args
		uri = uri.trim().replace(File.separatorChar, '/');
		if(uri.indexOf('?') >= 0){
			uri = uri.substring(0, uri.indexOf( '?' ));
		}
		// protect browsing
		if(uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0){
			return new HttpResponse( 
					HTTP_FORBIDDEN, MIME_TEXT,
					"invalid uri: "+uri
			);
		}
		// check for existence
		File file = new File(root, uri);
		if(!file.exists()){
			return new HttpResponse( 
					HTTP_NOTFOUND, MIME_TEXT,
					"not a file: "+file.getAbsolutePath() 
			);
		}
		
		// check for directory
		if(file.isDirectory()){
			if (!uri.endsWith( "/")){ // redirect to folder
				uri += "/";
				HttpResponse response = new HttpResponse( 
						HTTP_REDIRECT, MIME_HTML,
						"<html><body>Redirected: <a href=\""+uri+"\">"+uri+"</a></body></html>"
				);
				response.header.put("Location", uri);
				return response;
			}
			
			// check for index-page
			if(new File(file, "index.html").exists()){
				file = new File(root, uri+"/index.html");
			}else if(new File(file, "index.htm").exists()){
				file = new File(root, uri+"/index.htm");
			}else{ // list the directory
				String message = getListing(uri, file);
				return new HttpResponse(
						HTTP_OK, MIME_HTML, 
						message
				);
			}
		}

		// create response
		try{
			String mime = getMimeType(file);
			long start = parseStart(header);
			FileInputStream stream = new FileInputStream(file);
			stream.skip(start);
			HttpResponse response = new HttpResponse(HTTP_OK, mime, stream);
			response.header.put("Content-length", ""+(file.length()-start));
			response.header.put("Content-range", start+"-"+(file.length()-1)+"/"+file.length());
			return response;
		}catch(IOException e){
			return new HttpResponse(
					HTTP_FORBIDDEN, MIME_TEXT, 
					"Could not read: "+file.getAbsolutePath() 
			);
		}
	}

	private String getListing(String uri, File folder) {
		String[] files = folder.list();
		String message = "<html><body><h1>Directory "+uri+"</h1><br/>";
		// parent path
		if(uri.length() > 1){
			String part = uri.substring(0, uri.length()-1);
			int index = part.lastIndexOf('/');
			if(index >= 0 && index  < part.length()){
				message += "<b><a href=\"" + uri.substring(0, index+1) + "\">..</a></b><br/>";
			}
		}
		// folder content
		for(int i=0; i<files.length; ++i){
			File file = new File(folder, files[i]);
			if(file.isDirectory()){
				message += "<b>";
				files[i] += "/";
			}
			message += "<a href=\""+encodeUri(uri+files[i])+"\">"+files[i]+"</a>";
			if(file.isFile()){
				message += " &nbsp;<font size=2>("+file.length()+" bytes"+")</font>";
			}
			message += "<br/>";
			if(file.isDirectory()){ 
				message += "</b>"; 
			}
		}
		return message;
	}

	public static String formatDate(Date date){
		SimpleDateFormat formater = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		return formater.format(date);
	}
	
	@SuppressWarnings("deprecation")
	private String encodeUri(String uri){
		String encoded = "";
		StringTokenizer tokenizer = new StringTokenizer(uri, "/ ", true);
		while(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();
			if(token.equals( "/" )){
				encoded += "/";
			}else if(token.equals( " " )){
				encoded += "%20";
			}else{
				try{ 
					encoded += URLEncoder.encode(token, "UTF-8"); 
				}catch(UnsupportedEncodingException e){
					encoded += URLEncoder.encode(token);
				}
			}
		}
		return encoded;
	}
	
	private long parseStart(HashMap<String, String> header) {
		long start = 0;
		String range = header.get("range");
		if(range != null){
			if(range.startsWith("bytes=")){
				range = range.substring("bytes=".length());
				int index = range.indexOf('-');
				if(index > 0){
					range = range.substring(0, index);
				}
				try{
					start = Long.parseLong(range);
				}catch(NumberFormatException e){
					/* NOTHING */
				}
			}
		}
		return start;
	}
	
	private String getMimeType(File file) throws IOException {
		String mime = MIME_BINARY;
		int index = file.getCanonicalPath().lastIndexOf('.');
		if(index >= 0){
			String extention = file.getCanonicalPath().substring(index + 1).toLowerCase();
			mime = getMimeType(extention);
		}
		return mime;
	}
	
	public static String getMimeType(String extention){
		if(
			extention.equals("txt") ||
			extention.equals("log")
		){
			return MIME_TEXT;
		}else if(
			extention.equals("htm") ||
			extention.equals("html")
		){
			return MIME_HTML;
		}else{
			return MIME_BINARY;
		}
	}

	public static final String
		HTTP_OK = "200 OK",
		HTTP_REDIRECT = "301 Moved Permanently",
		HTTP_FORBIDDEN = "403 Forbidden",
		HTTP_NOTFOUND = "404 Not Found",
		HTTP_BADREQUEST = "400 Bad Request",
		HTTP_INTERNALERROR = "500 Internal Server Error",
		HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	public static final String
		MIME_TEXT = "text/plain",
		MIME_HTML = "text/html",
		MIME_BINARY = "application/octet-stream";
}

