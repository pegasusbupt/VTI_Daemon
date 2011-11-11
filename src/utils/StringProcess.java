package utils;

public class StringProcess {
	/*
	 * @ this function shortens a message to 140 character or below
	 */
	public static String messageShorten(String msg){
		return msg.substring(0, 139);
	}

}
