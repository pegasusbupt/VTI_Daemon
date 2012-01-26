/**
 * @author Sol Ma
 * reference:  http://www.transitchicago.com/developers/ttdocs/default.aspx
 */

package tracker.train;

import java.util.ArrayList;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.Log;



public class CTATrainTracker {
	private static final String BASEURL="http://lapi.transitchicago.com/api/1.0/ttarrivals.aspx?key=1501afcdab194e6e9d286199bec4a19a"; 
	public static void main(String[] args){
		new CTATrainTracker().main();
	}
	
	public void main(){
		ArrayList<String> ans=requestStop("30070");
		if(ans.size()>0)
			for(String s: ans)
				Log.println(s);
	}
	
	/**
	 * @param: stpid: the stop id, return all available results 
	 */
	public ArrayList<String> requestStop(String stpId){
		String url=BASEURL+"&stpid="+stpId;
		Log.println(url);
		ArrayList<String> ret=new ArrayList<String>();
		try {
			Document doc=Jsoup.connect(url).get();
			long tmst=string2Long(doc.select("ctatt > tmst").text());
			Elements arrivals=doc.select("ctatt > eta > arrT");
			String nextRunTime;
			for(Element ele: arrivals){
				int diff=(int) ((string2Long(ele.text())-tmst)/1000/60);
				ret.add(String.valueOf(diff)+" minutes");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * @param arrT, e.g. "20120123 21:43:18"
	 * @return time 
	 */
	private long string2Long(String arrT){
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

