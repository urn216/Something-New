package code.world.unit;

import mki.math.vector.Vector2;

import code.world.Camera;
import code.world.Collider;
import code.world.RigidBody;
import code.world.fixed.WorldObject;
import code.world.fixed.dividers.Door;
import code.world.inv.Item;
import code.world.scene.Scene;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Color;

/**
* Write a description of class Unit here.
*
* @author (your name)
* @version (a version number or a date)
*/
public abstract class Unit implements RigidBody {
  protected Scene scene;

  protected Item held;

  protected boolean alive = true;

  protected Vector2 position;
  protected Vector2 direction = new Vector2();
  protected Vector2 v = new Vector2();
  protected Vector2 a = new Vector2();
  protected Vector2 addAcc = new Vector2();
  protected Collider.Round collider;
  protected List<WorldObject> triggering;
  protected boolean updated = false;
  protected double walkF;
  protected double vMax;
  protected double m;
  protected float bounceAmount = 0f;

  protected Color col = Color.white;

  protected double hitPoints;
  protected int hurtFrames;

  public Vector2 getPos() {return position;}

  public Vector2 getDir() {return direction;}

  public Vector2 getVel() {return v;}

  public Scene getScene() {return scene;}

  public double getHitPoints() {
    return hitPoints;
  }

  public List<Collider> getColls() {
    return List.of(collider);
  }

  public void setPos(Vector2 pos) {position = pos;}

  public void setDir(Vector2 dir) {direction = dir;}

  public void setVel(Vector2 vel) {v = vel;}

  public void setHeld(Item i) {held = i;}

  public void takeDamage(double damage) {
    hitPoints -= damage;
    hurtFrames = 2;
    if (hitPoints <= 0) {
      hitPoints = 0;
      collider.setVoid();
      alive = false;
    }
  }

  public boolean isAlive() {return alive;}

  public void update(List<WorldObject> objs, List<RigidBody> rbs) {
    if (updated || !alive) {
      return;
    }
    updated = true;
    hurtFrames--;
    List<Collider> colliders = new ArrayList<Collider>();
    for (RigidBody rb : rbs) {
      colliders.addAll(rb.getColls());
    }
    move(colliders);
    trigger(objs);
  }

  public void move(List<Collider> colliders) {
    step(colliders);
  }

  public void step(List<Collider> colliders) {
    direction = direction.unitize();
    Vector2 inputAcc = direction.scale(walkF/m);
    Vector2 slowAcc = v.scale(walkF/(vMax*m)).add(v.scale(m/1000));
    a = inputAcc.subtract(slowAcc).add(addAcc);
    v = v.add(a);

    addAcc = new Vector2();

    // if non-solid
    if (!collider.isSolid()) {
      position = position.add(v);
    }

    // if solid
    else {
      stepX(colliders);
      stepY(colliders);
    }
  }

  private void stepX(List<Collider> colliders) {
    position = position.add(v.x, 0);
    Collider collided = collision(colliders, true);
    if (collided != null) {
      position = new Vector2(collided.getPos().x-collider.snapTo(collided, true)*Math.signum(v.x), position.y);
      Vector2 dir = new Vector2(collided.getClosest().subtract(collider.getPos()).unitize());
      v = v.subtract(dir.scale(dir.dot(v)*(1+bounceAmount)));
    }
  }

  private void stepY(List<Collider> colliders) {
    position = position.add(0, v.y);
    Collider collided = collision(colliders, false);
    if (collided != null) {
      position = new Vector2(position.x, collided.getPos().y-collider.snapTo(collided, false)*Math.signum(v.y));
      Vector2 dir = new Vector2(collided.getClosest().subtract(collider.getPos()).unitize());
      v = v.subtract(dir.scale(dir.dot(v)*(1+bounceAmount)));
    }
  }

  public Unit summon(double x, double y, Scene s) {
    System.out.println("This unit either does not exist or cannot be summoned.");
    return null;
  }

  public void undone() {
    updated = false;
  }

  public Collider collision(List<Collider> colliders, boolean isX) {
    double shortest = Double.POSITIVE_INFINITY;
    Collider ans = null;
    for (Collider coll : colliders) {
      if (!coll.isSolid()) continue;
      Vector2 dist = collider.collide(coll);
      if (dist==null) continue;
      double cDist = isX ? dist.x : dist.y;
      if (cDist < shortest) {
        shortest = cDist;
        ans = coll;
      }
    }
    return ans;
  }

  public void trigger(List<WorldObject> objects) {
    for (WorldObject obj : objects) {
      if (obj instanceof Door) {
        for (Collider other : obj.getColls()) {
          if (other.isTrigger() && collider.collide(other)!=null) {
            if (!triggering.contains(obj)) {
              triggering.add(obj);
              obj.activate(this);
            }
          }
          else if (triggering.contains(obj)) {
            triggering.remove(obj);
            obj.deactivate(this);
          }
        }
      }
    }
  }

  public String toString() {
    return this.getClass().getSimpleName()+" "+position.x+" "+position.y;
  }

  public void draw(Graphics2D g, Camera cam) {
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    Vector2 pos = collider.getPos();
    double rad = collider.getRadius();
    g.setColor(!alive || hurtFrames > 0 ? Color.pink : col);
    g.fill(new Ellipse2D.Double((pos.x-rad)*z-conX, (pos.y-rad)*z-conY, rad*2*z, rad*2*z));
    g.drawLine(
      (int)(pos.x*z-conX), 
      (int)(pos.y*z-conY), 
      (int)((pos.x+direction.x*20)*z-conX), 
      (int)((pos.y+direction.y*20)*z-conY)
    );
  }
}
