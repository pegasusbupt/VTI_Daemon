package main;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;

import utils.FeedReader;
import account.TrainRouteVTIAccount;
import account.VTIAccount;


public class VTI {
	public final static String VTI_CONSUMER_KEY = "UJxOUdtJm8p3wEOFatp1Q";
	public final static String VTI_CONSUMER_SECRET = "6wIgL90ZKeWPk7G1y0QfztkSm13NiD2Rk3v5Lf7XAg";
	//static database connection for the whole VTI daemon program
	public static Connection conn;
	static{
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/VTI", "postgres",
					"postgres");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static HashMap<String, VTIAccount> vti = new HashMap<String, VTIAccount>();
	
	public static void main(String[] args){
		new VTI().run(args);
	}
	
	public void run(String[] args){
		// default running time equals to minutes
		long run_time = 1000 * 5;
		if (args.length > 0) {
			try {
				run_time = Long.parseLong(args[0]) * 1000;
			} catch (NumberFormatException e) {
				System.out.println("The first parameter is not a number");
				System.exit(1);
			}
		}
        
		addCTAFeedsAccounts();
		//addMasterAccount();

		// for each account, start monitoring statuses
		long start_time=System.currentTimeMillis();
		ArrayList<Thread> threads=new ArrayList<Thread>();
		for( VTIAccount account: vti.values()){
			Thread t=new Thread(account);
			threads.add(t);
			t.start();
		}
	    
	    //while(true){
		while(System.currentTimeMillis()-start_time<run_time){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// interrupt running threads
		for( Thread t: threads)
			t.interrupt();
		
		System.out.println("**********************************************************");
		System.out.println("Run time drains out! Program is termined.");
		System.out.println("**********************************************************");
		System.exit(0);
       
	}
	
	public void addCTAFeedsAccounts(){
		try {
			for(String route: FeedReader.route_id.keySet()){
				vti.put(route, new TrainRouteVTIAccount(route) );
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addMasterAccount(){
		try {
			vti.put("VTI_Robot", new VTIAccount("VTI_Robot") );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
