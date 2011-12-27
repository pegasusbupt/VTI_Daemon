/*
 * @return this is the thread class handles a single request
 */

package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.PreparedStatement;

import main.VTI;

public class RateMultiServerThread extends Thread {
	private Socket socket = null;

	public RateMultiServerThread(Socket soc) {
		this.socket = soc;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			String input = in.readLine();
			PreparedStatement stat;
			StringBuilder feedback=new StringBuilder();
			System.err.println(input);
			if (input != null) {// a feedback message
				if(input.startsWith("Feedback")){
					stat = VTI.conn.prepareStatement("insert into feedback values (?, ?, now());");
					stat.setString(1,in.readLine());
					while((input=in.readLine())!=null)
						feedback.append(input);
					stat.setString(2, feedback.toString());
				}else{// a message has a format "p_id+rater_name+up/down"
					String[] fields = input.split(",");
					//System.err.println(input);
					if (fields[2].equalsIgnoreCase("up"))
						stat = VTI.conn.prepareStatement("UPDATE publications SET up_votes=up_votes+1 where geotagged_text=?;");
					else
						stat = VTI.conn.prepareStatement("UPDATE publications SET down_votes=down_votes+1 where geotagged_text=?;");
					stat.setString(1, fields[0]);
				}
				stat.executeUpdate();
				stat.close();
				in.close();
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
