package csc165_lab3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import graphicslib3D.Vector3D;
import myGameEngine.Entity;
import sage.networking.client.GameConnectionClient;

public class GameClientTCP extends GameConnectionClient{
	private MyGame game;
	private UUID id;
	private char avatarType;
	//private Vector<Entity> entities;
	public Entity entity; //max of 2 players.
	
	public GameClientTCP(InetAddress remAddr, int remPort, ProtocolType pType, MyGame game) throws IOException{
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
	}
	
	protected void processPacket(Object msg){
		//extract incoming message into substrings, then process
		String message = (String) msg;
		String[] msgTokens = message.split(",");
		
		if(msgTokens[0].compareTo("join") == 0){ //received join
			//format: join, (success/failure)
			if(msgTokens[1].compareTo("success") == 0){
				game.setIsConnected(true);
				sendCreateMessage(game.getPlayerPosition());
				System.out.println("Client Connected");			
			}
			if(msgTokens[1].compareTo("failure") == 0){
				game.setIsConnected(false);
				System.out.println("Client Connection Failed");			
			}
		}
		if(msgTokens[0].compareTo("bye") == 0){ //received bye
			//format: bye, remoteID
			UUID ghostID = UUID.fromString(msgTokens[1]);
			removeGhostAvatar(ghostID);
		}
		if(msgTokens[0].compareTo("dsfr") == 0){ //received details for
			//format: dsfr, remoteID, x,y,z, avatarType
			UUID ghostID = UUID.fromString(msgTokens[1]);
			//extract ghost x,y,z position from message
			Vector3D ghostPosition = new Vector3D(Double.parseDouble(msgTokens[2]),Double.parseDouble(msgTokens[3]),Double.parseDouble(msgTokens[4]));
			//then:
			char remoteAvatar = msgTokens[5].charAt(0);
			createGhostAvatar(ghostID, ghostPosition, remoteAvatar);
		}
		if(msgTokens[0].compareTo("create") == 0){ //received create
			//format: create, remoteID, x,y,z, avatarType
			UUID ghostID = UUID.fromString(msgTokens[1]);
			//extract ghost x,y,z position from message
			Vector3D ghostPosition = new Vector3D(Double.parseDouble(msgTokens[2]),Double.parseDouble(msgTokens[3]),Double.parseDouble(msgTokens[4]));
			//then:
			char remoteAvatar = msgTokens[5].charAt(0);
			createGhostAvatar(ghostID, ghostPosition, remoteAvatar);
		}
		if(msgTokens[0].compareTo("wsds") == 0){ //received wants details
			//format: wsds, remoteID
			UUID remoteID = UUID.fromString(msgTokens[1]);
			sendDetailsForMessage(remoteID, game.getPlayerPosition(), avatarType);
		}
		if(msgTokens[0].compareTo("move") == 0){ //received move
			//format: move, remoteID, x,y,z
			UUID ghostID = UUID.fromString(msgTokens[1]);
			//extract ghost new x,y,z position from message
			Vector3D ghostPosition = new Vector3D(Double.parseDouble(msgTokens[2]),Double.parseDouble(msgTokens[3]),Double.parseDouble(msgTokens[4]));
			//then:
			moveGhostAvatar(ghostID, ghostPosition);
		}
	}
	
	public void sendCreateMessage(Vector3D pos){
		//format: create, localID, x,y,z
		try{
			String message = new String("create," + id.toString());
			message += "," + pos.getX();
			message += "," + pos.getY();
			message += "," + pos.getZ();
			message += "," + avatarType;
			sendPacket(message);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendJoinMessage(char playerAvatar){
		//format: join, localID
		avatarType = playerAvatar;
		try{
			sendPacket(new String("join," + id.toString()));
		} catch(IOException e) { e.printStackTrace(); }
	}
	public void sendByeMessage(){
		//format: bye, localID
		try{
			sendPacket(new String("bye," + id.toString()));
			System.out.println("Client Disconnected");
		} catch(IOException e) { e.printStackTrace(); }
	}
	public void sendDetailsForMessage(UUID remID, Vector3D pos, char localAvatar){
		//format: dsfr, localID, remoteID, localAvatar
		try{
			String message = new String("dsfr," + id.toString() + "," + remID.toString());
			message += "," + pos.getX();
			message += "," + pos.getY();
			message += "," + pos.getZ();
			message += "," + localAvatar;
			sendPacket(message);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendMoveMessage(Vector3D pos){
		//format: move, localID, pos
		try{
			String message = new String("move," + id.toString());
			message += "," + pos.getX();
			message += "," + pos.getY();
			message += "," + pos.getZ();
			sendPacket(message);
		} catch(IOException e){ e.printStackTrace(); }
	}
	
	private void createGhostAvatar(UUID remID, Vector3D pos, char remoteAvatar){
		this.entity = new Entity(remID, pos, remoteAvatar, game.display);
		System.out.print("Add entity\n\n");
		game.addGameWorldObject(this.entity.model);
	}
	private void removeGhostAvatar(UUID remID){
		//if(this.entity.id == remID){
			game.removeGameWorldObject(this.entity.model);
			System.out.print("StringStringString\n\n\n\n\n");
			this.entity = null;
		//}
	}
	private void moveGhostAvatar(UUID remID, Vector3D ghostPos){
		/*if(entity == null){
			System.out.print("null entity");
		}
		if(entity.id == null)
		{
			System.out.print("null id");
		}
		if(remID == null){
			System.out.print("null remID");
		}*/
		if(this.entity != null){
			System.out.print("not null entity");
			//if(this.entity.id == remID){
				System.out.print("updatePos");
				this.entity.updatePosition(ghostPos);
			//}
		}
		else{
			System.out.print("null entity");
		}
	}
}