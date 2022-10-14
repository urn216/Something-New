package code.world.inv;

import code.math.Vector2;

import code.world.RigidBody;

/**
* Item interface
*/
public class BuildTool extends Item {


  public BuildTool(RigidBody parent) {
    this.parent = parent;
  }

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
  public void primeUse(Vector2 usePos) {
    parent.getScene().getTile(usePos).activate();
  }

  /**
  * secondary use to delete a selected object
  *
  * @param usePos the position in the scene to delete at
  */
  public void secondUse(Vector2 usePos) {
    parent.getScene().getTile(usePos).deactivate();
  }
}
