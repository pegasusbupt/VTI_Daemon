package templates;

public class GeoLocationService {
	
	private static double EARTH_RADIUS_KM=6371.009;

	public static void main(String[] args) {
		Point p1 = new Point(1.723411, 178.2708026);
		for (Point point : getExtremePointsFrom(p1, 300.00)) {
			System.out.println("Extreme Point : " + point);
			System.out.println("Distance from point  : "
					+ getDistanceBetweenPoints(p1, point, "K"));
		}

	}

	/**
	 * Method used to convert the value form radians to degrees
	 * 
	 * @param rad
	 * @return value in degrees
	 */
	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	/**
	 * Converts the value from Degrees to radians
	 * 
	 * @param deg
	 * @return value in radians
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * Returns the difference in degrees of longitude corresponding to the
	 * distance from the center point. This distance can be used to find the
	 * extreme points.
	 * 
	 * @param p1
	 * @param distance
	 * @return
	 */
	private static double getExtremeLongitudesDiffForPoint(Point p1,
			double distance) {
		double lat1 = p1.latitude;
		lat1 = deg2rad(lat1);
		double longitudeRadius = Math.cos(lat1) * EARTH_RADIUS_KM;
		double diffLong = (distance / longitudeRadius);
		diffLong = rad2deg(diffLong);
		return diffLong;
	}

	/**
	 * Returns the difference in degrees of latitude corresponding to the
	 * distance from the center point. This distance can be used to find the
	 * extreme points.
	 * 
	 * @param p1
	 * @param distance
	 * @return
	 */
	private static double getExtremeLatitudesDiffForPoint(Point p1,
			double distance) {
		double latitudeRadians = distance / EARTH_RADIUS_KM;
		double diffLat = rad2deg(latitudeRadians);
		return diffLat;
	}

	/**
	 * Returns an array of two extreme points corresponding to center point and
	 * the distance from the center point. These extreme points are the points
	 * with max/min latitude and longitude.
	 * 
	 * @param point
	 * @param distance
	 * @return
	 */
	private static Point[] getExtremePointsFrom(Point point, Double distance) {
		double longDiff = getExtremeLongitudesDiffForPoint(point, distance);
		double latDiff = getExtremeLatitudesDiffForPoint(point, distance);
		Point p1 = new Point(point.latitude - latDiff, point.longitude
				- longDiff);
		p1 = validatePoint(p1);
		Point p2 = new Point(point.latitude + latDiff, point.longitude
				+ longDiff);
		p2 = validatePoint(p2);

		return new Point[] { p1, p2 };
	}

	/**
	 * Validates if the point passed has valid values in degrees i.e. latitude lies between -90 and +90 and the longitude 
	 * @param point
	 * @return
	 */
	private static Point validatePoint(Point point) {
		if (point.latitude > 90)
			point.latitude = 90 - (point.latitude - 90);
		if (point.latitude < -90)
			point.latitude = -90 - (point.latitude + 90);
		if (point.longitude > 180)
			point.longitude = -180 + (point.longitude - 180);
		if (point.longitude < -180)
			point.longitude = 180 + (point.longitude + 180);

		return point;
	}

	/**
	 * Returns the distance between tow points
	 * 
	 * @param p1
	 * @param p2
	 * @param unit
	 * @return
	 */
	private static double getDistanceBetweenPoints(Point p1, Point p2,
			String unit) {
		double theta = p1.longitude - p2.longitude;
		double dist = Math.sin(deg2rad(p1.latitude))
				* Math.sin(deg2rad(p2.latitude))
				+ Math.cos(deg2rad(p1.latitude))
				* Math.cos(deg2rad(p2.latitude)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit.equals("K")) {
			dist = dist * 1.609344;
		} else if (unit.equals("M")) {
			dist = dist * 0.8684;
		}
		return (dist);
	}

}

class Point {
	double latitude;
	double longitude;

	public Point(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "Latitude : " + latitude + "   Longitude  : " + longitude;
	}
}
