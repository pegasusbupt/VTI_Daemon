package account;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.TwitterException;

public class TrainRouteVTIAccount extends VTIAccount{

	public TrainRouteVTIAccount(String screen_name) throws IOException {
		super(screen_name);
	}

	public void run() {
		PrintWriter logOut=null;
		try {
			logOut = new PrintWriter(new FileWriter("logs/"+twitter.getScreenName()+".txt"));
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TwitterException e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			List<Status> statuses;
			List<DirectMessage> dms;
						
			try {
				statuses = twitter.getMentions();
				dms = twitter.getDirectMessages();
						
				logOut.println("Showing latest @" + user.getScreenName()
						+ "'s mentions.");
				for (Status status : statuses)
					if (!seen_statuses.contains(String.valueOf(status.getId()))) {
						seen_statuses.add(String.valueOf(status.getId()));
						logOut.println("@"
								+ status.getUser().getScreenName() + " - "
								+ status.getText());
						// remove @screen_name (regardless letter cases) within the status
						String new_status=status.getText().replaceAll(
								"@" + user.getScreenName(), "");
						new_status=new_status.replaceAll("@" + user.getScreenName().toLowerCase(), "");
						
						twitter.updateStatus(new_status);

					}
				
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		
			try {
				logOut.println();
				Thread.sleep(1000); // check the received tweets every 1 sec
			} catch (InterruptedException e) {
				//e.printStackTrace();
				logOut.close();
				break;
			}

		}
	}
}
