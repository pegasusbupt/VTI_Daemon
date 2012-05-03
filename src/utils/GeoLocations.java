package utils;

/*
 * @return the utility class that provides API for GeoLocation functions 
 * such as 
 * Find distance between two points
 * Find extreme points i.e. points with min/max latitude and longitude from a center point, for a given distance
 * Find point of interest near you
 *  
 */
public class GeoLocations {
	/*
	 * @return Distance between two Points : Using the below function you can
	 * find distance between two points.
	 */
	private static double EARTHRADIUS=3958.75;
	private static double MILETOKM=1.609344;
	
	public static void main(String[] args){
		Log.println(distBetween(-87.776785, 41.870886,-87.774135 , 41.887293));
		Log.println(distBetween(-87.776785, 41.870886,-87.783661, 41.887159));
	}
	
	/*
	 * @return return distance between two geo points in meters
	 */
	public static double distBetween(double lat1, double lng1, double lat2,
			double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (EARTHRADIUS*MILETOKM*1.0E3) * c;

		return dist;
	}

}
