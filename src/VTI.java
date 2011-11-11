import java.io.IOException;
import java.util.HashMap;

import twitter4j.TwitterException;
import utils.FeedReader;
import account.TrainRouteVTIAccount;


public class VTI {

	public static void main(String[] args){
		new VTI().run(args);
	}
	
	public void run(String[] args){
		// default running time equals to minutes
		long run_time = 1000 * 10;
		if (args.length > 0) {
			try {
				run_time = Long.parseLong(args[0]) * 1000;
			} catch (NumberFormatException e) {
				System.out.println("The first parameter is not a number");
				System.exit(1);
			}
		}
        
		HashMap<String, Thread> vti = new HashMap<String, Thread>();
		addTrainRoutes(vti);
		//vti.put("simpleasure", new Thread(new VTIAccount("simpleasure")) );
		//vti.put("VTIDEMOROBOT", new Thread(new VTIAccount("VTIDEMOROBOT")) );
		//vti.put("Sol_Ma", new Thread(new VTIAccount("Sol_Ma")) );

		// for each account, start monitoring statuses
		long start_time=System.currentTimeMillis();
		for( Thread account: vti.values())
			account.start();
	
		while(System.currentTimeMillis()-start_time<run_time){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// for each account, stop monitoring statuses
		for( Thread account: vti.values())
			account.interrupt();
		
		System.out.println("**********************************************************");
		System.out.println("Run time drains out! Program is termined.");
		System.out.println("**********************************************************");
		//System.exit(0);
       
	}
	
	public void addTrainRoutes(HashMap<String, Thread> vti){
		try {
			for(String route: FeedReader.route_id.keySet())
				vti.put(route, new Thread(new TrainRouteVTIAccount(route)) );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
