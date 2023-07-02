package code.world.fixed;

import mki.io.FileIO;
import mki.math.vector.Vector2;

import code.world.Camera;
import code.world.scene.Scene;

import java.awt.Graphics2D;
//import java.awt.geom.Rectangle2D;
//import java.awt.Color;

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
    position = new Vector2(x, y);
    String[] parts = file.split("/");
    this.type = parts[0];
    this.directory = parts[parts.length-1];

    img = FileIO.readImage(file);
    camPan = pan;
    width = img.getWidth();
    height = img.getHeight();
    origin = new Vector2(x-width/2, y-height/2);
  }

  public void draw(Graphics2D g, Camera cam) {
    if (camPan) {
      double z = cam.getZoom();
      double conX = cam.conX();
      double conY = cam.conY();
      g.drawImage(img, null, (int)(position.x*z-conX-width/2), (int)(position.y*z-conY-height/2));
    }
    else {
      g.drawImage(img, null, (int)(origin.x), (int)(origin.y));
    }
  }

  public String toString() {
    return type+" "+(int)position.x+" "+(int)position.y+" "+directory+" "+camPan;
  }
}
