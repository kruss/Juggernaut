package core.runtime.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class HttpResponse {

	public String status;
	public String mime;
	public InputStream content;
	public HashMap<String, String> header;

	public HttpResponse(String status, String mime, InputStream content){
		this.status = status;
		this.mime = mime;
		this.content = content;
		header = new HashMap<String, String>();
	}

	public HttpResponse(String status, String mime, String content){
		this.status = status;
		this.mime = mime;
		this.content = new ByteArrayInputStream(content.getBytes());
		header = new HashMap<String, String>();
	}
}

