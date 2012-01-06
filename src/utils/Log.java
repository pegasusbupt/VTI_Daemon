package utils;

public class Log {
	private final static boolean LOG = false;
	
	public static void print(String msg){
		if(LOG)
			System.out.print(msg);
	}
	
	public static void print(String tag, String msg){
		if(LOG)
			System.out.print(msg+"\n\t at "+tag);
	}
	
	public static void println(){
		if(LOG)
			System.out.println();
	}
	
	public static void println(double msg){
		if(LOG)
			System.out.println(msg);
	}
	
	public static void println(String msg){
		if(LOG)
			System.out.println(msg);
	}
	
	public static void println(String tag, String msg){
		if(LOG)
			System.out.print(msg+"\n\t at "+tag);
	}

}
