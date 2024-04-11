package code.world.fixed;

import mki.io.FileIO;
import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.math.vector.Vector3I;
import mki.world.Material;
import mki.world.RigidBody;
import mki.world.object.primitive.Face;
import code.core.Core;
import code.world.Tile;
import code.world.scene.Scene;

// import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
* Write a description of class Decal here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class Decal extends WorldObject {
  private boolean camPan;
  private BufferedImage img;
  private final String type;
  private final String directory;

  private static int stackSize = 0;
  private static final double LAYER_BUFFER = 0.1;

  /**
  * Constructor for Decal objects
  */
  public Decal(double x, double y, String file, boolean pan, Scene scene) {
    this.scene = scene;
    Vector2 position = new Vector2(x, y);
    String[] parts = file.split("/");
    this.type = parts[0];
    this.directory = parts[parts.length-1];
    
    this.img = FileIO.readImage(file);
    this.camPan = pan;
    this.width = img.getWidth();
    this.height = img.getHeight();
    this.origin = new Vector2(x-width/2, y-height/2);

    this.renderedBody = new Face(new Vector3(position.x*Tile.SCALE_U_TO_M, LAYER_BUFFER*stackSize++, -position.y*Tile.SCALE_U_TO_M), width*Tile.SCALE_U_TO_M, height*Tile.SCALE_U_TO_M, new Material(new Vector3I(150), 0f, new Vector3(), file));
    this.renderedBody.setPitch(90);
    RigidBody.removeBody(renderedBody);
  }

  public Decal(double x, double y, BufferedImage img, boolean pan, Scene scene) {
    this.scene = scene;
    Vector2 position = new Vector2(x, y);
    this.type = null;
    this.directory = null;

    this.img = img;
    this.camPan = pan;
    this.width = img.getWidth();
    this.height = img.getHeight();
    this.origin = new Vector2(x-width/2, y-height/2);

    this.renderedBody = new Face(new Vector3(position.x*Tile.SCALE_U_TO_M, LAYER_BUFFER*stackSize++, -position.y*Tile.SCALE_U_TO_M), width*Tile.SCALE_U_TO_M, height*Tile.SCALE_U_TO_M, new Material(new Vector3I(150), 0f, new Vector3(0)));
    this.renderedBody.setPitch(90);
    RigidBody.removeBody(renderedBody);
  }

  @Override
  public int getShape() {
    return 1<<4;
  }

  @Override
  public void draw(Graphics2D g) {
    if (camPan) {
      if(!scene.getCam().canSee(origin.x, origin.y, origin.x+width, origin.y+height)) return;

      double z = scene.getCam().getZoom();
      double conX = scene.getCam().conX();
      double conY = scene.getCam().conY();

      int x = (int)Math.max(-(origin.x*z-conX)/z, 0);
      int y = (int)Math.max(-(origin.y*z-conY)/z, 0);
      int w = (int)Math.min(-(origin.x*z-conX-Core.WINDOW.screenWidth ())/z+1, width);
      int h = (int)Math.min(-(origin.y*z-conY-Core.WINDOW.screenHeight())/z+1, height);
      // g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      BufferedImage sub = img.getSubimage(x, y, w-x, h-y);
      g.drawImage(sub.getScaledInstance((int)((w-x)*z), (int)((h-y)*z), BufferedImage.SCALE_DEFAULT), (int)((origin.x+x)*z-conX), (int)((origin.y+y)*z-conY), null);
      // g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
    }
    else {
      g.drawImage(img, null, (int)(origin.x), (int)(origin.y));
    }
  }

  public String toString() {
    if (type == null) return "";
    return type+" "+(int)renderedBody.getPosition().x*Tile.SCALE_M_TO_U+" "+(int)-renderedBody.getPosition().z*Tile.SCALE_M_TO_U+" "+directory+" "+camPan;
  }
}
