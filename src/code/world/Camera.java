package code.world;

import code.core.Core;
import code.core.Window;
import mki.math.vector.Vector2;

import code.world.unit.Unit;

/**
* A camera allows a player to see what's happening in the game.
*/
public class Camera {
  private static final double CLOSE_MAGNITUDE = 0.125;

  private double defaultZoom;
  private double zoom;
  private Vector2 position;
  private Vector2 offset;
  private Vector2 target;
  private Unit tarU;

  /**
  * @Camera
  *
  * Constructs a camera with an x position, a y position, a default zoom level, and the current resolution of the game window.
  */
  public Camera(Vector2 worldPos, Vector2 offset, double z) {
    this.position = worldPos;
    this.offset = offset;
    this.defaultZoom = z;
    this.zoom = Core.WINDOW.screenHeight()/Window.DEFAULT_SCREEN_SIZE.y*z;
  }

  public Vector2 getOffset() {return offset;}

  public Vector2 getTarget() {return target;}

  public Unit getTarU() {return tarU;}

  public double getZoom() {return zoom;}

  public double getDZoom() {return (Core.WINDOW.screenHeight()/Window.DEFAULT_SCREEN_SIZE.y)*defaultZoom;}

  public void resetZoom() {this.zoom = getDZoom();}

  public void setOffset(Vector2 offset) {this.offset = offset;}

  public void setTarget(Vector2 t) {
    target = t;
    tarU = null;
  }

  public void setTarU(Unit u) {
    tarU = u;
    target = u.getPosition();
  }

  public void setZoom(double z) {
    this.zoom = z;
  }

  public void follow() {
    if (tarU != null) target = tarU.getPosition();
    if (target != null) {
      Vector2 dist = new Vector2(target.subtract(position));
      if (dist.magsquare() >= 0.1) position = position.add(dist.scale(CLOSE_MAGNITUDE));
    }
  }

  public double conX() {
    return position.x*zoom-Core.WINDOW.screenWidth()/2-offset.x;
  }

  public double conY() {
    return position.y*zoom-Core.WINDOW.screenHeight()/2-offset.y;
  }

  public Vector2 getPos() {
    return position;
  }

  /**
   * Checks to see if an object is currently visible within the bounds of a camera
   * 
   * @param leftWorldBound  the left-most extent of the object within world-space
   * @param upperWorldBound the top-most extent of the object within world-space
   * @param rightWorldBound the right-most extent of the object within world-space
   * @param lowerWorldBound the bottom-most extent of the object within world-space
   * 
   * @return {@code true} if the given bounds lie within the frame of the camera
   */
  public boolean canSee(double leftWorldBound, double upperWorldBound, double rightWorldBound, double lowerWorldBound) {
    double conX = conX();
    double conY = conY();

    if (leftWorldBound*zoom-conX < Core.WINDOW.screenWidth ()
    && upperWorldBound*zoom-conY < Core.WINDOW.screenHeight()
    && rightWorldBound*zoom-conX >= 0
    && lowerWorldBound*zoom-conY >= 0) {return true;}
    return false;
  }
}
