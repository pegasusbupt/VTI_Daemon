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
			// a message has a format "p_id+rater_name+up/down"
			String input = in.readLine();
			if (input != null) {
				String[] fields = input.split(",");
				PreparedStatement stat;
				if (fields[2].equalsIgnoreCase("up"))
					stat = VTI.conn
							.prepareStatement("UPDATE publications SET up_votes=up_votes+1 where p_id=?;");
				else
					stat = VTI.conn
							.prepareStatement("UPDATE publications SET down_votes=down_votes+1 where p_id=?;");
				stat.setLong(1, Long.parseLong(fields[0]));
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
