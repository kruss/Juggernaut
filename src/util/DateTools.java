package util;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTools {

	/** get date of format e.g: 2008.11.22_02.52.11 */
	public static String getFileSystemDate(Date date){

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		DecimalFormat df4 = new DecimalFormat("0000");
		DecimalFormat df2 = new DecimalFormat("00");
		return 
			df4.format(calendar.get(Calendar.YEAR))+"."+
			df2.format((calendar.get(Calendar.MONTH)+1))+"."+
			df2.format(calendar.get(Calendar.DAY_OF_MONTH))+"_"+
			df2.format(calendar.get(Calendar.HOUR_OF_DAY))+"."+
			df2.format(calendar.get(Calendar.MINUTE))+"."+
			df2.format(calendar.get(Calendar.SECOND));

	}
	
	/** parse string of format e.g: 2008.11.22_02.52.11 */
	public static Date parseFileSystemDate(String dateString){

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.clear();
		calendar.set(
			new Integer(dateString.substring(0, 4)).intValue(), 
			new Integer(dateString.substring(5, 7)).intValue()-1, 
			new Integer(dateString.substring(8, 10)).intValue(), 
			new Integer(dateString.substring(11, 13)).intValue(), 
			new Integer(dateString.substring(14, 16)).intValue(), 
			new Integer(dateString.substring(17, 19)).intValue()
		);
		return calendar.getTime();
	}
	
	/** get date of format e.g: 22.11.2008 02:52:11 */
	public static String getTextDate(Date date){
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		DecimalFormat df4 = new DecimalFormat("0000");
		DecimalFormat df2 = new DecimalFormat("00");
		return 
			df2.format(calendar.get(Calendar.DAY_OF_MONTH))+"."+
			df2.format((calendar.get(Calendar.MONTH)+1))+"."+
			df4.format(calendar.get(Calendar.YEAR))+" "+
			df2.format(calendar.get(Calendar.HOUR_OF_DAY))+":"+
			df2.format(calendar.get(Calendar.MINUTE))+":"+
			df2.format(calendar.get(Calendar.SECOND));
	}
	
	/** get date of format e.g: 22.11.2008 */
	public static String getTextDateShort(Date date){
		return getTextDate(date).substring(0, 10);
	}
	
	/** get date of format e.g: 02:52:11 */
	public static String getTextTime(Date date){
		return getTextDate(date).substring(11, 19);
	}
	
	/** get time difference in minutes */
	public static int getDiffMinutes(Date start, Date end) {
		return (int)((end.getTime() - start.getTime()) / (1000 * 60));
	}
	
	/** get time difference in seconds */
	public static int getDiffSeconds(Date start, Date end) {
		return (int)((end.getTime() - start.getTime()) / (1000));
	}
	
	public static long millis2sec(long millis){
		return millis / 1000;
	}
	
	public static long millis2min(long millis){
		return millis2sec(millis) / 60;
	}
	
	public static long millis2hour(long millis){
		return millis2min(millis) / 60;
	}
	
	public static long millis2days(long millis){
		return millis2hour(millis) / 24;
	}
	
	public static long sec2millis(long sec){
		return sec * 1000;
	}
	
	public static long min2millis(long min){
		return sec2millis(min * 60);
	}
	
	public static long hour2millis(long hour){
		return min2millis(hour * 60);
	}
	
	public static long day2millis(long day){
		return hour2millis(day * 24);
	}
}
