package account;

import java.io.IOException;
import java.util.List;

import twitter4j.TwitterException;
import utils.FeedReader;

public class TrainRouteVTIAccount extends VTIAccount {

	public TrainRouteVTIAccount(String screen_name) throws IOException {
		super(screen_name);
	}

	@Override
	public void run() {
		while (true) {
			List<String> statuses;
			
			try {
				//System.out.println(FeedReader.route_id.get(twitter.getScreenName().toLowerCase())+"  "+twitter.getScreenName().toLowerCase());
				statuses = FeedReader
						.retrieveFeeds("http://www.transitchicago.com/rss/railalertsrss.aspx?RouteId="+FeedReader.route_id.get(twitter.getScreenName().toLowerCase()));
				// dms = twitter.getDirectMessages();
				if (statuses.size() > 0) {
					for (String status : statuses) {
						if (status.length() > 140)
							twitter.updateStatus(status.substring(0,139));
						else
							twitter.updateStatus(status);
					}
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000); // check the received tweets every 1 sec
			} catch (InterruptedException e) {
				break;
			}

		}
	}
}
