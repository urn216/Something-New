package code.world.fixed;

import mki.math.vector.Vector2;

import code.world.Camera;
import code.world.Collider;
import code.world.RigidBody;
import code.world.Tile;
import code.world.scene.Scene;
import code.world.unit.Unit;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Write a description of class WorldObject here.
*
* @author (your name)
* @version (a version number or a date)
*/
public abstract class WorldObject implements RigidBody, Comparable<WorldObject> {

  protected Color col = Color.gray;
  protected Direction direction = Direction.North;
  protected Vector2 position;
  protected Vector2 origin;
  protected double width;
  protected double height;
  protected List<Collider> colliders = new ArrayList<Collider>();
  protected Scene scene;

  public Vector2 getOrigin() {return origin;}

  public Vector2 getPos() {return position;}

  public Vector2 getVel() {return new Vector2();}

  public List<Collider> getColls() {return colliders;}

  public Scene getScene() {return scene;}

  public Tile getTile() {return scene.getTile(position);}

  public void setOrigin(Vector2 pos) {origin = pos;}

  public void setPos(Vector2 pos) {position = pos;}

  public void setParent(Scene s) {scene = s;}

  public void takeDamage(double damage) {}

  public void doTrigger() {}

  public void undoTrigger() {}

  public void activate(Unit user) {}

  public void deactivate(Unit user) {}

  public void toggle(Unit user) {}

  public void move(double xOff, double yOff) {
    position = position.add(xOff, yOff);
    origin = origin.add(xOff, yOff);
  }

  public void move(Vector2 offset) {
    position = position.add(offset);
    origin = origin.add(offset);
  }

  public void setColour(Color col) {
    this.col = col;
  }

  public void draw(Graphics2D g, Camera cam) {
    g.setColor(col);
    g.fill(new Rectangle2D.Double(origin.x-cam.conX(), origin.y-cam.conY(), width, height));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double(origin.x-cam.conX(), origin.y-cam.conY(), width, height));
  }

  public String toString() {
    return this.getClass().getSimpleName()+" "+(int)(origin.x/Tile.TILE_SIZE)+" "+(int)(origin.y/Tile.TILE_SIZE)+" "+direction;
  }

  public int hashCode() {
    return Integer.hashCode(((int)position.x))^Integer.hashCode(((int)position.y))^direction.hashCode();
  }

  public int compareTo(WorldObject other) {
    int one = Integer.compare((int)position.x, (int)other.position.x);   //this.position.compareTo(other.position);
    if (one == 0) {
      one = Integer.compare((int)position.y, (int)other.position.y);
      if (one == 0) {
        one = this.getClass().getName().compareTo(other.getClass().getName());
        if (one == 0) {
          one = this.direction.compareTo(other.direction);
        }
      }
    }
    return one;
  }

  public boolean equals(Object ot) {
    if (ot.getClass()!=this.getClass()) {return false;}
    WorldObject other = (WorldObject)ot;
    return (int)position.x==(int)other.position.x&&(int)position.y==(int)other.position.y&&direction == other.direction;
  }
}
