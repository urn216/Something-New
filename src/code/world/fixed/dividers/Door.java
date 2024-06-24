package code.world.fixed.dividers;

import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.world.Material;
import mki.world.Model;
import mki.world.RigidBody;
import code.core.Core;
import code.world.Camera;
import code.world.Collider;
import code.world.Tile;
import code.world.unit.Player;
import code.world.unit.Unit;
import code.world.fixed.Direction;
import code.world.fixed.Divider;

//import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Doors and stuff
*/
public class Door extends Divider {
  public static final int DOOR_WIDTH_U = 48;
  public static final double DOOR_BORDER_WIDTH_U = (Divider.DIVIDER_COLLIDER_LENGTH_U-Door.DOOR_WIDTH_U)/2;
  public static final double DOOR_BORDER_OFFSET_U = (Divider.DIVIDER_COLLIDER_LENGTH_U-Door.DOOR_BORDER_WIDTH_U)/2;

  private boolean highlight;
  private boolean open;
  
  /**
  * Constructor for Door objects
  */
  public Door(Tile tile, Direction direction) {
    super(tile);
    this.direction = direction;
    double xOff = 0, yOff = 0;

    switch (direction) {
      case South:
        yOff = 1.0;
      case North:
        xOff = 0.5;
        width = Tile.TILE_SIZE_U+Divider.DIVIDER_THICKNESS_U;
        height = Divider.DIVIDER_THICKNESS_U;
        colliders.add(new Collider.Square(this, new Vector2(-DOOR_BORDER_OFFSET_U, 0), DOOR_BORDER_WIDTH_U, height-Divider.DIVIDER_THICKNESS_U/4, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2( DOOR_BORDER_OFFSET_U, 0), DOOR_BORDER_WIDTH_U, height-Divider.DIVIDER_THICKNESS_U/4, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2(), DOOR_WIDTH_U, height-8, Collider.FLAG_SOLID));
        this.renderedBody = new DoorModel(new Vector3((tile.x+xOff)*Tile.TILE_SIZE_M, 0, -(tile.y+yOff)*Tile.TILE_SIZE_M), false);
      break;
      case East:
        xOff = 1.0;
      case West:
        yOff = 0.5;
        width = Divider.DIVIDER_THICKNESS_U;
        height = Tile.TILE_SIZE_U+Divider.DIVIDER_THICKNESS_U;
        colliders.add(new Collider.Square(this, new Vector2(0, -DOOR_BORDER_OFFSET_U), width-Divider.DIVIDER_THICKNESS_U/4, DOOR_BORDER_WIDTH_U, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2(0,  DOOR_BORDER_OFFSET_U), width-Divider.DIVIDER_THICKNESS_U/4, DOOR_BORDER_WIDTH_U, Collider.FLAG_SOLID));
        colliders.add(new Collider.Square(this, new Vector2(), width-8, DOOR_WIDTH_U, Collider.FLAG_SOLID));
        this.renderedBody = new DoorModel(new Vector3((tile.x+xOff)*Tile.TILE_SIZE_M, 0, -(tile.y+yOff)*Tile.TILE_SIZE_M), true);
      break;
    
      default: throw new RuntimeException("Invalid direction for type Door");
    }
    
    colliders.add(new Collider.Square(this, new Vector2(), Tile.TILE_SIZE_U/2, Tile.TILE_SIZE_U/2, Collider.FLAG_TRIGGER_VOL, (u) -> {
      if (u instanceof Player) highlight = true;
      else open();
    }, (u) -> {
      if (u instanceof Player) highlight = false;
      else close();
    }));
  }
  
  public void open() {
    colliders.get(2).removeSolidity();
    ((DoorModel)this.renderedBody).openDoor();
    open = true;
  }
  
  public void close() {
    for (Unit i : getTile().getNBUs()) {
      if (colliders.get(2).collide(i.getColliders().get(0))!=null) {return;}
    }
    colliders.get(2).addSolidity();
    ((DoorModel)this.renderedBody).closeDoor();
    open = false;
  }
  
  public void toggle() {
    if (open) close();
    else open();
  }

  public boolean isOpen() {
    return open;
  }

  @Override
  public void use(Unit user) {
    toggle();
  }
  
  @Override
  public void draw2D(Graphics2D g) {
    Camera cam = getScene().getCam();
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    for (Collider col : colliders) {
      Collider.Square collider = (Collider.Square) col;
      if (collider.isSolid()) {
        g.setColor(Divider.DIVIDER_COLOUR);
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
    
    g.setColor(Divider.DIVIDER_COLOUR);
    g.fill(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, Divider.DIVIDER_THICKNESS_U*z, Divider.DIVIDER_THICKNESS_U*z));
    g.fill(new Rectangle2D.Double((position.x+width/2-Divider.DIVIDER_THICKNESS_U)*z-conX, (position.y+height/2-Divider.DIVIDER_THICKNESS_U)*z-conY, Divider.DIVIDER_THICKNESS_U*z, Divider.DIVIDER_THICKNESS_U*z));
    g.setColor(Color.black);
    g.draw(new Rectangle2D.Double((position.x-width/2)*z-conX, (position.y-height/2)*z-conY, Divider.DIVIDER_THICKNESS_U*z, Divider.DIVIDER_THICKNESS_U*z));
    g.draw(new Rectangle2D.Double((position.x+width/2-Divider.DIVIDER_THICKNESS_U)*z-conX, (position.y+height/2-Divider.DIVIDER_THICKNESS_U)*z-conY, Divider.DIVIDER_THICKNESS_U*z, Divider.DIVIDER_THICKNESS_U*z));
  }
}

class DoorModel extends RigidBody {
  
  private static final Model modelClosedNS = Model.generateMesh("models/door_closed.obj");
  private static final Model modelClosedEW = Model.generateMesh("models/door_closed.obj");
  private static final Model modelOpenNS = Model.generateMesh("models/door_open.obj");
  private static final Model modelOpenEW = Model.generateMesh("models/door_open.obj");

  private final boolean EW;

  static {
    DoorModel.modelClosedEW.setRadius(DoorModel.modelClosedNS.calculateRadius());
    DoorModel.modelOpenEW.setRadius(DoorModel.modelOpenNS.calculateRadius());
    Material mat = new Material(Core.FULL_BRIGHT);
    DoorModel.modelClosedNS.setMat(mat);
    DoorModel.modelClosedEW.setMat(mat);
    DoorModel.modelOpenNS.setMat(mat);
    DoorModel.modelOpenEW.setMat(mat);

    RigidBody.removeBody(new RigidBody(new Vector3(), modelClosedEW) {
      {setYaw(90);}
    });
    RigidBody.removeBody(new RigidBody(new Vector3(), modelOpenEW) {
      {setYaw(90);}
    });
  }

  public DoorModel(Vector3 position, boolean EW) {
    super(position, EW ? modelClosedEW : modelClosedNS);
    this.EW = EW;
  }
  
  public void openDoor() {
    this.model = EW ? modelOpenEW : modelOpenNS;
  }

  public void closeDoor() {
    this.model = EW ? modelClosedEW : modelClosedNS;
  }
}
