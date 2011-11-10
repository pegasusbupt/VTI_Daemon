package utils;

/**
 * It Reads and prints any RSS/Atom feed type.
 * 
 * @author Sol Ma
 *
 */
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
 

public class FeedReader {
	/*
	 * @ this function writes a collection of lines to a file
	 */
	public static void writeFile(String file, Collection<String> lines){
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file,true));
			System.out.println(file);
			for(String line: lines){
				System.out.println(line);
				out.write(line);
			}
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
    }
	
	/*
	 * @ this function writes a collection of lines to a file
	 */
	public static HashSet<String> readFile(String file){
		HashSet<String> ret=new HashSet<String>();
		try {
			Scanner s=new Scanner(new File(file));
			while(s.hasNextLine())
				ret.add(s.nextLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
    public static void main(String[] args) {
    	try{
    		  // Create file 
    		  FileWriter fstream = new FileWriter("output.txt");
    		  BufferedWriter out = new BufferedWriter(fstream);
    		  out.write("Hello Java");
    		  //Close the output stream
    		  out.close();
    		  }catch (Exception e){//Catch exception if any
    		  System.err.println("Error: " + e.getMessage());
    		  }
    	
    /*
    	boolean ok = false;
        if (args.length==1) {
            try {
                URL feedUrl = new URL(args[0]);
                String routeId=args[0].substring(args[0].lastIndexOf('=')+1);
                System.out.println(routeId);
                String idFile="cta_rss/alert_ids/"+routeId;
                              
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(feedUrl));
 
                //System.out.println(feed);
                HashSet<String> old_alertIds=readFile(idFile);
                HashSet<String> alertIds=new HashSet<String>();
                @SuppressWarnings("unchecked")
				List<SyndEntryImpl> entries=feed.getEntries();
                for(SyndEntryImpl entry: entries){
                	String link=entry.getLink();
                	String id=link.substring(link.lastIndexOf('=')+1);
                    if(!old_alertIds.contains(id)){ //is a new alert
                    	System.out.println(entry.getDescription().getValue());
                    	alertIds.add(id);
                    }
                }
                writeFile(idFile, alertIds);
                System.out.println(alertIds);
                
                
                ok = true;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("ERROR: "+ex.getMessage());
            }
        }
 
        if (!ok) {
            System.out.println();
            System.out.println("FeedReader reads and prints any RSS/Atom feed type.");
            System.out.println("The first parameter must be the URL of the feed to read.");
            System.out.println();
        }
        */
    }
 
}
