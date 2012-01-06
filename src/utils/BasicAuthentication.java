package utils;

import main.VTI;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class BasicAuthentication {
	public static void main(String[] args) throws Exception{
		Configuration config = new ConfigurationBuilder()
		.setOAuthConsumerKey(VTI.VTI_CONSUMER_KEY)
		.setOAuthConsumerSecret(VTI.VTI_CONSUMER_SECRET)
		.build();
		Twitter twitter = new TwitterFactory(config).getInstance(new BasicAuthorization("vti_robot", "vti&&vti"));
		AccessToken token = twitter.getOAuthAccessToken();
		Log.println("token = "+token.getToken());
		Log.println("tokenSecret = "+token.getTokenSecret());
	}
}
