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
  public static final double WALL_THICKNESS_U = 18;
  public static final double WALL_COLLIDER_LENGTH_U = Tile.TILE_SIZE_U+Wall.WALL_THICKNESS_U*0.75;

  public static final double WALL_HEIGHT_M = 2.5;

  public static final Color WALL_COLOUR = new Color(50, 50, 50);
  
  /**
  * Constructor for Wall objects
  */
  public Wall(double x, double y, Direction direction, Scene scene) {
    this.scene = scene;
    this.direction = direction;
    origin = new Vector2(x*Tile.TILE_SIZE_U, y*Tile.TILE_SIZE_U);
    double xOff = 0, yOff = 0;

    switch (direction) {
      case North:
        xOff = 0.5;
        width = Tile.TILE_SIZE_U+WALL_THICKNESS_U;
        height = WALL_THICKNESS_U;
      break;
      case West:
        yOff = 0.5;
        width = WALL_THICKNESS_U;
        height = Tile.TILE_SIZE_U+WALL_THICKNESS_U;
      break;
      case South:
        xOff = 0.5;
        yOff = 1.0;
        width = Tile.TILE_SIZE_U+WALL_THICKNESS_U;
        height = WALL_THICKNESS_U;
      break;
      case East:
        xOff = 1.0;
        yOff = 0.5;
        width = WALL_THICKNESS_U;
        height = Tile.TILE_SIZE_U+WALL_THICKNESS_U;
      break;
    
      default: throw new RuntimeException("Invalid direction for type Wall");
    }

    this.renderedBody = new Quad(
      new Vector3((x+xOff)*Tile.TILE_SIZE_M, WALL_HEIGHT_M/2, -(y+yOff)*Tile.TILE_SIZE_M), 
      (width -WALL_THICKNESS_U/4)*Tile.SCALE_U_TO_M, 
      WALL_HEIGHT_M,
      (height-WALL_THICKNESS_U/4)*Tile.SCALE_U_TO_M,
      1,
      new Material(new Vector3I(150), 0f, new Vector3())
    );
    colliders.add(new Collider.Square(this, new Vector2(), width-WALL_THICKNESS_U/4, height-WALL_THICKNESS_U/4, Collider.FLAG_SOLID));
  }

  @Override
  public int getShape() {
    return 1<<((this.direction.ordinal()/2)+Tile.OFFSET_BORDER);
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
    Vector2 position = getPosition();

    g.setColor(WALL_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, WALL_THICKNESS_U*z, WALL_THICKNESS_U*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-WALL_THICKNESS_U)*z-conX, (position.y+height/2-WALL_THICKNESS_U)*z-conY, WALL_THICKNESS_U*z, WALL_THICKNESS_U*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, WALL_THICKNESS_U*z, WALL_THICKNESS_U*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-WALL_THICKNESS_U)*z-conX, (position.y+height/2-WALL_THICKNESS_U)*z-conY, WALL_THICKNESS_U*z, WALL_THICKNESS_U*z));
  }
}
