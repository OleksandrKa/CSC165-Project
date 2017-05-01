package MyGameEngine;

import java.awt.Canvas;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import sage.display.DisplaySystem;
import sage.display.IDisplaySystem;
import sage.renderer.IRenderer;
import sage.renderer.RendererFactory;
import csc165_lab3.*;

public class MyDisplaySystem implements IDisplaySystem {
	private JFrame myFrame;
	private GraphicsDevice device;
	private IRenderer myRenderer;
	private boolean isCreated;
	private int width, height, bitDepth, refreshRate;
	private Canvas rendererCanvas;
	private boolean isFullScreen;

	public MyDisplaySystem(int w, int h, int depth, int rate, boolean isFS, String rName) {
		width = w;
		height = h;
		bitDepth = depth;
		refreshRate = rate;
		this.isFullScreen = isFS;
		// get a renderer from the RendererFactory
		myRenderer = RendererFactory.createRenderer(rName);
		if (myRenderer == null) {
			throw new RuntimeException("Unable to find renderer");
		}
		rendererCanvas = myRenderer.getCanvas();
		myFrame = new JFrame("SpaceFarming3D - Part 2!");
		myFrame.add(rendererCanvas);
		// initialize the screen with the specified parameters
		DisplayMode displayMode = new DisplayMode(width, height, bitDepth, refreshRate);
		initScreen(displayMode, isFullScreen);
		// save DisplaySystem, show frame and indicate DisplaySystem is created
		DisplaySystem.setCurrentDisplaySystem(this);
		myFrame.setVisible(true);
		isCreated = true;
	}

	private void initScreen(DisplayMode dispMode, boolean FSRequested) {
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		device = environment.getDefaultScreenDevice();
		if (device.isFullScreenSupported() && FSRequested) {
			myFrame.setUndecorated(true); // suppress title bar, borders, etc.
			myFrame.setResizable(false); // full-screen so not resizeable
			myFrame.setIgnoreRepaint(true); // ignore AWT repaints
			// Put device in full-screen mode. This must be done before
			// attempting
			// to change the DisplayMode; the application must first have FSEM
			device.setFullScreenWindow(myFrame);
			// try to set the full-screen device DisplayMode
			if (dispMode != null && device.isDisplayChangeSupported()) {
				try {
					device.setDisplayMode(dispMode);
					myFrame.setSize(dispMode.getWidth(), dispMode.getHeight());
				} catch (Exception ex) {
					System.err.println("Exception setting DisplayMode: " + ex);
				}
			} else {
				System.err.println("Cannot set display mode");
			}
		} else { // use windowed mode – set JFrame characteristics
			myFrame.setSize(dispMode.getWidth(), dispMode.getHeight());
			myFrame.setLocationRelativeTo(null); // centers window on screen

		}
	}

	@Override
	public void addKeyListener(KeyListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMouseListener(MouseListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMouseMotionListener(MouseMotionListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void convertPointToScreen(Point arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBitDepth() {
		// TODO Auto-generated method stub
		return this.bitDepth;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return this.height;
	}

	@Override
	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return this.refreshRate;
	}

	@Override
	public IRenderer getRenderer() {
		// TODO Auto-generated method stub
		return this.myRenderer;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return this.width;
	}

	@Override
	public boolean isCreated() {
		// TODO Auto-generated method stub
		return isCreated;
	}

	@Override
	public boolean isFullScreen() {
		// TODO Auto-generated method stub
		return isFullScreen;
	}

	@Override
	public boolean isShowing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBitDepth(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCustomCursor(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeight(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPredefinedCursor(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRefreshRate(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitle(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setWidth(int arg0) {
		// TODO Auto-generated method stub

	}
}