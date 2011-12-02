package account;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import main.VTI;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import utils.StringProcess;
import utils.GeoLocations;

/**
 * @author Sol Ma
 * @description: the master VTI account that does the assigning work
 * 
 */
public class MasterVTIAccount extends VTIAccount {
	// HashMap schema : <"account_name", "coordinateX,coordinateY">
	private HashMap<String, String> geoAccounts = new HashMap<String, String>();
	// HashSet schema: <"publication_id">
	private HashSet<String> existing_publications = new HashSet<String>();

	public MasterVTIAccount(String screen_name) throws IOException {
		super(screen_name);

		Statement stat;
		try {
			stat = VTI.conn.createStatement();
			ResultSet rs;
			rs = stat.executeQuery("select p_id from publications;");
			while (rs.next()) {
				existing_publications.add(rs.getString("p_id"));
			}

			rs = stat
					.executeQuery("select longname, latitude, longitude from train_station;");
			while (rs.next()) {
				geoAccounts.put(
						rs.getString("longname"),
						rs.getDouble("latitude") + ","
								+ rs.getDouble("longitude"));
			}

			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	VTIAccount assignPublication(Status status) {
		GeoLocation loc = status.getGeoLocation();
		String ret = null;

		if (loc == null) {
			System.out.println("\""+status.getText()+"\""
					+ " is not embeded with location information");
			return null;
		}

		double minimum = Double.MAX_VALUE;
		// TODO: the assigning logic need to be enhanced and optimized.
		for (String key : geoAccounts.keySet()) {
			String[] coordinates = geoAccounts.get(key).split(",");
			double dis = GeoLocations.distBetween(loc.getLatitude(),
					loc.getLongitude(), Double.parseDouble(coordinates[0]),
					Double.parseDouble(coordinates[1]));
			if (dis < minimum) {
				minimum = dis;
				ret = key;
			}
		}
		try {
			PreparedStatement stat = VTI.conn
					.prepareStatement("UPDATE publications SET latitude=?, longitude=?, vti_account=? where p_id=?;");
			stat.setDouble(1, loc.getLatitude());
			stat.setDouble(2, loc.getLongitude());
			stat.setString(3, ret);
			stat.setString(4, String.valueOf(status.getId()));
			stat.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println(ret + " is the closest station to "
				+ "\""+status.getText()+"\"");
		return VTI.vti.get(ret);
	}

	@Override
	public void run() {
		while (true) {
			List<Status> statuses;
			try {
				statuses = twitter.getMentions();
				if (statuses.size() > 0) {
					PreparedStatement stat;
					for (Status status : statuses) {
						// if this is a new publication
						if (!existing_publications.contains(String.valueOf(status.getId()))) {
							stat = VTI.conn
									.prepareStatement("INSERT INTO publications(p_id, publisher, tweet, create_time, fetch_time) VALUES (?, ?, ?, ?, now());");
							stat.setString(1, String.valueOf(status.getId()));
							stat.setString(2, status.getUser().getName());
							stat.setString(3, status.getText());
							stat.setTimestamp(4, new Timestamp(status
									.getCreatedAt().getTime()));
							stat.executeUpdate();
							existing_publications.add(String.valueOf(status
									.getId()));

							// determine which account this status is assigned
							// to
							VTIAccount targetAccount = assignPublication(status);
							if (targetAccount != null) {
								System.out.println(targetAccount.getTwitter()
										.getScreenName());
								if (status.getText().length() > 140)
									targetAccount.twitter
											.updateStatus(StringProcess
													.messageShorten(status
															.getText()));
								else
									targetAccount.twitter.updateStatus(status
											.getText());
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(100); // check the received tweets every 0.1 sec
			} catch (InterruptedException e) {
				break;
			}

		}
	}
}