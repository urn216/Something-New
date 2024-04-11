package code.world.fixed;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
// import mki.math.vector.Vector3I;
// import mki.world.Material;
// import mki.world.object.primitive.Face;
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

  @Override
  public Vector2 getPosition() {return new Vector2(renderedBody.getPosition().x*Tile.SCALE_M_TO_U, -renderedBody.getPosition().z*Tile.SCALE_M_TO_U);}

  @Override
  public Vector2 getVelocity() {return new Vector2();}

  @Override
  public List<Collider> getColliders() {return colliders;}

  @Override
  public Scene getScene() {return scene;}

  public Tile getTile() {return scene.getTile(getPosition());}

  @Override
  public mki.world.RigidBody getRenderedBody() {
    return renderedBody;
  }

  public abstract int getShape();

  public void setOrigin(Vector2 pos) {origin = pos;}

  @Override
  public void setPosition(Vector2 position) {renderedBody.setPosition(new Vector3(position.x*Tile.SCALE_U_TO_M, Tile.TILE_SIZE_U/4*Tile.SCALE_U_TO_M, -position.y*Tile.SCALE_U_TO_M));}

  public void setParent(Scene s) {scene = s;}

  @Override
  public void takeDamage(double damage, Vector2 location) {
    // double size = damage*Tile.UNIT_SCALE_DOWN/20+Tile.UNIT_SCALE_DOWN*3;
    // new Face(new Vector3(location.x*Tile.UNIT_SCALE_DOWN, 8*Tile.UNIT_SCALE_DOWN, -location.y*Tile.UNIT_SCALE_DOWN-0.008), size, size, new Material(new Vector3I(150), 0, new Vector3(), "decal/hole.png"))
    // .setRoll(Math.random()*360);
  }

  @Override
  public void use(Unit user) {}

  public void move(double xOff, double yOff) {
    renderedBody.setPosition(renderedBody.getPosition().add(xOff*Tile.SCALE_U_TO_M, 0, -yOff*Tile.SCALE_U_TO_M));
    origin = origin.add(xOff, yOff);
  }

  public void move(Vector2 offset) {
    renderedBody.setPosition(renderedBody.getPosition().add(offset.x*Tile.SCALE_U_TO_M, 0, -offset.y*Tile.SCALE_U_TO_M));
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
    return this.getClass().getSimpleName()+" "+(int)(origin.x/Tile.TILE_SIZE_U)+" "+(int)(origin.y/Tile.TILE_SIZE_U)+" "+direction;
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
