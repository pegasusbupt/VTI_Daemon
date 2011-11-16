package utils;

import java.io.*;
import java.net.URL;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.sql.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

//this class updates the database of buses moving in real time. it reads from the cta bustraker and put it in a databse
public class CTABusParser extends DefaultHandler {
	Stack<String> currentElement = new Stack<String>();
	HashMap<String, String> busProperties = new HashMap<String, String>();

	// caches to reduce number of database accesses
	Set<String> knownBuses = new HashSet<String>();
	Set<String> knownPatterns = new HashSet<String>();

	PreparedStatement maybeCreateBusStmt;
	PreparedStatement updateBusStmt;

	PreparedStatement maybeCreatePatternStmt;
	PreparedStatement updatePatternStmt;

	public CTABusParser(PreparedStatement maybeCreateBusStmt,
			PreparedStatement updateBusStmt,
			PreparedStatement maybeCreatePatternStmt,
			PreparedStatement updatePatternStmt) {
		this.maybeCreateBusStmt = maybeCreateBusStmt;
		this.updateBusStmt = updateBusStmt;

		this.maybeCreatePatternStmt = maybeCreatePatternStmt;
		this.updatePatternStmt = updatePatternStmt;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		currentElement.push(qName);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		int id = 0;
		if ("bus".equals(qName)) {
			String idstring = busProperties.get("id");
			if (idstring != null) {
				id = Integer.parseInt(idstring);
			}
			try {
				if (!knownBuses.contains(idstring)) {
					maybeCreateBusStmt.setInt(1, id);
					maybeCreateBusStmt.setInt(2, id);
					maybeCreateBusStmt.executeUpdate();

					knownBuses.add(idstring);
				}

				String patternidstring = busProperties.get("pid");
				if (!knownPatterns.contains(patternidstring)) {
					if (!"OR".equals(busProperties.get("rt"))) {
						maybeCreatePatternStmt.setInt(1,
								Integer.parseInt(patternidstring));
						maybeCreatePatternStmt.setString(2,
								busProperties.get("rt"));
						maybeCreatePatternStmt.setInt(3,
								Integer.parseInt(patternidstring));
						maybeCreatePatternStmt.executeUpdate();
					}
					knownPatterns.add(patternidstring);
				}

				updateBusStmt.setDouble(1,
						Double.parseDouble(busProperties.get("lat")));
				updateBusStmt.setDouble(2,
						Double.parseDouble(busProperties.get("lon")));
				updateBusStmt.setInt(3, Integer.parseInt(patternidstring));
				updateBusStmt.setInt(4, id);
				updateBusStmt.executeUpdate();

				if (!"OR".equals(busProperties.get("rt"))) {
					updatePatternStmt.setString(1, busProperties.get("rt"));
					updatePatternStmt.setInt(2,
							Integer.parseInt(busProperties.get("pid")));
					updatePatternStmt.executeUpdate();
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
			busProperties.clear();
		}
		currentElement.pop();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// handle nasty cases where you get several calls to this function for a
		// single element content
		if (busProperties.get(currentElement.peek()) != null)
			busProperties.put(currentElement.peek(),
					busProperties.get(currentElement.peek())
							+ new String(ch, start, length));
		else
			busProperties.put(currentElement.peek(), new String(ch, start,
					length));
	}

	public static void main(String args[]) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			System.err.println("CTA Bus Parsing server started: ");
			Class.forName("org.postgresql.Driver").newInstance();
			Connection db = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5433/postgis", "postgres",
					"postgresql");
			
			String DocumentURL = "http://www.ctabustracker.com/bustime/map/getBusesForRouteAll.jsp";
			SAXParser saxParser = factory.newSAXParser();

			String maybeBus = "INSERT INTO bus(id) SELECT ? WHERE ? NOT IN (SELECT id FROM bus)";
			PreparedStatement maybeCreateBusStmt = db
					.prepareStatement(maybeBus);

			String updateBus = "UPDATE bus SET location=SETSRID(MakePoint(?,?),4326), pattern_id=?, last_update=now(),mode_id = 1 where id=?;";
			PreparedStatement updateBusStmt = db.prepareStatement(updateBus);

			String maybePattern = "INSERT INTO pattern (id, route_id) SELECT ?,? WHERE ? NOT IN (SELECT id from pattern)";
			PreparedStatement maybeCreatePatternStmt = db
					.prepareStatement(maybePattern);

			String updatePattern = "UPDATE pattern SET route_id=? where id=?;";
			PreparedStatement updatePatternStmt = db
					.prepareStatement(updatePattern);

			CTABusParser parser = new CTABusParser(maybeCreateBusStmt,
					updateBusStmt, maybeCreatePatternStmt, updatePatternStmt);
			while (true) {
				try {
					saxParser.parse(DocumentURL, parser);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.sleep(250);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}