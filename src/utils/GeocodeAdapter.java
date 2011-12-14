package utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import twitter4j.GeoLocation;

public class GeocodeAdapter {
	private static final String WEST_SOUTH="4700 S Pulaski Road Chicago";
	private static final String NORTH="Irving Park road and North Lake Shore Chicago";
	private static final String EAST="Navy Pier Chicago";
	private static final int ZONE_NUM=10;
	
	public static final double ZONE_LATITUDE=14688.169999999553;
	public static final double ZONE_LONGITUDE=11434.390000000596;
	public static final double WEST=-87.7239589;
	public static final double SOUTH=41.807737;
	
	
	public static void main(String[] args){
		new GeocodeAdapter().main();
	}

	public void main(){
		String[] tests={"4700 S Pulaski Road Chicago", "Irving Park road and North Lake Shore Chicago", "Navy Pier Chicago",
				"923 S Carpenter Street Chicago", "IIT Chicago", "Laselle Street and Washington Street Chicago"};
		setBoundaries();
		for(int i=0;i<tests.length;i++)
			reverseGeocode(geocode(buildQueryAddress(tests[i])));
	}
	
	public static GeoLocation geocode(String address){
		double [] laln=new double[2];
		String url="http://maps.googleapis.com/maps/api/geocode/xml?address="+address+"&sensor=true";
		System.out.println(url);
		try {
			Document doc=Jsoup.connect(url).get();
			laln[0]=Double.parseDouble(doc.select("result > geometry > location > lat").first().text());
			laln[1]=Double.parseDouble(doc.select("result > geometry > location > lng").first().text());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(laln[0]+"   "+laln[1]);
		return new GeoLocation(laln[0], laln[1]);
	}
	
	public static String reverseGeocode(GeoLocation  loc){
		String url="http://maps.googleapis.com/maps/api/geocode/xml?address="+loc.getLatitude()+","+loc.getLongitude()+"&sensor=true";
		String address=null;
		try {
			Document doc=Jsoup.connect(url).get();
			if(doc!=null){
				address=doc.select("result > address_component > type:matches(route)").parents().first().select("long_name").text();
				//e=doc.select("result > address_component > type:matches(street_number)").first();
				//if(e!=null) streetNumber=e.parents().first().select("long_name").text();
				//System.out.println(streetName+" "+streetNumber);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//System.out.println("WITHIN GeocodeAdapter.reverseGeocode: "+ url);
		//System.out.println("WITHIN GeocodeAdapter.reverseGeocode: "+address);
		return address;
	}
	
	
	public static void setBoundaries(){
		GeoLocation southwest=geocode(buildQueryAddress(WEST_SOUTH));
		/*
		SOUTH=southwest.getLatitude();
		WEST=southwest.getLongitude();
		System.out.println("SOUTH="+SOUTH);
		System.out.println("NORTH="+geocode(buildQueryAddress(NORTH)).getLatitude());
		System.out.println("WEST="+WEST);
		System.out.println("EAST="+geocode(buildQueryAddress(EAST)).getLongitude());
		
		ZONE_LATITUDE=(((geocode(buildQueryAddress(NORTH)).getLatitude())*1.0E6)-(southwest.getLatitude()*1.0E6))/ZONE_NUM;
		ZONE_LONGITUDE=(((geocode(buildQueryAddress(EAST)).getLongitude())*1.0E6)-(southwest.getLongitude()*1.0E6))/ZONE_NUM;
		System.out.println(ZONE_LATITUDE+"  "+ZONE_LONGITUDE);
		*/
	}
	
	public static String buildQueryAddress(String stree1, String street2){
		return stree1+"+and+"+street2+"+Street+Chicago";
	}
	
	public static String buildQueryAddress(String address){
		return address.replaceAll(" ","+");
	}
}
