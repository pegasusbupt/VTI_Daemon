package utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;



public class TransitAdapter {
	public static void main(String[] args){
		new TransitAdapter().main();
	}
	
	void main(){
		String uri1="http://maps.google.com/m/directions?saddr=923+S+Carpenter+Street&daddr=O'Hare&dirflg=r&hl=en&ri=0&date=2011-12-06&time=3:25pm";
		String uri2="http://maps.google.com/m/directions?saddr=923+S+Carpenter+Street&daddr=iit&dirflg=r&hl=en&ri=0&date=2011-12-06&time=3:27pm";

		ArrayList<String> routes=calculateTransitRoutes("923 S Carpenter Street, Chicago", "IIT, Chicago");
		
		ArrayList<String> stations;
		TransitRoute r;
		for(String route: routes){
			stations=new TransitRoute(route).getTransferStations();
			for(String station: stations){
				Log.println(station);
				Log.println(GeocodeAdapter.reverseGeocode(GeocodeAdapter.geocode(stationStringFormat(station))));
				Log.println();
			}
			Log.println("*******************");
		}
	}
	
	String stationStringFormat(String station){
		//if a train station name
		//e.g. Blue Line+Jackson-Blue ->
		String [] fields=station.split("\\+");
		String routeId,stationName;
		final String[] colors={"Blue","Red","Green","Yellow","Brown","Purple","Orange"};
		if(station.contains("Line")){
			routeId=fields[0];
			stationName=fields[1];
			String tmp=stationName;
			//remove the color name in the station name: e.g. Addision-Blue->Addision
			for(int i=0;i<colors.length;i++){
				//Log.println(stationName+ "   "+ colors[i] );
				if(stationName.contains(colors[i])){
					stationName=stationName.replaceAll(colors[i], "");
					break;
				}
			}
			tmp="vti_"+tmp.replaceAll("-", "_").replaceAll("/","_").replaceAll(" ","_");
			if(tmp.length()>20)
				Log.println(tmp.substring(0,20));
			else
				Log.println(tmp);
			return routeId.replaceAll(" ","+")+"+"+stationName+"+Chicago";
		}
		// a bus station name
		// e.g. 29 - State+State & Van Buren ->
		else{
			stationName=fields[1];
			return stationName.replaceAll("&","+and+").replace(" ", "")+"+Chicago";
		}
	}
	
	
    /*
     * @return all transit routes between two addresses
     * each route is represented by a string
     */

	ArrayList<String> calculateTransitRoutes(String src, String dest){
		final String delimiter="VTI_BREAK"; 
		ArrayList<String> routes=new ArrayList<String>();
		ArrayList<String> routeTravelTime=new ArrayList<String>();

		Document doc;
		StringBuilder route = new StringBuilder();
		StringBuilder tmp; 
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.google.com/m/directions?");
		urlString.append("&saddr=");// from
		urlString.append(URLAdapter.encode(src));
		urlString.append("&daddr=");// to
		urlString.append(URLAdapter.encode(dest));
		urlString.append("&ie=UTF8&0&om=0");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfTime = new SimpleDateFormat("h:mm a");
		Date now=Calendar.getInstance().getTime();
		tmp=urlString;
		
		
		// fetch the first route, to determine the # of alternative routes
		int i;
		tmp.append("&dirflg=r&ri=0&output=html&date="
				+ sdfDate.format(now) + "&time="
				+ sdfTime.format(now).replaceAll(" ", ""));
		try {
			doc = Jsoup.connect(tmp.toString()).get();
			Element directions = doc.select("p").get(2);
			// Log.println(Jsoup.parse(directions.html()));
			Document doc1 = Jsoup.parse(Jsoup.parse(directions.html())
					.toString().replaceAll("<br />", delimiter));
			// Log.println(doc1);
			String[] steps = doc1.text().split(delimiter);
			for (i=0;i<steps.length;i++) {
				if (steps[i].startsWith(" Alternative routes:"))
					break;
				route.append(steps[i].trim() + "\n");
			}
			for(i=i+1;i<steps.length;i++) {
				if(steps[i].contains("-"))// not empty line
					routeTravelTime.add(steps[i].trim());
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		// save the first route
		StringBuilder firstRoute=new StringBuilder(route);
		//Log.println(routeTravelTime.size());
		
		//fetch the alternative routes
		int numberofAlternativeRoutes=routeTravelTime.size(), j;
		for(i=1;i<=numberofAlternativeRoutes;i++){
			tmp=urlString;
			route.delete(0, route.length());
			tmp.append("&dirflg=r&ri="+i+"&output=html&date="
					+ sdfDate.format(now) + "&time="
					+ sdfTime.format(now).replaceAll(" ", ""));
			try {
				doc = Jsoup.connect(tmp.toString()).get();
				Element directions = doc.select("p").get(2);
				// Log.println(Jsoup.parse(directions.html()));
				Document doc1 = Jsoup.parse(Jsoup.parse(directions.html())
						.toString().replaceAll("<br />", "delimiter"));
				// Log.println(doc1);
				String[] steps = doc1.text().split("delimiter");
				for (j=0; j<steps.length;j++) {
					//Log.println(steps[j]);
					if (steps[j].startsWith(" Alternative routes:")) break;
					route.append(steps[j].trim() + "\n");
				}
				//i==1, need to retrieve the travel time of the first route
				if(i==1)
					routes.add(steps[j+1].trim()+"\n"+firstRoute.toString());
				routes.add(routeTravelTime.get(i-1)+"\n"+route.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return routes;
	}
}


class TransitRoute{
	String travelTime;
	private String title;
	private String modeSequence;
	private ArrayList<String> transferStations;
	private String toString;
	

	public TransitRoute(String route){
		toString=route;
		transferStations=new ArrayList<String>();
		String[] lines = route.split("\n");
		title=lines[0];
		travelTime=title.substring(title.indexOf('(')+1, title.indexOf(')'));
	
		// extract modeSquences and transferStations
		int count=0;
		String routeId="";
		StringBuilder modes=new StringBuilder();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			//Log.println(line + "   "+ routeId + "  "+ count);
			if(line.startsWith("Bus")) modes.append("Bus ");
			if(line.startsWith("Subway")) modes.append("Subway ");
			if(line.startsWith("Walk")) modes.append("Walk ");
			if (count==0) {
				if (line.startsWith("Bus") || line.startsWith("Subway")) {
					routeId = line.substring(line.indexOf('-')+1).trim();
					count=(count+1)%4;
				}
			}else{
				//count==1 is the travel time line
				if(count>1)
					transferStations.add(routeId+"+"+line.substring(line.indexOf(' ')+1).trim());
				count=(count+1)%4;
			}
		}
		modeSequence=modes.toString();
		//Log.println(transferStations);
		//Log.println();
	}
	
	public ArrayList<String> getTransferStations(){
		return transferStations;
	}
	
	public String getModeSequence(){
		return modeSequence;
	}
	
	public String getTravelTime(){
		return travelTime;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public String toString(){
		return toString;
	}
}


