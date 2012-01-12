/*
 * @return this is the thread class handles a single request
 */

package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import main.VTI;

public class MultiServerThread extends Thread {
	private Socket socket = null;

	public MultiServerThread(Socket soc) {
		this.socket = soc;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			String input = in.readLine();
			PreparedStatement stat;
			StringBuilder feedback=new StringBuilder();
			Log.println(input);
			while (input != null){
				// a message starts with "Feedback" is a feedback message 
				if(input.startsWith("Feedback")){
					stat = VTI.conn.prepareStatement("insert into feedback values (?, ?, now());");
					stat.setString(1,in.readLine());
					while((input=in.readLine())!=null)
						feedback.append(input);
					stat.setString(2, feedback.toString());
					stat.executeUpdate();
					stat.close();
				}else{
					if(input.startsWith("Top Publishers")){
						stat=VTI.conn.prepareStatement("select publisher, count(p_id) AS \"numbers\" from publications group by publisher order by numbers desc limit 10;");
						ResultSet rs = stat.executeQuery();
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						while (rs.next()) {
							out.println(rs.getString("publisher"));
						}
						stat.close();
						out.close();
					}else{
						// the message is vote info. and has a format "p_msg+rater_name+up/down+location+p_id"
						String[] fields = input.split(",");
						int len=fields.length;
						//update ratings table 
						if(!fields[3].equals("not available")){
							String[] coord=fields[3].split(":");
							stat=VTI.conn.prepareStatement("INSERT INTO ratings(p_id, rater, tweet, rate, rate_lat, rate_lng, rate_time) VALUES (?, ?, ?, ?, ?, ?, now());");
							stat.setDouble(5, Double.parseDouble(coord[0]));
							stat.setDouble(6, Double.parseDouble(coord[1]));
						}else{
							stat=VTI.conn.prepareStatement("INSERT INTO ratings(p_id, rater, tweet, rate, rate_time) VALUES (?, ?, ?, ?, now());");
						}
						stat.setLong(1, Long.parseLong(fields[len-1]));
						stat.setString(2, fields[1]);
						stat.setString(3, fields[0]);
						stat.setString(4, fields[2]);
						stat.executeUpdate();
						
						// update publication table
						if (fields[2].equalsIgnoreCase("up")){
							stat = VTI.conn.prepareStatement("UPDATE publications SET up_votes=up_votes+1 where geotagged_text=?;");
						}
						else{
							stat = VTI.conn.prepareStatement("UPDATE publications SET down_votes=down_votes+1 where geotagged_text=?;");
						}
						stat.setString(1, fields[0]);
						stat.executeUpdate();
						stat.close();
					}
				}
				input = in.readLine();
				Log.println(input);
			}
			in.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
