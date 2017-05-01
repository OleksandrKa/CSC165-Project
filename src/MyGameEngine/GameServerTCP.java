package MyGameEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import sage.networking.server.GameConnectionServer;
import sage.networking.server.IClientInfo;
import csc165_lab3.*;

public class GameServerTCP extends GameConnectionServer<UUID>{
	
	public GameServerTCP(int localPort) throws IOException{
		super(localPort, ProtocolType.TCP);
	}
	
	public void acceptClient(IClientInfo ci, Object o){
		String message = (String)o;
		String[] messageTokens = message.split(",");
		
		if(messageTokens.length > 0){
			if(messageTokens[0].compareTo("join") == 0){ //received "join"
				//format: join, localID
				UUID clientID = UUID.fromString(messageTokens[1]);
				addClient(ci, clientID);
				sendJoinedMessage(clientID, true);
				System.out.println("Client Connected to Server");
			}
		}
	}
	
	public void processPacket(Object o, InetAddress senderIP, int sndPort){
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0){
			
			if(msgTokens[0].compareTo("bye") == 0){ //received "bye"
				//format: bye, localID
				UUID clientID = UUID.fromString(msgTokens[1]);
				sendByeMessages(clientID);
				removeClient(clientID);
			}
			if(msgTokens[0].compareTo("create") == 0){ //received "create"
				//format: create, localID, x,y,z, remoteAvatar
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2],msgTokens[3],msgTokens[4]};
				char remoteAvatar = msgTokens[5].charAt(0);
				sendCreateMessages(clientID, pos, remoteAvatar);
				sendWantsDetailsMessages(clientID);
			}
			if(msgTokens[0].compareTo("dsfr") == 0){ //received "details for"
				//format: dsfr, localID, remoteID, x,y,z, remoteAvatar
				UUID clientID = UUID.fromString(msgTokens[1]);
				UUID remoteID = UUID.fromString(msgTokens[2]);
				String[] pos = {msgTokens[3],msgTokens[4],msgTokens[5]};
				char remoteAvatar = msgTokens[6].charAt(0);
				sendDetailsMessage(clientID, remoteID, pos, remoteAvatar);
			}
			if(msgTokens[0].compareTo("move") == 0){ //received "move"
				//format: move, localID, x,y,z
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2],msgTokens[3],msgTokens[4]};
				sendMoveMessages(clientID, pos);
			}
		}
	}
	
	public void sendJoinedMessage(UUID clientID, boolean success){
		//format: join, (success/failure)
		try{
			String message = new String("join,");
			if(success) message += "success";
			else		message += "failure";
			sendPacket(message, clientID);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendCreateMessages(UUID clientID, String[] position, char remoteAvatar){
		//format: create, clientID, x,y,z, remoteAvatar
		try{
			String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + remoteAvatar;
			forwardPacketToAll(message, clientID);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendDetailsMessage(UUID clientID, UUID remoteID, String[] position, char remoteAvatar){
		//format: dsfr, clientID, x,y,z, remoteAvatar
		try{
			String message = new String("dsfr," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			message += "," + remoteAvatar;
			sendPacket(message, remoteID);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendWantsDetailsMessages(UUID clientID){
		//format: wsds, clientID
		try{
			String message = new String("wsds," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendMoveMessages(UUID clientID, String[] position){
		//format: move, clientID, x,y,z
		try{
			String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendByeMessages(UUID clientID){
		//format: bye, clientID
		try{
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
			System.out.println("Client Disconnected from Server");
		} catch(IOException e){ e.printStackTrace(); }
	}
}