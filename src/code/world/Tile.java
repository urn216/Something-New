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
  public static final int TILE_SIZE = 64;

  private Vector2 position;
  private Vector2 origin;

  private boolean active;

  private List<Unit> units = new ArrayList<Unit>();
  private List<WorldObject> fixedObj = new ArrayList<WorldObject>();
  private List<Bullet> bullets = new ArrayList<Bullet>();

  /** Whther there's a solid, airtight, wall to the [Left, Right, Up, Down] */
  // private boolean[] walled = {false, false, false, false};
  // private float airPressure = 0f;

  private Tile[][] nb;

  /**
  * Constructor for Tiles
  */
  public Tile(double x, double y, int active) {
    origin = new Vector2(x*TILE_SIZE, y*TILE_SIZE);
    position = new Vector2(x*TILE_SIZE+TILE_SIZE/2.0, y*TILE_SIZE+TILE_SIZE/2.0);
    this.active = active != 0 ? true: false;
  }

  // public boolean onScreen(Camera cam) {
  //   if (!active && fixedObj.isEmpty() && units.isEmpty() && bullets.isEmpty()) {
  //     visible = false;
  //     return visible;
  //   }
  //   Vector2 camPos = cam.getPos();
  //   Vector2 camSize = cam.getSize();
  //   if (!((camPos.x-camSize.x < position.x+TILE_SIZE/2 && camPos.y-camSize.y < position.y+TILE_SIZE/2) && (camPos.x+camSize.x > position.x-TILE_SIZE/2 && camPos.y+camSize.y > position.y-TILE_SIZE/2))) {
  //     visible = false;
  //     return visible;
  //   }
  //   visible = true;
  //   if (!active) return false;
  //   return visible;
  // }

  // public boolean onScreen(Player player, int rangeX, int rangeY) {
  //   if (!active && fixedObj.isEmpty()) {
  //     visible = false;
  //     return false;
  //   }
  //   Vector2 pPos = player.getPos();
  //   if ((pPos.x-rangeX < position.x+TILE_SIZE/2 && pPos.y-rangeY < position.y+TILE_SIZE/2) && (pPos.x+rangeX > position.x-TILE_SIZE/2 && pPos.y+rangeY > position.y-TILE_SIZE/2)) {
  //     visible = true;
  //     if (!active) return false;
  //     return true;
  //   }
  //   visible = false;
  //   return false;
  // }

  public void activate() {active = true;}

  public void deactivate() {active = false;}

  public void toggle() {active = !active;}

  public boolean isActive() {return active;}

  public void getNeighbours(Tile[][] map, int x, int y, int mapSX, int mapSY) {
    nb = new Tile[3][3];
    for (int i = Math.max(0, x-1); i < Math.min(mapSX, x+2); i++) {
      for (int j = Math.max(0, y-1); j < Math.min(mapSY, y+2); j++) {
        nb[1+i-x][1+j-y] = map[i][j];
      }
    }
    nb[1][1] = null;
  }

  public void bigUpdate(Collection<Unit> allUnits, Collection<WorldObject> objs) {
    units.clear();
    fixedObj.clear();
    bullets.clear();
    for (Unit unit : allUnits) {
      Vector2 unitPos = unit.getPos();
      if (unitPos.x >= position.x-TILE_SIZE/2.0 && unitPos.x < position.x+TILE_SIZE/2.0 && unitPos.y >= position.y-TILE_SIZE/2.0 && unitPos.y < position.y+TILE_SIZE/2.0) {
        units.add(unit);
      }
    }
    for (WorldObject obj : objs) {
      Vector2 objPos = obj.getOrigin();
      if (objPos.x >= position.x-TILE_SIZE/2.0 && objPos.x < position.x+TILE_SIZE/2.0 && objPos.y >= position.y-TILE_SIZE/2.0 && objPos.y < position.y+TILE_SIZE/2.0) {
        fixedObj.add(obj);
        // if (obj instanceof Light light) light.calculateShadows(objs);
      }
    }
  }

  public List<WorldObject> getObjs() {return fixedObj;}

  public List<Unit> getUnits() {return units;}

  public void passOff(Unit u) {units.add(u);}

  public void passOff(Bullet b) {bullets.add(b);}

  public List<WorldObject> getNBOs() {
    List<WorldObject> objs = new ArrayList<WorldObject>();
    for (int i = 0; i<3; i++) {
      for (int j = 0; j<3; j++) {
        if (nb[i][j]!=null) {objs.addAll(nb[i][j].getObjs());}
      }
    }
    objs.addAll(fixedObj);
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
    Vector2 topLeft = new Vector2(position.x-TILE_SIZE/2.0, position.y-TILE_SIZE/2.0);
    List<WorldObject> nbOs = getNBOs();
    List<RigidBody> rbs = new ArrayList<RigidBody>(units.size()+fixedObj.size());
    rbs.addAll(getNBUs());
    rbs.addAll(nbOs);
    for (int i = 0; i<units.size(); i++) {
      Unit unit = units.get(i);
      unit.update(nbOs, rbs);
      Vector2 unitPos = unit.getPos().subtract(topLeft).scale(1.0/TILE_SIZE).add(1);
      int x = (int)MathHelp.clamp(unitPos.x, 0, 2);
      int y = (int)MathHelp.clamp(unitPos.y, 0, 2);

      if (nb[x][y] != null) {nb[x][y].passOff(unit); units.remove(i); i--;}
    }

    for (int b = 0; b < bullets.size(); b++) {
      Bullet bullet = bullets.get(b);
      bullet.update(rbs);
      if(!bullet.isAlive()) {
        bullets.remove(b);
        b--;
        continue;
      }
      Vector2 bulletPos = bullet.getPos().subtract(topLeft).scale(1.0/TILE_SIZE).add(1);
      int x = (int)MathHelp.clamp(bulletPos.x, 0, 2);
      int y = (int)MathHelp.clamp(bulletPos.y, 0, 2);

      if (nb[x][y] != null) {nb[x][y].passOff(bullet); bullets.remove(b); b--;}
    }
  }

  public void draw(Graphics2D g, Camera cam) {
    if (!active) return;
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    g.setColor(Color.lightGray);
    g.fill(new Rectangle2D.Double(origin.x*z-conX, origin.y*z-conY, TILE_SIZE*z, TILE_SIZE*z));
    g.setColor(Color.gray);
    g.draw(new Rectangle2D.Double(origin.x*z-conX, origin.y*z-conY, TILE_SIZE*z, TILE_SIZE*z));
  }

  public void drawGhost(Graphics2D g, Camera cam, Color ghostColour) {
    double z = cam.getZoom();
    g.setColor(ghostColour);
    g.fill(new Rectangle2D.Double(origin.x*z-cam.conX(), origin.y*z-cam.conY(), TILE_SIZE*z, TILE_SIZE*z));
  }

  public void drawLowerObjects(Graphics2D g) {

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
    for (int i = 0; i < fixedObj.size(); i++) {
      fixedObj.get(i).draw(g);
    }
  }
}
