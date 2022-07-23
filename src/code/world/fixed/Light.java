package code.world.fixed;

import code.core.Scene;

import code.math.Ray;
import code.math.Vector2;

import code.world.Camera;
import code.world.Collider;
import code.world.Tile;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
// import java.awt.geom.*;
import java.awt.Color;

/**
* Lights and stuff
*/
public class Light extends WorldObject
{
  public static final double RANGE = Tile.TILE_SIZE*3;
  List<Ray> rays = new ArrayList<Ray>();

  /**
  * Constructor for Light objects
  */
  public Light(double x, double y, String type, Scene scene)
  {
    this.scene = scene;
    this.type = type;
    origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    position = origin.add(Tile.TILE_SIZE/2);
    width = 10;
    colliders.add(new Collider(new Vector2(), width, false, this));
  }

  public void calculateShadows(List<WorldObject> objs) {
    List<WorldObject> localObj = new ArrayList<WorldObject>();
    // List<Ray> rays = new ArrayList<Ray>();
    double limit = RANGE*RANGE+Tile.TILE_SIZE*Tile.TILE_SIZE;
    for (WorldObject obj : objs) {
      if (obj.getPos().subtract(position).magsquare() < limit) {
        localObj.add(obj);
        obj.setColour(Color.red);
        for (Collider col : obj.getColls()) {
          if (!col.isSolid()) continue;
          if (col.isRound()) {
            continue;
          }
          rays.add(new Ray(position, col.getTL().subtract(position).unitize().scale(RANGE)));
          rays.add(new Ray(position, col.getTR().subtract(position).unitize().scale(RANGE)));
          rays.add(new Ray(position, col.getBL().subtract(position).unitize().scale(RANGE)));
          rays.add(new Ray(position, col.getBR().subtract(position).unitize().scale(RANGE)));
        }
      }
    }

    for (Ray ray : rays) {
      for (WorldObject obj : localObj) {
        for (Collider col : obj.getColls()) {
          if (!col.isSolid()) continue;
          col.collide(ray);
        }
      }
    }
  }

  public void draw(Graphics2D g, Camera cam) {
    double z = cam.getZoom();
    double conX = cam.conX();
    double conY = cam.conY();
    g.setColor(Color.gray);
    g.fill(new Ellipse2D.Double((position.x-width/2)*z-conX, (position.y-width/2)*z-conY, width*z, width*z));
    // g.fill(new Ellipse2D.Double((position.x-RANGE)*z-conX, (position.y-RANGE)*z-conY, RANGE*2*z, RANGE*2*z));
    g.setColor(Color.blue);
    for (Ray r : rays) {
      Vector2 prevPos = r.getHitLocation();
      g.draw(new Line2D.Double(position.x*z-conX, position.y*z-conY, prevPos.x*z-conX, prevPos.y*z-conY));
    }
  }
}
