package account;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import main.VTI;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import utils.StringProcess;

/**
 * @author Sol Ma
 * @description: the master VTI account that does the assigning work
 * 
 */
public class MasterVTIAccount extends VTIAccount {
	public MasterVTIAccount(String screen_name) throws IOException {
		super(screen_name);
	}
	
	double calculateDistance(GeoLocation l1, GeoLocation l2){
		//TODO: implement distance calculation
		return 0;
	}

	VTIAccount assignPublication(Status status) {
		GeoLocation loc = status.getGeoLocation();
		VTIAccount ret=null;
		if (loc != null)
			System.out.println(loc);
		else{
			System.out.println(status.getText()+" is not embeded with location information");
			return ret;
		}
		double minimum=Double.MAX_VALUE;
		for(VTIAccount account: VTI.vti.values()){
			double dis=calculateDistance(account.location, loc);
			if(dis<minimum){
				minimum=dis;
				ret=account;
			}
		}
        return ret;
		
	}

	@Override
	public void run() {
		BufferedWriter logOut = null;
		Calendar cal = Calendar.getInstance();
		try {
			logOut = new BufferedWriter(new FileWriter("logs/"
					+ twitter.getScreenName() + ".txt", true));
		} catch (IllegalStateException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (TwitterException e2) {
			e2.printStackTrace();
		}
		while (true) {
			List<Status> statuses;
			try {
				statuses = twitter.getMentions();
				if (statuses.size() > 0) {
					logOut.write(DateFormat.getDateTimeInstance(
							DateFormat.FULL, DateFormat.MEDIUM).format(
							cal.getTime()));
					logOut.newLine();
					for (Status status : statuses) {
						logOut.write(status.toString());
						logOut.newLine();

						// determine which account this status is assigned to
						VTIAccount targetAccount = assignPublication(status);
						if (targetAccount != null)
							if (status.getText().length() > 140)
								targetAccount.twitter.updateStatus(StringProcess
										.messageShorten(status.getText()));
							else
								targetAccount.twitter.updateStatus(status.getText());

					}
					logOut.newLine();
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000); // check the received tweets every 1 sec
			} catch (InterruptedException e) {
				try {
					logOut.close();
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				break;
			}

		}
	}
}