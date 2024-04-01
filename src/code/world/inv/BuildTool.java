package code.world.inv;

import java.awt.Color;
import java.awt.Graphics2D;

import mki.math.MathHelp;
import mki.math.vector.Vector2;
import code.core.Core;
import code.world.RigidBody;

/**
* Item interface
*/
public class BuildTool extends Item {

  public boolean getAutoType() {return false;}

  public boolean getAutoType2() {return false;}

  public int getCooldown() {return 0;}

  public int getCooldown2() {return 0;}

  public boolean hasSecondary() {return true;}

  /**
  * primary use to place an object
  *
  * @param usePos the position in the scene to place at
  */
  public void primeUse(RigidBody parent, Vector2 usePos) {
    parent.getScene().getTile(usePos).activate();
  }

  /**
  * secondary use to delete a selected object
  *
  * @param usePos the position in the scene to delete at
  */
  public void secondUse(RigidBody parent, Vector2 usePos) {
    parent.getScene().getTile(usePos).deactivate();
  }

  @Override
  public void drawReticle(Graphics2D g, RigidBody parent, Vector2 usePos) {
    parent.getScene().getTile(usePos).drawGhost(g, parent.getScene().getCam(), new Color(0, 255, 0, MathHelp.abs((int)(Core.currentTicks()%30)-15)*8+72));
  }
}
