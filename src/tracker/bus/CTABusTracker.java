package tracker.bus;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.Log;
import utils.StringProcess;
import utils.URLAdapter;


public class CTABusTracker {
	private static final String KEY="7tFU9dumgYNZshTuPjKRhZJTA"; 
	private static final String DIR_DELIMITER=";";
	
	
	ArrayList<Route> routes;

	public static void main(String[] args){
		new CTABusTracker().main();
	}
	
	public void main(){
		try {
			buildBusStopTable();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class Route{
		String rtId;
		String rtName;
		String rtDirs;
		public Route(String rt, String rtnm){
			rtId=rt;
			rtName=rtnm;
		}
		public String rtId(){return rtId;}
		public String rtName(){return rtName;}
		public String rtDirs(){return rtDirs;}
		public void setDirs(String dirs){rtDirs=dirs;}
		public String toString(){return rtId+" "+rtName+" "+rtDirs;}
	}
	
	public class Stop{
		String stpId;
		String stpName;
		String stpLat;
		String stpLon;
		String rtId;
		String rtDir;
		public Stop(String id, String name){
			stpId=id;
			stpName=name;
		}
		public String stpId(){return stpId;}
		public String stpName(){return stpName;}
		public String toString(){return stpId+","+stpName+","+stpLat+","+stpLon+","+rtId+"rtDir";}
	}
	
	private void buildBusStopTable() throws Exception{
		PrintWriter out = new PrintWriter(new FileWriter("C:\\Users\\Sol\\Desktop\\cta_tracker_data\\bus_stop.csv")); 

		final String url="http://www.ctabustracker.com/bustime/api/v1/getroutes?key="+KEY;
		String dir_url="http://www.ctabustracker.com/bustime/api/v1/getdirections?key="+KEY+"&rt=";
		String stop_url;
		final String DIR_DELIMETER=";";
		try {
			String rtId, rtName, dir, stpId, stpName, stpLat, stpLon;
			long noOfRowsEffected;
			StringBuilder rtDir=new StringBuilder();
			Document doc=Jsoup.connect(url).get();
			Elements rts=doc.select("bustime-response > route");

			//iterates all routes
			for(int i=0;i<rts.size();i++){
				try{
					rtId=rts.get(i).select("rt").text();
					rtName=rts.get(i).select("rtnm").text();
					rtDir.delete(0, rtDir.length());
					doc=Jsoup.connect(dir_url+rtId).get();
					Elements dirs=doc.select("bustime-response > dir ");
					// iterates all directions of a route
					for(Element ele: dirs){
						dir=ele.text();
						if(rtDir.length()>0)
							rtDir.append(DIR_DELIMETER);
						rtDir.append(dir);
						
						stop_url="http://www.ctabustracker.com/bustime/api/v1/getstops?key="+KEY+"&rt="+rtId+"&dir="+URLAdapter.encode(dir);
						doc=Jsoup.connect(stop_url).get();
						Elements stops=doc.select("bustime-response > stop ");
						// iterates all stops of a directed route
						for(Element stop: stops){
							try{
								stpId=stop.select("stpid").text();
								stpName=stop.select("stpnm").text();
								stpLat=stop.select("lat").text();
								stpLon=stop.select("lon").text();
								out.println(stpId+","+stpName+","+stpLat+","+stpLon+","+rtId+","+dir+",true");
							}catch(Exception e){
								Log.println(StringProcess.stack2string(e));
								continue;
							}
						}
						
					}
					//out.println(rtId+","+rtName+","+rtDir.toString()+","+"true");
				}catch(Exception e){
					Log.println(StringProcess.stack2string(e));
					continue;
				}
			}
			out.close();
		} catch (Exception e) {
			Log.println(StringProcess.stack2string(e));
		}
	}
	
	public void buildRouteTable(){
	
	}
	
	public void buildStopTable(){
		
		for(int i=0;i<3;i++){
			String routeId=routes.get(i).rtId();
			String [] dirs=routes.get(i).rtDirs().split(DIR_DELIMITER);
			Log.println(routeId);
			for(int j=0;j<dirs.length;j++){
				Log.println(dirs[j]);
				getStops(routeId, URLAdapter.encode(dirs[j]));
			}
		}
		
	}

	public ArrayList<Route> getRoutes(){
		String url="http://www.ctabustracker.com/bustime/api/v1/getroutes?key="+KEY;
		ArrayList<Route> ret=new ArrayList<Route>();
		try {
			Document doc=Jsoup.connect(url).get();
			Elements rtIds=doc.select("bustime-response > route > rt");
			Elements rtNames=doc.select("bustime-response > route > rtnm");
			int size=rtIds.size();
			if(size!=rtNames.size()){
				Log.println("rtIds are not matched with rtNames");
			}
			for(int i=0;i<size;i++){
				ret.add(new Route(rtIds.get(i).text(), rtNames.get(i).text()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public String getRouteDirections(String rtId){
		String url="http://www.ctabustracker.com/bustime/api/v1/getdirections?key="+KEY+"&rt="+rtId;
		StringBuilder ret=new StringBuilder();
		try {
			Document doc=Jsoup.connect(url).get();
			Elements dirs=doc.select("bustime-response > dir ");
			for(Element ele: dirs){
				if(ret.length()>0)
					ret.append(";");
				ret.append(ele.text());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret.toString();
	}
	
	public void getStops(String rtId, String dir){
		String url="http://www.ctabustracker.com/bustime/api/v1/getstops?key="+KEY+"&rt="+rtId+"&dir="+dir;
		try {
			Document doc=Jsoup.connect(url).get();
			Elements stops=doc.select("bustime-response > stop ");
			for(Element ele: stops){
				Log.print(ele.select("stpid").text()+" ");
				Log.print(ele.select("stpnm").text()+" ");
				Log.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getPredictions(String rtId, String stpId){
		String url="http://www.ctabustracker.com/bustime/api/v1/getpredictions?key="+KEY+"&rt="+rtId+"&stpid="+stpId;
		try {
			Document doc=Jsoup.connect(url).get();
			Elements prds=doc.select("bustime-response > prd ");
			Log.println("I am here");
			for(Element ele: prds){
				int diff=(int) ((string2Long(ele.select("prdtm").text())-(string2Long(ele.select("tmstmp").text())))/1000/60);
				Log.println(String.valueOf(diff)+" minutes");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param arrT, e.g. "20120123 21:43:18" for train predication and "20120124 22:33" for bus prediction
	 * @return time 
	 */
	private long string2Long(String arrT){
		if(arrT.length()<17){
			arrT=arrT+":00";
		}
		Log.println(arrT);
		Calendar cal=Calendar.getInstance();
		int year=Integer.parseInt(arrT.substring(0, 4));
		int month=Integer.parseInt(arrT.substring(4, 6));
		int date=Integer.parseInt(arrT.substring(6, 8));
		int hourOfDay=Integer.parseInt(arrT.substring(9, 11));
		int minute=Integer.parseInt(arrT.substring(12, 14));
		int second=Integer.parseInt(arrT.substring(15, 17));
		cal.set(year, month, date, hourOfDay, minute, second);
		return cal.getTimeInMillis();
	}
}
