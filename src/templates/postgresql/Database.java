package templates.postgresql;

import java.sql.*;
import java.util.HashSet;

public class Database {
	public static void main(String[] args){
        new Database().main();	
	}
	
    public void main(){
    	postgresqlExample("solMa");
    }	

	public void postgresqlExample(String user){
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			Connection conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5433/VTI", "postgres",
					"postgresql");
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("select * from credentials;");
			HashSet<String> existing_users=new HashSet<String>();
			while (rs.next()) {
				existing_users.add(rs.getString("username"));
				System.out.println("username = " + rs.getString("username"));
				System.out.println("accessToken = " + rs.getString("accessToken"));
				System.out.println("accessTokenSecret = " + rs.getString("accessTokenSecret"));
			}
			rs.close();
			if(!existing_users.contains(user)){
				String[] values=new String[]{"ac","acs"};
				PreparedStatement prep=conn.prepareStatement("INSERT INTO credentials VALUES(?,?,?, now())");
				prep.setString(1, user);
				prep.setString(2, values[0]);
				prep.setString(3, values[1]);
				prep.executeUpdate();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sqliteExample() {
		try {
			Class.forName("org.sqlite.JDBC");

			Connection conn = DriverManager
					.getConnection("jdbc:sqlite:credentials/credentials.db");
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists people;");
			stat.executeUpdate("create table people (name, occupation);");
			PreparedStatement prep = conn
					.prepareStatement("insert into people values (?, ?);");

			prep.setString(1, "Gandhi");
			prep.setString(2, "politics");
			prep.addBatch();
			prep.setString(1, "Turing");
			prep.setString(2, "computers");
			prep.addBatch();
			prep.setString(1, "Wittgenstein");
			prep.setString(2, "smartypants");
			prep.addBatch();
			prep.setString(1, "Wittgenstein");
			prep.setString(2, "smartypants");
			prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);

			ResultSet rs = stat.executeQuery("select * from people;");
			while (rs.next()) {
				System.out.println("name = " + rs.getString("name"));
				System.out.println("job = " + rs.getString("occupation"));
			}
			rs.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
