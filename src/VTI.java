import java.io.IOException;
import java.util.HashMap;

import account.VTIAccount;

import twitter4j.TwitterException;


public class VTI {

	public static void main(String[] args) throws IOException,
			IllegalStateException, TwitterException, InterruptedException {
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
        
		/*
		VTIAccount ins=new VTIAccount("Sol_Ma");
		for(int i=0;i<500;i++)
			ins.getTwitter().getMentions();
		*/
		
		HashMap<String, Thread> vti = new HashMap<String, Thread>();
		//vti.put("simpleasure", new Thread(new VTIAccount("simpleasure")) );
		//vti.put("VTIDEMOROBOT", new Thread(new VTIAccount("VTIDEMOROBOT")) );
		vti.put("VTI_brownLine", new Thread(new VTIAccount("VTI_brownline")) );
		//vti.put("Sol_Ma", new Thread(new VTIAccount("Sol_Ma")) );

		// for each account, start monitoring statuses
		long start_time=System.currentTimeMillis();
		for( Thread account: vti.values())
			account.start();
	
		while(System.currentTimeMillis()-start_time<run_time){
			Thread.sleep(1000);
		}

		// for each account, stop monitoring statuses
		for( Thread account: vti.values())
			account.interrupt();
		
		System.out.println("**********************************************************");
		System.out.println("Run time drains out! Program is termined.");
		System.out.println("**********************************************************");
		//System.exit(0);
       
	}

}
