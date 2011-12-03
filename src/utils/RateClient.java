package utils;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class RateClient {
    public static void main(String[] args) throws IOException {
        final String server="localhost";
        Socket clientSocket = null;
        PrintWriter out = null;

        try {
            clientSocket = new Socket(server, RateMultiServer.PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            String ud=new Scanner(System.in).nextLine();
            out.print("138504424040562690,Sol,"+ud);
            
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ server+" .");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: "+ server+ " .");
            System.exit(1);
        }

        out.close();
        clientSocket.close();
    }
}

