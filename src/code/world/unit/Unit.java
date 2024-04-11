package code.world.unit;

import mki.math.MathHelp;
import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.math.vector.Vector3I;
import mki.world.Material;
import mki.world.object.primitive.Quad;
import code.world.Collider;
import code.world.RigidBody;
import code.world.Tile;
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
  protected final mki.world.RigidBody renderedBody;

  protected Scene scene;

  protected Item held;

  protected boolean alive = true;

  protected final List<RigidBody> triggering = new ArrayList<>();

  protected Vector2 movementDirection;
  protected Vector2 lookDirection;
  
  protected Vector2 velocity;
  protected Vector2 acceleration;
  protected Vector2 addAcc = new Vector2();
  protected Collider.Round collider;
  protected boolean updated = false;
  protected double walkF;
  protected double vMax;
  protected double m;
  protected float elasticity = 0f;

  protected Color colour;

  protected double hitPoints;
  protected int hurtFrames;

  public Unit(Scene scene, int radius,
              Vector2 position, Vector2 velocity, Vector2 lookDirection, 
              double walkF, double vMax, double m, double hitPoints, float elasticity) {
    this(scene, radius, Color.white, position, velocity, lookDirection, walkF, vMax, m, hitPoints, elasticity);
  }

  public Unit(Scene scene, int radius, Color colour,
              Vector2 position, Vector2 velocity, Vector2 lookDirection, 
              double walkF, double vMax, double m, double hitPoints, float elasticity) {
    this.scene = scene;
    this.collider = new Collider.Round(this, new Vector2(), radius, Collider.FLAG_SOLID);
    this.colour = colour;
    this.movementDirection = new Vector2();
    this.velocity = velocity;
    this.acceleration = new Vector2();
    this.walkF = walkF;
    this.vMax = vMax;
    this.m = m;
    this.hitPoints = hitPoints;
    this.elasticity = elasticity;
    
    this.renderedBody = new Quad(
      new Vector3(position.x*Tile.SCALE_U_TO_M, radius*2.8*Tile.SCALE_U_TO_M, -position.y*Tile.SCALE_U_TO_M), 
      radius*2*Tile.SCALE_U_TO_M, 
      radius*5.6*Tile.SCALE_U_TO_M, 
      radius*2*Tile.SCALE_U_TO_M, 
      1,
      new Material(new Vector3I(colour.getRed(), colour.getGreen(), colour.getBlue()), 0f, new Vector3())
    );

    this.lookDirection = new Vector2(MathHelp.clamp(lookDirection.x, -90, 90), (lookDirection.y+360)%360);
  }

  @Override
  public Vector2 getPosition() {
    return new Vector2(renderedBody.getPosition().x*Tile.SCALE_M_TO_U, -renderedBody.getPosition().z*Tile.SCALE_M_TO_U);
  }

  public Vector2 getMovementDirection() {
    return movementDirection;
  }

  public Vector2 getLookDirection() {
    return lookDirection;
  }

  @Override
  public Vector2 getVelocity() {
    return velocity;
  }

  public double getHitPoints() {
    return hitPoints;
  }

  @Override
  public List<Collider> getColliders() {
    return List.of(collider);
  }

  @Override
  public Scene getScene() {
    return scene;
  }

  @Override
  public mki.world.RigidBody getRenderedBody() {
    return renderedBody;
  }

  public void offsetPosition(Vector2 position) {
    offsetPosition(position.x, position.y);
  }

  public void offsetPosition(double x, double y) {
    renderedBody.offsetPosition(new Vector3(x*Tile.SCALE_U_TO_M, 0, -y*Tile.SCALE_U_TO_M));
  }

  @Override
  public void setPosition(Vector2 position) {
    setPosition(position.x, position.y);
  }

  public void setPosition(double x, double y) {
    renderedBody.setPosition(new Vector3(
      x                   *Tile.SCALE_U_TO_M, 
      collider.getRadius()*Tile.SCALE_U_TO_M*3, 
     -y                   *Tile.SCALE_U_TO_M
    ));
  }

  public void setMovementDirection(Vector2 direction) {
    this.movementDirection = direction.unitize();
    this.renderedBody.setYaw(Math.atan2(this.movementDirection.x, -this.movementDirection.y)*180/Math.PI); //leg/body rotation?
  }

  public void setLookDirection(double pitch, double yaw) {
    this.lookDirection = new Vector2(MathHelp.clamp(pitch, -90, 90), (yaw+360)%360);
    //head rotation
  }

  public void setVelocity(Vector2 vel) {velocity = vel;}

  public void setHeldItem(Item i) {held = i;}

  public void dropHeldItem() {
    if (held == null) return;
    scene.addUnit(new ItemUnit(scene, held, getPosition(), this.velocity.add((Math.random()-0.5)*vMax/2, (Math.random()-0.5)*vMax/2)));
    held = null;
  }

  public void takeItem(Item item) {
    dropHeldItem();
    held = item;
  }

  @Override
  public void takeDamage(double damage, Vector2 location) {
    hitPoints -= damage;
    hurtFrames = 2;
    renderedBody.setRoll(4);
    if (hitPoints <= 0) {
      hitPoints = 0;
      collider.removeSolidity();
      alive = false;
      dropHeldItem();
      renderedBody.offsetPosition(new Vector3(0, -1.25, 0));
    }
  }

  public boolean isAlive() {return alive;}

  @Override
  public void use(Unit user) {}

  public void update(List<RigidBody> rbs) {
    if (updated || !alive) {
      return;
    }
    updated = true;
    hurtFrames--;
    if (hurtFrames == 0) renderedBody.setRoll(0);
    List<Collider> colliders = new ArrayList<Collider>();
    for (RigidBody rb : rbs) {
      colliders.addAll(rb.getColliders());
    }
    move(colliders);
    trigger(rbs);
  }

  public void move(List<Collider> colliders) {
    step(colliders);
  }

  public void step(List<Collider> colliders) {
    Vector2 inputAcc = movementDirection.scale(walkF/m);
    Vector2 slowAcc = velocity.scale(walkF/(vMax*m)).add(velocity.scale(m/1000));
    acceleration = inputAcc.subtract(slowAcc).add(addAcc);
    velocity = velocity.add(acceleration);

    addAcc = new Vector2();

    // if non-solid
    if (!collider.isSolid()) {
      offsetPosition(velocity);
    }

    // if solid
    else {
      stepX(colliders);
      stepY(colliders);
    }
  }

  protected void stepX(List<Collider> colliders) {
    offsetPosition(velocity.x, 0);
    Collider collided = collision(colliders, true);
    if (collided != null) {
      setPosition(collided.getPos().x-collider.snapTo(collided, true)*Math.signum(velocity.x), -renderedBody.getPosition().z*Tile.SCALE_M_TO_U);
      Vector2 dir = new Vector2(collided.getClosest().subtract(collider.getPos()).unitize());
      velocity = velocity.subtract(dir.scale(dir.dot(velocity)*(1+elasticity)));
    }
  }

  protected void stepY(List<Collider> colliders) {
    offsetPosition(0, velocity.y);
    Collider collided = collision(colliders, false);
    if (collided != null) {
      setPosition(renderedBody.getPosition().x*Tile.SCALE_M_TO_U, collided.getPos().y-collider.snapTo(collided, false)*Math.signum(velocity.y));
      Vector2 dir = new Vector2(collided.getClosest().subtract(collider.getPos()).unitize());
      velocity = velocity.subtract(dir.scale(dir.dot(velocity)*(1+elasticity)));
    }
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

  public void trigger(List<RigidBody> bodies) {
    for (RigidBody obj : bodies) {
      for (Collider other : obj.getColliders()) {
        if (!other.isTrigger()) continue;

        if (collider.collide(other)!=null) {
          if (!triggering.contains(obj)) {
            triggering.add(obj);
            other.enterVolume(this);
          }
        }
        else if (triggering.remove(obj)) {
          other.leaveVolume(this);
        }
      }
    }
  }

  public String toString() {
    return this.getClass().getSimpleName()+
    " "+ renderedBody.getPosition().x*Tile.SCALE_M_TO_U+
    " "+-renderedBody.getPosition().z*Tile.SCALE_M_TO_U+
    " "+velocity.x+
    " "+velocity.y;
  }

  public void draw(Graphics2D g) {
    double z = scene.getCam().getZoom();
    double conX = scene.getCam().conX();
    double conY = scene.getCam().conY();
    Vector2 pos = collider.getPos();
    double rad = collider.getRadius();
    g.setColor(!alive || hurtFrames > 0 ? Color.pink : colour);
    g.fill(new Ellipse2D.Double((pos.x-rad)*z-conX, (pos.y-rad)*z-conY, rad*2*z, rad*2*z));
    g.drawLine(
      (int)(pos.x*z-conX), 
      (int)(pos.y*z-conY), 
      (int)((pos.x+movementDirection.x*37.5)*z-conX), 
      (int)((pos.y+movementDirection.y*37.5)*z-conY)
    );
  }
}
