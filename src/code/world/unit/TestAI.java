package code.world.unit;

import mki.math.vector.Vector2;
import code.core.Core;
import code.world.Collider;

import code.world.inv.Item;
import code.world.scene.Scene;

import java.util.*;
import java.awt.Color;

/**
* Write a description of class TestAI here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class TestAI extends Unit {
  private int dirChange = (int) (Math.random()*60);
  /**
  * Constructor for objects of class TestAI
  */
  public TestAI(Scene scene, Item item, Vector2 position, Vector2 velocity) {
    super(
      scene,                            //scene
      8,                                //size
      Color.getHSBColor((float)Math.random(), 1f, 0.5f),
      position,                         //pos
      velocity,
      new Vector2(),                    //dir
      3000/Core.TICKS_PER_SECOND,       //walk-force
      140/Core.TICKS_PER_SECOND,        //max-velocity
      100,                              //mass
      160,                              //hitpoints
      0                                 //elasticity
    );

    held = item;
  }

  public void move(List<Collider> colliders) {
    dirChange --;
    if (dirChange <= 0) {
      dirChange = (int) (Math.random()*2*Core.TICKS_PER_SECOND);
      setMovementDirection(Vector2.fromAngle((Math.random()*2-1)*Math.PI, 1));
    }

    Unit target = scene.getPlayer();
    Vector2 position = getPosition();
    Vector2 direction = getMovementDirection();
    if (direction.dot(target.getPosition().subtract(position)) > 0) {
      // held.primeUse(target.getPos().add(target.getVel()));
      held.primeUse(this, position.add(direction));
    }

    step(colliders);
  }
}
