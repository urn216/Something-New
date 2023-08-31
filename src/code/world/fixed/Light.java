package code.world.fixed;

import code.world.Ray;
import mki.math.vector.Vector2;

import code.world.Collider;
import code.world.Tile;
import code.world.Collider.Round;
import code.world.fixed.dividers.Wall;
import code.world.scene.Scene;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
// import java.awt.geom.Line2D;
// import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
// import java.awt.geom.*;
import java.awt.Color;

/**
* Lights and stuff
*/
public class Light extends WorldObject {
  public static final double RANGE = Tile.TILE_SIZE*3;
  
  private static final float[] fs = {0f, 1f};
  private static final Color[] cs = {new Color(0), new Color(0, true)};
  
  private List<Ray> rays = new ArrayList<Ray>();

  private boolean functioning = true;

  /**
  * Constructor for Light objects
  */
  public Light(double x, double y, boolean functioning, Scene scene) {
    this.scene = scene;
    this.origin = new Vector2(x*Tile.TILE_SIZE, y*Tile.TILE_SIZE);
    this.position = origin.add(Tile.TILE_SIZE/2);
    this.width = 6;
    colliders.add(new Collider.Round(new Vector2(), 5, false, this));

    this.functioning = functioning;
  }

  public void calculateShadows(Graphics2D g, Collection<WorldObject> objs) {
    if (!functioning) return;

    List<WorldObject> localObj = new ArrayList<WorldObject>();
    List<Ray> rays = new ArrayList<Ray>();
    double limit = RANGE*RANGE+Tile.TILE_SIZE*Tile.TILE_SIZE;
    for (WorldObject obj : objs) {
      if (obj.getPos().subtract(position).magsquare() < limit) {
        localObj.add(obj);
        // obj.setColour(Color.red);
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

    int conX = scene.getMapSX()/2*Tile.TILE_SIZE;
    int conY = scene.getMapSY()/2*Tile.TILE_SIZE;

    g.setPaint(new java.awt.RadialGradientPaint((float)(position.x+conX), (float)(position.y+conY), (float)(RANGE), fs, cs));
    // for (int i = 0; i < this.rays.size(); i++) { //WINDING IS VERY WRONG
    //   Path2D s = new Path2D.Double();

    //   s.moveTo(position.x+conX, position.y+conY);
    //   Vector2 one = this.rays.get(i).getHitLocation();
    //   s.lineTo(one.x+conX, one.y+conY);
    //   Vector2 two = this.rays.get((i+1)%this.rays.size()).getHitLocation();
    //   s.lineTo(two.x+conX, two.y+conY);

    //   g.fill(s);
    // }
    g.fillRect(0, 0, conX*2, conY*2);
  }

  public void draw(Graphics2D g) {
    double z = scene.getCam().getZoom();
    double conX = scene.getCam().conX();
    double conY = scene.getCam().conY();
    g.setColor(this.functioning ? Color.gray : Wall.WALL_COLOUR);
    g.fill(new Ellipse2D.Double((position.x-width/2)*z-conX, (position.y-width/2)*z-conY, width*z, width*z));
    
    // g.setColor(Color.blue);
    // for (Ray r : rays) {
    //   Vector2 prevPos = r.getHitLocation();
    //   g.draw(new Line2D.Double(position.x*z-conX, position.y*z-conY, prevPos.x*z-conX, prevPos.y*z-conY));
    // }
  }

  public String toString() {
    return this.getClass().getSimpleName()+" "+(int)(origin.x/Tile.TILE_SIZE)+" "+(int)(origin.y/Tile.TILE_SIZE)+" "+functioning;
  }

  public static Decal createShadowMap(Collection<WorldObject> fixedObj, Tile[][] map, int width, int height) {
    BufferedImage img = new BufferedImage(width, height, 2);
    Graphics2D g = img.createGraphics();
    Scene scene = null;
    for (WorldObject obj : fixedObj) {
      if (scene == null) scene = obj.getScene();
      if (!(obj instanceof Light)) continue;
      ((Light)obj).calculateShadows(g, fixedObj);
    }
    g.setColor(cs[0]);
    for (int i = 0; i < map.length; i++) {
      for (int j = 0; j < map[i].length; j++) {
        if (map[i][j].isActive()) continue;

        g.fillRect(i*Tile.TILE_SIZE, j*Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE);
      }
    }
    g.dispose();

    int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
    for (int i = 0; i < pixels.length; i++) {
      pixels[i] = (int)((~(pixels[i] >> 24) & 255)*(0.75)) << 24;
    }
    img.setRGB(0, 0, width, height, pixels, 0, width);
    return new Decal(0, 0, img, true, scene);
  }
}
