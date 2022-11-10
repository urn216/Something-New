package code.world;

import code.math.MathHelp;
import code.math.Ray;
import code.math.Vector2;

//import java.util.*;
/**
* Collider class
*/
public class Collider
{
  private static final double OFFSET = 0.000001;

  private final boolean round;
  private boolean shootable;
  private boolean solid;
  private boolean trigger;
  private Vector2 offset; // centre relative to parent
  private double width;
  private double height;
  private RigidBody parent;

  private Vector2 closest;

  /**
  * Constructor for Colliders
  */
  public Collider(Vector2 offset, double diameter, boolean solid, RigidBody parent) {
    this.offset = offset;
    this.closest = offset;
    this.width = diameter;
    this.height = diameter;
    this.shootable = solid;
    this.solid = solid;
    this.trigger = !solid;
    this.round = true;
    this.parent = parent;
  }

  public Collider(Vector2 offset, double width, double height, boolean solid, RigidBody parent) {
    this.offset = offset;
    this.closest = offset;
    this.width = width;
    this.height = height;
    this.shootable = solid;
    this.solid = solid;
    this.trigger = !solid;
    this.round = false;
    this.parent = parent;
  }

  public boolean isRound() {
    return round;
  }

  public boolean isShootable() {
    return solid ? shootable : solid;
  }

  public boolean isSolid() {
    return solid;
  }

  public boolean isTrigger() {
    return trigger;
  }

  public void setShootable(boolean shoot) {
    shootable = shoot;
  }

  public void setSolid() {
    solid = trigger ? false:true;
  }

  public void setVoid() {
    solid = false;
  }

  public void toggle() {
    solid = trigger ? false:!solid;
  }

  public void setTrigger(boolean trig) {
    trigger = trig;
  }

  public double getWidth() {return width;}

  public double getHeight() {return height;}

  public RigidBody getParent() {return parent;}

  public Vector2 getClosest() {return closest;}

  public void setWidth(double w) {width = w;}

  public void setHeight(double h) {height = h;}

  public Vector2 getPos() {return parent.getPos().add(offset);}

  public Vector2 getOff() {return offset;}

  public void setOff(Vector2 offset) {this.offset = offset;}

  public void setClosest(Vector2 v) {closest = v;}

  public Vector2 getTL() {return getPos().add(new Vector2(-width/2, -height/2));}

  public Vector2 getTR() {return getPos().add(new Vector2(width/2, -height/2));}

  public Vector2 getBL() {return getPos().add(new Vector2(-width/2, height/2));}

  public Vector2 getBR() {return getPos().add(new Vector2(width/2, height/2));}

  public void move(Vector2 v) {
    offset = offset.add(v);
  }

  public Vector2 touching(Collider other) {
    if (other != this) {
      Vector2 opos = other.getPos();
      Vector2 tpos = getPos();
      if (!round) {
        if (!other.isRound()) {
          Vector2 test = new Vector2(Math.abs(opos.x-tpos.x)-(other.getWidth()+this.width)/2, Math.abs(opos.y-tpos.y)-(other.getHeight()+this.height)/2);
          if (test.x<=0&&test.y<=0) {return test;}
        }
        else {
          return other.touching(this);
        }
      }
      else {
        if (!other.isRound()) {
          Vector2 rectRelocate = new Vector2(
          MathHelp.clamp(tpos.x, other.getPos().x-other.getWidth()/2, other.getPos().x+other.getWidth()/2),
          MathHelp.clamp(tpos.y, other.getPos().y-other.getHeight()/2, other.getPos().y+other.getHeight()/2));
          other.setClosest(rectRelocate);
          Vector2 test = Vector2.abs(rectRelocate.subtract(tpos));
          double radiusSquare = (width/2)*(width/2);
          if (test.magsquare() <= radiusSquare) {return test.subtract(new Vector2(Math.sqrt(radiusSquare-test.y*test.y), Math.sqrt(radiusSquare-test.x*test.x)));}
        }
        else {
          double totRadius = (other.getWidth()+this.width)/2;
          Vector2 test = Vector2.abs(other.getPos().subtract(tpos));
          if (test.magsquare() <= totRadius*totRadius) {return test.subtract(totRadius);}
        }
      }
    }
    return null;
  }

  public double snapTo(Collider other, boolean isX) {
    if (!round) {
      if (!other.isRound()) {
        return isX ? (this.width+other.getWidth())/2+OFFSET : (this.height+other.getHeight())/2+OFFSET;
      }
      else {

      }
    }
    else {
      Vector2 pos = getPos();
      if (!other.isRound()) {
        Vector2 dist = Vector2.abs(other.getPos().subtract(pos));
        if (isX) {
          if (dist.y>other.getHeight()/2) {
            double b = dist.y-other.getHeight()/2;
            double c = this.width/2;
            return Math.sqrt(c*c-b*b)+other.getWidth()/2+OFFSET;
          } else {return (this.width+other.getWidth())/2+OFFSET;}
        }
        else {
          if (dist.x>other.getWidth()/2) {
            double b = dist.x-other.getWidth()/2;
            double c = this.height/2;
            return Math.sqrt(c*c-b*b)+other.getHeight()/2+OFFSET;
          } else {return (this.height+other.getHeight())/2+OFFSET;}
        }
      }
      else {
        double b = isX ? pos.y-other.getPos().y : pos.x-other.getPos().x;
        double c = (this.width+other.getWidth())/2;
        return Math.sqrt(c*c-b*b)+OFFSET;
      }
    }
    return 0;
  }

  public void collide(Ray ray) {
    Vector2 pos = getPos();
    if (round) {
      ray.roundCollision(pos, width/2, this);
    }
    else {
      Vector2 topLeft = new Vector2(pos.x-width/2, pos.y-height/2);
      Vector2 botRight = new Vector2(pos.x+width/2, pos.y+height/2);
      Vector2 topRight = new Vector2(pos.x+width/2, pos.y-height/2);
      Vector2 botLeft = new Vector2(pos.x-width/2, pos.y+height/2);
      ray.squareCollision(topLeft, botLeft, this);
      ray.squareCollision(topLeft, topRight, this);
      ray.squareCollision(topRight, botRight, this);
      ray.squareCollision(botLeft, botRight, this);
    }
  }
}

/**
LEGACY COLLISION
**/
/*
Vector2 topLeft = new Vector2(position.x-width/2, position.y-height/2);
Vector2 botRight = new Vector2(position.x+width/2, position.y+height/2);
Vector2 otherTopLeft = new Vector2(other.position.x-other.getWidth()/2, other.position.y-other.getHeight()/2);
Vector2 otherBotRight = new Vector2(other.position.x+other.getWidth()/2, other.position.y+other.getHeight()/2);
if (other!=this) {
if (((topLeft.x >= otherTopLeft.x && topLeft.x <= otherBotRight.x) || (botRight.x >= otherTopLeft.x && botRight.x <= otherBotRight.x)
|| (otherTopLeft.x >= topLeft.x && otherTopLeft.x <= botRight.x) || (otherBotRight.x >= topLeft.x && otherBotRight.x <= botRight.x))
&& ((topLeft.y >= otherTopLeft.y && topLeft.y <= otherBotRight.y) || (botRight.y >= otherTopLeft.y && botRight.y <= otherBotRight.y)
|| (otherTopLeft.y >= topLeft.y && otherTopLeft.y <= botRight.y) || (otherBotRight.y >= topLeft.y && otherBotRight.y <= botRight.y)
)) {return new Vector2(Math.abs(other.position.x-this.position.x)-(other.getWidth()+this.width)/2, Math.abs(other.position.y-this.position.y)-(other.getHeight()+this.height)/2); }
}
return null;
*/
