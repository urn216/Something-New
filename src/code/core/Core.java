package code.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import code.math.IOHelp;
import code.math.Vector2;

import code.ui.UIController;
import code.ui.UICreator;
import code.ui.UIState;

import code.world.Camera;

//import java.util.*;
//import java.awt.Color;
//import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.Insets;
//import java.awt.Toolkit;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;

enum State {
  MAINMENU,
  TRANSITION,
  RUN,
  PAUSE,
  DEATH
}

/**
* Core class for the currently unnamed game
*/
public class Core extends JPanel {
  private static final long serialVersionUID = 1;

  public static final Vector2 DEFAULT_SCREEN_SIZE = new Vector2(1920, 1080);
  public static final String BLACKLISTED_CHARS = "/\\.?!*\n";

  private JFrame f;
  private boolean maximized = true;

  private boolean quit = false;

  private int toolBarLeft, toolBarRight, toolBarTop, toolBarBot;

  private boolean[] keyDown = new boolean[65536];
  private boolean[] mouseDown = new boolean[4];
  private Vector2 mousePos;

  private final UIController uiCon;

  private Scene current;
  private Scene previous;

  private String saveName;
  private String newSaveName = null;

  private Camera cam;
  private int screenSizeX;
  private int screenSizeY;
  private int smallScreenX = (int)DEFAULT_SCREEN_SIZE.x;
  private int smallScreenY = (int)DEFAULT_SCREEN_SIZE.y;

  private static final double TICKS_PER_SECOND = 30;
  private static final double MILLISECONDS_PER_TICK = 1000/TICKS_PER_SECOND;

  private long pFTime = System.currentTimeMillis();
  private double fps = 0;
  private int fCount = 0;

  private String TransitionName;

  private int sceneTransitionCounter;
  private final int SceneTransitionLimit = 30;
  private boolean SceneTransitionFirstHalf = false;

  // int pauseCool = 0;
  private int deathTime = 0;

  /** Current game state */
  private State state = State.MAINMENU;

  public final Settings globalSettings;

  /**
  * Main method. Called on execution. Performs basic startup
  *
  * @param args Ignored for now
  */
  public static void main(String[] args) {
    Core core = new Core();
    core.playGame();
  }

  /**
  * Performs initialisation of the program. Only to be run on startup
  */
  public Core() {
    f = new JFrame("Game o Fun");
    f.getContentPane().add(this);
    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    f.setResizable(true);
    BufferedImage image = IOHelp.readImage("icon.png");
    f.setIconImage(image);
    f.addWindowListener( new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        quit = true;
      }
    });
    f.addComponentListener( new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        screenSizeX = f.getWidth() - toolBarLeft - toolBarRight;
        screenSizeY = f.getHeight() - toolBarTop - toolBarBot;
        if (cam != null) {cam.setScreenSize(screenSizeX, screenSizeY);}
        // System.out.println(screenSizeX + ", " + screenSizeY);
      }
    });
    f.setExtendedState(JFrame.MAXIMIZED_BOTH);
    f.setUndecorated(true);
    f.setVisible(true);
    f.requestFocus();
    globalSettings = new Settings();
    screenSizeX = f.getWidth();
    screenSizeY = f.getHeight();

    uiCon = new UIController(globalSettings);

    uiCon.putPane("Main Menu", UICreator.createMain(this, uiCon));
    uiCon.putPane("HUD"      , UICreator.createHUD(this, uiCon) );

    TransitionName = "Title";
    current = new Scene(TransitionName, saveName);
    cam = new Camera(new Vector2(), new Vector2(), 1, screenSizeX, screenSizeY);

    initialiseControls();

    transition();
  }

  /**
  * Switches the current scene to a new one via the new scene's name
  *
  * @param name The name of the scene to switch to
  */
  public void toScene(String name) {
    uiCon.transOut();
    TransitionName = name;
    sceneTransitionCounter = 0;
    state = State.TRANSITION;
    SceneTransitionFirstHalf = true;
  }

  /**
  * A two part method, which provides a smooth transition between two scenes. Upon first call, deloads the previous scene.
  * Upon second call, loads in the new one designated by the toScene method
  */
  private void transition() {
    if (SceneTransitionFirstHalf) {
      Scene temp = previous;
      previous = current;
      if (temp != null && temp.equals(TransitionName)) {current = temp; current.reset();}
      else {current = new Scene(TransitionName, saveName);}
      cam = new Camera(new Vector2(32, 32), new Vector2(), 2, screenSizeX, screenSizeY);
      cam.setZoom(0);
      sceneTransitionCounter = 0;
      SceneTransitionFirstHalf = false;
    }
    else {
      cam.setZoom(cam.getDZoom());
      if (TransitionName.equals("Title")) {uiCon.setCurrent("Main Menu"); state = State.MAINMENU; cam.setTarget(new Vector2(0,0));}
      else {uiCon.setCurrent("HUD"); state = State.RUN; cam.setTarU(current.getPlayer());}
      // uiCon.setMode("Default");
    }
  }

  /**
  * A helper method that updates the window insets to match their current state
  */
  private void updateInsets() {
    Insets i = f.getInsets(); //Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration())
    // System.out.println(i);
    toolBarLeft = i.left;
    toolBarRight = i.right;
    toolBarTop = i.top;
    toolBarBot = i.bottom;
  }

  /**
  * A helper method that toggles fullscreen for the window
  */
  public void doFull() {
    f.removeNotify();
    if (maximized) {
      f.setExtendedState(JFrame.NORMAL);
      f.setUndecorated(false);
      f.addNotify();
      updateInsets();
      f.setSize(smallScreenX + toolBarLeft + toolBarRight, smallScreenY + toolBarTop + toolBarBot);
    }
    else {
      smallScreenX = screenSizeX;
      smallScreenY = screenSizeY;
      f.setVisible(false);
      f.setExtendedState(JFrame.MAXIMIZED_BOTH);
      f.setUndecorated(true);
      f.setVisible(true);
      updateInsets();
      f.addNotify();
    }
    f.requestFocus();
    maximized = !maximized;
  }

  /**
  * A helper method that toggles whether or not the game is paused
  */
  private void pause() {
    if (state != State.PAUSE && state != State.RUN) return;

    state = uiCon.getMode()!=UIState.DEFAULT ? State.PAUSE : State.RUN;
  }

  /**
  * Returns the scene to the last played save game
  */
  public void lastSave() {
    saveName = "Test Save";
    toScene("Achoo");
  }

  /**
  * Sets a flag to start up a new game in the save game slot designated by 'newSaveName' and switch to the opening scene
  *
  * @param newSaveName the name of the new save game.
  */
  public void newGame(String newSaveName) {this.newSaveName = newSaveName;}

  /**
  * Starts up a new game in the current save game slot designated by 'saveName' and switches to the opening scene
  */
  private void newGame() {
    IOHelp.createDir("../saves/"+saveName);
    IOHelp.saveToFile("../saves/"+saveName+"/saveInfo", saveName+"\n"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    previous = null;
    try {
      Thread.sleep(100);
    } catch(InterruptedException e){Thread.currentThread().interrupt();}
    toScene("Achoo");
  }

  /**
  * Returns the scene to the main menu state
  */
  public void quitToMenu() {
    saveName = null;
    toScene("Title");
  }

  /**
  * Sets the flag to quit the game at the nearest convenience
  */
  public void quitToDesk() {
    quit = true;
  }

  /**
  * Main loop. Should always be running. Runs the rest of the game engine
  */
  private void playGame() {
    while (true) {
      long tickTime = System.currentTimeMillis();
      switch (state) {
        case MAINMENU:
        if (newSaveName!=null) {
          saveName = newSaveName;
          newSaveName = null;
          newGame();
        }
        break;

        case RUN:
        cam.follow();
        current.update(keyDown, mouseDown, new Vector2 ((mousePos.x+cam.conX())/cam.getZoom(), (mousePos.y+cam.conY())/cam.getZoom()));
        if (!current.getPlayer().isAlive()) {state = State.DEATH;}
        break;

        case PAUSE:
        break;

        case DEATH:
        if (deathTime%2==0) {
          cam.follow();
          current.update(keyDown, mouseDown, new Vector2 ((mousePos.x+cam.conX())/cam.getZoom(), (mousePos.y+cam.conY())/cam.getZoom()));
        }
        if (deathTime >= 120) {
          deathTime = 0;
          current.reset();
          cam.setTarU(current.getPlayer());
          state = State.RUN;
        }
        deathTime++;
        break;

        case TRANSITION:
        cam.follow();
        sceneTransitionCounter++;
        if (SceneTransitionFirstHalf) {cam.setZoom(cam.getZoom()*2/3);}
        else {cam.setZoom((Math.pow(2, SceneTransitionLimit-sceneTransitionCounter)/Math.pow(3, SceneTransitionLimit-sceneTransitionCounter))*cam.getDZoom());}
        if (sceneTransitionCounter >= SceneTransitionLimit) {transition();}
        break;
      }
      repaint();
      if (quit) {
        System.exit(0);
      }
      tickTime = System.currentTimeMillis() - tickTime;
      try {
        Thread.sleep(Math.max((long)(MILLISECONDS_PER_TICK - tickTime), 0));
      } catch(InterruptedException e){System.out.println(e); System.exit(0);}
    }
  }

  public void paintComponent(Graphics gra) {
    Graphics2D g = (Graphics2D) gra;

    if (fCount >= 60) {
      long cFTime = System.currentTimeMillis();
      fps = fCount*1000.0/(cFTime-pFTime);
      pFTime = cFTime;
      fCount=0;
    }
    fCount++;

    switch (state) {
      case MAINMENU:
      if (current != null) {current.draw(g, cam, false);}
      uiCon.draw(g, screenSizeX, screenSizeY);
      break;
      case TRANSITION:
      current.draw(g, cam, true);
      uiCon.draw(g, screenSizeX, screenSizeY);
      break;
      case RUN:
      current.draw(g, cam, true);
      uiCon.draw(g, screenSizeX, screenSizeY, current.getPlayer().getStats(fps));
      break;
      case PAUSE:
      current.draw(g, cam, true);
      uiCon.draw(g, screenSizeX, screenSizeY, current.getPlayer().getStats(fps));
      break;
      case DEATH:
      current.draw(g, cam, true);
      uiCon.draw(g, screenSizeX, screenSizeY, current.getPlayer().getStats(cam.getZoom()));
      break;
    }
  }

  /**
  * Starts up all the listeners for the window. Only to be called once on startup.
  */
  private void initialiseControls() {

    //Mouse Controls
    f.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int x = e.getX() - toolBarLeft;
        int y = e.getY() - toolBarTop;
        mousePos = new Vector2(x, y);
        uiCon.cursorMove(x, y);
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        int x = e.getX() - toolBarLeft;
        int y = e.getY() - toolBarTop;
        mousePos = new Vector2(x, y);
        uiCon.cursorMove(x, y);
        uiCon.useSlider(x);
      }
    });
    f.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int x = e.getX() - toolBarLeft;
        int y = e.getY() - toolBarTop;
        mousePos = new Vector2(x, y);
        if (uiCon.getHighlighted() == null) mouseDown[e.getButton()] = true;
        if (e.getButton() == 1) {
          uiCon.cursorMove(x, y);
          uiCon.press();
        }
      }

      @Override
      public void mouseReleased(MouseEvent e){
        int x = e.getX() - toolBarLeft;
        int y = e.getY() - toolBarTop;
        mouseDown[e.getButton()] = false;
        if (e.getButton() == 1) {
          uiCon.cursorMove(x, y);
          uiCon.release();
          pause();
        }
      }
    });

    //Keyboard Controls
    f.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (uiCon.getActiveTextfield() != null && !keyDown[KeyEvent.VK_CONTROL]) uiCon.typeKey(e);

        if(keyDown[keyCode]) return; //Key already in
        keyDown[keyCode] = true;

        // System.out.print(keyCode);
        if (keyCode == KeyEvent.VK_F11) {
          doFull();
        }
        if (state == State.TRANSITION) return;
        if (keyCode == KeyEvent.VK_ESCAPE) {
          uiCon.back();
          pause();
        }
        if (keyCode == KeyEvent.VK_ENTER) {
          uiCon.press();
        }
        if (keyCode == KeyEvent.VK_MINUS) {
          cam.setZoom(cam.getZoom()/2);
        }
        if (keyCode == KeyEvent.VK_EQUALS) {
          cam.setZoom(cam.getZoom()*2);
        }
      }

      @Override
      public void keyReleased(KeyEvent e){
        int keyCode = e.getKeyCode();
        keyDown[keyCode] = false;

        if (keyCode == KeyEvent.VK_ENTER) {
          uiCon.release();
          pause();
        }
      }
    });
  }
}
