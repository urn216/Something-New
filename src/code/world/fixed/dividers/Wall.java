package code.world.fixed.dividers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.world.Material;
import mki.world.Model;
import mki.world.RigidBody;
import code.core.Core;
import code.world.Camera;
import code.world.Collider;
import code.world.Tile;
import code.world.fixed.Direction;
import code.world.fixed.Divider;

/**
* Walls and stuff
*/
public class Wall extends Divider {

  private static final Model modelNS = Model.generateMesh("models/wall.obj");
  private static final Model modelEW = Model.generateMesh("models/wall.obj");

  static {
    Wall.modelEW.setRadius(Wall.modelNS.calculateRadius());
    Material mat = new Material(Core.FULL_BRIGHT);
    Wall.modelNS.setMat(mat);
    Wall.modelEW.setMat(mat);

    RigidBody.removeBody(new RigidBody(new Vector3(), modelEW) {
      {setYaw(90);}
    });
  }
  
  /**
  * Constructor for Wall objects
  */
  public Wall(Tile tile, Direction direction) {
    this(tile, direction, modelNS, modelEW);
  }

  public Wall(Tile tile, Direction direction, Model modelNS, Model modelEW) {
    super(tile);
    this.direction = direction;
    double xOff = 0, yOff = 0;

    switch (direction) {
      case South:
        yOff = 1.0;
      case North:
        xOff = 0.5;
        width = Tile.TILE_SIZE_U+DIVIDER_THICKNESS_U;
        height = DIVIDER_THICKNESS_U;
        this.renderedBody = new RigidBody(new Vector3((tile.x+xOff)*Tile.TILE_SIZE_M, 0, -(tile.y+yOff)*Tile.TILE_SIZE_M), modelNS) {};
      break;
      case East:
        xOff = 1.0;
      case West:
        yOff = 0.5;
        width = DIVIDER_THICKNESS_U;
        height = Tile.TILE_SIZE_U+DIVIDER_THICKNESS_U;
        this.renderedBody = new RigidBody(new Vector3((tile.x+xOff)*Tile.TILE_SIZE_M, 0, -(tile.y+yOff)*Tile.TILE_SIZE_M), modelEW) {};
      break;
    
      default: throw new RuntimeException("Invalid direction for type Wall");
    }

    colliders.add(new Collider.Square(this, new Vector2(), width-DIVIDER_THICKNESS_U/4, height-DIVIDER_THICKNESS_U/4, Collider.FLAG_SOLID));
  }
  
  @Override
  public void draw2D(Graphics2D g) {
    Camera cam = getScene().getCam();
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    for (Collider coll : colliders) {
      Collider.Square collider = (Collider.Square) coll;
      g.setColor(DIVIDER_COLOUR);
      g.fill(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, collider.getWidth()*z, collider.getHeight()*z));
      g.setColor(Color.black);
      g.draw(new Rectangle2D.Double((collider.getPos().x-collider.getWidth()/2)*z-conX, (collider.getPos().y-collider.getHeight()/2)*z-conY, (collider.getWidth()*z), (collider.getHeight()*z)));
    }
    Vector2 position = getPosition();

    g.setColor(DIVIDER_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, DIVIDER_THICKNESS_U*z, DIVIDER_THICKNESS_U*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-DIVIDER_THICKNESS_U)*z-conX, (position.y+height/2-DIVIDER_THICKNESS_U)*z-conY, DIVIDER_THICKNESS_U*z, DIVIDER_THICKNESS_U*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, DIVIDER_THICKNESS_U*z, DIVIDER_THICKNESS_U*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-DIVIDER_THICKNESS_U)*z-conX, (position.y+height/2-DIVIDER_THICKNESS_U)*z-conY, DIVIDER_THICKNESS_U*z, DIVIDER_THICKNESS_U*z));
  }
}
