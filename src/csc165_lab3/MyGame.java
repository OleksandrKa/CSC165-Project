package csc165_lab3;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.display.DisplaySettingsDialog;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.IAction;
//import myGameEngine.*;
import sage.networking.IGameConnection.ProtocolType;
import sage.renderer.IRenderer;
import sage.scene.HUDString;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.shape.Line;
import sage.texture.Texture;
import sage.texture.TextureManager;

import csc165_lab3.*;
import MyGameEngine.*;

public class MyGame extends BaseGame{
	boolean testOnSingleComputerFlag = false;
	
	GameClientTCP thisClient;
	
	//Server Data
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private char playerAvatar;
	
	//Script Data
	ScriptEngineManager factory;
	String scriptFileName;
	ScriptEngine jsEngine;
	
	//Interfaces
	IDisplaySystem display;
	IInputManager im;
	IEventManager eventMg;
	IRenderer renderer;
	ICamera camera;
	DisplaySettingsDialog displayDialog;
	
	//Game Objects
	private SceneNode player;
	private OrbitCameraController playerCam;
	private HUDString timeString;
	private SkyBox skybox;
	
	//Game Data
	private float speed = 0.02f;
	private float time = 0;
	boolean isConnected = false;

	public MyGame(String serverAddr, int sPort){
		super();
		this.serverAddress = serverAddr;
		this.serverPort = sPort;
		this.serverProtocol = ProtocolType.TCP;
		this.playerAvatar = selectAvatar();
		
	}
	
	public void initGame(){
		initScript();
		initNetwork();
		initDisplay();
		initGameObjects();
		initPlayer();
		initActions();
	}
	
	protected void initSystem(){
		//call a local method to create a DisplaySystem object.
		display = createDisplaySystem();
		setDisplaySystem(display);
		//create an Input Manager
		IInputManager inputManager = new InputManager();
		setInputManager(inputManager);
		//create an (empty) gameworld
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}
	
	public void update(float elapsedTimeMS){
		Point3D camLoc = camera.getLocation();
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		if(testOnSingleComputerFlag == false)
			skybox.setLocalTranslation(camTranslation);
		//#endif
		
		time += elapsedTimeMS;
		DecimalFormat df = new DecimalFormat("0.0");
		timeString.setText("Time = " + df.format(time / 1000));
		playerCam.update(elapsedTimeMS);
		if(thisClient != null) thisClient.processPackets();
		
		// tell BaseGame to update game world state
		super.update(elapsedTimeMS);

	}
	
	private void initScript(){
		factory = new ScriptEngineManager();
		scriptFileName = "init.js";
		jsEngine = factory.getEngineByName("js");
		
	}
	
	private void initNetwork(){
		try{
			thisClient = new GameClientTCP(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		}
		catch(UnknownHostException e){ e.printStackTrace(); }
		catch(IOException e)  { e.printStackTrace(); }
		
		if(thisClient != null) { thisClient.sendJoinMessage(playerAvatar); }
	}
	
	private void initDisplay(){
		display = getDisplaySystem();
		System.out.println(display.getRenderer().toString());
		display.setTitle("CSC165 Project#3");
		renderer = display.getRenderer();
	}
	
	private void initActions(){
		im = getInputManager();
		eventMg = EventManager.getInstance();
		
		//String gpName = im.getFirstGamepadName();
		String kbName = im.getKeyboardName();

		playerCam = new OrbitCameraController(camera, player, im, kbName);
		

		// Gamepad Bindings
		IAction mvXAxis = new MoveXAxis(player, speed);
		IAction mvZAxis = new MoveZAxis(player, speed);

		/* im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, mvXAxis,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, mvZAxis,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); */

		// Keyboard Bindings
		QuitGameAction escQuit = new QuitGameAction(this);
		IAction mvForward = new MoveForward(player, speed);
		IAction mvBack = new MoveBack(player, speed);
		IAction mvRight = new MoveRight(player, speed);
		IAction mvLeft = new MoveLeft(player, speed);

		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, mvForward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, mvBack,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, mvRight,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, mvLeft,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, escQuit,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		// List out controllers
		//f = new FindComponents();
		//f.listControllers();
	}

	private void initPlayer(){
		//TODO: Replace with a local avatar class, add a variable containing the local avatar class to ghostavatar.
		player = new GhostAvatar(UUID.fromString("00000000-0000-0000-0000-000000000000")
								, new Vector3D(0,0,0), playerAvatar);
		player.translate(0,0,0);
		player.rotate(180, new Vector3D(0, 1, 0));
		addGameWorldObject(player);
		
		camera = new JOGLCamera(renderer);
		//camera.setPerspectiveFrustum(60, 2, 1, 1000);
		//camera.setViewDirection(new Vector3D(-1, 0, 1));
		//camera.setViewport(0.0, 1.0, 0.0, 0.45);
		
		initHUD();
	}
	
	private void initHUD(){
		HUDString playerID;
		if( playerAvatar == 'p') playerID = new HUDString("Pyramid");
		else 					playerID = new HUDString("AntiPyramid");
		
		playerID.setName("Player1ID");
		playerID.setLocation(0.01, 0.1);
		playerID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		playerID.setColor(Color.YELLOW);
		playerID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera.addToHUD(playerID);
		
		timeString = new HUDString("Time = " + time);
		timeString.setLocation(0, 0.05); // (0,0) [lower-left] to (1,1)
		addGameWorldObject(timeString);
	}
	
	private void initGameObjects(){
		//#ifndef TESTONSINGLECOMPUTER
		if(testOnSingleComputerFlag == false){
		// construct a skybox for the scene
		skybox = new SkyBox("SkyBox", 20.0f, 20.0f, 20.0f);
		// load skybox textures
		Texture frontTex = TextureManager.loadTexture2D("./images/lakes_ft.bmp");
		Texture backTex = TextureManager.loadTexture2D("./images/lakes_bk.bmp");
		Texture leftTex = TextureManager.loadTexture2D("./images/lakes_lf.bmp");
		Texture rightTex = TextureManager.loadTexture2D("./images/lakes_rt.bmp");
		Texture topTex = TextureManager.loadTexture2D("./images/lakes_up.bmp");
		Texture bottomTex = TextureManager.loadTexture2D("./images/lakes_dn.bmp");
		
		//...etc...
		// attach textures to skybox
		skybox.setTexture(SkyBox.Face.North, frontTex);
		skybox.setTexture(SkyBox.Face.South, backTex);
		skybox.setTexture(SkyBox.Face.East, leftTex);
		skybox.setTexture(SkyBox.Face.West, rightTex);
		skybox.setTexture(SkyBox.Face.Up, topTex);
		skybox.setTexture(SkyBox.Face.Down, bottomTex);
		addGameWorldObject(skybox);
		}//#endif
		
		
		this.executeScript(jsEngine, scriptFileName);
		
		Line xAxis = (Line) jsEngine.get("xAxis");
		addGameWorldObject(xAxis);
		Line yAxis = (Line) jsEngine.get("yAxis");
		addGameWorldObject(yAxis);
		Line zAxis = (Line) jsEngine.get("zAxis");
		addGameWorldObject(zAxis);
	}
	
	private char selectAvatar(){
		Scanner s = new Scanner(System.in);
		System.out.print("What avatar would you like? (p for pyramid, d for hollow pyramid)");
		String avatar = s.nextLine();
		s.close();
		if(avatar.charAt(0) == 'p')		 return 'p';
		else if(avatar.charAt(0) == 'd') return 'd';
		else 							 return 0;
	}
	
	private void executeScript(ScriptEngine engine, String scriptFileName){
		try{
			FileReader fileReader = new FileReader(scriptFileName);
			engine.eval(fileReader);
			fileReader.close();
		}
		catch (FileNotFoundException e1)
		{  System.out.println(scriptFileName + " not found " + e1); }
		catch (IOException e2)
		{  System.out.println("IO problem with " + scriptFileName + e2); }
		catch (ScriptException e3)
		{ System.out.println("ScriptException in " + scriptFileName + e3); }
		catch (NullPointerException e4)
		{ System.out.println ("Null ptr exception in " + scriptFileName + e4); }
	}
	
	private IDisplaySystem createDisplaySystem(){
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = environment.getDefaultScreenDevice();
		displayDialog = new DisplaySettingsDialog(device);
		displayDialog.showIt();
		display = new MyDisplaySystem(displayDialog.getSelectedDisplayMode().getWidth(),
				displayDialog.getSelectedDisplayMode().getHeight(),
				displayDialog.getSelectedDisplayMode().getBitDepth(),
				displayDialog.getSelectedDisplayMode().getRefreshRate(), true, "sage.renderer.jogl.JOGLRenderer");
		System.out.println("\nWaiting for display creation...");
		int count = 0;

		while (!display.isCreated()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				throw new RuntimeException("Display creation interrupted");
			}

			count++;
			System.out.print("+");
			if (count % 80 == 0) {
				System.out.println();
			}

			if (count > 2000) {
				throw new RuntimeException("Unable to create display");
			}
		}

		System.out.println();
		return display;
	}
	
	protected void render(){
		renderer.setCamera(camera);
		super.render();
	}

	public void start() {
		super.start();
	}
	
	protected void shutdown(){
		super.shutdown();
		if(thisClient != null){
			thisClient.sendByeMessage();
			try{ thisClient.shutdown(); }
			catch(IOException e){ e.printStackTrace(); }
		}
		display.close();
	}
	
	public void setIsConnected(boolean flag){
		isConnected = flag;
	}
	
	public Vector3D getPlayerPosition(){
		return new Vector3D(camera.getLocation().getX(),camera.getLocation().getY(),camera.getLocation().getZ());
	}
	
	public void addGameWorldObject(SceneNode s){
		super.addGameWorldObject(s);
	}
	public boolean removeGameWorldObject(SceneNode s){
		return super.removeGameWorldObject(s);
	}
	
}
