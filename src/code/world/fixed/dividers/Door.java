package code.world.fixed.dividers;

import code.core.Scene;

import code.math.Vector2;

import code.world.Camera;
import code.world.Collider;
import code.world.Tile;

import code.world.unit.Unit;

import code.world.fixed.WorldObject;

//import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Doors and stuff
*/
public class Door extends WorldObject
{
  private boolean highlight;
  private boolean open;

  private double thickness = 10;

  /**
  * Constructor for Door objects
  */
  public Door(double x, double y, String dir, String type, Scene scene)
  {
    this.scene = scene;
    this.type = type;
    this.dir = dir;
    origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    if (dir.equals("Up")) {
      position = new Vector2(x*Tile.TILE_SIZE+Tile.TILE_SIZE/2, y*Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+thickness;
      height = thickness;
      colliders.add(new Collider(new Vector2(-25, 0), 24-thickness/4, height-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(25, 0), 24-thickness/4, height-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(), 28.5, height-3.5, true, this));
    }
    else if (dir.equals("Left")) {
      position = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE+Tile.TILE_SIZE/2);
      width = thickness;
      height = Tile.TILE_SIZE+thickness;
      colliders.add(new Collider(new Vector2(0, -25), width-thickness/4, 24-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(0, 25), width-thickness/4, 24-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(), width-3.5, 28.5, true, this));
    }
    else if (dir.equals("Down")) {
      position = new Vector2(x*Tile.TILE_SIZE+Tile.TILE_SIZE/2, y*Tile.TILE_SIZE+Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+thickness;
      height = thickness;
      colliders.add(new Collider(new Vector2(-25, 0), 24-thickness/4, height-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(25, 0), 24-thickness/4, height-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(), 28.5, height-3.5, true, this));
    }
    else if (dir.equals("Right")) {
      position = new Vector2(x*Tile.TILE_SIZE+Tile.TILE_SIZE, y*Tile.TILE_SIZE+Tile.TILE_SIZE/2);
      width = thickness;
      height = Tile.TILE_SIZE+thickness;
      colliders.add(new Collider(new Vector2(0, -25), width-thickness/4, 24-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(0, 25), width-thickness/4, 24-thickness/4, true, this));
      colliders.add(new Collider(new Vector2(), width-3.5, 28.5, true, this));
    }
    colliders.add(new Collider(new Vector2(), Tile.TILE_SIZE/2, Tile.TILE_SIZE/2, false, this));
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
      if (colliders.get(2).touching(i.getColls().get(0))!=null) {return;}
    }
    colliders.get(2).setSolid();
    open = false;
  }

  public void toggle(Unit user) {
    for (Unit i : getTile().getNBUs()) {
      if (colliders.get(2).touching(i.getColls().get(0))!=null) {return;}
    }
    colliders.get(2).toggle();
    open = !open;
  }

  public void draw(Graphics2D g, Camera cam) {
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    if (type.equals("Door")) {
      for (Collider collider : colliders) {
        if (collider.isSolid()) {
          g.setColor(Color.gray);
          g.fill(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
          g.setColor(Color.black);
          g.draw(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
        }
      }
      if (highlight) {
        Collider door = colliders.get(2);
        g.setColor(Color.white);
        g.draw(new Rectangle2D.Double((door.getPos().x-door.getWidth()/2)*z-conX, (door.getPos().y-door.getHeight()/2)*z-conY, door.getWidth()*z, door.getHeight()*z));
      }
      g.setColor(Color.gray);
      g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, thickness*z, thickness*z));
      g.fill(new Rectangle2D.Double((position.x+width/2-thickness)*z-conX, (position.y+height/2-thickness)*z-conY, thickness*z, thickness*z));
      g.setColor(Color.black);
      g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, thickness*z, thickness*z));
      g.draw(new Rectangle2D.Double((position.x+width/2-thickness)*z-conX, (position.y+height/2-thickness)*z-conY, thickness*z, thickness*z));
    }
  }
}
