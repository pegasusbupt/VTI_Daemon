package utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

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
		//Log.println(readServerIP());
		createAccountXML();
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
		final String trainRouteAccDesc="This account posts scheduled alerts related to the associated train route. Data is fetched from CTA RSS feeds in real-time.";
		final String zoneArea="2 miles * 2 miles";
		final String zoneAccDesc="This account posts user generated publications that are reported within the area defined by its associated coordinates. Currently each zone covers a 2 mile * 2 mile square area.";
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
			elem = account.addElement("description");
			elem.setText(zoneAccDesc);
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
			elem = account.addElement("description");
			elem.setText(trainRouteAccDesc);
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
		HashMap<String,String> ret=new HashMap<String,String>();
		int i,j;
		try {
			doc = Jsoup.connect("http://cs.uic.edu/~sma/VTI/accounts.xml").get();
			org.jsoup.select.Elements accounts = doc.select("account");
			StringBuilder details=new StringBuilder();
			for(i=0;i<accounts.size();i++) {
				details.delete(0, details.length());
				org.jsoup.select.Elements childrenEles=accounts.get(i).children();
				for(j=0;j<childrenEles.size();j++){
					org.jsoup.nodes.Element ele=childrenEles.get(j);
					String tagName=ele.tagName();
					if(tagName.equals("southwest")||tagName.equals("northeast")){
						String coords=ele.select("lat").text()+" , "+ele.select("lng").text();
						details.append(tagName+" : "+coords+"\n");
					}
					else{
						details.append(tagName+" : "+ele.text()+"\n");
					}
				}
				//Log.println(account);
				ret.put(childrenEles.get(0).text(), details.toString());
				//ret.put(childrenEles.get(0).text(), accounts.get(i).html());
			}
			Log.println();
			for(String key: ret.keySet())
				Log.println(key+"\n"+ret.get(key));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
