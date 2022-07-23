package code.world;

import code.core.Core;

import code.math.Vector2;

import code.world.unit.Unit;

/**
* A camera allows a player to see what's happening in the game.
*/
public class Camera
{
  private static final double CLOSE_MAGNITUDE = 0.125;

  private double defaultZoom;
  private double zoom;
  private Vector2 position;
  private Vector2 offset;
  private Vector2 target;
  private Unit tarU;
  private int screenSizeX;
  private int screenSizeY;

  /**
  * @Camera
  *
  * Constructs a camera with an x position, a y position, a default zoom level, and the current resolution of the game window.
  */
  public Camera(Vector2 worldPos, Vector2 offset, double z, int sX, int sY)
  {
    this.position = worldPos;
    this.offset = offset;
    this.defaultZoom = z;
    this.screenSizeX = sX;
    this.screenSizeY = sY;
    this.zoom = sY/Core.DEFAULT_SCREEN_SIZE.y*z;
  }

  public Vector2 getOffset() {return offset;}

  public Vector2 getSize() {return new Vector2(screenSizeX/(zoom*2), screenSizeY/(zoom*2));}

  public Vector2 getTarget() {return target;}

  public Unit getTarU() {return tarU;}

  public double getZoom() {return zoom;}

  public double getDZoom() {return (screenSizeY/Core.DEFAULT_SCREEN_SIZE.y)*defaultZoom;}

  public void setOffset(Vector2 offset) {this.offset = offset;}

  public void setScreenSize(int sX, int sY) {
    this.screenSizeX = sX;
    this.screenSizeY = sY;
    this.zoom = (sY/Core.DEFAULT_SCREEN_SIZE.y)*defaultZoom;
  }

  public void setTarget(Vector2 t){
    target = t;
    tarU = null;
  }

  public void setTarU(Unit u){
    tarU = u;
    target = u.getPos();
  }

  public void setZoom(double z) {
    this.zoom = z;
  }

  public void follow() {
    if (tarU != null) target = tarU.getPos();
    if (target != null) {
      Vector2 dist = new Vector2(target.subtract(position));
      if (dist.magsquare() >= 0.1) position = position.add(dist.scale(CLOSE_MAGNITUDE));
    }
  }

  public double conX() {
    return position.x*zoom-screenSizeX/2-offset.x;
  }

  public double conY() {
    return position.y*zoom-screenSizeY/2-offset.y;
  }

  public Vector2 getPos() {
    return position;
  }
}
