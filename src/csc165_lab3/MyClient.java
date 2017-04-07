public class MyClient extends GameConnectionClient{
	private MyNetworkingClient game;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	
	public MyClient(InetAddress remAddr, int remPort, ProtocolType pType, myNetworkingClient game) throws IOException{
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.random UUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
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
			}
			if(msgTokens[1].compareTo("failure") == 0){
				game.setIsConnected(false);
			}
		}
		if(messageTokens[0].compareTo("bye") == 0){ //received bye
			//format: bye, remoteID
			UUID ghostID = UUID.fromString(messageTokens[1]);
			removeGhostAvatar(ghostID);
		}
		if(messageTokens[0].compareTo("dsfr") == 0){ //received details for
			//format: dsfr, remoteID, x,y,z
			UUID ghostID = UUID.fromString(messageTokens[1]);
			//extract ghost x,y,z position from message
			Vector3D ghostPosition = new Vector3D(msgTokens[2],msgTokens[3],msgTokens[4]);
			//then:
			createGhostAvatar(ghostID, ghostPosition);
		}
		if(messageTokens[0].compareTo("create") == 0){ //received create
			//format: create, remoteID, x,y,z
			UUID ghostID = UUID.fromString(messageTokens[1]);
			//extract ghost x,y,z position from message
			Vector3D ghostPosition = new Vector3D(msgTokens[2],msgTokens[3],msgTokens[4]);
			//then:
			createGhostAvatar(ghostID, ghostPosition);
		}
		if(messageTokens[0].compareTo("wsds") == 0){ //received wants details
			//format: wsds, remoteID
			UUID remoteID = UUID.fromString(messageTokens[1]);
			sendDetailsForMessage(remoteID, game.getPlayerPosition());
		}
		if(messageTokens[0].compareTo("move") == 0){ //received move
			//format: move, remoteID, x,y,z
			UUID ghostID = UUID.fromString(messageTokens[1]);
			//extract ghost new x,y,z position from message
			Vector3D ghostPosition = new Vector3D(msgTokens[2],msgTokens[3],msgTokens[4]);
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
			sendPacket(message);
		} catch(IOException e){ e.printStackTrace(); }
	}
	public void sendJoinMessage(){
		//format: join, localID
		try{
			sendPacket(new String("join," + id.toString()));
		} catch(IOException e) { e.printStackTrace(); }
	}
	public void sendByeMessage(){
		//format: bye, localID
		try{
			sendPacket(new String("bye," + id.toString()));
		} catch(IOException e) { e.printStackTrace(); }
	}
	public void sendDetailsForMessage(UUID remID, Vector3D pos){
		//format: dsfr, localID, remoteID
		try{
			String message = new String("dsfr," + id.toString() + "," + remID.toString());
			message += "," + pos.getX();
			message += "," + pos.getY();
			message += "," + pos.getZ();
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
}