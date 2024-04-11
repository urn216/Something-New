package code.world;

import mki.math.vector.Vector2;
import mki.math.MathHelp;
// import code.world.fixed.Light;
import code.world.fixed.WorldObject;

import code.world.unit.Unit;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;

/**
* Write a description of class Tile here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class Tile {
  public static final int TILE_SIZE_U = 120;
  public static final double TILE_SIZE_M = 3;

  public static final int MINI_TILE_SIZE = 40;

  //One game-unit is 25mm. Tile size is 3m or 120u
  public static final double SCALE_M_TO_U = TILE_SIZE_U/TILE_SIZE_M;
  public static final double SCALE_U_TO_M = 1/SCALE_M_TO_U;

  public static final int OBJECTS_FLOOR   = 9;
  public static final int OBJECTS_BORDER  = 4;
  public static final int OBJECTS_CEILING = 1;

  public static final int OFFSET_FLOOR   = 0;
  public static final int OFFSET_BORDER  = OFFSET_FLOOR+OBJECTS_FLOOR;
  public static final int OFFSET_CEILING = OFFSET_BORDER+OBJECTS_BORDER;

  private static final int HAS_FLOOR_BIT = 1 << (OFFSET_CEILING + OBJECTS_CEILING);
  
  private final WorldObject[] fixedObjects = new WorldObject[OBJECTS_FLOOR + OBJECTS_BORDER + OBJECTS_CEILING];
  private int occupiedSpace;

  private List<Unit> units = new ArrayList<Unit>();
  private List<Bullet> bullets = new ArrayList<Bullet>();

  private final int x, y;



  /** Whther there's a solid, airtight, wall to the [Left, Right, Up, Down] */
  // private boolean[] walled = {false, false, false, false};
  // private float airPressure = 0f;

  private Tile[][] nb;

  /**
  * Constructor for Tiles
  */
  public Tile(int x, int y, int active) {
    this.x = x;
    this.y = y;
    this.occupiedSpace = active != 0 ? HAS_FLOOR_BIT : 0;
  }

  public void activate() {
    this.occupiedSpace |= HAS_FLOOR_BIT;
  }

  public void deactivate() {
    this.occupiedSpace &= ~HAS_FLOOR_BIT;
  }

  public void toggle() {
    if (isActive()) deactivate();
    else activate();
  }

  public boolean isActive() {return (this.occupiedSpace & HAS_FLOOR_BIT) != 0;}

  public void getNeighbours(Tile[][] map, int x, int y, int mapSX, int mapSY) {
    nb = new Tile[3][3];
    for (int i = Math.max(0, x-1); i < Math.min(mapSX, x+2); i++) {
      for (int j = Math.max(0, y-1); j < Math.min(mapSY, y+2); j++) {
        nb[1+i-x][1+j-y] = map[i][j];
      }
    }
    nb[1][1] = null;
  }

  public WorldObject[] getObjects() {return this.fixedObjects;}

  public List<Unit> getUnits() {return units;}

  public void add(Unit u) {units.add(u);}
  
  public void remove(Unit u) {units.remove(u);}

  public void add(Bullet b) {bullets.add(b);}

  public void remove(Bullet b) {bullets.remove(b);}

  public boolean addObject(WorldObject o) {
    int shape = o.getShape();
    if ((this.occupiedSpace & shape) != 0) return false;

    this.occupiedSpace |= shape;
    for (int i = 0; i < fixedObjects.length; i++) {
      if (((1 << i) & shape) != 0) fixedObjects[i] = o;
    }
    return true;
  }

  public boolean removeObject(WorldObject o) {
    boolean found = false;
    for (int i = 0; i < fixedObjects.length; i++) {
      if (fixedObjects[i] != o) continue;
      fixedObjects[i] = null;
      this.occupiedSpace &= ~(1<<i);
      found = true;
    }
    return found;
  }

  public void clearAll() {
    this.occupiedSpace &= HAS_FLOOR_BIT;
    this.bullets.clear();
    this.units.clear();
    for (int i = 0; i < fixedObjects.length; i++) {
      fixedObjects[i] = null;
    }
  }

  public List<WorldObject> getNBOs() {
    List<WorldObject> objs = new ArrayList<WorldObject>();
    for (int i = 0; i<3; i++) {
      for (int j = 0; j<3; j++) {
        if (nb[i][j]!=null) for (int k = 0; k < fixedObjects.length; k++) {
          if (nb[i][j].getObjects()[k] != null) objs.add(nb[i][j].getObjects()[k]);
        }
      }
    }
    for (int k = 0; k < fixedObjects.length; k++) {
      if (fixedObjects[k] != null) objs.add(fixedObjects[k]);
    }
    return objs;
  }

  public List<Unit> getNBUs() {
    List<Unit> uns = new ArrayList<Unit>();
    for (int i = 0; i<3; i++) {
      for (int j = 0; j<3; j++) {
        if (nb[i][j]!=null) {uns.addAll(nb[i][j].getUnits());}
      }
    }
    uns.addAll(units);
    return uns;
  }

  public void update() {
    if (units.isEmpty()&&bullets.isEmpty()) {return;}
    List<WorldObject> nbOs = getNBOs();
    List<RigidBody> rbs = new ArrayList<RigidBody>(units.size()+fixedObjects.length);
    rbs.addAll(getNBUs());
    rbs.addAll(nbOs);
    for (int i = 0; i<units.size(); i++) {
      Unit unit = units.get(i);
      unit.update(rbs);
      Vector2 unitPos = unit.getPosition().subtract(x*TILE_SIZE_U, y*TILE_SIZE_U).scale(1.0/TILE_SIZE_U).add(1);
      int x = (int)MathHelp.clamp(unitPos.x, 0, 2);
      int y = (int)MathHelp.clamp(unitPos.y, 0, 2);

      if (nb[x][y] != null) {nb[x][y].add(unit); units.remove(i); i--;}
    }

    for (int b = 0; b < bullets.size(); b++) {
      Bullet bullet = bullets.get(b);
      bullet.update(rbs);
      if(!bullet.isAlive()) {
        bullets.remove(b);
        b--;
        continue;
      }
      Vector2 bulletPos = bullet.getPosition().subtract(x*TILE_SIZE_U, y*TILE_SIZE_U).scale(1.0/TILE_SIZE_U).add(1);
      int x = (int)MathHelp.clamp(bulletPos.x, 0, 2);
      int y = (int)MathHelp.clamp(bulletPos.y, 0, 2);

      if (nb[x][y] != null) {nb[x][y].add(bullet); bullets.remove(b); b--;}
    }
  }

  public void draw(Graphics2D g, Camera cam) {
    if (!isActive()) return;
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    g.setColor(Color.lightGray);
    g.fill(new Rectangle2D.Double(x*TILE_SIZE_U*z-conX, y*TILE_SIZE_U*z-conY, TILE_SIZE_U*z, TILE_SIZE_U*z));
    g.setColor(Color.gray);
    g.draw(new Rectangle2D.Double(x*TILE_SIZE_U*z-conX, y*TILE_SIZE_U*z-conY, TILE_SIZE_U*z, TILE_SIZE_U*z));
  }

  public void drawGhost(Graphics2D g, Camera cam, Color ghostColour) {
    double z = cam.getZoom();
    g.setColor(ghostColour);
    g.fill(new Rectangle2D.Double(x*TILE_SIZE_U*z-cam.conX(), y*TILE_SIZE_U*z-cam.conY(), TILE_SIZE_U*z, TILE_SIZE_U*z));
  }

  public void drawLowerObjects(Graphics2D g) {
    for (int i = 0; i < OBJECTS_FLOOR; i++) {
      if (fixedObjects[i] != null) fixedObjects[i].draw(g);
    }
  }

  public void drawUnits(Graphics2D g) {
    for (int i = 0; i < units.size(); i++) {
      units.get(i).draw(g);
    }
  }

  public void drawBullets(Graphics2D g) {
    for (int i = 0; i < bullets.size(); i++) {
      bullets.get(i).draw(g);
    }
  }

  public void drawHigherObjects(Graphics2D g) {
    for (int i = OFFSET_BORDER; i < fixedObjects.length; i++) {
      if (fixedObjects[i] != null) fixedObjects[i].draw(g);
    }
  }
}
