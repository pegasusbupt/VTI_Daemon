package utils;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class RateClient {
    public static void main(String[] args){
        final String server="67.167.207.236";
/*
        Thread vote=new Thread(){
        	public void run(){
     	       Socket clientSocket = null;
   	        PrintWriter out = null;
   	        BufferedReader in=null;
        try {
        	clientSocket = new Socket(server, RateMultiServer.PORT);
            clientSocket.setSoTimeout(1000);
            //Timer timeout=new Timer(1000);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
	        in = new BufferedReader(
					new InputStreamReader(
					clientSocket.getInputStream()));
	        /** Integer timeout in milliseconds for blocking accept or read/receive operations (but not write/send operations). A timeout of 0 means no timeout. So I need to add read operation here in case time is out*/
	        //TODO: SoTimeOut does not work here, why?
	    /* 
        while (in.readLine() != null);
	        Log.println("after blocking read");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            String ud=new Scanner(System.in).nextLine();
            out.print("138504424040562690,Sol,"+ud);
            out.close();
			clientSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ server+" .");
            //System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: "+ server+ " .");
            //System.exit(1);
        } 
        }
       };
        Timer timer = new Timer(1000, vote);
		timer.start();

*/
        InetAddress addr=null;
        SocketAddress sockaddr=null;
		try {
			addr = InetAddress.getByName(server);
	        sockaddr = new InetSocketAddress(addr, MultiServer.PORT);
	        // Create an unbound socket
	   
	        
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
	    Socket clientSocket = new Socket();
		if(sockaddr!=null)
			try {
				clientSocket.connect(sockaddr, 1000);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			//	e1.printStackTrace();
			}


  	        PrintWriter out = null;
  	        BufferedReader in=null;
   
          
	
	
           try {

           //Timer timeout=new Timer(1000);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
	        in = new BufferedReader(
					new InputStreamReader(
					clientSocket.getInputStream()));
	        /** Integer timeout in milliseconds for blocking accept or read/receive operations (but not write/send operations). A timeout of 0 means no timeout. So I need to add read operation here in case time is out*/
	        //TODO: SoTimeOut does not work here, why?
	     
	        while (in.readLine() != null);
	        Log.println("after blocking read");
           out = new PrintWriter(clientSocket.getOutputStream(), true);
           String ud=new Scanner(System.in).nextLine();
           out.print("138504424040562690,Sol,"+ud);
           out.close();
			clientSocket.close();
   		} catch (SocketException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		  
       } catch (IOException e) {
           //System.err.println("Couldn't get I/O for the connection to: "+ server+ " .");
           //System.exit(1);
       } 

        for(int i=0;i<100;i++);
        Log.println("Go on");
        	
    }
}


/** 
 * The Timer class allows a graceful exit when an application
 * is stalled due to a networking timeout. Once the timer is
 * set, it must be cleared via the reset() method, or the
 * timeout() method is called.
 * <p>
 * The timeout length is customizable, by changing the 'length'
 * property, or through the constructor. The length represents
 * the length of the timer in milliseconds.
 *
 * @author	David Reilly
 */
class Timer extends Thread
{
	/** Rate at which timer is checked */
	protected int m_rate = 100;
	
	/** Length of timeout */
	private int m_length;

	/** Time elapsed */
	private int m_elapsed;
	
	/** Thread to be killed */
	private Thread m_t;

	/**
	  * Creates a timer of a specified length
	  * @param	length	Length of time before timeout occurs
	  */
	public Timer ( int length, Thread t )
	{
		// Assign to member variable
		m_length = length;

		// Set time elapsed
		m_elapsed = 0;
		
		m_t=t;
	}

	
	/** Resets the timer back to zero */
	public synchronized void reset()
	{
		m_elapsed = 0;
	}

	/** Performs timer specific code */
	public void run()
	{
		// Keep looping
		m_t.start();
		for (;;)
		{
			// Put the timer to sleep
			try
			{ 
				Thread.sleep(m_rate);
			}
			catch (InterruptedException ioe) 
			{
				break;
			}

			// Use 'synchronized' to prevent conflicts
			synchronized ( this )
			{
				// Increment time remaining
				m_elapsed += m_rate;

				// Check to see if the time has been exceeded
				if (m_elapsed > m_length)
				{
					// Trigger a timeout
					timeout();
					break;
				}
			}

		}
	}

	// Override this to provide custom functionality
	public void timeout()
	{
		System.err.println ("Network timeout occurred.... terminating");
        m_t.interrupt();
   	}
}









