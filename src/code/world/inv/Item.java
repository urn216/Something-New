package code.world.inv;

import java.awt.Graphics2D;

import code.world.RigidBody;
import mki.math.vector.Vector2;

/**
* Item interface
*/
public abstract class Item {

  /**
  * Returns whether or not the Item's primary use is 'full-auto'
  *
  * @return true if the primary use will activate repeatedly if the activation button is held
  */
  public abstract boolean getAutoType();

  /**
  * Returns whether or not the Item's secondary use is 'full-auto'
  *
  * @return true if the secondary use will activate repeatedly if the activation button is held
  */
  public abstract boolean getAutoType2();

  /**
  * Gets the cooldown of this Item's primary use
  *
  * @return the number of ticks before another primary activation can occur
  */
  public abstract int getCooldown();

  /**
  * Gets the cooldown of this Item's secondary use
  *
  * @return the number of ticks before another secondary activation can occur
  */
  public abstract int getCooldown2();

  /**
  * Returns whether or not this Item has a secondary use
  *
  * @return true if there is a secondary use of this Item
  */
  public abstract boolean hasSecondary();

  /**
  * Performs this Item's primary function
  *
  * @param parent The {@code RigidBody} initiating the primary function of this {@code Item}
  * @param usePos The point in the scene to perform the function at
  */
  public abstract void primeUse(RigidBody parent, Vector2 usePos);

  /**
  * Performs this Item's secondary function
  *
  * @param parent The {@code RigidBody} initiating the secondary function of this {@code Item}
  * @param usePos The point in the scene to perform the function at
  */
  public abstract void secondUse(RigidBody parent, Vector2 usePos);

  /**
   * Draws a reticle to the screen if needed
   * 
   * @param usePos The point in the scene to perform the function at
   */
  public abstract void drawReticle(Graphics2D g, RigidBody parent, Vector2 usePos);
}
