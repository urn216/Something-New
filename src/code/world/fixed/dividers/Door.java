package code.world.fixed.dividers;

import mki.math.vector.Vector2;

import code.world.Collider;
import code.world.Tile;

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
  private boolean highlight;
  private boolean open;
  
  /**
  * Constructor for Door objects
  */
  public Door(double x, double y, Direction direction, Scene scene) {
    this.scene = scene;
    this.direction = direction;
    origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    if (direction == Direction.North) {
      position = new Vector2((x+0.5)*Tile.TILE_SIZE, (y    )*Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+Wall.WALL_THICKNESS;
      height = Wall.WALL_THICKNESS;
      colliders.add(new Collider.Square(new Vector2(-25, 0), 24-Wall.WALL_THICKNESS/4, height-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2( 25, 0), 24-Wall.WALL_THICKNESS/4, height-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2(), 28.5, height-4, true, this));
    }
    else if (direction == Direction.West) {
      position = new Vector2((x    )*Tile.TILE_SIZE, (y+0.5)*Tile.TILE_SIZE);
      width = Wall.WALL_THICKNESS;
      height = Tile.TILE_SIZE+Wall.WALL_THICKNESS;
      colliders.add(new Collider.Square(new Vector2(0, -25), width-Wall.WALL_THICKNESS/4, 24-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2(0,  25), width-Wall.WALL_THICKNESS/4, 24-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2(), width-4, 28.5, true, this));
    }
    else if (direction == Direction.South) {
      position = new Vector2((x+0.5)*Tile.TILE_SIZE, (y+1  )*Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+Wall.WALL_THICKNESS;
      height = Wall.WALL_THICKNESS;
      colliders.add(new Collider.Square(new Vector2(-25, 0), 24-Wall.WALL_THICKNESS/4, height-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2( 25, 0), 24-Wall.WALL_THICKNESS/4, height-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2(), 28.5, height-4, true, this));
    }
    else if (direction == Direction.East) {
      position = new Vector2((x+1  )*Tile.TILE_SIZE, (y+0.5)*Tile.TILE_SIZE);
      width = Wall.WALL_THICKNESS;
      height = Tile.TILE_SIZE+Wall.WALL_THICKNESS;
      colliders.add(new Collider.Square(new Vector2(0, -25), width-Wall.WALL_THICKNESS/4, 24-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2(0,  25), width-Wall.WALL_THICKNESS/4, 24-Wall.WALL_THICKNESS/4, true, this));
      colliders.add(new Collider.Square(new Vector2(), width-4, 28.5, true, this));
    }
    colliders.add(new Collider.Square(new Vector2(), Tile.TILE_SIZE/2, Tile.TILE_SIZE/2, false, this));
  }
  
  public void doTrigger() {
    highlight = true;
  }
  
  public void undoTrigger() {
    highlight = false;
  }
  
  public void activate(Unit user) {
    colliders.get(2).setVoid();
    open = true;
  }
  
  public void deactivate(Unit user) {
    for (Unit i : getTile().getNBUs()) {
      if (colliders.get(2).collide(i.getColls().get(0))!=null) {return;}
    }
    colliders.get(2).setSolid();
    open = false;
  }
  
  public void toggle(Unit user) {
    for (Unit i : getTile().getNBUs()) {
      if (colliders.get(2).collide(i.getColls().get(0))!=null) {return;}
    }
    colliders.get(2).toggle();
    open = !open;
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
    g.setColor(Wall.WALL_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, Wall.WALL_THICKNESS*z, Wall.WALL_THICKNESS*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-Wall.WALL_THICKNESS)*z-conX, (position.y+height/2-Wall.WALL_THICKNESS)*z-conY, Wall.WALL_THICKNESS*z, Wall.WALL_THICKNESS*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, Wall.WALL_THICKNESS*z, Wall.WALL_THICKNESS*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-Wall.WALL_THICKNESS)*z-conX, (position.y+height/2-Wall.WALL_THICKNESS)*z-conY, Wall.WALL_THICKNESS*z, Wall.WALL_THICKNESS*z));
  }
}
