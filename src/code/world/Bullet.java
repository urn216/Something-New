package code.world;

import mki.math.vector.Vector2;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.Color;
/**
* Collider class
*/
public class Bullet {
  private Vector2 position;
  private Vector2 prevPos;
  private Vector2 velocity;
  private int lifetime;
  private double damage;
  private boolean alive = true;
  private boolean updated = false;
  private RigidBody parent;

  /**
  * Constructor for Colliders
  */
  public Bullet(RigidBody parent, Vector2 v, int lifetime, double damage) {
    this.parent = parent;
    this.position = parent.getPos().add(parent.getVel());
    this.prevPos = position;
    this.velocity = v.add(parent.getVel());
    this.lifetime = lifetime;
    this.damage = damage;
  }

  public Vector2 getPos() {return position;}

  public void update(List<RigidBody> rbs) {
    if (updated) {return;}
    updated = true;
    lifetime--;
    prevPos = position;
    if (!alive) {return;}
    Ray ray = new Ray(position, velocity);
    for (RigidBody rb : rbs) {
      if (rb==parent) {continue;}
      for (Collider coll : rb.getColls()) {
        if (!coll.isShootable()) {continue;}
        coll.collide(ray);
      }
    }
    if (ray.hasHit()) {
      position = ray.getHitLocation();
      ray.getHitObject().takeDamage(damage);
      alive = false;
      lifetime = 1;
    }
    else {
      position = position.add(velocity);
    }
    if (lifetime <= 0) {alive = false;}
  }

  public void undone() {updated = false;}

  public boolean isAlive() {
    return (lifetime > 0);
  }

  public void draw(Graphics2D g, Camera cam) {
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    g.setColor(Color.red);
    g.draw(new Line2D.Double(position.x*z-conX, position.y*z-conY, prevPos.x*z-conX, prevPos.y*z-conY) );
  }
}
