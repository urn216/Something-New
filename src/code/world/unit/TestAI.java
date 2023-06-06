package code.world.unit;

import code.core.Scene;

import mki.math.vector.Vector2;

import code.world.Collider;
import code.world.fixed.WorldObject;

import code.world.inv.Gun;
import code.world.inv.GunLauncher;

import java.util.*;
import java.awt.Color;

/**
* Write a description of class TestAI here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class TestAI extends Unit
{
  private int dirChange = (int) (Math.random()*60);
  /**
  * Constructor for objects of class TestAI
  */
  public TestAI(double X, double Y, Scene scene)
  {
    position = new Vector2(X, Y);

    triggering = new ArrayList<WorldObject>();
    this.scene = scene;
    this.hitPoints = 160;
    walkF = 100;
    m = 100;
    vMax = 4;

    this.collider = new Collider.Round(new Vector2(), 8, true, this);

    col = Color.getHSBColor((float)Math.random(), 1f, 0.5f);
    held = new Gun(this, 40, 1, 30, 5, 30, 0.96, true, new GunLauncher(this, new ItemUnit(0, 0, null), 20, 1, 30, 15, 0.96, true));
  }

  public void move(List<Collider> colliders) {
    dirChange --;
    if (dirChange <= 0) {
      dirChange = (int) (Math.random()*60);
      direction = new Vector2(Vector2.fromAngle((Math.random()*2-1)*Math.PI, 1));
    }

    if (heldCool1 <= 0) {
      held.primeUse(position.add(direction));
      heldCool1 = held.getCooldown();
    }

    step(colliders);
  }

  public Unit summon(double x, double y, Scene s) {return new TestAI(x, y, s);}
}
