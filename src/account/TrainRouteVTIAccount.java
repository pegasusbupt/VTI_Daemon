package account;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import utils.*;

import twitter4j.Status;
import twitter4j.TwitterException;

public class TrainRouteVTIAccount extends VTIAccount {

	public TrainRouteVTIAccount(String screen_name) throws IOException {
		super(screen_name);
	}

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
			List<String> statuses;
			
			try {
				statuses = FeedReader
						.retrieveFeeds("http://www.transitchicago.com/rss/railalertsrss.aspx?RouteId="+FeedReader.route_id.get(twitter.getScreenName().toLowerCase()));
				// dms = twitter.getDirectMessages();
				if (statuses.size() > 0) {
					logOut.write(DateFormat.getDateTimeInstance(
							DateFormat.FULL, DateFormat.MEDIUM).format(
							cal.getTime()));
					logOut.newLine();
					for (String status : statuses) {
						logOut.write(status);
						logOut.newLine();
						if (status.length() > 140)
							twitter.updateStatus(StringProcess.messageShorten(status));

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
