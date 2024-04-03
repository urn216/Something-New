package code.world.fixed;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
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
  protected mki.world.RigidBody renderedBody;

  protected Color col = Color.gray;
  protected Direction direction = Direction.North;
  protected Vector2 origin;
  protected double width;
  protected double height;
  protected List<Collider> colliders = new ArrayList<Collider>();
  protected Scene scene;

  public Vector2 getOrigin() {return origin;}

  public Vector2 getPosition() {return new Vector2(renderedBody.getPosition().x*Tile.UNIT_SCALE_UP, -renderedBody.getPosition().z*Tile.UNIT_SCALE_UP);}

  public Vector2 getVelocity() {return new Vector2();}

  public List<Collider> getColliders() {return colliders;}

  public Scene getScene() {return scene;}

  public Tile getTile() {return scene.getTile(getPosition());}

  public mki.world.RigidBody getRenderedBody() {
    return renderedBody;
  }

  public void setOrigin(Vector2 pos) {origin = pos;}

  public void setPosition(Vector2 position) {renderedBody.setPosition(new Vector3(position.x*Tile.UNIT_SCALE_DOWN, Tile.TILE_SIZE/4*Tile.UNIT_SCALE_DOWN, -position.y*Tile.UNIT_SCALE_DOWN));}

  public void setParent(Scene s) {scene = s;}

  public void takeDamage(double damage) {}

  @Override
  public void use(Unit user) {}

  public void move(double xOff, double yOff) {
    renderedBody.setPosition(renderedBody.getPosition().add(xOff*Tile.UNIT_SCALE_DOWN, 0, -yOff*Tile.UNIT_SCALE_DOWN));
    origin = origin.add(xOff, yOff);
  }

  public void move(Vector2 offset) {
    renderedBody.setPosition(renderedBody.getPosition().add(offset.x*Tile.UNIT_SCALE_DOWN, 0, -offset.y*Tile.UNIT_SCALE_DOWN));
    origin = origin.add(offset);
  }

  public void setColour(Color col) {
    this.col = col;
  }

  public void draw(Graphics2D g) {
    g.setColor(col);
    g.fill(new Rectangle2D.Double(origin.x-scene.getCam().conX(), origin.y-scene.getCam().conY(), width, height));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double(origin.x-scene.getCam().conX(), origin.y-scene.getCam().conY(), width, height));
  }

  public String toString() {
    return this.getClass().getSimpleName()+" "+(int)(origin.x/Tile.TILE_SIZE)+" "+(int)(origin.y/Tile.TILE_SIZE)+" "+direction;
  }

  public int hashCode() {
    return Integer.hashCode(((int)renderedBody.getPosition().x))^Integer.hashCode(((int)-renderedBody.getPosition().z))^direction.hashCode();
  }

  public int compareTo(WorldObject other) {
    int one = Integer.compare((int)renderedBody.getPosition().x, (int)other.renderedBody.getPosition().x);   //this.position.compareTo(other.position);
    if (one == 0) {
      one = Integer.compare((int)-renderedBody.getPosition().z, (int)-other.renderedBody.getPosition().z);
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
    return (int)renderedBody.getPosition().x==(int)other.renderedBody.getPosition().x&&(int)renderedBody.getPosition().z==(int)other.renderedBody.getPosition().z&&direction == other.direction;
  }
}
