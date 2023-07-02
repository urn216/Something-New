package code.world.fixed;

import code.world.Ray;
import mki.math.vector.Vector2;

import code.world.Camera;
import code.world.Collider;
import code.world.Tile;
import code.world.Collider.Round;
import code.world.scene.Scene;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
// import java.awt.geom.*;
import java.awt.Color;

/**
* Lights and stuff
*/
public class Light extends WorldObject {
  public static final double RANGE = Tile.TILE_SIZE*3;
  List<Ray> rays = new ArrayList<Ray>();

  /**
  * Constructor for Light objects
  */
  public Light(double x, double y, Scene scene)
  {
    this.scene = scene;
    origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    position = origin.add(Tile.TILE_SIZE/2);
    width = 10;
    colliders.add(new Collider.Round(new Vector2(), 5, false, this));
  }

  public void calculateShadows(Collection<WorldObject> objs) {
    List<WorldObject> localObj = new ArrayList<WorldObject>();
    List<Ray> rays = new ArrayList<Ray>();
    double limit = RANGE*RANGE+Tile.TILE_SIZE*Tile.TILE_SIZE;
    for (WorldObject obj : objs) {
      if (obj.getPos().subtract(position).magsquare() < limit) {
        localObj.add(obj);
        obj.setColour(Color.red);
        for (Collider collider : obj.getColls()) {
          if (!collider.isSolid() || collider instanceof Round) continue;
          Collider.Square col = (Collider.Square) collider;
          Vector2 pos = col.getPos();
          rays.add(new Ray(position, pos.add(-col.getWidth()/2, -col.getHeight()/2).subtract(position).unitize().scale(RANGE)));
          rays.add(new Ray(position, pos.add( col.getWidth()/2, -col.getHeight()/2).subtract(position).unitize().scale(RANGE)));
          rays.add(new Ray(position, pos.add(-col.getWidth()/2,  col.getHeight()/2).subtract(position).unitize().scale(RANGE)));
          rays.add(new Ray(position, pos.add( col.getWidth()/2,  col.getHeight()/2).subtract(position).unitize().scale(RANGE)));
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
      if (ray.hasHit()) this.rays.add(ray);
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
