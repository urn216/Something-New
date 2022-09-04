package code.world.inv;

import code.math.Vector2;

/**
* Item interface
*/
public interface Item
{

  /**
  * Returns whether or not the Item's primary use is 'full-auto'
  *
  * @return true if the primary use will activate repeatedly if the activation button is held
  */
  public boolean getAutoType();

  /**
  * Returns whether or not the Item's secondary use is 'full-auto'
  *
  * @return true if the secondary use will activate repeatedly if the activation button is held
  */
  public boolean getAutoType2();

  /**
  * Gets the cooldown of this Item's primary use
  *
  * @return the number of ticks before another primary activation can occur
  */
  public int getCooldown();

  /**
  * Gets the cooldown of this Item's secondary use
  *
  * @return the number of ticks before another secondary activation can occur
  */
  public int getCooldown2();

  /**
  * Returns whether or not this Item has a secondary use
  *
  * @return true if there is a secondary use of this Item
  */
  public boolean hasSecondary();

  /**
  * Performs this Item's primary function
  *
  * @param usePos The point on the screen to perform the function at
  */
  public void primeUse(Vector2 usePos);

  /**
  * Performs this Item's secondary function
  *
  * @param usePos The point on the screen to perform the function at
  */
  public void secondUse(Vector2 usePos);
}
