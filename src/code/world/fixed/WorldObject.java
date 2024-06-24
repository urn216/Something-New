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
import java.awt.Color;

//TODO we have dividers, make floor and ceiling equivelant. Floor objects have an offset in addition to shape. 
//TODO If offset puts shape beyond Tile bounds, then add to other Tile(s) too. Floor objects across two or more Tiles block dividers.

/**
* Write a description of class WorldObject here.
*
* @author (your name)
* @version (a version number or a date)
*/
public abstract class WorldObject implements RigidBody, Comparable<WorldObject> {
  protected Tile tile;

  protected mki.world.RigidBody renderedBody;

  protected Color colour = Color.gray;
  protected Direction direction = Direction.North;
  protected double width;
  protected double height;
  protected List<Collider> colliders = new ArrayList<Collider>();

  public WorldObject(Tile tile) {
    this.tile = tile;
  }

  @Override
  public Vector2 getPosition() {
    return new Vector2(renderedBody.getPosition().x*Tile.SCALE_M_TO_U, -renderedBody.getPosition().z*Tile.SCALE_M_TO_U);
  }

  @Override
  public Vector2 getVelocity() {
    return new Vector2();
  }

  @Override
  public List<Collider> getColliders() {
    return colliders;
  }

  @Override
  public Scene getScene() {
    return tile.getScene();
  }

  public Tile getTile() {
    return tile;
  }

  @Override
  public mki.world.RigidBody getRenderedBody() {
    return renderedBody;
  }

  public abstract int getShape();

  @Override
  public void setPosition(Vector2 position) {
    renderedBody.setPosition(new Vector3(position.x*Tile.SCALE_U_TO_M, Tile.TILE_SIZE_U/4*Tile.SCALE_U_TO_M, -position.y*Tile.SCALE_U_TO_M));
  }

  public void setTile(Tile tile) {
    this.tile = tile;
  }

  @Override
  public void takeDamage(double damage, Vector2 location) {
    // double size = damage*Tile.UNIT_SCALE_DOWN/20+Tile.UNIT_SCALE_DOWN*3;
    // new Face(new Vector3(location.x*Tile.UNIT_SCALE_DOWN, 8*Tile.UNIT_SCALE_DOWN, -location.y*Tile.UNIT_SCALE_DOWN-0.008), size, size, new Material(new Vector3I(150), 0, new Vector3(), "decal/hole.png"))
    // .setRoll(Math.random()*360);
  }

  @Override
  public void use(Unit user) {}

  public void setColour(Color colour) {
    this.colour = colour;
  }

  public abstract void draw2D(Graphics2D g);

  public String toString() {
    return this.getClass().getSimpleName()+" "+tile.x+" "+tile.y+" "+direction;
  }

  public int hashCode() {
    return Integer.hashCode(((int)renderedBody.getPosition().x))^Integer.hashCode(((int)-renderedBody.getPosition().z))^direction.hashCode();
  }

  public int compareTo(WorldObject other) {
    int i = Integer.compare((int)renderedBody.getPosition().x, (int)other.renderedBody.getPosition().x);   //this.position.compareTo(other.position);
    if (i == 0) {
      i = Integer.compare((int)-renderedBody.getPosition().z, (int)-other.renderedBody.getPosition().z);
      if (i == 0) {
        i = this.getClass().getName().compareTo(other.getClass().getName());
        if (i == 0) {
          i = this.direction.compareTo(other.direction);
        }
      }
    }
    return i;
  }

  public boolean equals(Object ot) {
    if (ot.getClass()!=this.getClass()) {return false;}
    WorldObject other = (WorldObject)ot;
    return (int)renderedBody.getPosition().x==(int)other.renderedBody.getPosition().x&&(int)renderedBody.getPosition().z==(int)other.renderedBody.getPosition().z&&direction == other.direction;
  }
}
