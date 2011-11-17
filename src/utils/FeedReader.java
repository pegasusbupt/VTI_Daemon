package utils;

/**
 * It Reads and prints any RSS/Atom feed type.
 * 
 * @author Sol Ma
 *
 */
import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import main.VTI;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedReader {
	public final static HashMap<String, String> route_id;
	static {
		route_id = new HashMap<String, String>();
		route_id.put("vti_redline", "307");
		route_id.put("vti_purpleline", "308");
		route_id.put("vti_yellowline", "309");
		route_id.put("vti_blueline", "310");
		route_id.put("vti_pinkline", "311");
		//route_id.put("vti_greenline", "312");
		route_id.put("vti_brownline", "313");
		route_id.put("vti_orangeline", "314");
		// route_id.put("vti_purpleexpressline", "323");
	}
	// caches to reduce number of database accesses
	// <route_id+" "+alert_id, alert>
	protected static HashMap<String, String> existing_alerts;
	static {
		// only access the credential table in the local database once
		try {
			existing_alerts = new HashMap<String, String>();
			Statement stat = VTI.conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from cta_rss;");
			while (rs.next()) {
				existing_alerts.put(
						rs.getString("route_id") + " "
								+ rs.getString("alert_id"),
						rs.getString("alert"));
				System.out.println("route_id = " + rs.getString("route_id"));
				System.out.println("alert_id = " + rs.getString("alert_id"));
				System.out.println("alert= " + rs.getString("alert"));
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> retrieveFeeds(String url) {
		// used to save the new alerts
		ArrayList<String> alerts = new ArrayList<String>();
		try {
			URL feedUrl = new URL(url);
			String routeId = url.substring(url.lastIndexOf('=') + 1);
			// System.out.println(routeId);
			String idFile = "cta_rss/alert_ids/" + routeId;

			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));
			PreparedStatement prep = VTI.conn
					.prepareStatement("INSERT INTO cta_rss(alert_id, route_id, alert, pub_date, fetch_date) VALUES(?,?,?,?,now())");

			@SuppressWarnings("unchecked")
			List<SyndEntryImpl> entries = feed.getEntries();
			// iterates through fetched items and save new alerts into local
			// database
			for (SyndEntryImpl entry : entries) {
				String link = entry.getLink();
				String alertId = link.substring(link.lastIndexOf('=') + 1);
				// is a new alert
				if (!existing_alerts.containsKey(routeId + " " + alertId)) {
					// System.out.println(entry.getDescription().getValue());
					existing_alerts.put(routeId + " " + alertId, entry
							.getDescription().getValue());
					prep.setInt(1, Integer.parseInt(alertId));
					prep.setInt(2, Integer.parseInt(routeId));
					prep.setString(3, entry.getDescription().getValue());
					prep.setDate(4, new Date(entry.getPublishedDate().getTime()));
					prep.executeUpdate();
					//added to the new alerts collection
					alerts.add(entry.getDescription().getValue());
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("ERROR: " + ex.getMessage());
		}

		return alerts;
	}

}
