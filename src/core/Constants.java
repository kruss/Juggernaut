package core;

import java.io.File;

/**
 * Contains general constants.<br>
 * <p>
 * Version <i>major.minor.micro</i> increased if:
 * <ul>
 * 	<li><b>major</b>: changes do affect serialized data and external interfaces</li>
 * 	<li><b>minor</b>: changes do affect serialized data but not external interfaces</li>
 * 	<li><b>micro</b>: changes do not affect serialized data and external interfaces</li>
 * </ul>
 * </p>
 */
public class Constants {

	public static final String 		APP_NAME = "Juggernaut";
	public static final String 		APP_VERSION = "0.1.4"; 
	public static final String 		APP_FULL_NAME = APP_NAME+" ("+APP_VERSION+")";
	public static final int 		APP_WIDTH = 800;
	public static final int 		APP_HEIGHT = 600;
	public static final int 		APP_STYLE = 1;
	
	public static final String 		DATA_FOLDER = "data";
	public static final String 		BUILD_FOLDER = DATA_FOLDER+File.separator+"build";
	public static final String 		HISTORY_FOLDER = DATA_FOLDER+File.separator+"history";
	public static final String 		TEMP_FOLDER = DATA_FOLDER+File.separator+"temp";
	
	public static final long 		LOGFILE_MAX = 2 * 1024 * 1024; // 2MB
	
	public static final int 		PROCESS_OK = 0;
	public static final int 		PROCESS_NOK = 0;
}
