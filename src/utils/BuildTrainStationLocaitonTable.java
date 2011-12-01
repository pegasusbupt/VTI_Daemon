package utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import main.VTI;
/*
 * @return this is the java file that I used to build the table for CTA train stations
 * There are two
 */

public class BuildTrainStationLocaitonTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new BuildTrainStationLocaitonTable().main();
	}
	
	
	/*
	 * @return choose the methods needed
	 */
	public void main(){
		//buildTable();
		updateCoordinates();
	}

	/*
	 * @return parse the CTA_RailStations file and feed the information into the database
	 * 
	 */
	public void buildTable(){
		try {
			Scanner sc=new Scanner(new File("data/CTA_train_station/CTA_RailStations.csv"));
			//skip the header line
			sc.nextLine();
			PreparedStatement prep=VTI.conn.prepareStatement("insert into train_station values (?, ?, ?, ?, ?, ?, ?, ?, ?);");
			while(sc.hasNextLine()){
				String[] fields=sc.nextLine().split(",");
				//System.out.println(fields);
				//table train_station 's schema: STATION_ID,LONGNAME,LINES,ADDRESS,X, Y, ADA,PKNRD,GTFS 
				prep.setInt(1, Integer.parseInt(fields[0]));
				prep.setString(2, fields[1]);
				//some processing needed for the line field
				prep.setString(3, process(fields[2]));
				prep.setString(4, fields[3]);
				prep.setFloat(5, 0);
				prep.setFloat(6, 0);
				prep.setInt(7, Integer.parseInt(fields[4]));
				prep.setInt(8, Integer.parseInt(fields[5]));
				prep.setInt(9, Integer.parseInt(fields[6]));
				prep.executeUpdate();
			}
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
	private String process(String line){
		if(line.charAt(0)=='"'){ //if the line contains quotes
			return line.substring(1,line.length()-1);
		}
		return line;
	}
	
	/*
	 * @return update the geo-coordinates for all train-stations
	 * CTARailStations.xml is downloaded from http://www.transitchicago.com/data/
	 */
	public void updateCoordinates(){
		// HashMap <station_id, x+y>
		HashMap<String, String> coordinates=new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read("data/CTA_train_station/CTARailStations.xml");
			Element root=document.getRootElement();
			List stations=root.element("Document").element("Folder").elements("Placemark");
			Iterator it=stations.iterator();
			while(it.hasNext()){
				Element ele=(Element)it.next();
				String desc=ele.element("description").getText();
				//System.out.println(getStationId(desc));
				//System.out.println(ele.element("Point").element("coordinates").getText());
				coordinates.put(getStationId(desc), ele.element("Point").element("coordinates").getText().trim());
			}
			//System.out.println(coordinates.size());
		
			PreparedStatement prep=VTI.conn.prepareStatement("UPDATE train_station SET x=?, y=? where station_id=?;");
			for(String s: coordinates.keySet()){
				String[] xy=coordinates.get(s).split(",");
				prep.setDouble(1, Double.parseDouble(xy[0]));
				prep.setDouble(2, Double.parseDouble(xy[1]));
				prep.setInt(3, Integer.parseInt(s));
				prep.executeUpdate();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getStationId(String desc){
		//a ugly/awkward way to find out the station_id from the xml file
		String [] parti=desc.split("<td>STATION ID</td>\n\n<td>");
		//System.out.println(parti[0]);
		return parti[1].substring(0,parti[1].indexOf('<'));
	}
}
