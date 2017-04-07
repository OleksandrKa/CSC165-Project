package csc165_lab3;

import sage.app.AbstractGame;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.display.*;
import sage.display.DisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.IAction;
import sage.renderer.IRenderer;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.TriMesh;
import sage.scene.shape.*;
import sage.texture.Texture;
import sage.texture.TextureManager;
import sage.scene.Group;
import sage.scene.HUDString;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import sage.networking.IGameConnection.ProtocolType;
import csc165_lab3.*;
import java.util.UUID;

public class MyGameEngine extends BaseGame {
	final boolean testOnSingleComputerFlag = true;
	
	private int player1Score = 0, player2Score = 0;
	private float time = 0; // game elapsed time
	private float speed = 0.02f;
	private float angle = 0.008f;
	private int randX, randY;
	private Random ran;
	private HUDString player1ScoreHUD, player2ScoreHUD, timeString;
	private Sphere plant;
	private IEventManager eventMg;
	private OrbitCameraController player1Cam, player2Cam;
	private SceneNode player1, player2;
	private SkyBox skybox;
	private Group plants;
	private IRenderer renderer;
	DisplaySettingsDialog displayDialog;
	IDisplaySystem display;
	ICamera camera1, camera2;
	IInputManager im;
	FindComponents f;
	
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private GameClientTCP thisClient;
	private boolean isConnected;
	
	char playerAvatar;
	

	public MyGameEngine(String serverAddr, int sPort, char avatar){
		// assumes main() passes server address, port to this.
		super();
		this.serverAddress = serverAddr;
		this.serverPort = sPort;
		this.serverProtocol = ProtocolType.TCP;
		this.playerAvatar = avatar;
	}
	
	public void initGame() {
		display = getDisplaySystem();
		System.out.println(display.getRenderer().toString());
		display.setTitle("SpaceFarming3D - Part 2!");
		renderer = display.getRenderer();
		im = getInputManager();
		eventMg = EventManager.getInstance();
		initGameObjects();
		initPlayers();
		inputHandler();
		//items as before, plus initializing network:
		try{
			thisClient = new GameClientTCP(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		}
		catch(UnknownHostException e){ e.printStackTrace(); }
		catch(IOException e)  { e.printStackTrace(); }
		
		if(thisClient != null) { thisClient.sendJoinMessage(playerAvatar); }
	}

	protected void initSystem() {
		display = createDisplaySystem();
		setDisplaySystem(display);
		IInputManager inputManager = new InputManager();
		setInputManager(inputManager);
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}

	private void initPlayers() {
		player1 = new GhostAvatar(UUID.fromString("00000000-0000-0000-0000-000000000000")
								, new Vector3D(0,0,0), playerAvatar);
		player1.translate(0, 0, 0);
		player1.rotate(180, new Vector3D(0, 1, 0));
		addGameWorldObject(player1);

		camera1 = new JOGLCamera(renderer);
		//camera1.setPerspectiveFrustum(60, 2, 1, 1000);
		//camera1.setViewDirection(new Vector3D(-1, 0, 1));
		//camera1.setViewport(0.0, 1.0, 0.0, 0.45);

		player2 = new Cube("PLAYER2");
		player2.translate(50, 1, 0);
		player2.rotate(-90, new Vector3D(0, 1, 0));
		addGameWorldObject(player2);

		camera2 = new JOGLCamera(renderer);
		camera2.setPerspectiveFrustum(60, 2, 1, 1000);
		camera2.setViewDirection(new Vector3D(1, 0, 1));
		camera2.setViewport(0.0, 1.0, 0.55, 1.0);

		initPlayerHUDs();
	}

	private void initPlayerHUDs() {
		HUDString player1ID = new HUDString("Player1");
		player1ID.setName("Player1ID");
		player1ID.setLocation(0.01, 0.1);
		player1ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player1ID.setColor(Color.YELLOW);
		player1ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera1.addToHUD(player1ID);

		HUDString player2ID = new HUDString("Player2");
		player2ID.setName("Player2ID");
		player2ID.setLocation(0.01, 0.11);
		player2ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player2ID.setColor(Color.BLUE);
		player2ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera2.addToHUD(player2ID);

		timeString = new HUDString("Time = " + time);
		timeString.setLocation(0, 0.05); // (0,0) [lower-left] to (1,1)
											// addGameWorldObject(timeString);
		player1ScoreHUD = new HUDString("Score = " + player1Score); // default
																	// is (0,0)
		camera1.addToHUD(player1ScoreHUD);
		player2ScoreHUD = new HUDString("Score = " + player2Score); // default
																	// is (0,0)
		camera2.addToHUD(player2ScoreHUD);
		addGameWorldObject(timeString);
	}

	private void initGameObjects() {
		ran = new Random();

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
		
		plants = new Group("root");
		for (int i = 1; i <= 20; i++) {
			randX = ran.nextInt(62) + 0;
			randY = ran.nextInt(62) + 0;
			plant = new Sphere();
			plant.setColor(Color.GREEN);
			plant.translate(randX, 0, randY);
			plants.addChild(plant);
		}

		addGameWorldObject(plants);
		GrowController gc = new GrowController();
		//gc.addControlledNode(plants);
		//plants.addController(gc);

		/*float planeSize = 125.0f;
		Rectangle plane = new Rectangle(planeSize, planeSize);
		plane.setColor(Color.GRAY);
		plane.rotate(90, new Vector3D(1, 0, 0));
		addGameWorldObject(plane); */

		Point3D origin = new Point3D(0, 0, 0);
		Point3D xEnd = new Point3D(100, 0, 0);
		Point3D yEnd = new Point3D(0, 100, 0);
		Point3D zEnd = new Point3D(0, 0, 100);
		Line xAxis = new Line(origin, xEnd, Color.red, 2);
		Line yAxis = new Line(origin, yEnd, Color.green, 2);
		Line zAxis = new Line(origin, zEnd, Color.blue, 2);
		addGameWorldObject(xAxis);
		addGameWorldObject(yAxis);
		addGameWorldObject(zAxis);
	}

	// update is called by BaseGame once each time around game loop
	public void update(float elapsedTimeMS) {

		Point3D camLoc = camera1.getLocation();
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		//#ifndef TESTONSINGLECOMPUTER
		if(testOnSingleComputerFlag == false)
		skybox.setLocalTranslation(camTranslation);
		//#endif
		
		// Loops through SceneNodes
		for (SceneNode node : getGameWorld()) {
			// If object is a sphere (plant), then check if plant intersects
			// with a player
			if (node instanceof ICollectible) {
				ICollectible collectible = (ICollectible) node;
				if (collectible.worldBound().intersects(player1.getWorldBound())) {
					player1Score++;
					GatherEvent gather = new GatherEvent(player1Score);
					eventMg.triggerEvent(gather);
					removeGameWorldObject((SceneNode) collectible);
					break;
				}
				if (collectible.worldBound().intersects(player2.getWorldBound())) {
					player2Score++;
					GatherEvent collect = new GatherEvent(player2Score);
					eventMg.triggerEvent(collect);
					removeGameWorldObject((SceneNode) collectible);
					break;
				}
			}
		}

		// update the HUD
		time += elapsedTimeMS;
		DecimalFormat df = new DecimalFormat("0.0");
		timeString.setText("Time = " + df.format(time / 1000));

		// update 3P controllers
		player1Cam.update(elapsedTimeMS);
		//player2Cam.update(elapsedTimeMS);

		//same as before, plus process any packets received from server:
		if(thisClient != null) thisClient.processPackets();
		
		// tell BaseGame to update game world state
		super.update(elapsedTimeMS);
	}

	public void inputHandler() {
		//String gpName = im.getFirstGamepadName();
		String kbName = im.getKeyboardName();

		player1Cam = new OrbitCameraController(camera1, player1, im, kbName);
		//player2Cam = new OrbitCameraController(camera2, player2, im, gpName);

		// Gamepad Bindings
		IAction mvXAxis = new MoveXAxis(player1, speed);
		IAction mvZAxis = new MoveZAxis(player1, speed);

		/* im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, mvXAxis,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, mvZAxis,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN); */

		// Keyboard Bindings
		QuitGameAction escQuit = new QuitGameAction(this);
		IAction mvForward = new MoveForward(player1, speed);
		IAction mvBack = new MoveBack(player1, speed);
		IAction mvRight = new MoveRight(player1, speed);
		IAction mvLeft = new MoveLeft(player1, speed);

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
		renderer.setCamera(camera1);
		super.render();

		//renderer.setCamera(camera2);
		//super.render();
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
	}
	
	public void setIsConnected(boolean flag){
		isConnected = flag;
	}
	
	public Vector3D getPlayerPosition(){
		return new Vector3D(camera1.getLocation().getX(),camera1.getLocation().getY(),camera1.getLocation().getZ());
	}
	
	public void addGameWorldObject(SceneNode s){
		super.addGameWorldObject(s);
	}
	public boolean removeGameWorldObject(SceneNode s){
		return super.removeGameWorldObject(s);
	}
}