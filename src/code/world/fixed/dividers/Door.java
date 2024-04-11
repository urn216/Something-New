package code.world.fixed.dividers;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.math.vector.Vector3I;
import mki.world.Material;
import mki.world.object.primitive.Quad;
import code.world.Collider;
import code.world.Tile;
import code.world.unit.Player;
import code.world.unit.Unit;
import code.world.fixed.Direction;
import code.world.fixed.WorldObject;
import code.world.scene.Scene;

//import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Doors and stuff
*/
public class Door extends WorldObject {
  public static final int DOOR_WIDTH_U = 48;
  public static final double DOOR_BORDER_WIDTH_U = (Wall.WALL_COLLIDER_LENGTH_U-Door.DOOR_WIDTH_U)/2;
  public static final double DOOR_BORDER_OFFSET_U = (Wall.WALL_COLLIDER_LENGTH_U-DOOR_BORDER_WIDTH_U)/2;

  private boolean highlight;
  private boolean open;
  
  /**
  * Constructor for Door objects
  */
  public Door(double x, double y, Direction direction, Scene scene) {
    this.scene = scene;
    this.direction = direction;
    origin = new Vector2(x*Tile.TILE_SIZE_U, y*Tile.TILE_SIZE_U);
    double xOff = 0, yOff = 0;

    switch (direction) {
      case South:
        yOff = 1.0;
      case North:
        xOff = 0.5;
        width = Tile.TILE_SIZE_U+Wall.WALL_THICKNESS_U;
        height = Wall.WALL_THICKNESS_U;
        colliders.add(new Collider.Square(this, new Vector2(-DOOR_BORDER_OFFSET_U, 0), DOOR_BORDER_WIDTH_U, height-Wall.WALL_THICKNESS_U/4, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2( DOOR_BORDER_OFFSET_U, 0), DOOR_BORDER_WIDTH_U, height-Wall.WALL_THICKNESS_U/4, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2(), DOOR_WIDTH_U, height-8, Collider.FLAG_SOLID));
      break;
      case East:
        xOff = 1.0;
      case West:
        yOff = 0.5;
        width = Wall.WALL_THICKNESS_U;
        height = Tile.TILE_SIZE_U+Wall.WALL_THICKNESS_U;
        colliders.add(new Collider.Square(this, new Vector2(0, -DOOR_BORDER_OFFSET_U), width-Wall.WALL_THICKNESS_U/4, DOOR_BORDER_WIDTH_U, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2(0,  DOOR_BORDER_OFFSET_U), width-Wall.WALL_THICKNESS_U/4, DOOR_BORDER_WIDTH_U, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2(), width-8, DOOR_WIDTH_U, Collider.FLAG_SOLID));
      break;
    
      default: throw new RuntimeException("Invalid direction for type Door");
    }
    
    this.renderedBody = new Quad(
      new Vector3((x+xOff)*Tile.TILE_SIZE_M, Wall.WALL_HEIGHT_M/2, -(y+yOff)*Tile.TILE_SIZE_M), 
      (width -Wall.WALL_THICKNESS_U/2)*Tile.SCALE_U_TO_M, 
      Wall.WALL_HEIGHT_M,
      (height-Wall.WALL_THICKNESS_U/2)*Tile.SCALE_U_TO_M,
      1,
      new Material(new Vector3I(100), 0f, new Vector3())
    );
    colliders.add(new Collider.Square(this, new Vector2(), Tile.TILE_SIZE_U/2, Tile.TILE_SIZE_U/2, Collider.FLAG_TRIGGER_VOL, (u) -> {
      if (u instanceof Player) highlight = true;
      else open();
    }, (u) -> {
      if (u instanceof Player) highlight = false;
      else close();
    }));
  }

  @Override
  public int getShape() {
    return 1<<((this.direction.ordinal()/2)+Tile.OFFSET_BORDER);
  }
  
  public void open() {
    colliders.get(2).removeSolidity();
    this.renderedBody.setPosition(new Vector3(renderedBody.getPosition().x, Wall.WALL_HEIGHT_M/2+2, renderedBody.getPosition().z));
    open = true;
  }
  
  public void close() {
    for (Unit i : getTile().getNBUs()) {
      if (colliders.get(2).collide(i.getColliders().get(0))!=null) {return;}
    }
    colliders.get(2).addSolidity();
    this.renderedBody.setPosition(new Vector3(renderedBody.getPosition().x, Wall.WALL_HEIGHT_M/2, renderedBody.getPosition().z));
    open = false;
  }
  
  public void toggle() {
    if (open) close();
    else open();
  }

  @Override
  public void use(Unit user) {
    toggle();
  }
  
  @Override
  public void draw(Graphics2D g) {
    double z = scene.getCam().getZoom();
    double conX = scene.getCam().conX();
    double conY = scene.getCam().conY();
    for (Collider col : colliders) {
      Collider.Square collider = (Collider.Square) col;
      if (collider.isSolid()) {
        g.setColor(Wall.WALL_COLOUR);
        g.fill(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
        g.setColor(Color.black);
        g.draw(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
      }
    }
    if (highlight) {
      Collider.Square door = (Collider.Square)colliders.get(2);
      g.setColor(Color.white);
      g.draw(new Rectangle2D.Double((door.getPos().x-door.getWidth()/2)*z-conX, (door.getPos().y-door.getHeight()/2)*z-conY, door.getWidth()*z, door.getHeight()*z));
    }
    Vector2 position = getPosition();
    
    g.setColor(Wall.WALL_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, Wall.WALL_THICKNESS_U*z, Wall.WALL_THICKNESS_U*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-Wall.WALL_THICKNESS_U)*z-conX, (position.y+height/2-Wall.WALL_THICKNESS_U)*z-conY, Wall.WALL_THICKNESS_U*z, Wall.WALL_THICKNESS_U*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, Wall.WALL_THICKNESS_U*z, Wall.WALL_THICKNESS_U*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-Wall.WALL_THICKNESS_U)*z-conX, (position.y+height/2-Wall.WALL_THICKNESS_U)*z-conY, Wall.WALL_THICKNESS_U*z, Wall.WALL_THICKNESS_U*z));
  }
}
