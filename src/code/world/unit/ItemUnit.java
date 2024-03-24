package code.world.unit;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import code.core.Core;
import code.world.Collider;
import code.world.RigidBody;

import code.world.fixed.WorldObject;
import code.world.scene.Scene;

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
public class ItemUnit extends Unit {

  public ItemUnit(double X, double Y, Scene scene) {
    super(
      scene,                            //scene
      4,                                //size
      new Vector2(X, Y),                //pos
      new Vector2(),                    //dir
      0,                                //walk-force
      240/Core.TICKS_PER_SECOND,        //max-velocity
      40,                               //mass
      1,                                //hitpoints
      0                                 //elasticity
    );
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
    renderedBody.setPosition(renderedBody.getPosition().add(v.x, 0, 0));
    Collider collided = collision(colliders, true);
    if (collided != null) {
      Vector3 position = renderedBody.getPosition();
      renderedBody.setPosition(new Vector3(collided.getPos().x-collider.snapTo(collided, true)*Math.signum(v.x), position.y, position.z));
      v = v.scale(-0.7, 1);
      direction = v.unitize();
    }
  }

  private void stepY(List<Collider> colliders) {
    renderedBody.setPosition(renderedBody.getPosition().add(0, 0, v.y));
    Collider collided = collision(colliders, false);
    if (collided != null) {
      Vector3 position = renderedBody.getPosition();
      renderedBody.setPosition(new Vector3(position.x, position.y, collided.getPos().y-collider.snapTo(collided, false)*Math.signum(v.y)));
      v = v.scale(1, -0.7);
      direction = v.unitize();
    }
  }

  public Unit summon(double x, double y, Scene s) {return new ItemUnit(x, y, s);}
}
