package code.world.unit;

import code.core.Scene;

import code.math.Vector2;

import code.world.Collider;
import code.world.RigidBody;

import code.world.fixed.WorldObject;

import java.util.*;
//import java.awt.Graphics2D;
//import java.awt.geom.Rectangle2D;
//import java.awt.Color;

/**
* Write a description of class Unit here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class ItemUnit extends Unit
{

  public ItemUnit(double X, double Y, Scene scene) {
    position = new Vector2(X, Y);
    direction = new Vector2();
    v = new Vector2();

    m = 40;
    walkF = 0;
    triggering = new ArrayList<WorldObject>();
    this.size = 8;
    this.scene = scene;
    this.hitPoints = 1;

    collider = new Collider(new Vector2(), size, false, this);
  }

  public void update(List<WorldObject> objs, List<RigidBody> rbs) {
    if (updated || !alive) {
      return;
    }
    updated = true;
    List<Collider> colliders = new ArrayList<Collider>();
    for (WorldObject o : objs) {
      colliders.addAll(o.getColls());
    }
    move(colliders);
  }

  public void step(List<Collider> colliders) {
    Vector2 slowAcc = v.scale(m/1000);
    v = v.subtract(slowAcc);

    stepX(colliders);
    stepY(colliders);
  }

  private void stepX(List<Collider> colliders) {
    position = position.add(v.x, 0);
    Collider collided = collision(colliders, true);
    if (collided != null) {
      position = new Vector2(collided.getPos().x-(collided.getWidth()/2+collider.getWidth()/2+0.00001)*Math.signum(v.x), position.y);
      v = v.scale(-0.7, 1);
      direction = v.unitize();
    }
  }

  private void stepY(List<Collider> colliders) {
    position = position.add(0, v.y);
    Collider collided = collision(colliders, false);
    if (collided != null) {
      position = new Vector2(position.x, collided.getPos().y-(collided.getHeight()/2+collider.getHeight()/2+0.00001)*Math.signum(v.y));
      v = v.scale(1, -0.7);
      direction = v.unitize();
    }
  }

  public Unit summon(double x, double y, Scene s) {return new ItemUnit(x, y, s);}
}
