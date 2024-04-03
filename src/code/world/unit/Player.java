package code.world.unit;

import mki.math.vector.Vector2;
import mki.rendering.renderers.Renderer;
import mki.world.Camera3D;
import code.core.Core;
import code.world.Tile;

import code.world.inv.*;
import code.world.scene.Scene;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
* Player Object
*/
public class Player extends Unit {

  public static final int DEFAULT_HITPOINTS = 160;

  private final Camera3D viewPort;

  /**
  * Constructor for objects of class Player
  */
  public Player(Scene scene, Item held, Vector2 position, Vector2 velocity, double pitch, double yaw, double hitpoints) {
    super(
      scene,                            //scene
      8,                                //radius
      position,                         //pos
      velocity,
      new Vector2(pitch, yaw),          //dir
      6000/Core.TICKS_PER_SECOND,       //walk-force
      240/Core.TICKS_PER_SECOND,        //max-velocity
      100,                              //mass
      hitpoints,                        //hitpoints
      0                                 //elasticity
    );

    this.viewPort = new Camera3D(this.renderedBody.getPosition(), 144, 81, 80, Renderer.rasterizer());
    this.viewPort.setRotation(pitch, yaw, 0);

    // held = new Gun(this, 40, 1, 30, 168, 15, 0.96, true);
    this.held = held;
    // held = new Gun(this, 40, 1, 30, 160, 30, 0.96, true, new GunLauncher(this, new ItemUnit(0, 0, null), 20, 1, 500, 0.96, true));
    // held = new BuildTool(this);
  }

  public void input(boolean[] keys, boolean[] mouse, Vector2 mousePos, Vector2 mouseOff) {
    if (!alive) {return;}
    //movement
    int horizontalInput = 0, verticalInput = 0;

    if(keys[KeyEvent.VK_W])   verticalInput--;
    if(keys[KeyEvent.VK_A]) horizontalInput--;
    if(keys[KeyEvent.VK_S])   verticalInput++;
    if(keys[KeyEvent.VK_D]) horizontalInput++;

    Vector2 usePos = mousePos;

    if (Core.isRender3D()) {
      // if (mouseOff.x != 0 || mouseOff.y != 0)
      setLookDirection(lookDirection.x+mouseOff.y*0.25, lookDirection.y+mouseOff.x*0.3);

      Vector2 right = Vector2.fromAngle(Math.toRadians(this.lookDirection.y), 1);
      setMovementDirection(right.scale(horizontalInput).add(-right.y*verticalInput, right.x*verticalInput));

      usePos = getPosition().add(right.y, -right.x);
    }
    else setMovementDirection(new Vector2(horizontalInput, verticalInput));
    
    //mouse
    if (held != null) {
      if (mouse[1]) {
        held.primeUse(this, usePos);
        mouse[1] = held.getAutoType();
      }
  
      if (mouse[3] && held.hasSecondary()) {
        held.secondUse(this, usePos);
        mouse[3] = held.getAutoType2();
      }
    }

    //triggering
    if (!triggering.isEmpty() && keys[KeyEvent.VK_E]) {
      triggering.get(triggering.size()-1).use(this);
      triggering.remove(triggering.size()-1);
      keys[KeyEvent.VK_E] = false;
      // System.out.println("Toggling " + triggering.get(triggering.size()-1));
    }
    if(keys[KeyEvent.VK_V]) {
      collider.toggleSolidness();
      keys[KeyEvent.VK_V] = false;
    }
  }

  public Camera3D getViewPort() {
    return viewPort;
  }

  public void setLookDirection(double pitch, double yaw) {
    this.viewPort.setRotation(pitch, yaw, renderedBody.getRoll());
    this.lookDirection = new Vector2(viewPort.getPitch(), viewPort.getYaw());
  }

  @Override
  public void offsetPosition(double x, double y) {
    super.offsetPosition(x, y);
    this.viewPort.setPosition(this.renderedBody.getPosition().add(0, 8*Tile.UNIT_SCALE_DOWN, 0));
  }

  @Override
  public void setPosition(double x, double y) {
    super.setPosition(x, y);
    this.viewPort.setPosition(this.renderedBody.getPosition().add(0, 8*Tile.UNIT_SCALE_DOWN, 0));
  }

  public void drawReticle(Graphics2D g) {
    if (alive && held != null) held.drawReticle(g, this, scene.getCursorWorldPos());
  }

  public String toString() {
    return this.getClass().getSimpleName()+
    " "+ renderedBody.getPosition().x*Tile.UNIT_SCALE_UP+
    " "+-renderedBody.getPosition().z*Tile.UNIT_SCALE_UP+
    " "+velocity.x+
    " "+velocity.y+
    " "+lookDirection.x+
    " "+lookDirection.y+
    " "+hitPoints;
  }
}
