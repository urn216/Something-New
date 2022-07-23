package code.world.inv;

import code.math.Vector2;

/**
* Item interface
*/
public interface Item
{

  public boolean getAutoType();

  public boolean getAutoType2();

  public int getCooldown();

  public int getCooldown2();

  public boolean hasSecondary();

  public void primeUse(Vector2 usePos);

  public void secondUse(Vector2 usePos);
}
