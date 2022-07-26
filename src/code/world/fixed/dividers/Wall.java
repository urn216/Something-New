package code.world.fixed.dividers;

import code.core.Scene;

import code.math.Vector2;

import code.world.Camera;
import code.world.Collider;
import code.world.Tile;

import code.world.fixed.WorldObject;

//import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Walls and stuff
*/
public class Wall extends WorldObject
{
  private double thickness = 10;

  /**
  * Constructor for Wall objects
  */
  public Wall(double x, double y, String dir, String type, Scene scene)
  {
    this.scene = scene;
    this.type = type;
    this.dir = dir;
    origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    if (dir.equals("Up")) {
      position = new Vector2(x*Tile.TILE_SIZE+Tile.TILE_SIZE/2, y*Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+thickness;
      height = thickness;
    }
    else if (dir.equals("Left")) {
      position = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE+Tile.TILE_SIZE/2);
      width = thickness;
      height = Tile.TILE_SIZE+thickness;
    }
    else if (dir.equals("Down")) {
      position = new Vector2(x*Tile.TILE_SIZE+Tile.TILE_SIZE/2, y*Tile.TILE_SIZE+Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+thickness;
      height = thickness;
    }
    else if (dir.equals("Right")) {
      position = new Vector2(x*Tile.TILE_SIZE+Tile.TILE_SIZE, y*Tile.TILE_SIZE+Tile.TILE_SIZE/2);
      width = thickness;
      height = Tile.TILE_SIZE+thickness;
    }
    colliders.add(new Collider(new Vector2(), width-thickness/4, height-thickness/4, true, this));
  }

  public void draw(Graphics2D g, Camera cam) {
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    if (type.equals("Wall")) {
      for (Collider collider : colliders) {
        g.setColor(col);
        g.fill(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
        g.setColor(Color.black);
        g.draw(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, (collider.getWidth()*z), (collider.getHeight()*z)));
      }
      g.setColor(col);
      g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, thickness*z, thickness*z));
      g.fill(new Rectangle2D.Double((position.x+width/2-thickness)*z-conX, (position.y+height/2-thickness)*z-conY, thickness*z, thickness*z));
      g.setColor(Color.black);
      g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, thickness*z, thickness*z));
      g.draw(new Rectangle2D.Double((position.x+width/2-thickness)*z-conX, (position.y+height/2-thickness)*z-conY, thickness*z, thickness*z));
    }
  }
}
