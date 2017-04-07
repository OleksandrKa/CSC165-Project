package csc165_lab3;

import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import csc165_lab3.*;
import csc165_lab3.MyGameEngine;

public class Starter {
	public static void main(String[] args) throws IOException{
		Scanner s = new Scanner(System.in);
		
		System.out.print("Host or Join game? (h/j): ");
		String input = s.nextLine();
		if(input.charAt(0) == 'h'){
			System.out.print("Enter hosting port: ");
			int port = s.nextInt();
			GameServerTCP myGameServer = new GameServerTCP(port);
			//Get external IP so user doesn't have to look it up themselves:
			URL ipAdress;

			try {
				ipAdress = new URL("http://myexternalip.com/raw");

				BufferedReader in = new BufferedReader(new InputStreamReader(ipAdress.openStream()));

				String ip = in.readLine();
				System.out.println("Server Started at address " + ip + ", port " + port + ".");
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String[] msgTokens = myGameServer.getLocalInetAddress().toString().split("/");
			
		
			MyGameEngine myGame = new MyGameEngine(msgTokens[1], port);
			myGame.start();
		}
		else if(input.charAt(0) == 'j'){
			System.out.print("Enter server IP address: ");
			String ip = s.nextLine();
			System.out.print("Enter server port: ");
			int port = s.nextInt();
			MyGameEngine myGame = new MyGameEngine(ip, port);
			myGame.start();
		}
	}
}