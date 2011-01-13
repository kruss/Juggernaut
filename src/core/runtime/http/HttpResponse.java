package core.runtime.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;


/**
 * HTTP response.
 * Return one of these from serve().
 */
public class HttpResponse
{
	public HttpResponse()
	{
		this.status = HttpServer.HTTP_OK;
	}

	public HttpResponse(String status, String mimeType, InputStream data)
	{
		this.status = status;
		this.mimeType = mimeType;
		this.data = data;
	}

	public HttpResponse(String status, String mimeType, String txt)
	{
		this.status = status;
		this.mimeType = mimeType;
		this.data = new ByteArrayInputStream(txt.getBytes());
	}

	public void addHeader(String name, String value)
	{
		header.put(name, value);
	}

	public String status;
	public String mimeType;
	public InputStream data;
	public Properties header = new Properties();
}

