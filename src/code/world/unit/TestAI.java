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
  private static final int burstLength = 2*(int)Core.TICKS_PER_SECOND;
  private static final int burstCool = 1*(int)Core.TICKS_PER_SECOND;

  private int dirChange = (int) (Math.random()*60);
  private int burstCounter = 0;
  /**
  * Constructor for objects of class TestAI
  */
  public TestAI(Scene scene, Item item, Vector2 position, Vector2 velocity) {
    super(
      scene,                            //scene
      13,                               //size
      Color.getHSBColor((float)Math.random(), 1f, 0.5f),
      position,                         //pos
      velocity,
      new Vector2(),                    //dir
      6000/Core.TICKS_PER_SECOND,       //walk-force
      280/Core.TICKS_PER_SECOND,        //max-velocity
      150,                              //mass
      160,                              //hitpoints
      0                                 //elasticity
    );

    held = item;
  }

  public void move(List<Collider> colliders) {
    dirChange --;
    if (dirChange <= 0) {
      dirChange = (int) (Math.random()*2*Core.TICKS_PER_SECOND);
      setMovementDirection(Vector2.fromAngle((Math.random()*2-1)*Math.PI, (int)(Math.random()+0.7)));
    }

    Unit target = scene.getPlayer();
    Vector2 position = getPosition();
    Vector2 direction = getMovementDirection();
    if (direction.dot(target.getPosition().subtract(position).unitize()) > 0.8) {
      if (burstCounter < burstLength) held.primeUse(this, target.getPosition().add(target.getVelocity()));
      // held.primeUse(this, position.add(direction));
      burstCounter++;

      if (burstCounter >= burstCool+burstLength) burstCounter = 0;
    }

    if (step(colliders)) dirChange = 0;
  }
}
