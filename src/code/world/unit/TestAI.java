package code.world.unit;

import mki.math.vector.Vector2;
import code.core.Core;
import code.world.Collider;

import code.world.inv.Gun;
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
  public TestAI(double X, double Y, Scene scene) {
    super(
      scene,                            //scene
      8,                                //size
      Color.getHSBColor((float)Math.random(), 1f, 0.5f),
      new Vector2(X, Y),                //pos
      new Vector2(),                    //dir
      3000/Core.TICKS_PER_SECOND,       //walk-force
      140/Core.TICKS_PER_SECOND,        //max-velocity
      100,                              //mass
      160,                              //hitpoints
      0                                 //elasticity
    );

    held = new Gun(this, 1200, 1, 1000, 160, 30, 0.96, true);
  }

  public void move(List<Collider> colliders) {
    dirChange --;
    if (dirChange <= 0) {
      dirChange = (int) (Math.random()*2*Core.TICKS_PER_SECOND);
      direction = Vector2.fromAngle((Math.random()*2-1)*Math.PI, 1);
    }

    Unit target = scene.getPlayer();
    if (direction.dot(target.getPos().subtract(position)) > 0) {
      // held.primeUse(target.getPos().add(target.getVel()));
      held.primeUse(position.add(direction));
    }

    step(colliders);
  }

  public Unit summon(double x, double y, Scene s) {return new TestAI(x, y, s);}
}
