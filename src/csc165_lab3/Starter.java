package csc165_lab3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import myGameEngine.GameServerTCP;


public class Starter{
	public static Scanner s = new Scanner(System.in);
	public static void main(String[] args){
		System.out.print("Host or Join game? (h/j): ");
		String input = s.nextLine();
		if(input.charAt(0) == 'h'){
			hostServer();
		}
		else if(input.charAt(0) == 'j'){
			joinServer();
		}
	}
	
	public static void hostServer(){
		System.out.print("Enter hosting port: ");
		int port = s.nextInt();
		
		GameServerTCP myGameServer = null;
		try {
			myGameServer = new GameServerTCP(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//Get external IP so user doesn't have to look it up themselves:
		URL ipAdress;
		try {
			ipAdress = new URL("http://myexternalip.com/raw");
			//Fetch external IP and assign to string.
			BufferedReader in = new BufferedReader(new InputStreamReader(ipAdress.openStream()));
			String ip = in.readLine();
			
			System.out.println("Server Started at address " + ip + ", port " + port + ".");
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] msgTokens = null;
		try {
			msgTokens = myGameServer.getLocalInetAddress().toString().split("/");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		new MyGame(msgTokens[1], port, 'h').start();
	}
	
	public static void joinServer(){
		System.out.print("Enter server IP address: ");
		String ip = s.nextLine();
		System.out.print("Enter server port: ");
		int port = s.nextInt();
		
		new MyGame(ip, port, 'j').start();
	}
}