package code.core;

import mki.math.vector.Vector2;
import mki.rendering.Constants;
import mki.ui.elements.*;
import mki.ui.components.*;
import mki.ui.components.interactables.*;
import mki.ui.control.UIAction;
import mki.ui.control.UIColours;
import mki.ui.control.UIColours.ColourSet;
import mki.ui.control.UIController;
import mki.ui.control.UIHelp;
import mki.ui.control.UIPane;
import mki.ui.control.UIState;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

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
    UIElement.TRANSITION_SLIDE_UP_LEFT
    ){
      protected void init() {components = new UIComponent[]{new UIText("A Game Title", 0.6, Font.BOLD)};}
      protected void draw(Graphics2D g, int screenSizeY, Vector2 tL, Vector2 bR, ColourSet c) {
        components[0].draw(g, (float)tL.x, (float)tL.y, (float)(bR.x-tL.x), (float)(bR.y-tL.y), c);
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
      new UIToggle("Fullscreen",  Core.WINDOW::isFullScreen,              (b) -> Core.GLOBAL_SETTINGS.setBoolSetting("v_fullScreen"   , b)),
      new UIToggle("3D Mode"   ,         Core::isRender3D,                (b) -> Core.GLOBAL_SETTINGS.setBoolSetting("v_3Drendering"  , b)),
      new UIToggle("Fancy 3D"  ,    Constants::usesDynamicRasterLighting, (b) -> Core.GLOBAL_SETTINGS.setBoolSetting("v_fancylighting", b)),
      new UIButton("Test4", null)
    );

    UIElement optaud = leftMenu(
      new Vector2(0.45, 0.28), 
      0.1, 
      new UISlider.Integer("Master: %.0f"  , () -> Core.GLOBAL_SETTINGS.getIntSetting ("s_master"   ), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("s_master"   , v), 0, 100),
      new UISlider.Integer("Sound FX: %.0f", () -> Core.GLOBAL_SETTINGS.getIntSetting ("s_FX"       ), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("s_FX"       , v), 0, 100),
      new UISlider.Integer("Music: %.0f"   , () -> Core.GLOBAL_SETTINGS.getIntSetting ("s_music"    ), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("s_music"    , v), 0, 100),
      new UIToggle        ("Subtitles"     , () -> Core.GLOBAL_SETTINGS.getBoolSetting("s_subtitles"), (v) -> Core.GLOBAL_SETTINGS.setBoolSetting("s_subtitles", v)        )
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
    UIElement.TRANSITION_FADE_IN_PLACE
    ){
      protected void init() {
        this.background = new UIComponent(){
        
          @Override
          protected void draw(Graphics2D g, UIColours.ColourSet c) {
            g.setColor(c.background());
            g.fill(new Rectangle2D.Double(x, y, width, height));
          }
        };
      }
      protected void draw(Graphics2D g, int screenSizeY, Vector2 tL, Vector2 bR, ColourSet c) {}
    };

    UIElement health = new ElemListVert(
      new Vector2(0, 0),
      new Vector2(0.05, COMPON_HEIGHT+2*BUFFER_HEIGHT),
      COMPON_HEIGHT,
      BUFFER_HEIGHT,
      new UIComponent[] {
        new UIComponent() {

          @Override
          protected void draw(Graphics2D g, ColourSet c) {
            Font font = new Font("Copperplate", Font.BOLD, (int) Math.round((height*0.6)));
            FontMetrics metrics = g.getFontMetrics(font);
            g.setFont(font);
            g.setColor(c.text());

            g.drawString(""+Core.getCurrentScene().getPlayer().getHitPoints(), x, y+((height - metrics.getHeight())/2) + metrics.getAscent());
          }
          
        },
      },
      UIElement.TRANSITION_SLIDE_UP_LEFT
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
      new UIToggle("Fullscreen",  Core.WINDOW::isFullScreen,              (b) -> Core.GLOBAL_SETTINGS.setBoolSetting("v_fullScreen"   , b)),
      new UIToggle("3D Mode"   ,         Core::isRender3D,                (b) -> Core.GLOBAL_SETTINGS.setBoolSetting("v_3Drendering"  , b)),
      new UIToggle("Fancy 3D"  ,    Constants::usesDynamicRasterLighting, (b) -> Core.GLOBAL_SETTINGS.setBoolSetting("v_fancylighting", b)),
      new UIButton("Back"      , UIController::back)
    );

    UIElement optaud = centreMenu(
      new Vector2(0.5, 0.5), 
      0.1, 
      new UISlider.Integer("Master: %.0f"  , () -> Core.GLOBAL_SETTINGS.getIntSetting ("s_master"   ), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("s_master"   , v), 0, 100),
      new UISlider.Integer("Sound FX: %.0f", () -> Core.GLOBAL_SETTINGS.getIntSetting ("s_FX"       ), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("s_FX"       , v), 0, 100),
      new UISlider.Integer("Music: %.0f"   , () -> Core.GLOBAL_SETTINGS.getIntSetting ("s_music"    ), (v) -> Core.GLOBAL_SETTINGS.setIntSetting ("s_music"    , v), 0, 100),
      new UIToggle        ("Subtitles"     , () -> Core.GLOBAL_SETTINGS.getBoolSetting("s_subtitles"), (v) -> Core.GLOBAL_SETTINGS.setBoolSetting("s_subtitles", v)        ),
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
    return new ElemListVert(
      tl,
      tl.add(w, UIHelp.calculateListHeight(BUFFER_HEIGHT, UIHelp.calculateComponentHeights(COMPON_HEIGHT, comps))),
      COMPON_HEIGHT,
      BUFFER_HEIGHT,
      comps,
      UIElement.TRANSITION_SLIDE_LEFT
    );
  }

  private static UIElement centreMenu(Vector2 c, double w, UIComponent... comps) {
    double h = UIHelp.calculateListHeight(BUFFER_HEIGHT, UIHelp.calculateComponentHeights(COMPON_HEIGHT, comps));
    Vector2 tl = c.subtract(w/2, h/2);
    return new ElemListVert(
      tl,
      tl.add(w, h),
      COMPON_HEIGHT,
      BUFFER_HEIGHT,
      comps,
      UIElement.TRANSITION_STRETCH_HORIZONTALLY
    );
  }

  private static final ElemConfirmation settingsChanged = new ElemConfirmation(
  new Vector2(0.35, 0.5-UIHelp.calculateListHeight(BUFFER_HEIGHT, COMPON_HEIGHT/2, COMPON_HEIGHT)/2),
  new Vector2(0.65, 0.5+UIHelp.calculateListHeight(BUFFER_HEIGHT, COMPON_HEIGHT/2, COMPON_HEIGHT)/2), 
  BUFFER_HEIGHT, 
  UIElement.TRANSITION_SLIDE_DOWN,
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
