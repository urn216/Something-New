package code.world.fixed;

import code.world.Ray;
import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.math.vector.Vector3I;
import code.world.Collider;
import code.world.Tile;
import mki.world.Material;
import mki.world.object.primitive.Cube;
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
  public static final double RANGE = Tile.TILE_SIZE_U*3;
  
  private static final float[] fs = {0f, 1f};
  private static final Color[] cs = {new Color(0), new Color(0, true)};
  
  private List<Ray> rays = new ArrayList<Ray>();

  private boolean functioning = true;

  /**
  * Constructor for Light objects
  */
  public Light(double x, double y, boolean functioning, Scene scene) {
    this.scene = scene;
    this.origin = new Vector2(x*Tile.TILE_SIZE_U, y*Tile.TILE_SIZE_U);
    // Vector2 position = origin.add(Tile.TILE_SIZE/2);
    this.width = 12;

    this.renderedBody = new Cube(
      new Vector3((x+0.5)*Tile.TILE_SIZE_M, Wall.WALL_HEIGHT_M-4*Tile.SCALE_U_TO_M, -(y+0.5)*Tile.TILE_SIZE_M), 
      this.width*Tile.SCALE_U_TO_M, 
      new Material(new Vector3I(150), 0f, new Vector3(functioning ? Tile.TILE_SIZE_U : 0))
    );
    // colliders.add(new Collider.Round(new Vector2(), 5, Collider.FLAG_EMPTY, this));

    this.functioning = functioning;
  }

  public void calculateShadows(Graphics2D g, Collection<WorldObject> objs) {
    if (!functioning) return;

    List<WorldObject> localObj = new ArrayList<WorldObject>();
    List<Ray> rays = new ArrayList<Ray>();
    double limit = RANGE*RANGE+Tile.TILE_SIZE_U*Tile.TILE_SIZE_U;
    Vector2 position = getPosition();
    for (WorldObject obj : objs) {
      if (obj.getPosition().subtract(position).magsquare() < limit) {
        localObj.add(obj);
        // obj.setColour(Color.red);
        for (Collider collider : obj.getColliders()) {
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
        for (Collider col : obj.getColliders()) {
          if (!col.isSolid()) continue;
          col.collide(ray);
        }
      }
      if (ray.hasHit()) this.rays.add(ray);
    }

    int conX = scene.getMapSX()/2*Tile.TILE_SIZE_U;
    int conY = scene.getMapSY()/2*Tile.TILE_SIZE_U;

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

  @Override
  public int getShape() {
    return 1<<Tile.OFFSET_CEILING;
  }

  public void draw(Graphics2D g) {
    double z = scene.getCam().getZoom();
    double conX = scene.getCam().conX();
    double conY = scene.getCam().conY();
    g.setColor(this.functioning ? Color.gray : Wall.WALL_COLOUR);
    g.fill(new Ellipse2D.Double((renderedBody.getPosition().x*Tile.SCALE_M_TO_U-width/2)*z-conX, (-renderedBody.getPosition().z*Tile.SCALE_M_TO_U-width/2)*z-conY, width*z, width*z));
    
    // g.setColor(Color.blue);
    // for (Ray r : rays) {
    //   Vector2 prevPos = r.getHitLocation();
    //   g.draw(new Line2D.Double(position.x*z-conX, position.y*z-conY, prevPos.x*z-conX, prevPos.y*z-conY));
    // }
  }

  public String toString() {
    return this.getClass().getSimpleName()+" "+(int)(origin.x/Tile.TILE_SIZE_U)+" "+(int)(origin.y/Tile.TILE_SIZE_U)+" "+functioning;
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

        g.fillRect(i*Tile.TILE_SIZE_U, j*Tile.TILE_SIZE_U, Tile.TILE_SIZE_U, Tile.TILE_SIZE_U);
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
