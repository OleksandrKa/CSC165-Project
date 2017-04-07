package csc165_lab3;

import java.io.IOException;
import java.util.Scanner;

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
			String[] msgTokens = myGameServer.getLocalInetAddress().toString().split("/");
			System.out.println("Server Started at address " + myGameServer.getLocalInetAddress().toString() + ", port " + port + ".");
		
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