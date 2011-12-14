/*
 * @return this is the server class that handle ratings
 */
package utils;

import java.io.IOException;
import java.net.ServerSocket;

public class RateMultiServer extends Thread{
	public final static int PORT='V'+'T'+'I';
	private ServerSocket server;
	
	public static void main(String[] args){
		new RateMultiServer().start();
		//System.out.println(PORT);
	}
	
	public RateMultiServer(){
		try {
			server = new ServerSocket(PORT);
			//System.out.println(server.getLocalPort());
			//System.out.println(server.getInetAddress());
			System.out.println(server.getLocalSocketAddress());
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+PORT);
		}
	}
	
	public void run(){
		while (true)
			try {
				new RateMultiServerThread(server.accept()).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}

