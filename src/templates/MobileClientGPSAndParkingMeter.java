package templates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;


public class MobileClientGPSAndParkingMeter {
	
	private static ResultSet rs;
	private static int stop_count;
	private ArrayList stop_name ;//= new ArrayList();
	
	private String client_mode = null;
	private String position = null;
	private double userGPSAndCTARailTrajectoryThreshold = 7; //10 meters
	private Connection db=null,db2=null;
	private Statement stmt=null,stmt2=null;

	public static void main (String args[]){
		MobileClientGPSAndParkingMeter runner = new MobileClientGPSAndParkingMeter();
		//uic lat lon 41.872533,-87.646909
		//home 41.8555619,-87.65315
		System.out.println("lowest dist "+ runner.run(41.883533,-87.646909,"none","none"));

	}
	
	
	public MobileClientGPSAndParkingMeter (){

	}
	public double run (double client_latitude, double client_longitude, String mode, String pos){
		double  closest_dist_to_rail = 0.0;
		client_mode = mode;
		position = pos;

		rs = null;
		stop_name = new ArrayList();
		read_rail_track_from_db();			
		closest_dist_to_rail =  runFeature(client_latitude, client_longitude);
		
		try{
			stmt.close();
			db.close();
			stmt2.close();
			db2.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		return closest_dist_to_rail ;

	}

	
	/**
	 * reads the rail line from the DB
	 */
	public void read_rail_track_from_db(){
		//double client_lat = -87.67575,client_lon = 41.87661;
					
		try{
			Class.forName("org.postgresql.Driver").newInstance();
			db=DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgis","postgres","Yhcaapa3");
			stmt=db.createStatement();
			//String query  = "SELECT X(public.rail_track.the_geom) as lat,Y(public.rail_track.the_geom) as lon FROM public.rail_track";
			String query = "SELECT X(public.chicagoparkingmeters.location) as lat, Y(public.chicagoparkingmeters.location) as lon, address, kioskid from public.chicagoparkingmeters";
			rs = stmt.executeQuery(query);
			

		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	public double runFeature(double client_latitude, double client_longitude){
		int count=0;		
		double lowest_distance = 10000,distance=0;		
	
		double rail_latitude=0,client_lat = client_latitude;// -87.6408;
		String parkingmeterlocationlat = null,parkingmeteraddress=null, parkingmeterlocationlon = null,parkingmeterid=null;

		double rail_longitude=0,client_lon = client_longitude;//41.8757; 

		String  name="";

		Date client_timestamp = new Date();


		try {			
			db2=DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgis","postgres","Yhcaapa3");
			stmt2 = db2.createStatement();	

			while(rs.next()){					
				parkingmeterlocationlat =  rs.getString(1);
				parkingmeterlocationlon =  rs.getString(2);
				//http://postgis.refractions.net/docs/ST_Distance.html most accurate for chi is 26971				
				String distanceQuery = "SELECT ST_Distance(ST_Transform(ST_GeomFromText(\'POINT("+client_lon+" "+client_lat+")\',4326),26971)" +
						",ST_Transform(ST_GeomFromText(\'POINT("+parkingmeterlocationlon+" "+parkingmeterlocationlat+")\', 4326),26971))";

				//String distanceQuery = "SELECT ST_Distance(ST_GeomFromText(\'POINT("+client_lat+" "+client_lon+")\',4326),ST_GeomFromText(\'"+rail_line+"\', 4326))";
				ResultSet rsDistanceSet = stmt2.executeQuery(distanceQuery);
				rsDistanceSet.next();
				distance = rsDistanceSet.getDouble(1);

				if(count==0){
					lowest_distance = distance;
					parkingmeteraddress = rs.getString(3);
					parkingmeterid = rs.getString(4);
					count++;

				}
				if(distance < lowest_distance){
					lowest_distance = distance;	
					parkingmeteraddress = rs.getString(3);
					parkingmeterid = rs.getString(4);
				}

			}

		}

		catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Closest parking meter is "+parkingmeterid+" at "+parkingmeteraddress+" it is "+lowest_distance+"m away");
		return lowest_distance;
		

	}

	//calculates the distance between two points


	/*public void railClosenessThreshold(double lowestDistance, String mode){
		String railClosenessThresholdFile = "C:\\apache-tomcat-5.5.17\\webapps\\ROOT\\tmode\\featuresFiles\\railClosenessThresholdFile.txt";
		PrintWriter outRailClosenessThresholdFile = null;
		try {
			outRailClosenessThresholdFile = new PrintWriter(new FileWriter(railClosenessThresholdFile,true),true);

			outRailClosenessThresholdFile.println(lowestDistance+" "+mode);
			outRailClosenessThresholdFile.flush();
			outRailClosenessThresholdFile.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}*/

}