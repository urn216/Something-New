package code.core;

import mki.math.vector.Vector2;

import mki.ui.elements.*;
import mki.ui.components.*;
import mki.ui.components.interactables.*;
import mki.ui.control.UIAction;
import mki.ui.control.UIColours;
import mki.ui.control.UIController;
import mki.ui.control.UIHelp;
import mki.ui.control.UIPane;
import mki.ui.control.UIState;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class UICreator {
  // private static final UIElement VIRTUAL_KEYBOARD = new ElemKeyboard();
  
  private static final double COMPON_HEIGHT = 0.04;
  private static final double BUFFER_HEIGHT = 0.008;

  /**
  * Creates the UI pane for the main menu.
  */
  public static UIPane createMain() {
    UIPane mainMenu = new UIPane();

    UIElement title = new UIElement(
    new Vector2(0   , 0),
    new Vector2(0.3, 0.14),
    new boolean[]{true, false, true, false}
    ){
      protected void init() {components = new UIComponent[]{new UIText("A Game Title", 0.6, Font.BOLD)};}
      protected void draw(Graphics2D g, int screenSizeY, Vector2 tL, Vector2 bR, Color[] c, UIInteractable highlighted) {
        components[0].draw(g, (float)tL.x, (float)tL.y, (float)(bR.x-tL.x), (float)(bR.y-tL.y), c[UIColours.TEXT]);
      }
    };
    
    UIElement outPanel = leftMenu(
      new Vector2(0, 0.28), 
      0.1, 
      new UIButton("Continue"       , Core::lastSave),
      new UIButton("New Game"       , () -> UIController.setState(UIState.NEW_GAME)),
      new UIButton("Load Game"      , () -> UIController.setState(UIState.LOBBY)   ),
      new UIButton("Options"        , () -> UIController.setState(UIState.OPTIONS) ),
      new UIButton("Quit to Desktop", Core::quitToDesk)
    );

    UITextfield ng = new UITextfield("Save name...", 20, 1, Core::newGame, null, Core.BLACKLISTED_CHARS);
    UIElement newGame = leftMenu(
      new Vector2(0.4, 0.28), 
      0.2, 
      ng,
      new UIButton("Begin" , ng::enterAct      ),
      new UIButton("Cancel", UIController::back)
    );

    UIElement options = leftMenu(
      new Vector2(0, 0.28), 
      0.1, 
      new UIButton("Video"   , () -> UIController.setState(UIState.VIDEO)   ),
      new UIButton("Audio"   , () -> UIController.setState(UIState.AUDIO)   ),
      new UIButton("Gameplay", () -> UIController.setState(UIState.GAMEPLAY)),
      new UIButton("Back"    , UIController::back)
    );

    UIElement optvid = leftMenu(
      new Vector2(0.45, 0.28), 
      0.1, 
      new UIToggle("Fullscreen", Core.WINDOW::isFullScreen, (b) -> {Core.GLOBAL_SETTINGS.setBoolSetting("fullScreen", b); Core.WINDOW.setFullscreen(b);}),
      new UIButton("Test2", null),
      new UIButton("Test3", null),
      new UIButton("Test4", null)
    );

    UIElement optaud = leftMenu(
      new Vector2(0.45, 0.28), 
      0.1, 
      new UISlider.Integer("Master: %.0f"  , () -> Core.GLOBAL_SETTINGS.getIntSetting("soundMaster"), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("soundMaster", v), 0, 100),
      new UISlider.Integer("Sound FX: %.0f", () -> Core.GLOBAL_SETTINGS.getIntSetting("soundFX")    , (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("soundFX"    , v), 0, 100),
      new UISlider.Integer("Music: %.0f"   , () -> Core.GLOBAL_SETTINGS.getIntSetting("soundMusic") , (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("soundMusic" , v), 0, 100),
      new UIToggle        ("Subtitles"     , () -> Core.GLOBAL_SETTINGS.getBoolSetting("subtitles") , (v) -> Core.GLOBAL_SETTINGS.setBoolSetting("subtitles"  , v))
    );

    mainMenu.addState(UIState.DEFAULT , title   );
    mainMenu.addState(UIState.DEFAULT , outPanel);
    mainMenu.addState(UIState.NEW_GAME, title    , UIState.DEFAULT);
    mainMenu.addState(UIState.NEW_GAME, newGame );
    mainMenu.addState(UIState.OPTIONS , title    , UIState.DEFAULT , checkSettings);
    mainMenu.addState(UIState.OPTIONS , options );
    mainMenu.addState(UIState.VIDEO   , title    , UIState.OPTIONS);
    mainMenu.addState(UIState.VIDEO   , options );
    mainMenu.addState(UIState.VIDEO   , optvid  );
    mainMenu.addState(UIState.AUDIO   , title    , UIState.OPTIONS);
    mainMenu.addState(UIState.AUDIO   , options );
    mainMenu.addState(UIState.AUDIO   , optaud  );

    return mainMenu;
  }

  /**
  * Creates the HUD for use during gameplay.
  */
  public static UIPane createHUD() {
    UIPane HUD = new UIPane();

    UIElement greyed = new UIElement(
    new Vector2(0,0),
    new Vector2(1, 1),
    new boolean[]{false, false, false, false}
    ){
      protected void init() {this.backgroundColour = UIColours.SCREEN_TINT;}
      protected void draw(Graphics2D g, int screenSizeY, Vector2 tL, Vector2 bR, Color[] c, UIInteractable highlighted) {}
    };

    UIElement health = new ElemList(
      new Vector2(0, 0),
      new Vector2(0.05, COMPON_HEIGHT+2*BUFFER_HEIGHT),
      COMPON_HEIGHT,
      BUFFER_HEIGHT,
      new UIComponent[] {
        new UIComponent() {

          @Override
          protected void draw(Graphics2D g, Color... colours) {
            Font font = new Font("Copperplate", Font.BOLD, (int) Math.round((height*0.6)));
            FontMetrics metrics = g.getFontMetrics(font);
            g.setFont(font);
            g.setColor(colours[0]);

            g.drawString(""+Core.getCurrentScene().getPlayer().getHitPoints(), x, y+((height - metrics.getHeight())/2) + metrics.getAscent());
          }
          
        },
      },
      new boolean[] {true, false, true, false}
    );

    UIElement outPause = centreMenu(
      new Vector2(0.5, 0.5), 
      0.1, 
      new UIButton("Resume"         , UIController::back),
      new UIButton("Save Game"      , () -> UIController.setState(UIState.NEW_GAME)),
      new UIButton("Load Game"      , () -> UIController.setState(UIState.LOBBY)   ),
      new UIButton("Options"        , () -> UIController.setState(UIState.OPTIONS) ),
      new UIButton("Quit to Title"  , Core::quitToMenu),
      new UIButton("Quit to Desktop", Core::quitToDesk)
    );

    UIElement options = centreMenu(
      new Vector2(0.5, 0.5), 
      0.1, 
      new UIButton("Video"   , () -> UIController.setState(UIState.VIDEO)   ),
      new UIButton("Audio"   , () -> UIController.setState(UIState.AUDIO)   ),
      new UIButton("Gameplay", () -> UIController.setState(UIState.GAMEPLAY)),
      new UIButton("Back"    , UIController::back)
    );

    UIElement optvid = centreMenu(
      new Vector2(0.5, 0.5), 
      0.1, 
      new UIButton("Test1", null),
      new UIButton("Test2", null),
      new UIButton("Test3", null),
      new UIButton("Back" , UIController::back)
    );

    UIElement optaud = centreMenu(
      new Vector2(0.5, 0.5), 
      0.1, 
      new UISlider.Integer("Master: %.0f"  , () -> Core.GLOBAL_SETTINGS.getIntSetting("soundMaster"), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("soundMaster", v), 0, 100),
      new UISlider.Integer("Sound FX: %.0f", () -> Core.GLOBAL_SETTINGS.getIntSetting("soundFX")    , (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("soundFX"    , v), 0, 100),
      new UISlider.Integer("Music: %.0f"   , () -> Core.GLOBAL_SETTINGS.getIntSetting("soundMusic") , (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("soundMusic" , v), 0, 100),
      new UIToggle        ("Subtitles"     , () -> Core.GLOBAL_SETTINGS.getBoolSetting("subtitles") , (v) -> Core.GLOBAL_SETTINGS.setBoolSetting("subtitles"  , v)        ),
      new UIButton        ("Back"          , UIController::back)
    );

    HUD.setModeParent(UIState.DEFAULT, UIState.PAUSED);
    HUD.setModeBackAction(UIState.DEFAULT, ()->{UIController.retState(); Core.pause();});

    HUD.addState(UIState.DEFAULT, health  );
    HUD.addState(UIState.PAUSED , greyed   , UIState.DEFAULT , ()->{UIController.retState(); Core.pause();});
    HUD.addState(UIState.PAUSED , outPause);
    HUD.addState(UIState.OPTIONS, greyed   , UIState.PAUSED  , checkSettings);
    HUD.addState(UIState.OPTIONS, options );
    HUD.addState(UIState.VIDEO  , greyed   , UIState.OPTIONS);
    HUD.addState(UIState.VIDEO  , optvid  );
    HUD.addState(UIState.AUDIO  , greyed   , UIState.OPTIONS);
    HUD.addState(UIState.AUDIO  , optaud  );

    return HUD;
  }



  private static UIElement leftMenu(Vector2 tl, double w, UIComponent... comps) {
    return new ElemList(
      tl,
      tl.add(w, UIHelp.calculateListHeight(BUFFER_HEIGHT, UIHelp.calculateComponentHeights(COMPON_HEIGHT, comps))),
      COMPON_HEIGHT,
      BUFFER_HEIGHT,
      comps,
      new boolean[]{false, false, true, false}
    );
  }

  private static UIElement centreMenu(Vector2 c, double w, UIComponent... comps) {
    double h = UIHelp.calculateListHeight(BUFFER_HEIGHT, UIHelp.calculateComponentHeights(COMPON_HEIGHT, comps));
    Vector2 tl = c.subtract(w/2, h/2);
    return new ElemList(
      tl,
      tl.add(w, h),
      COMPON_HEIGHT,
      BUFFER_HEIGHT,
      comps,
      new boolean[]{false, true, true, true}
    );
  }

  private static final ElemConfirmation settingsChanged = new ElemConfirmation(
  new Vector2(0.35, 0.5-UIHelp.calculateListHeight(BUFFER_HEIGHT, COMPON_HEIGHT/2, COMPON_HEIGHT)/2),
  new Vector2(0.65, 0.5+UIHelp.calculateListHeight(BUFFER_HEIGHT, COMPON_HEIGHT/2, COMPON_HEIGHT)/2), 
  BUFFER_HEIGHT, 
  new boolean[]{false, false, false, false}, 
  () -> {Core.GLOBAL_SETTINGS.saveChanges();   UIController.retState();},
  () -> {Core.GLOBAL_SETTINGS.revertChanges(); UIController.retState();},
  "Save Changes?"
  );
  
  /**
  * A lambda function which, in place of transitioning back a step,
  * checks if the global settings have been changed and if so, 
  * brings up a confirmation dialogue to handle the changes before transitioning back.
  */
  public static final UIAction checkSettings = () -> {
    if (Core.GLOBAL_SETTINGS.hasChanged()) UIController.displayTempElement(settingsChanged);
    else UIController.retState();
  };
}
