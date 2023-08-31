package code.world.fixed;

import mki.io.FileIO;
import mki.math.vector.Vector2;
import code.core.Core;
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

  /**
  * Constructor for Decal objects
  */
  public Decal(double x, double y, String file, boolean pan, Scene scene) {
    this.scene = scene;
    this.position = new Vector2(x, y);
    String[] parts = file.split("/");
    this.type = parts[0];
    this.directory = parts[parts.length-1];

    this.img = FileIO.readImage(file);
    this.camPan = pan;
    this.width = img.getWidth();
    this.height = img.getHeight();
    this.origin = new Vector2(x-width/2, y-height/2);
  }

  public Decal(double x, double y, BufferedImage img, boolean pan, Scene scene) {
    this.scene = scene;
    this.position = new Vector2(x, y);
    this.type = null;
    this.directory = null;

    this.img = img;
    this.camPan = pan;
    this.width = img.getWidth();
    this.height = img.getHeight();
    this.origin = new Vector2(x-width/2, y-height/2);
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
    return type+" "+(int)position.x+" "+(int)position.y+" "+directory+" "+camPan;
  }
}
