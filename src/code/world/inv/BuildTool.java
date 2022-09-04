package code.world.inv;

import code.math.Vector2;

/**
* Item interface
*/
public class BuildTool implements Item
{

  public boolean getAutoType() {return false;}

  public boolean getAutoType2() {return false;}

  public int getCooldown() {return 0;}

  public int getCooldown2() {return 0;}

  public boolean hasSecondary() {return true;}

  /**
  * primary use to place an object
  *
  * @param usePos the position on the screen to place at
  */
  public void primeUse(Vector2 usePos) {

  }

  /**
  * secondary use to delete a selected object
  *
  * @param usePos the position on the screen to delete at
  */
  public void secondUse(Vector2 usePos) {

  }
}
