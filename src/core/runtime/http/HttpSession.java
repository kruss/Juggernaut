package core.runtime.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class HttpSession implements Runnable {
	
	private HttpServer server;
	private Socket socket;
	
	public HttpSession(HttpServer server, Socket socket){
		this.server = server;
		this.socket = socket;
		
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	public void run(){
		try{
			InputStream input = socket.getInputStream();
			if(input == null){ return; }

			// get request
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String firstLine = reader.readLine();
			if(firstLine == null){ return; }
			StringTokenizer tokenizer = new StringTokenizer(firstLine);
			if(!tokenizer.hasMoreTokens()){
				sendResponse(HttpServer.HTTP_BADREQUEST, "Missing methode");
				return;
			}
			String method = tokenizer.nextToken();
			if(!tokenizer.hasMoreTokens()){
				sendResponse( HttpServer.HTTP_BADREQUEST, "Missing URI");
				return;
			}
			String uri = tokenizer.nextToken();

			// decode parameters
			HashMap<String, String> parms = new HashMap<String, String>();
			uri = extractUriParameters(uri, parms);

			// get header
			HashMap<String, String> header = new HashMap<String, String>();
			extractHeader(reader, tokenizer, header);

			// check payload
			if(method.equalsIgnoreCase("POST")){
				extractContentParams(reader, parms, header);
			}

			// create response
			HttpResponse response = server.serve(uri, method, header, parms);
			if(response != null){
				sendResponse( response.status, response.mime, response.header, response.content );
			}else{
				sendResponse(HttpServer.HTTP_INTERNALERROR, "Invalid request: "+uri);
			}
			reader.close();
		}catch(Exception e){
			sendResponse(HttpServer.HTTP_INTERNALERROR, "Exception: "+e.getMessage());
		}
	}

	private void extractHeader(
			BufferedReader reader, 
			StringTokenizer tokenizer, 
			HashMap<String, String> header
	) throws Exception {
		if(tokenizer.hasMoreTokens()){
			String line = reader.readLine();
			while(line.trim().length() > 0){
				int index = line.indexOf(':');
				header.put(line.substring(0, index).trim().toLowerCase(), line.substring(index+1).trim());
				line = reader.readLine();
			}
		}
	}

	private String extractUriParameters(
			String uri, 
			HashMap<String, String> parms
	) throws Exception {
		int index = uri.indexOf('?');
		if(index >= 0){
			decodeParmeter(uri.substring(index+1), parms);
			return decodeUri(uri.substring(0, index));
		}else{
			return decodeUri(uri);
		}
	}

	private void extractContentParams(
			BufferedReader reader, 
			HashMap<String, String> parms, 
			HashMap<String, String> header
	) throws Exception {
		// get length
		long lengthTotal = 0x7FFFFFFFFFFFFFFFl;
		String lengthProperty = header.get("content-length");
		if(lengthProperty != null){
			try{ 
				lengthTotal = Integer.parseInt(lengthProperty); 
			}catch(NumberFormatException e){
				/* NOTHING */
			}
		}
		// extract parameters
		String line = "";
		int length;
		char buffer[] = new char[512];
		while( 
				(length = reader.read(buffer)) >= 0 && lengthTotal > 0 && 
				!line.endsWith("\r\n") 
		){
			lengthTotal -= length;
			line += String.valueOf(buffer, 0, length);
			if(lengthTotal > 0){
				length = reader.read(buffer);
			}
		}
		line = line.trim();
		decodeParmeter(line, parms);
	}

	private String decodeUri(String uri) throws Exception {
		StringBuilder decoded = new StringBuilder();
		for(int i=0; i<uri.length(); i++){
		    char c = uri.charAt(i);
		    switch (c){
		        case '+':
		            decoded.append(' ');
		            break;
		        case '%':
	                decoded.append((char)Integer.parseInt(uri.substring(i+1,i+3), 16));
		            i += 2;
		            break;
		        default:
		            decoded.append(c);
		            break;
		    }
		}
		return decoded.toString();
	}

	private void decodeParmeter(String uri, HashMap<String, String> params) throws Exception {
		if(uri != null ){
			StringTokenizer tokenizer = new StringTokenizer(uri, "&");
			while ( tokenizer.hasMoreTokens()){
				String token = tokenizer.nextToken();
				int index = token.indexOf('=');
				if(index >= 0){
					params.put(
						decodeUri(token.substring( 0, index )).trim(),
						decodeUri(token.substring( index+1 ))
					);
				}
			}
		}
	}

	private void sendResponse(String status, String message) {
		sendResponse(status, HttpServer.MIME_TEXT, null, new ByteArrayInputStream(message.getBytes()));
	}
	
	private void sendResponse(String status, String mime, HashMap<String, String> header, InputStream body){
		try{
			OutputStream output = socket.getOutputStream();
			PrintWriter writer = new PrintWriter(output);
			
			// create header
			writer.print("HTTP/1.0 "+status+" \r\n");
			if(mime != null){
				writer.print("Content-Type: " + mime + "\r\n");
			}
			if(header == null || header.get("Date") == null){
				writer.print( "Date: " + HttpServer.formatDate( new Date()) + "\r\n");
			}
			if(header != null){
				for(String key : header.keySet()){
					String value = header.get(key);
					writer.print(key+": "+value+"\r\n");
				}
			}
			writer.print("\r\n");
			writer.flush();

			// write content
			if(body != null){
				try{
					byte[] buffer = new byte[2048];
					int length;
					while((length = body.read(buffer, 0, 2048)) > 0){
						output.write(buffer, 0, length);
					}
				}finally{
					output.flush();
					output.close();
					body.close();
				}
			}
		}catch(Exception e){
			try{ 
				socket.close(); 
			}catch(Exception ex){
				/* NOTHING */
			}
		}
	}
}

