package code.world.fixed.dividers;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.math.vector.Vector3I;
import mki.world.Material;
import mki.world.object.primitive.Quad;
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
    double xOff = 0, yOff = 0;

    switch (direction) {
      case North:
        xOff = 0.5;
        width = Tile.TILE_SIZE+WALL_THICKNESS;
        height = WALL_THICKNESS;
      break;
      case West:
        yOff = 0.5;
        width = WALL_THICKNESS;
        height = Tile.TILE_SIZE+WALL_THICKNESS;
      break;
      case South:
        xOff = 0.5;
        yOff = 1.0;
        width = Tile.TILE_SIZE+WALL_THICKNESS;
        height = WALL_THICKNESS;
      break;
      case East:
        xOff = 1.0;
        yOff = 0.5;
        width = WALL_THICKNESS;
        height = Tile.TILE_SIZE+WALL_THICKNESS;
      break;
    
      default: throw new RuntimeException("Invalid direction for type Wall");
    }

    this.renderedBody = new Quad(
      new Vector3((x+xOff)*Tile.TILE_SIZE, 0, (y+yOff)*Tile.TILE_SIZE), 
      width, 
      1,
      height,
      1,
      new Material(new Vector3I(150), 0f, new Vector3())
    );
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
    Vector3 position = renderedBody.getPosition();

    g.setColor(WALL_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.z-height/2)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-WALL_THICKNESS)*z-conX, (position.z+height/2-WALL_THICKNESS)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.z-height/2)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-WALL_THICKNESS)*z-conX, (position.z+height/2-WALL_THICKNESS)*z-conY, WALL_THICKNESS*z, WALL_THICKNESS*z));
  }
}
