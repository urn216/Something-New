package code.math;

import code.world.Collider;
import code.world.RigidBody;

//import java.util.*;
/**
* Collider class
*/
public class Ray
{
  private final Vector2 sPos;
  private final Vector2 offset;

  private double t = Double.POSITIVE_INFINITY; // number of multiples of offest from startpos
  private boolean hasHit = false;
  private RigidBody hit;

  /**
  * Constructor for Colliders
  */
  public Ray(Vector2 start, Vector2 offset) {
    sPos = start;
    this.offset = offset;
  }

  public void squareCollision(Vector2 p1, Vector2 p2, Collider coll) {
    Vector2 pDir = p2.subtract(p1);
    double t = (pDir.x*(sPos.y-p1.y)-pDir.y*(sPos.x-p1.x))/(pDir.y*offset.x-pDir.x*offset.y);
    //System.out.println(t);
    if (t < 0.0 || t > 1.0) {return;} // if facing wrong way or too far away
    double tPrimeY = (t*offset.y+sPos.y-p1.y)/pDir.y;
    double tPrimeX = (t*offset.x+sPos.x-p1.x)/pDir.x;
    double tPrime;
    if (pDir.y == 0) {tPrime = tPrimeX;}
    else {tPrime = tPrimeY;}
    //System.out.println(tPrimeX+" "+tPrimeY+"       "+tPrime);
    if (tPrime < 0.0 || tPrime > 1.0) {return;} // not between p1, p2 (pDir.magnitude())
    if (t < this.t) {
      this.t = t;
      hasHit = true;
      hit = coll.getParent();
    }
  }

  public void roundCollision(Vector2 p, double r, Collider coll) {
    double magSquare = offset.magsquare();
    double t = 0;
    double dist2 = Double.POSITIVE_INFINITY;
    if (magSquare==0) {dist2 = p.subtract(sPos).magsquare();}
    else {
      t = MathHelp.clamp(p.subtract(sPos).dot(offset)/magSquare, 0, 1);
      dist2 = p.subtract(sPos.add(offset.scale(t))).magsquare();
    }
    if (dist2 <= r*r) {
      this.t = t;
      hasHit = true;
      hit = coll.getParent();
    }
  }

  public Vector2 getHitLocation() {
    if (hasHit) {return sPos.add(offset.scale(t));}
    else {return sPos.add(offset);}
  }

  public RigidBody getHitObject() {
    if (hasHit) {return hit;}
    else {return null;}
  }

  public boolean hasHit() {return hasHit;}
}
