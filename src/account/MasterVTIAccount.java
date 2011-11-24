package account;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import main.VTI;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import utils.FeedReader;
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

	Twitter assignPublication(Status status) {
		GeoLocation loc = status.getGeoLocation();
		Place plc = status.getPlace();
		if (loc != null)
			System.out.println(loc);
		else{
			System.out.println(status.getText()+" is not embeded with location information");
			return null;
		}
		
        return null;
		
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
						Twitter targetAccount = assignPublication(status);
						if (targetAccount != null)
							if (status.getText().length() > 140)
								targetAccount.updateStatus(StringProcess
										.messageShorten(status.getText()));
							else
								targetAccount.updateStatus(status.getText());

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