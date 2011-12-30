package utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringProcess {
	public static String stack2string(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} catch (Exception e2) {
			return "bad stack2string, exception info. lost.";
		}
	}
	
	public static String shortenTrainAlerts(String alert){
		return alert.replaceAll("2012 ", "").substring(0, 139);
	}
}
