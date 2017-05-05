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
import sage.model.loader.OBJLoader;
//import myGameEngine.*;
import sage.networking.IGameConnection.ProtocolType;
import sage.renderer.IRenderer;
import sage.scene.HUDString;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.TriMesh;
import sage.scene.shape.Line;
import sage.texture.Texture;
import sage.texture.TextureManager;
import sage.scene.state.RenderState.RenderStateType;
import sage.scene.state.TextureState;
import sage.terrain.AbstractHeightMap;
import sage.terrain.HillHeightMap;
import sage.terrain.TerrainBlock;
//Physics
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
//temp:
import sage.scene.shape.Sphere;

import csc165_lab3.*;
import MyGameEngine.*;

public class MyGame extends BaseGame{
	boolean testOnSingleComputerFlag = true;
	
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
	IPhysicsEngine physicsEngine;
	IPhysicsObject ballP, heroP, terrainP;
	DisplaySettingsDialog displayDialog;
	
	//Game Objects
	private SceneNode player;
	private TriMesh heroNPC;
	private OrbitCameraController playerCam;
	private HUDString timeString;
	private SkyBox skybox;
	//temp:
	private Sphere ball;
	
	//Game Data
	private float speed = 0.02f;
	private float time = 0;
	boolean isConnected = false;
	boolean running;

	//Terrain
	private TerrainBlock hillTerrain;
	
	//NPC
	private NPCcontroller npcCtrl;

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
		initPhysics();
		initGameObjects();
		//TODO: Should probably move initTerrain, initNPC, inside of GameObjects. Maybe initPlayer too.
		initTerrain();
		initNPC();
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
		
		//Physics Processing.
		Matrix3D mat;
		physicsEngine.update(20.0f);
		for(SceneNode s : getGameWorld()){
			if(s.getPhysicsObject() != null){
				mat = new Matrix3D(s.getPhysicsObject().getTransform());
				s.getLocalTranslation().setCol(3,mat.getCol(3));
				//TODO: should also get and apply rotation.
			}
		}
		
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
		IAction mvForward = new MoveForward(player, speed, hillTerrain);
		IAction mvBack = new MoveBack(player, speed, hillTerrain);
		IAction mvRight = new MoveRight(player, speed, hillTerrain);
		IAction mvLeft = new MoveLeft(player, speed, hillTerrain);

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
		
		//TODO: Remove demo sphere, add objects for game.
		ball = new Sphere(1.0,16,16, Color.blue);
		Matrix3D translateM = new Matrix3D();
		translateM.translate(5,20,5);
		ball.setLocalTranslation(translateM);
		addGameWorldObject(ball);
		
		ball.updateGeometricState(1.0f, true);
		
		float mass = 1.0f;
		ballP = physicsEngine.addSphereObject(physicsEngine.nextUID(), 
				mass, ball.getWorldTransform().getValues(), 1.0f);
		ballP.setBounciness(1.0f);
		ball.setPhysicsObject(ballP);
	}
	
	private void initNPC() {
		OBJLoader loader = new OBJLoader();

		// Instantiate Hero NPC
		heroNPC = loader.loadModel("./images/hero.obj");
		heroNPC.updateLocalBound();
		Point3D heroLoc = new Point3D(5,0,5);
		heroNPC.translate((float) heroLoc.getX(),(float) heroLoc.getY(),(float) heroLoc.getZ());
		// Apply Textures
		TextureState heroState;
		Texture heroTexture = TextureManager.loadTexture2D("./images/heroTexture.png");
		heroTexture.setWrapMode(Texture.WrapMode.Repeat);
		heroTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		heroState = (TextureState) display.getRenderer().createRenderState(RenderStateType.Texture);
		heroState.setTexture(heroTexture, 0);
		heroState.setEnabled(true);
		heroNPC.setRenderState(heroState);

		// Add NPCs to world
		addGameWorldObject(heroNPC);
		
		heroNPC.updateGeometricState(1.0f, true);
		
		//Add NPC Physics
		float mass = 5.0f;
		//TODO: Experimentally determine best radius and height values.
		heroP = physicsEngine.addCapsuleObject(physicsEngine.nextUID(), 
				mass, heroNPC.getWorldTransform().getValues(), 1.0f, 0.5f);
		heroP.setBounciness(0.5f);
		heroNPC.setPhysicsObject(heroP);
		
		//Add NPC AI
		//TODO: Move all npc code to separate npc class.
		npcCtrl = new NPCcontroller(this, heroNPC, heroLoc);
		npcCtrl.startNPControl();
	}

	private void initTerrain() { // create height map and terrain block
		HillHeightMap myHillHeightMap = new HillHeightMap(300, 2000, 5.0f, 20.0f, (byte) 2, 12345);
		myHillHeightMap.setHeightScale(0.1f);
		hillTerrain = createTerBlock(myHillHeightMap);
		// create texture and texture state to color the terrain
		TextureState grassState;
		Texture grassTexture = TextureManager.loadTexture2D("./images/grass.jpg");
		grassTexture.setWrapMode(Texture.WrapMode.Repeat);
		grassTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		grassState = (TextureState) display.getRenderer().createRenderState(RenderStateType.Texture);
		grassState.setTexture(grassTexture, 0);
		grassState.setEnabled(true);
		// apply the texture to the terrain
		hillTerrain.setRenderState(grassState);
		addGameWorldObject(hillTerrain);
		
		hillTerrain.updateGeometricState(1.0f, true);
		
		//Add Terrain Physics
		float up[] = {0,1,0};
		terrainP = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), 
					hillTerrain.getWorldTransform().getValues(), up, 0.0f);
		terrainP.setBounciness(0.0f);
		//remove this line for ODE4J.
		hillTerrain.setPhysicsObject(terrainP);
		
		//TODO: should also set damping, friction, etc.
	}

	private void initPhysics() {
		//String engine = "sage.physics.ODE4J.ODE4JPhysicsEngine";
		String engine = "sage.physics.JBullet.JBulletPhysicsEngine";
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		float[] gravity = {0, -1f, 0};
		physicsEngine.setGravity(gravity);
	}
	
	private TerrainBlock createTerBlock(AbstractHeightMap heightMap) {
		float heightScale = 0.05f;
		Vector3D terrainScale = new Vector3D(1, heightScale, 1);
		// use the size of the height map as the size of the terrain
		int terrainSize = heightMap.getSize();
		// specify terrain origin so heightmap (0,0) is at world origin
		float cornerHeight = heightMap.getTrueHeightAtPoint(0, 0) * heightScale;
		Point3D terrainOrigin = new Point3D(0, -cornerHeight, 0);
		// create a terrain block using the height map
		String name = "Terrain:" + heightMap.getClass().getSimpleName();
		TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale, heightMap.getHeightData(), terrainOrigin);
		return tb;
	}

	private char selectAvatar() {
		Scanner s = new Scanner(System.in);
		System.out.print("What avatar would you like? (p for pyramid, d for hollow pyramid)");
		String avatar = s.nextLine();
		s.close();
		if (avatar.charAt(0) == 'p')
			return 'p';
		else if (avatar.charAt(0) == 'd')
			return 'd';
		else
			return 0;
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

	public void checkAvatarNear(Point3D npcP) {
		boolean isNear = false;
		Point3D avLoc = new Point3D(player.getLocalTranslation().getCol(3));
		if (Math.abs(npcP.getX() - avLoc.getX()) <= 5
			&& Math.abs(npcP.getY() - avLoc.getY()) <= 5){
				isNear = true;
			}
		
		
		npcCtrl.setNearFlag(isNear);
	}
}
