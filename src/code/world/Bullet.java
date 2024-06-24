package code.world;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.world.Material;
import mki.world.object.primitive.Cube;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.Color;
/**
* Collider class
*/
public class Bullet {
  private static final Material BULLET_GLOW = new Material((255<<24)|(200<<16), 0f, new Vector3(2, 0, 0));

  private Vector2 position;
  private Vector2 prevPos;
  private Vector2 velocity;
  private int lifetime;
  private double damage;
  private boolean alive = true;
  private boolean updated = false;
  private RigidBody parent;
  
  public final mki.world.RigidBody renderedBullet;

  /**
  * Constructor for Colliders
  */
  public Bullet(RigidBody parent, Vector2 v, int lifetime, double damage) {
    this.parent = parent;
    this.position = parent.getPosition().add(parent.getVelocity());
    this.prevPos = position;
    this.velocity = v.add(parent.getVelocity());
    this.lifetime = lifetime;
    this.damage = damage;
    renderedBullet = new Cube(
      new Vector3(this.position.x*Tile.SCALE_U_TO_M, parent.getRenderedBody().getPosition().y*1.5, -this.position.y*Tile.SCALE_U_TO_M), 
      0.05, 
      BULLET_GLOW
    );
  }

  public Vector2 getPosition() {return position;}

  public void update(List<RigidBody> rbs) {
    if (updated) {return;}
    updated = true;
    lifetime--;
    prevPos = position;
    if (!alive) {return;}
    Ray ray = new Ray(position, velocity);
    for (RigidBody rb : rbs) {
      if (rb==parent) {continue;}
      for (Collider coll : rb.getColliders()) {
        if (!coll.isShootable()) {continue;}
        coll.collide(ray);
      }
    }
    if (ray.hasHit()) {
      position = ray.getHitLocation();
      this.renderedBullet.setPosition(new Vector3(this.position.x*Tile.SCALE_U_TO_M, parent.getRenderedBody().getPosition().y*1.5, -this.position.y*Tile.SCALE_U_TO_M));
      ray.getHitObject().takeDamage(damage, position);
      alive = false;
      lifetime = 1;
    }
    else {
      position = position.add(velocity);
      this.renderedBullet.setPosition(new Vector3(this.position.x*Tile.SCALE_U_TO_M, parent.getRenderedBody().getPosition().y*1.5, -this.position.y*Tile.SCALE_U_TO_M));
    }
    if (lifetime <= 0) {alive = false;}
  }

  public void undone() {updated = false;}

  public boolean isAlive() {
    return (lifetime > 0);
  }

  public void draw(Graphics2D g) {
    Camera cam = parent.getScene().getCam();
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    g.setColor(Color.red);
    g.draw(new Line2D.Double(position.x*z-conX, position.y*z-conY, prevPos.x*z-conX, prevPos.y*z-conY) );
  }
}
