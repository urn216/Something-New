package code.world;

import mki.math.MathHelp;
import mki.math.vector.Vector2;

//import java.util.*;
/**
* Collider class
*/
public abstract class Collider {
  protected static final double OFFSET = 0.000001;
  
  protected boolean shootable;
  protected boolean solid;
  protected boolean trigger;
  protected Vector2 offset; // centre relative to parent
  protected RigidBody parent;
  
  protected Vector2 closest;
  
  public static class Round extends Collider {
    private double radius;
    public Round(Vector2 offset, double radius, boolean solid, RigidBody parent) {
      this.offset = offset;
      this.closest = offset;
      this.radius = radius;
      this.shootable = solid;
      this.solid = solid;
      this.trigger = !solid;
      this.parent = parent;
    }
    
    public double getRadius() {return radius;}
    
    public void setRadius(double radius) {this.radius = radius;}
    
    public Vector2 collide(Collider o) {
      if (o != this) {
        Vector2 opos = o.getPos();
        Vector2 tpos = getPos();
        if (o instanceof Square) {
          Square other = (Square) o;
          Vector2 rectRelocate = new Vector2(
          MathHelp.clamp(tpos.x, opos.x-other.getWidth()/2, opos.x+other.getWidth()/2),
          MathHelp.clamp(tpos.y, opos.y-other.getHeight()/2, opos.y+other.getHeight()/2));
          other.setClosest(rectRelocate);
          Vector2 test = Vector2.abs(rectRelocate.subtract(tpos));
          
          double radiusSquare = radius*radius;
          if (test.magsquare() <= radiusSquare) {return test.subtract(new Vector2(Math.sqrt(radiusSquare-test.y*test.y), Math.sqrt(radiusSquare-test.x*test.x)));}
        }
        else {
          Round other = (Round) o;
          double totRadius = other.radius + this.radius;
          Vector2 test = Vector2.abs(opos.subtract(tpos));
          if (test.magsquare() <= totRadius*totRadius) {return test.subtract(totRadius);}
        }
      }
      return null;
    }
    
    public void collide(Ray ray) {
      Vector2 pos = getPos();
      ray.roundCollision(pos, radius, this);
    }
    
    public double snapTo(Collider o, boolean isX) {
      Vector2 pos = getPos();
      if (o instanceof Square) {
        Square other = (Square) o;
        Vector2 dist = Vector2.abs(other.getPos().subtract(pos));
        if (isX) {
          if (dist.y>other.getHeight()/2) {
            double b = dist.y-other.getHeight()/2;
            double c = radius;
            return Math.sqrt(c*c-b*b)+other.getWidth()/2+OFFSET;
          } 
          return this.radius+other.getWidth()/2+OFFSET;
        }
        if (dist.x>other.getWidth()/2) {
          double b = dist.x-other.getWidth()/2;
          double c = radius;
          return Math.sqrt(c*c-b*b)+other.getHeight()/2+OFFSET;
        } 
        return this.radius+other.getHeight()/2+OFFSET;
      }
      
      Round other = (Round) o;
      double b = isX ? pos.y-other.getPos().y : pos.x-other.getPos().x;
      double c = this.radius+other.radius;
      return Math.sqrt(c*c-b*b)+OFFSET;
    }
  }
  
  public static class Square extends Collider {
    private double width;
    private double height;
    public Square(Vector2 offset, double width, double height, boolean solid, RigidBody parent) {
      this.offset = offset;
      this.closest = offset;
      this.width = width;
      this.height = height;
      this.shootable = solid;
      this.solid = solid;
      this.trigger = !solid;
      this.parent = parent;
    }
    
    public double getWidth() {return width;}
    
    public double getHeight() {return height;}
    
    public void setWidth(double w) {width = w;}
    
    public void setHeight(double h) {height = h;}
    
    public Vector2 collide(Collider o) {
      if (o != this) {
        Vector2 opos = o.getPos();
        Vector2 tpos = getPos();
        if (o instanceof Square) {
          Square other = (Square) o;
          Vector2 test = new Vector2(Math.abs(opos.x-tpos.x)-(other.getWidth()+this.width)/2, Math.abs(opos.y-tpos.y)-(other.getHeight()+this.height)/2);
          if (test.x<=0&&test.y<=0) {return test;}
        }
        else {
          return o.collide(this);
        }
      }
      return null;
    }
    
    public void collide(Ray ray) {
      Vector2 pos = getPos();
      Vector2 topLeft = new Vector2(pos.x-width/2, pos.y-height/2);
      Vector2 botRight = new Vector2(pos.x+width/2, pos.y+height/2);
      Vector2 topRight = new Vector2(pos.x+width/2, pos.y-height/2);
      Vector2 botLeft = new Vector2(pos.x-width/2, pos.y+height/2);
      ray.squareCollision(topLeft, botLeft, this);
      ray.squareCollision(topLeft, topRight, this);
      ray.squareCollision(topRight, botRight, this);
      ray.squareCollision(botLeft, botRight, this);
    }
    
    public double snapTo(Collider o, boolean isX) {
      if (o instanceof Square) {
        Square other = (Square) o;
        return isX ? (this.width+other.getWidth())/2+OFFSET : (this.height+other.getHeight())/2+OFFSET;
      }
      else {
        return 0; //Undefined
      }
    }
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
  
  public RigidBody getParent() {return parent;}
  
  public Vector2 getClosest() {return closest;}
  
  public Vector2 getPos() {return parent.getPos().add(offset);}
  
  public Vector2 getOff() {return offset;}
  
  public void setOff(Vector2 offset) {this.offset = offset;}
  
  public void setClosest(Vector2 v) {closest = v;}
  
  public void move(Vector2 v) {
    offset = offset.add(v);
  }
  
  public abstract Vector2 collide(Collider other);
  
  public abstract double snapTo(Collider other, boolean isX);
  
  public abstract void collide(Ray ray);
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
