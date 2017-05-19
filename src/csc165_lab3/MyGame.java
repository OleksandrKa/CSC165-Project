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
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.omg.CORBA.ARG_IN;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import myGameEngine.MoveBack;
import myGameEngine.MoveForward;
import myGameEngine.MoveLeft;
import myGameEngine.MoveRight;
import myGameEngine.MoveXAxis;
import myGameEngine.MoveZAxis;
import myGameEngine.MyDisplaySystem;
import myGameEngine.QuitGameAction;
import net.java.games.input.Component.Identifier.Axis;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.camera.controllers.ThirdPersonOrbitCameraController;
import sage.display.DisplaySettingsDialog;
import sage.display.IDisplaySystem;
import sage.event.*;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.IAction;
//import myGameEngine.*;
import sage.networking.IGameConnection.ProtocolType;
//Physics
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.renderer.IRenderer;
import sage.scene.Group;
import sage.scene.HUDString;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.shape.Cylinder;
import sage.scene.state.RenderState.RenderStateType;
import sage.scene.state.TextureState;
import sage.terrain.AbstractHeightMap;
import sage.terrain.HillHeightMap;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;
//Sound
import sage.audio.*;
import com.jogamp.openal.ALFactory;

public class MyGame extends BaseGame {

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
	IEventManager eventMgr;
	IRenderer renderer;
	ICamera camera;
	IPhysicsEngine physicsEngine;
	IPhysicsObject crashPodP, terrainP;
	DisplaySettingsDialog displayDialog;

	//Game Objects
	private Entity player;
	private Point3D player1Loc;
	private Point3D player2Loc;

	Group mines;
	private NPC[] mine;
	private int mineCount;
	private Point3D[] mineLoc;
	private int mineDistance;

	private ThirdPersonOrbitCameraController playerCam;
	private HUDString timeString;
	private SkyBox skybox;
	//temp:
	private Cylinder crashPod;

	//Game Data
	private float speed = 0.02f;
	private float time = 0;
	boolean isConnected = false;
	boolean running;
	char hostingStatus;

	//Terrain
	private TerrainBlock hillTerrain;

	//NPC
	private NPCcontroller[] npcCtrl;

	//Sound
	IAudioManager audioMgr;
	Sound bgSound, npcSound, beepSound, explosionSound, victorySound;
	private ArrayList<Sound> sounds; // contains all sounds, used to pass into EventHandler

	//Events
	PlayerMineEvent collisionEvent;

	public MyGame(String serverAddr, int sPort, char host) {
		super();
		this.serverAddress = serverAddr;
		this.serverPort = sPort;
		this.serverProtocol = ProtocolType.TCP;
		this.playerAvatar = selectAvatar();
		this.hostingStatus = host;

	}

	public void initGame() {
		initScript();
		initNetwork();
		initDisplay();
		initPhysics();
		initGameObjects();
		//TODO: Should probably move initTerrain, initNPC, inside of GameObjects. Maybe initPlayer too.
		initTerrain();
		initPlayer();
		initNPC();
		initActions();
		initAudio();
	}

	protected void initSystem() {
		//call a local method to create a DisplaySystem object.
		display = createDisplaySystem();
		setDisplaySystem(display);
		//create an Input Manager
		IInputManager inputManager = new InputManager();
		setInputManager(inputManager);
		eventMgr = EventManager.getInstance();
		//create an (empty) gameworld
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}

	public void update(float elapsedTimeMS) {
		Point3D camLoc = camera.getLocation();
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		skybox.setLocalTranslation(camTranslation);

		time += elapsedTimeMS;
		DecimalFormat df = new DecimalFormat("0.0");
		timeString.setText("Time = " + df.format(time / 1000));
		playerCam.update(elapsedTimeMS);

		if (thisClient != null) {
			thisClient.processPackets();
			Vector3D pos = player.model.getLocalTranslation().getCol(3);
			Vector3D degVec = player.model.getLocalRotation().getCol(2);
			double sinOfDegrees = degVec.getX();
			double cosOfDegrees = degVec.getZ();
			int deg = (int) Math.round(Math.toDegrees((Math.atan2(sinOfDegrees,cosOfDegrees))));
			thisClient.sendMoveMessage(pos, deg);
		}

		/*//Test if calculated correctly.
		Vector3D pos = player.model.getLocalTranslation().getCol(3);
		Vector3D degVec = player.model.getLocalRotation().getCol(2);
		double sinOfDegrees = degVec.getX();
		double cosOfDegrees = degVec.getZ();
		int deg = (int) Math.round(Math.toDegrees((Math.atan2(sinOfDegrees,cosOfDegrees))));
		player.updatePosition(pos, deg);
		System.out.println(deg);
		*///

		//Physics Processing.
		Matrix3D mat;
		physicsEngine.update(20.0f);
		for (SceneNode s : getGameWorld()) {
			if (s.getPhysicsObject() != null) {
				mat = new Matrix3D(s.getPhysicsObject().getTransform());
				s.getLocalTranslation().setCol(3, mat.getCol(3));
				//TODO: should also get and apply rotation.
			}
		}

		//AI Processing.
		for (int i = 0; i < mineCount; i++)
			npcCtrl[i].npcLoop();

		// Update 3D Sound direction
		//npcSound.setLocation(new Point3D(mines.getWorldTranslation().getCol(3)));
		setEarParameters();

		for (SceneNode s : mines) {
			if (s != null) {
				if (player.model.getWorldBound().intersects(s.getWorldBound())) {
					PlayerMineEvent collisionEvent = new PlayerMineEvent(s, sounds);
					eventMgr.triggerEvent(collisionEvent);
					//explosionSound.play(100, false);
			    	System.out.println(this.removeGameWorldObject(s));
			    	mines.removeChild(s);
			    	break;
				}
			}
		}

		if (thisClient.entity != null)
			if (player.model.getWorldBound().intersects(thisClient.entity.model.getWorldBound())) {
				player.model.translate(0, 1, 0);
				PlayerMineEvent collisionEvent = new PlayerMineEvent(player.model, sounds);
				eventMgr.triggerEvent(collisionEvent);
			}

		// tell BaseGame to update game world state
		super.update(elapsedTimeMS);

	}

	private void initScript() {
		factory = new ScriptEngineManager();
		scriptFileName = "init.js";
		jsEngine = factory.getEngineByName("js");

	}

	private void initNetwork() {
		try {
			thisClient = new GameClientTCP(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (thisClient != null) {
			thisClient.sendJoinMessage(playerAvatar);
		}
	}

	private void initDisplay() {
		display = getDisplaySystem();
		System.out.println(display.getRenderer().toString());
		display.setTitle("CSC165 Project#3");
		renderer = display.getRenderer();
	}

	private void initActions() {
		im = getInputManager();
		
		String gpName = im.getFirstGamepadName();
		String kbName = im.getKeyboardName();
		String msName = im.getMouseName();
		
		QuitGameAction escQuit = new QuitGameAction(this);
		IAction mvForward = new MoveForward(player.model, speed, hillTerrain);
		IAction mvBack = new MoveBack(player.model, speed, hillTerrain);
		IAction mvRight = new MoveRight(player.model, speed, hillTerrain);
		IAction mvLeft = new MoveLeft(player.model, speed, hillTerrain);
		
		IAction mvXAxis = new MoveXAxis(player.model, speed, hillTerrain);
		IAction mvZAxis = new MoveZAxis(player.model, speed, hillTerrain);
		
		if(gpName == null){
			//Mouse Bindings
			playerCam = new ThirdPersonOrbitCameraController(camera, player.model, im, msName);
			playerCam.enableSnapback();
			im.associateAction(msName, net.java.games.input.Component.Identifier.Button.MIDDLE, mvForward,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
		else{

			// Gamepad Bindings
			playerCam = new ThirdPersonOrbitCameraController(camera, player.model, im, gpName);
			playerCam.enableSnapback();
			im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, mvXAxis,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, mvZAxis,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		}
		
		if(kbName != null){
			
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
		}

		// List out controllers
		//f = new FindComponents();
		//f.listControllers();
	}

	private void initPlayer() {
		//TODO: Replace with a local avatar class, add a variable containing the local avatar class to ghostavatar.
		player = new Entity(UUID.fromString("00000000-0000-0000-0000-000000000000")
								, new Vector3D(0,0,0), 180, playerAvatar, display);
		//player.model.translate(0,0,0);
		if (hostingStatus == 'h') {
			player.model.translate((float) player1Loc.getX(), (float) player1Loc.getY(), (float) player1Loc.getZ());
		} else {
			player.model.translate((float) player2Loc.getX(), (float) player2Loc.getY(), (float) player2Loc.getZ());
		}

		//Set initial vertical position
		Point3D avLoc = new Point3D(player.model.getLocalTranslation().getCol(3));
		float x = (float) avLoc.getX();
		float z = (float) avLoc.getZ();
		float terHeight = hillTerrain.getHeight(x, z);
		float desiredHeight = terHeight + (float) hillTerrain.getOrigin().getY() + 0.1f;
		player.model.getLocalTranslation().setElementAt(1, 3, desiredHeight);

		addGameWorldObject(player.model);

		camera = new JOGLCamera(renderer);
		camera.setPerspectiveFrustum(60, 1, 0.01, 10000);
		//camera.setViewDirection(new Vector3D(-1, 0, 1));
		//camera.setViewport(0.0, 1.0, 0.0, 0.45);

		initHUD();
	}

	private void initHUD() {
		HUDString playerID;
		if (playerAvatar == 'h')
			playerID = new HUDString("Hero");
		else
			playerID = new HUDString("Robot");

		playerID.setName("Player1ID");
		playerID.setLocation(0.01, 0.1);
		playerID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		playerID.setColor(Color.YELLOW);
		playerID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera.addToHUD(playerID);

		timeString = new HUDString("Time = " + time);
		timeString.setLocation(0, 0.05); // (0,0) [lower-left] to (1,1)
		camera.addToHUD(timeString);

		HUDString mineNum = new HUDString(mineCount + " Mines");
		mineNum.setLocation(0.05, 0.15);
		camera.addToHUD(mineNum);
	}

	private void initGameObjects() {
		//#ifndef TESTONSINGLECOMPUTER

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

		/*
		Point3D origin = new Point3D(0, 0, 0);
		Point3D xEnd = new Point3D(100, 0, 0);
		Point3D yEnd = new Point3D(0, 100, 0);
		Point3D zEnd = new Point3D(0, 0, 100);
		Line xAxis = new Line(origin, xEnd, Color.red, 2);
		Line yAxis = new Line(origin, yEnd, Color.green, 2);
		Line zAxis = new Line(origin, zEnd, Color.blue, 2);
		
		Line xAxis = (Line) jsEngine.get("xAxis");
		addGameWorldObject(xAxis);
		Line yAxis = (Line) jsEngine.get("yAxis");
		addGameWorldObject(yAxis);
		Line zAxis = (Line) jsEngine.get("zAxis");
		addGameWorldObject(zAxis);
		*/

		//TODO: Remove demo sphere, add objects for game.
		//Valid range for players is 5,0,5 to 285,0,285.

		this.executeScript(jsEngine, scriptFileName);

		mineCount = (int) jsEngine.get("mineCount");
		mineDistance = (int) jsEngine.get("mineDistance");
		
		player1Loc = (Point3D) jsEngine.get("player1Loc");
		player2Loc = (Point3D) jsEngine.get("player2Loc");
		crashPod = new Cylinder(2, 1, 16, 16);
		crashPod.setColor(Color.white);
		crashPod.setSolid(true);

		Matrix3D translateM = new Matrix3D();
		if (hostingStatus == 'h') {
			translateM.translate((float) player2Loc.getX(), 25f, (float) player2Loc.getZ());
		} else {
			translateM.translate((float) player1Loc.getX(), 25f, (float) player1Loc.getZ());
		}
		crashPod.setLocalTranslation(translateM);

		addGameWorldObject(crashPod);

		crashPod.updateGeometricState(1.0f, true);

		float mass = 1.0f;
		crashPodP = physicsEngine.addSphereObject(physicsEngine.nextUID(), mass,
				crashPod.getWorldTransform().getValues(), 1.0f);
		crashPodP.setBounciness(1.0f);
		crashPod.setPhysicsObject(crashPodP);
	}

	private void initNPC() {
		/*OBJLoader loader = new OBJLoader();
		
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
		heroNPC.setRenderState(heroState);*/

		mine = new NPC[mineCount];
		mineLoc = new Point3D[mineCount];
		npcCtrl = new NPCcontroller[mineCount];

		mines = new Group("root");
		Random randomGenerator = new Random();

		for (int i = 0; i < mineCount; i++) {

			mineLoc[i] = new Point3D(randomGenerator.nextInt(285 + 1) + 5, 0, randomGenerator.nextInt(285 + 1) + 5);

			mine[i] = new NPC();
			mine[i].translate((float) mineLoc[i].getX(), (float) mineLoc[i].getY(), (float) mineLoc[i].getZ());
			npcCtrl[i] = new NPCcontroller(this, mine[i]);
			npcCtrl[i].startNPControl();

			mine[i].updateLocalBound();
			mine[i].updateWorldBound();
			mine[i].setName("mine " + i);

			eventMgr.addListener(mine[i], PlayerMineEvent.class);
			mines.addChild(mine[i]);
		}

		// Add NPCs to world
		addGameWorldObject(mines);
		//Can't scale a physicsObject so mines won't have phyiscs.
		/*
		mine.updateGeometricState(1.0f, true);
		
		//Add NPC Physics
		float mass = 5.0f;
		//TODO: Experimentally determine best radius and height values.
		mineP = physicsEngine.addSphereObject(physicsEngine.nextUID(), 
				mass, mine.getWorldTransform().getValues(), 1.0f);
		mineP.setBounciness(0.5f);
		mine.setPhysicsObject(mineP);
		*/
		//Add NPC AI
		//TODO: Move all npc code to separate npc class.
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
		float up[] = { 0, 1, 0 };
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
		float[] gravity = { 0, -1f, 0 };
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

	private void initAudio() {
		AudioResource resource1, resource2, resource3, resource4;
		sounds = new ArrayList<Sound>();
		audioMgr = AudioManagerFactory.createAudioManager("sage.audio.joal.JOALAudioManager");
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}
		resource1 = audioMgr.createAudioResource("./sounds/beep.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("./sounds/FamiliarRoads.wav", AudioResourceType.AUDIO_STREAM);
		resource3 = audioMgr.createAudioResource("./sounds/explosion.wav", AudioResourceType.AUDIO_SAMPLE);
		resource4 = audioMgr.createAudioResource("./sounds/victory.wav", AudioResourceType.AUDIO_SAMPLE);
		//npcSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		
		// Attach Sounds to In-Game Variables
		beepSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		bgSound = new Sound(resource2, SoundType.SOUND_EFFECT, 60, true);
		explosionSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, true);
		victorySound = new Sound(resource4, SoundType.SOUND_EFFECT, 100, true);

		// Add sounds to array
		sounds.add(beepSound);
		sounds.add(bgSound);
		sounds.add(explosionSound);
		sounds.add(victorySound);

		//npcSound.initialize(audioMgr);
		beepSound.initialize(audioMgr);
		explosionSound.initialize(audioMgr);
		bgSound.initialize(audioMgr);
		victorySound.initialize(audioMgr);
		//npcSound.setMaxDistance(4.0f);
		//npcSound.setMinDistance(1.0f);
		//npcSound.setRollOff(5.0f);
		explosionSound.setLocation(new Point3D(0, 0, 0));
		explosionSound.setMaxDistance(50.0f);
		explosionSound.setMinDistance(3.0f);
		explosionSound.setRollOff(5.0f);

		bgSound.setLocation(new Point3D(0, 0, 0));
		bgSound.setMaxDistance(50.0f);
		bgSound.setMinDistance(3.0f);
		bgSound.setRollOff(5.0f);

		victorySound.setLocation(new Point3D(0, 0, 0));
		victorySound.setMaxDistance(50.0f);
		victorySound.setMinDistance(3.0f);
		victorySound.setRollOff(5.0f);
		//npcSound.setLocation(new Point3D(5,0,5));
		setEarParameters();
		//npcSound.play();
		bgSound.play(100, true);
	}

	public void setEarParameters() {
		Matrix3D avDir = (Matrix3D) (player.model.getWorldRotation().clone());
		float camAz = playerCam.getAzimuth();
		avDir.rotateY(180.0f - camAz);
		Vector3D camDir = new Vector3D(0, 0, 1);
		camDir = camDir.mult(avDir);
		audioMgr.getEar().setLocation(camera.getLocation());
		audioMgr.getEar().setOrientation(camDir, new Vector3D(0, 1, 0));
	}

	private char selectAvatar() {
		Scanner s = new Scanner(System.in);
		System.out.print("What avatar would you like? (h for hero, r for robot)");
		String avatar = s.nextLine();
		s.close();
		if (avatar.charAt(0) == 'h')
			return 'h';
		else if (avatar.charAt(0) == 'r')
			return 'r';
		else
			return 0;
	}

	private void executeScript(ScriptEngine engine, String scriptFileName) {
		try {
			FileReader fileReader = new FileReader(scriptFileName);
			engine.eval(fileReader);
			fileReader.close();
		} catch (FileNotFoundException e1) {
			System.out.println(scriptFileName + " not found " + e1);
		} catch (IOException e2) {
			System.out.println("IO problem with " + scriptFileName + e2);
		} catch (ScriptException e3) {
			System.out.println("ScriptException in " + scriptFileName + e3);
		} catch (NullPointerException e4) {
			System.out.println("Null ptr exception in " + scriptFileName + e4);
		}
	}

	private IDisplaySystem createDisplaySystem() {
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

	protected void render() {
		renderer.setCamera(camera);
		super.render();
	}

	public void start() {
		super.start();
	}

	protected void shutdown() {
		super.shutdown();
		if (thisClient != null) {
			thisClient.sendByeMessage();
			try {
				thisClient.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		display.close();
	}

	public void setIsConnected(boolean flag) {
		isConnected = flag;
	}

	public Vector3D getPlayerPosition() {
		return player.model.getLocalTranslation().getCol(3);
	}

	public void addGameWorldObject(SceneNode s) {
		super.addGameWorldObject(s);
	}

	public boolean removeGameWorldObject(SceneNode s) {
		return super.removeGameWorldObject(s);
	}

	public void checkAvatarNear(NPCcontroller npcc, Vector3D npcP) {
		boolean isNear = false;
		Vector3D avLoc = player.model.getLocalTranslation().getCol(3);

		if (Math.abs(npcP.getX() - avLoc.getX()) <= mineDistance
				&& Math.abs(npcP.getZ() - avLoc.getZ()) <= mineDistance) {
			isNear = true;
		}

		if (thisClient != null && thisClient.entity != null) {
			Vector3D ghostLoc = thisClient.entity.model.getLocalTranslation().getCol(3);
			if (Math.abs(npcP.getX() - ghostLoc.getX()) <= mineDistance
					&& Math.abs(npcP.getZ() - ghostLoc.getZ()) <= mineDistance) {
				isNear = true;
			}
		}

		npcc.setNearFlag(isNear);
	}
}
