package code.world.unit;

import mki.math.vector.Vector2;
import code.core.Core;
import code.world.inv.Item;
import code.world.scene.Scene;

/**
* Write a description of class Dud here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class Dud extends Unit {
  /**
  * Constructor for objects of class Dud
  */
  public Dud(Scene scene, Item item, Vector2 position, Vector2 velocity) {
    super(
      scene,                            //scene
      8,                                //size
      position,                         //pos
      velocity,
      new Vector2(),                    //dir
      0,                                //walk-force
      240/Core.TICKS_PER_SECOND,        //max-velocity
      100,                              //mass
      160,                              //hitpoints
      0                                 //elasticity
    );
  }
}
