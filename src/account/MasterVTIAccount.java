package account;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import main.VTI;
import twitter4j.GeoLocation;
import twitter4j.Status;
import utils.GeoLocations;
import utils.GeocodeAdapter;

/**
 * @author Sol Ma
 * @description: the master VTI account that does the assigning work
 * 
 */
public class MasterVTIAccount extends VTIAccount {
	private static final int QUERY_FREQUENCY=20; //in seconds
	// HashMap schema : <"account_name", "coordinateX,coordinateY">
	
	// trainAccounts represent all train stations
	private HashMap<String, String> trainStationAccounts = new HashMap<String, String>();
	// HashSet schema: <"publication_id">
	private HashSet<Long> existing_publications = new HashSet<Long>();

	public MasterVTIAccount(String screen_name) throws IOException {
		super(screen_name);

		Statement stat;
		try {
			stat = VTI.conn.createStatement();
			ResultSet rs;
			rs = stat.executeQuery("select p_id from publications;");
			while (rs.next()) {
				existing_publications.add(rs.getLong("p_id"));
			}

			rs = stat.executeQuery("select longname, latitude, longitude from train_station;");
			while (rs.next()) {
				trainStationAccounts.put(
						rs.getString("longname"),
						rs.getDouble("latitude") + ","
								+ rs.getDouble("longitude"));
			}
			rs.close();
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	String assignPublication(Status status) {
		GeoLocation loc = status.getGeoLocation();
		String ret=null;
		if (loc == null) {
			System.out.println("\""+status.getText()+"\""
					+ " is not embeded with location information");
			return null;
		}else{
			double lat=loc.getLatitude();
			double ln=loc.getLongitude();
			
			double minimum=Double.MAX_VALUE;
			for (String key : trainStationAccounts.keySet()) {
				String[] coordinates = trainStationAccounts.get(key).split(",");
				double dis = GeoLocations.distBetween(loc.getLatitude(),
						loc.getLongitude(), Double.parseDouble(coordinates[0]),
						Double.parseDouble(coordinates[1]));
				if (dis < minimum) {
					minimum = dis;
					ret = key;
				}
			}
			System.out.println(ret + " is the closest station to " + "\"" + status.getText() + "\" distance is "+ minimum);
			
			// if the closest train statin is with 20 meters, then considered the publication is within the station
			if(minimum>20){
				// determine the zone to the publication is assigned
				ret=GeocodeAdapter.determineVTIAccount(lat, ln);
			}
			
			String address="not available";
			String tmp;
			try {
				PreparedStatement stat = VTI.conn
						.prepareStatement("UPDATE publications SET latitude=?, longitude=?, vti_account=?, address=?, geotagged_text=? where p_id=?;");
				stat.setDouble(1, loc.getLatitude());
				stat.setDouble(2, loc.getLongitude());
				stat.setString(3, ret);
				tmp=GeocodeAdapter.reverseGeocode(loc);
				if(tmp!=null) 
					address=tmp;
				stat.setString(4, address);
				stat.setString(5, status.getText().toLowerCase().replaceAll("@vti_robot ", "")+" reported from "+address);
				stat.setLong(6, status.getId());
				stat.executeUpdate();
				stat.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if(ret!=null)
				return ret+"VTI_BREAK"+address;
			else 
				return ret;
		}
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
					try{
						if (!existing_publications.contains(status.getId())) {
							stat = VTI.conn
									.prepareStatement("INSERT INTO publications(p_id, publisher, tweet, up_votes, down_votes, create_time, fetch_time) VALUES (?, ?, ?, ?, ?, ?, now());");
							stat.setLong(1, status.getId());
							stat.setString(2, status.getUser().getName());
							stat.setString(3, status.getText().toLowerCase().replaceAll("@vti_robot ", ""));
							stat.setInt(4, 0);
							stat.setInt(5, 0);
							stat.setTimestamp(6, new Timestamp(status
									.getCreatedAt().getTime()));
							stat.executeUpdate();
							existing_publications.add(status.getId());

							// determine which account this status is assigned to
							String ret = assignPublication(status);
							System.out.println(ret);
							VTIAccount targetAccount=null;
							String msg;
							String [] fields;
							if(ret!=null){
								fields=ret.split("VTI_BREAK");
								targetAccount=VTI.vti.get(fields[0]);
 								msg=status.getText().toLowerCase().replaceAll("@vti_robot ", "")+" at "+fields[1];
								System.out.println(targetAccount.getTwitter().getScreenName()+" posted : "+ msg);
								System.out.println();
								if(msg.length()<140)
									targetAccount.twitter.updateStatus(msg);
								else
									targetAccount.twitter.updateStatus(msg.substring(0, 139));
							}
						}
					}
					catch(Exception e){
						e.printStackTrace();
						continue;
					}
				}
			} 
			}catch (Exception e) {
			e.printStackTrace();
		}

			try {
				Thread.sleep(QUERY_FREQUENCY*1000); // check the received tweets every 0.1 sec
			} catch (InterruptedException e) {
				break;
			}

		}
	}
}