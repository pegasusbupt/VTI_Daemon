package utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;

public class CreateAccountXML {
	
	public static void main(String[] args){
		new CreateAccountXML().main();
	}
	
	void main(){
		//System.out.println(readServerIP());
		//createAccountXML();
		parseAccountXML();
	}
	
	public static String readServerIP(){
		URL serverIP;
		String inputLine, ret = "";
		try {
			serverIP = new URL("http://www.cs.uic.edu/~sma/VTI/serverIP.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(serverIP.openStream()));
			while ((inputLine = in.readLine()) != null)
				ret = ret + inputLine;
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static void createAccountXML(){
		String file = "data\\accounts\\accounts.xml";
		Document doc = DocumentFactory.getInstance().createDocument();
		Element root = doc.addElement("accounts");
		Element account, parent, elem;
		float coord;
		int row, col;
		/**
		 * add zone accounts
		 */
		for (int i = 0; i < 25; i++) {
			account = root.addElement("account");
			elem = account.addElement("name");
			if (i < 10)
				elem.addText("vti_zone_0" + i);
			else
				elem.addText("vti_zone_" + i);
			row=i/(GeocodeAdapter.ZONE_NUM/2);
			col=i%(GeocodeAdapter.ZONE_NUM/2);
			elem = account.addElement("type");
			elem.addText("zone");
			parent = account.addElement("southwest");
			elem = parent.addElement("lat");
			coord=(float)((GeocodeAdapter.SOUTH*1.0E6+row*GeocodeAdapter.ZONE_LATITUDE)/1.0E6);
			elem.setText(String.valueOf(coord));
			elem = parent.addElement("lng");
			coord=(float)((GeocodeAdapter.WEST*1.0E6+col*GeocodeAdapter.ZONE_LONGITUDE)/1.0E6);
			elem.setText(String.valueOf(coord));
			parent = account.addElement("northeast");
			elem = parent.addElement("lat");
			coord=(float)((GeocodeAdapter.SOUTH*1.0E6+(row+1)*GeocodeAdapter.ZONE_LATITUDE)/1.0E6);
			elem.setText(String.valueOf(coord));
			elem = parent.addElement("lng");
			coord=(float)((GeocodeAdapter.WEST*1.0E6+(col+1)*GeocodeAdapter.ZONE_LONGITUDE)/1.0E6);
			elem.setText(String.valueOf(coord));
		}

		/**
		 * add train route accounts
		 */
		for (String route : FeedReader.route_id.keySet()) {
			account = root.addElement("account");
			elem = account.addElement("name");
			elem.setText(route);
			elem = account.addElement("type");
			elem.addText("train_route");
		}
		
		/**
		 * write the document to a file
		 */
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(fos, format);
			writer.write(doc);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void parseAccountXML(){
		org.jsoup.nodes.Document doc;
		int i;
		try {
			doc = Jsoup.connect("http://www2.cs.uic.edu/~sma/VTI/accounts.xml").get();
			org.jsoup.select.Elements accounts = doc.select("account > name");
			// System.out.println(Jsoup.parse(directions.html()));
			for(i=0;i<accounts.size();i++) {
				System.out.println(accounts.get(i).text());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
