package code.world.fixed.dividers;

import mki.math.vector.Vector2;

import code.world.Collider;
import code.world.Tile;
import code.world.fixed.Direction;
import code.world.fixed.WorldObject;
import code.world.scene.Scene;

//import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Walls and stuff
*/
public class Wall extends WorldObject {
  public static final double WALL_THICKNESS = 10;
  public static final Color WALL_COLOUR = new Color(50, 50, 50);
  
  /**
  * Constructor for Wall objects
  */
  public Wall(double x, double y, Direction direction, Scene scene) {
    this.scene = scene;
    this.direction = direction;
    origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    if (direction == Direction.North) {
      position = new Vector2((x+0.5)*Tile.TILE_SIZE, (y    )*Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+WALL_THICKNESS;
      height = WALL_THICKNESS;
    }
    else if (direction == Direction.West) {
      position = new Vector2((x    )*Tile.TILE_SIZE, (y+0.5)*Tile.TILE_SIZE);
      width = WALL_THICKNESS;
      height = Tile.TILE_SIZE+WALL_THICKNESS;
    }
    else if (direction == Direction.South) {
      position = new Vector2((x+0.5)*Tile.TILE_SIZE, (y+1  )*Tile.TILE_SIZE);
      width = Tile.TILE_SIZE+WALL_THICKNESS;
      height = WALL_THICKNESS;
    }
    else if (direction == Direction.East) {
      position = new Vector2((x+1  )*Tile.TILE_SIZE, (y+0.5)*Tile.TILE_SIZE);
      width = WALL_THICKNESS;
      height = Tile.TILE_SIZE+WALL_THICKNESS;
    }
    colliders.add(new Collider.Square(new Vector2(), width-WALL_THICKNESS/4, height-WALL_THICKNESS/4, true, this));
  }
  
  @Override
  public void draw(Graphics2D g) {
    double z = scene.getCam().getZoom();
    double conX = scene.getCam().conX();
    double conY = scene.getCam().conY();
    for (Collider coll : colliders) {
      Collider.Square collider = (Collider.Square) coll;
      g.setColor(WALL_COLOUR);
      g.fill(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
      g.setColor(Color.black);
      g.draw(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, (collider.getWidth()*z), (collider.getHeight()*z)));
    }
    g.setColor(WALL_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-WALL_THICKNESS)*z-conX, (position.y+height/2-WALL_THICKNESS)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-WALL_THICKNESS)*z-conX, (position.y+height/2-WALL_THICKNESS)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
  }
}
