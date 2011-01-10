package http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import core.FileManager;
import core.ISystemComponent;
import core.TaskManager;

import logger.Logger;
import logger.ILogConfig.Module;

@SuppressWarnings({ "deprecation", "unchecked" })
public class HttpServer implements ISystemComponent, IHttpServer {

	private IHttpConfig config;
	private File root;
	private ServerSocket serverSocket;
	private Thread serverThread; 
	private Logger logger;
	private boolean running;
	
	@Override
	public IHttpConfig getConfig(){ return config; }
	
	@Override
	public boolean isRunning(){ return running; }
	
	public HttpServer(IHttpConfig config, FileManager fileManager, TaskManager taskManager, Logger logger)
	{
		this.config = config;
		this.logger = logger;
		root = fileManager.getHistoryFolder();
		running = false;
	}
	
	@Override
	public void init() throws Exception {
		if(config.isWebserver()){
			startServer();
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		stopServer();
	}
	
	@Override
	public void startServer() throws Exception {
		if(!running){
			startHttpServer();
		}
	}

	@Override
	public void stopServer() throws Exception {
		if(running){
			stopHttpServer();
		}
	}
	
	private void startHttpServer() throws Exception {
		
		int port = config.getHttpPort();
		logger.log(Module.HTTP, "Startup HTTP - Port: "+port);
		serverSocket = new ServerSocket(port);
		final HttpServer instance = this;
		running = true;
		serverThread = new Thread(new Runnable(){
			public void run(){
				while(running){
					try{
						new HttpSession( instance, serverSocket.accept());
					}catch(IOException e){
						logger.debug(Module.HTTP, e.getMessage());
					}
				}
			}
		});
		serverThread.setDaemon(true);
		serverThread.start();
	}
	
	private void stopHttpServer() throws Exception {
		
		logger.log(Module.HTTP, "Shutdown HTTP");
		running = false;
		serverSocket.close();
		serverThread.join();
	}
	
	/**
	 * By default, this delegates to serveFile() and allows directory listing.
	 *
	 * @parm uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @parm method	"GET", "POST" etc.
	 * @parm parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @parm header	Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public HttpResponse serve(String uri, String method, Properties header, Properties parms)
	{
		logger.debug(Module.HTTP, method+" '"+uri+"' ");

		Enumeration e = header.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			logger.debug(Module.HTTP, "  HDR: '" + value + "' = '" +
								header.getProperty( value ) + "'" );
		}
		e = parms.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			logger.debug(Module.HTTP, "  PRM: '" + value + "' = '" +
								parms.getProperty( value ) + "'" );
		}

		return serveFile( uri, header, root, true );
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.
	 */
	public HttpResponse serveFile( String uri, Properties header, File homeDir,
							   boolean allowDirectoryListing )
	{
		// Make sure we won't die of an exception later
		if ( !homeDir.isDirectory())
			return new HttpResponse( HTTP_INTERNALERROR, MIME_PLAINTEXT,
								 "INTERNAL ERRROR: serveFile(): given homeDir is not a directory." );

		// Remove URL arguments
		uri = uri.trim().replace( File.separatorChar, '/' );
		if ( uri.indexOf( '?' ) >= 0 )
			uri = uri.substring(0, uri.indexOf( '?' ));

		// Prohibit getting out of current directory
		if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.indexOf( "../" ) >= 0 )
			return new HttpResponse( HTTP_FORBIDDEN, MIME_PLAINTEXT,
								 "FORBIDDEN: Won't serve ../ for security reasons." );

		File f = new File( homeDir, uri );
		if ( !f.exists())
			return new HttpResponse( HTTP_NOTFOUND, MIME_PLAINTEXT,
								 "Error 404, file not found." );

		// List the directory, if necessary
		if ( f.isDirectory())
		{
			// Browsers get confused without '/' after the
			// directory, send a redirect.
			if ( !uri.endsWith( "/" ))
			{
				uri += "/";
				HttpResponse r = new HttpResponse( HTTP_REDIRECT, MIME_HTML,
										   "<html><body>Redirected: <a href=\"" + uri + "\">" +
										   uri + "</a></body></html>");
				r.addHeader( "Location", uri );
				return r;
			}

			// First try index.html and index.htm
			if ( new File( f, "index.html" ).exists())
				f = new File( homeDir, uri + "/index.html" );
			else if ( new File( f, "index.htm" ).exists())
				f = new File( homeDir, uri + "/index.htm" );

			// No index file, list the directory
			else if ( allowDirectoryListing )
			{
				String[] files = f.list();
				String msg = "<html><body><h1>Directory " + uri + "</h1><br/>";

				if ( uri.length() > 1 )
				{
					String u = uri.substring( 0, uri.length()-1 );
					int slash = u.lastIndexOf( '/' );
					if ( slash >= 0 && slash  < u.length())
						msg += "<b><a href=\"" + uri.substring(0, slash+1) + "\">..</a></b><br/>";
				}

				for ( int i=0; i<files.length; ++i )
				{
					File curFile = new File( f, files[i] );
					boolean dir = curFile.isDirectory();
					if ( dir )
					{
						msg += "<b>";
						files[i] += "/";
					}

					msg += "<a href=\"" + encodeUri( uri + files[i] ) + "\">" +
						   files[i] + "</a>";

					// Show file size
					if ( curFile.isFile())
					{
						long len = curFile.length();
						msg += " &nbsp;<font size=2>(";
						if ( len < 1024 )
							msg += curFile.length() + " bytes";
						else if ( len < 1024 * 1024 )
							msg += curFile.length()/1024 + "." + (curFile.length()%1024/10%100) + " KB";
						else
							msg += curFile.length()/(1024*1024) + "." + curFile.length()%(1024*1024)/10%100 + " MB";

						msg += ")</font>";
					}
					msg += "<br/>";
					if ( dir ) msg += "</b>";
				}
				return new HttpResponse( HTTP_OK, MIME_HTML, msg );
			}
			else
			{
				return new HttpResponse( HTTP_FORBIDDEN, MIME_PLAINTEXT,
								 "FORBIDDEN: No directory listing." );
			}
		}

		try
		{
			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = f.getCanonicalPath().lastIndexOf( '.' );
			if ( dot >= 0 )
				mime = (String)theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
			if ( mime == null )
				mime = MIME_DEFAULT_BINARY;

			// Support (simple) skipping:
			long startFrom = 0;
			String range = header.getProperty( "range" );
			if ( range != null )
			{
				if ( range.startsWith( "bytes=" ))
				{
					range = range.substring( "bytes=".length());
					int minus = range.indexOf( '-' );
					if ( minus > 0 )
						range = range.substring( 0, minus );
					try	{
						startFrom = Long.parseLong( range );
					}
					catch ( NumberFormatException nfe ) {}
				}
			}

			FileInputStream fis = new FileInputStream( f );
			fis.skip( startFrom );
			HttpResponse r = new HttpResponse( HTTP_OK, mime, fis );
			r.addHeader( "Content-length", "" + (f.length() - startFrom));
			r.addHeader( "Content-range", "" + startFrom + "-" +
						(f.length()-1) + "/" + f.length());
			return r;
		}
		catch( IOException ioe )
		{
			return new HttpResponse( HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
		}
	}

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Hashtable theMimeTypes = new Hashtable();
	static
	{
		StringTokenizer st = new StringTokenizer(
			"htm		text/html "+
			"html		text/html "+
			"txt		text/plain "+
			"asc		text/plain "+
			"gif		image/gif "+
			"jpg		image/jpeg "+
			"jpeg		image/jpeg "+
			"png		image/png "+
			"mp3		audio/mpeg "+
			"m3u		audio/mpeg-url " +
			"pdf		application/pdf "+
			"doc		application/msword "+
			"ogg		application/x-ogg "+
			"zip		application/octet-stream "+
			"exe		application/octet-stream "+
			"class		application/octet-stream " );
		while ( st.hasMoreTokens())
			theMimeTypes.put( st.nextToken(), st.nextToken());
	}

	/**
	 * Some HTTP response status codes
	 */
	public static final String
		HTTP_OK = "200 OK",
		HTTP_REDIRECT = "301 Moved Permanently",
		HTTP_FORBIDDEN = "403 Forbidden",
		HTTP_NOTFOUND = "404 Not Found",
		HTTP_BADREQUEST = "400 Bad Request",
		HTTP_INTERNALERROR = "500 Internal Server Error",
		HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String
		MIME_PLAINTEXT = "text/plain",
		MIME_HTML = "text/html",
		MIME_DEFAULT_BINARY = "application/octet-stream";


	/**
	 * URL-encodes everything between "/"-characters.
	 * Encodes spaces as '%20' instead of '+'.
	 */
	private String encodeUri( String uri )
	{
		String newUri = "";
		StringTokenizer st = new StringTokenizer( uri, "/ ", true );
		while ( st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if ( tok.equals( "/" ))
				newUri += "/";
			else if ( tok.equals( " " ))
				newUri += "%20";
			else
			{
				try { 
					newUri += URLEncoder.encode( tok, "UTF-8" ); 
				} catch ( UnsupportedEncodingException uee ){
					newUri += URLEncoder.encode( tok );
				}
			}
		}
		return newUri;
	}

	/**
	 * GMT date formatter
	 */
    public static java.text.SimpleDateFormat gmtFrmt;
	static
	{
		gmtFrmt = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
}

