package code.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import mki.io.FileIO;

import mki.math.vector.Vector2;

import mki.ui.control.UIController;
import mki.ui.control.UIState;

import code.world.Camera;
import code.world.fixed.Decal;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

enum State {
  MAINMENU,
  TRANSITION,
  RUN,
  PAUSE,
  DEATH, SPLASH
}

/**
* Core class for the currently unnamed game
*/
public abstract class Core {
  
  public static final Window WINDOW;
  
  public static final Settings GLOBAL_SETTINGS;
  
  public static final String BLACKLISTED_CHARS = "/\\.?!*\n";

  private static boolean quit = false;

  private static Scene currentScene;
  private static Scene previousScene;

  private static String saveName;
  private static String newSaveName = null;

  private static Camera cam;

  private static final double TICKS_PER_SECOND = 30;
  private static final double MILLISECONDS_PER_TICK = 1000/TICKS_PER_SECOND;
  
  private static final long START_TIME = System.currentTimeMillis();
  private static final int SPLASH_TIME = 1000;
  
  private static final Decal SPLASH;

  private static String TransitionName;

  private static int sceneTransitionCounter;
  private static final int SceneTransitionLimit = 30;
  private static boolean SceneTransitionFirstHalf = false;

  // int pauseCool = 0;
  private static int deathTime = 0;

  /** Current game state */
  private static State state = State.SPLASH;

  /**
  * Main method. Called on execution. Performs basic startup
  *
  * @param args Ignored for now
  */
  public static void main(String[] args) {
    playGame();
  }

  /**
  * Performs initialisation of the program. Only to be run on startup
  */
  static {
    WINDOW = new Window();

    GLOBAL_SETTINGS = new Settings();

    SPLASH = new Decal(WINDOW.screenWidth()/2, WINDOW.screenHeight()/2, "splash.png", false, null);
    WINDOW.FRAME.setBackground(new Color(173, 173, 173));
    cam = new Camera(new Vector2(), new Vector2(), 0);
    
    UIController.putPane("Main Menu", UICreator.createMain());
    UIController.putPane("HUD"      , UICreator.createHUD ());
  }
  
  /**
  * @return the currently active scene
  */
  public static Scene getCurrentScene() {
    return currentScene;
  }
  
  /**
  * @return the currently active camera
  */
  public static Camera getActiveCam() {
    return cam;
  }

  /**
  * Switches the current scene to a new one via the new scene's name
  *
  * @param name The name of the scene to switch to
  */
  public static void toScene(String name) {
    UIController.transOut();
    TransitionName = name;
    sceneTransitionCounter = 0;
    state = State.TRANSITION;
    SceneTransitionFirstHalf = true;
  }

  /**
  * A two part method, which provides a smooth transition between two scenes. Upon first call, deloads the previous scene.
  * Upon second call, loads in the new one designated by the toScene method
  */
  private static void transition() {
    if (SceneTransitionFirstHalf) {
      Scene temp = previousScene;
      previousScene = currentScene;
      if (temp != null && temp.equals(TransitionName)) {currentScene = temp; currentScene.reset();}
      else {currentScene = new Scene(TransitionName, saveName);}
      cam = new Camera(new Vector2(32, 32), new Vector2(), 2);
      cam.setZoom(0);
      sceneTransitionCounter = 0;
      SceneTransitionFirstHalf = false;
    }
    else {
      cam.setZoom(cam.getDZoom());
      if (TransitionName.equals("Title")) {UIController.setCurrentPane("Main Menu"); state = State.MAINMENU; cam.setTarget(new Vector2(0,0));}
      else {UIController.setCurrentPane("HUD"); state = State.RUN; cam.setTarU(currentScene.getPlayer());}
      // uiCon.setMode("Default");
    }
  }

  /**
  * A helper method that toggles whether or not the game is paused
  */
  public static void pause() {
    if (state != State.PAUSE && state != State.RUN) return;

    state = UIController.getState()!=UIState.DEFAULT ? State.PAUSE : State.RUN;
  }

  public static boolean controlsDisabled() {
    return state == State.TRANSITION || state == State.SPLASH;
  }

  /**
  * Returns the scene to the last played save game
  */
  public static void lastSave() {
    saveName = "Test Save";
    toScene("Achoo");
  }

  /**
  * Sets a flag to start up a new game in the save game slot designated by 'newSaveName' and switch to the opening scene
  *
  * @param newSaveName the name of the new save game.
  */
  public static void newGame(String newSaveName) {if (!newSaveName.matches(BLACKLISTED_CHARS)) Core.newSaveName = newSaveName;}

  /**
  * Starts up a new game in the current save game slot designated by 'saveName' and switches to the opening scene
  */
  private static void newGame() {
    FileIO.createDir("../saves/"+saveName);
    FileIO.saveToFile("../saves/"+saveName+"/saveInfo", saveName+"\n"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    previousScene = null;
    try {
      Thread.sleep(100);
    } catch(InterruptedException e){Thread.currentThread().interrupt();}
    toScene("Achoo");
  }

  /**
  * Returns the scene to the main menu state
  */
  public static void quitToMenu() {
    saveName = null;
    toScene("Title");
  }

  /**
  * Sets the flag to quit the game at the nearest convenience
  */
  public static void quitToDesk() {
    quit = true;
  }

  /**
  * Main loop. Should always be running. Runs the rest of the game engine
  */
  private static void playGame() {
    while (true) {
      long tickTime = System.currentTimeMillis();
      switch (state) {
        case SPLASH:
        if (tickTime-START_TIME >= SPLASH_TIME) {
          Controls.initialiseControls(WINDOW.FRAME);
          quitToMenu();
        }
        break;
        case MAINMENU:
        if (newSaveName!=null) {
          saveName = newSaveName;
          newSaveName = null;
          newGame();
        }
        break;

        case RUN:
        cam.follow();
        currentScene.update(Controls.KEY_DOWN, Controls.MOUSE_DOWN, new Vector2 ((Controls.mousePos.x+cam.conX())/cam.getZoom(), (Controls.mousePos.y+cam.conY())/cam.getZoom()));
        if (!currentScene.getPlayer().isAlive()) {state = State.DEATH;}
        break;

        case PAUSE:
        break;

        case DEATH:
        if (deathTime%2==0) {
          cam.follow();
          currentScene.update(Controls.KEY_DOWN, Controls.MOUSE_DOWN, new Vector2 ((Controls.mousePos.x+cam.conX())/cam.getZoom(), (Controls.mousePos.y+cam.conY())/cam.getZoom()));
        }
        if (deathTime >= 120) {
          deathTime = 0;
          currentScene.reset();
          cam.setTarU(currentScene.getPlayer());
          state = State.RUN;
        }
        deathTime++;
        break;

        case TRANSITION:
        cam.follow();
        sceneTransitionCounter++;
        if (SceneTransitionFirstHalf) {cam.setZoom(cam.getZoom()*2.0/3.0);}
        else {cam.setZoom((Math.pow(2.0, SceneTransitionLimit-sceneTransitionCounter)/Math.pow(3.0, SceneTransitionLimit-sceneTransitionCounter))*cam.getDZoom());}
        if (sceneTransitionCounter >= SceneTransitionLimit) {transition();}
        break;
      }
      if (quit) {
        System.exit(0);
      }
      WINDOW.PANEL.repaint();
      tickTime = System.currentTimeMillis() - tickTime;
      try {
        Thread.sleep(Math.max((long)(MILLISECONDS_PER_TICK - tickTime), 0));
      } catch(InterruptedException e){System.out.println(e); System.exit(0);}
    }
  }

  public static void paintComponent(Graphics gra) {
    Graphics2D g = (Graphics2D) gra;

    switch (state) {
      case SPLASH:
      SPLASH.draw(g, cam);
      break;
      case MAINMENU:
      case TRANSITION:
      if (currentScene != null) {currentScene.draw(g, cam, true);}
      UIController.draw(g, WINDOW.screenWidth(), WINDOW.screenHeight());
      break;
      case RUN:
      case PAUSE:
      case DEATH:
      currentScene.draw(g, cam, true);
      UIController.draw(g, WINDOW.screenWidth(), WINDOW.screenHeight());
      break;
    }
  }
}
