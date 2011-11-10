package core;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * It posts tweets on VTI Twitter accounts.
 * @author Sol Ma
 * 
 */
public class VTIAccount implements Runnable {
	public final static String VTI_CONSUMER_KEY = "UJxOUdtJm8p3wEOFatp1Q";
	public final static String VTI_CONSUMER_SECRET = "6wIgL90ZKeWPk7G1y0QfztkSm13NiD2Rk3v5Lf7XAg";

	private Twitter twitter;
	private User user;
	private LinkedHashSet<String> seen_statuses;

	public static Twitter authorize(String screen_name) throws IOException,
			TwitterException {
		String credentials = "credentials/" + screen_name;
		File file = new File(credentials);
		Properties prop = new Properties();
		Twitter twitter = null;
		AccessToken accessToken = null;

		if (file.exists()) { // the user has already authorized VTI
			InputStream is = new FileInputStream(file);
			prop.load(is);
			TwitterFactory tf = new TwitterFactory(new ConfigurationBuilder()
					.setDebugEnabled(true)
					.setOAuthConsumerKey(VTI_CONSUMER_KEY)
					.setOAuthConsumerSecret(VTI_CONSUMER_SECRET)
					.setOAuthAccessToken(prop.getProperty("oauth.accessToken"))
					.setOAuthAccessTokenSecret(
							prop.getProperty("oauth.accessTokenSecret"))
					.build());
			twitter = tf.getInstance();
			accessToken = twitter.getOAuthAccessToken();
			is.close();
			System.out
					.println(screen_name
							+ " has alreay authorized VTI, retrieve access token from local file");
		} else { // the user has not authorized VTI yet
			OutputStream os = null;
			try {
				twitter = new TwitterFactory().getInstance();
				twitter.setOAuthConsumer(VTI_CONSUMER_KEY, VTI_CONSUMER_SECRET);

				RequestToken requestToken = twitter.getOAuthRequestToken();
				System.out.println("Got request token.");
				System.out.println("Request token: " + requestToken.getToken());
				System.out.println("Request token secret: "
						+ requestToken.getTokenSecret());

				accessToken = null;
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));

				while (null == accessToken) {
					System.out
							.println("Open the following URL and grant access to your account:");
					System.out.println(requestToken.getAuthorizationURL());
					try {
						Desktop.getDesktop().browse(
								new URI(requestToken.getAuthorizationURL()));
					} catch (IOException ignore) {
					} catch (URISyntaxException e) {
						throw new AssertionError(e);
					}
					System.out
							.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
					String pin = br.readLine();
					try {
						if (pin.length() > 0) {
							accessToken = twitter.getOAuthAccessToken(
									requestToken, pin);
						} else {
							accessToken = twitter
									.getOAuthAccessToken(requestToken);
						}
					} catch (TwitterException te) {
						if (401 == te.getStatusCode()) {
							System.out
									.println("Unable to get the access token.");
						} else {
							te.printStackTrace();
						}
					}
				}
				prop.setProperty("oauth.accessToken", accessToken.getToken());
				prop.setProperty("oauth.accessTokenSecret",
						accessToken.getTokenSecret());
				os = new FileOutputStream(file);
				prop.store(os, twitter.getScreenName() + "'s credential");
				os.close();

				System.out.println("Successfully stored access token to "
						+ file.getAbsolutePath() + ".");
				// System.exit(0);
			} catch (TwitterException te) {
				te.printStackTrace();
				System.out.println("Failed to get accessToken: "
						+ te.getMessage());
				System.exit(-1);
			}
		}
		System.out.println("Got access token.");
		System.out.println("Access token: " + accessToken.getToken());
		System.out.println("Access token secret: "
				+ accessToken.getTokenSecret());
		System.out.println();

		return twitter;
	}

	public VTIAccount(String screen_name) throws IOException {
		try {
			twitter = authorize(screen_name);
			user = twitter.verifyCredentials();
			seen_statuses = new LinkedHashSet<String>();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	public Twitter getTwitter() {
		return twitter;
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