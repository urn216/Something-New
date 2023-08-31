package code.world.unit;

import mki.math.vector.Vector2;
import code.core.Core;
import code.world.Collider;

import code.world.fixed.WorldObject;

import code.world.inv.*;
import code.world.scene.Scene;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
* Player Object
*/
public class Player extends Unit {
  /**
  * Constructor for objects of class Player
  */
  public Player(double X, double Y, Scene scene) {
    super(
      scene,                            //scene
      8,                                //radius
      new Vector2(X, Y),                //pos
      new Vector2(),                    //dir
      6000/Core.TICKS_PER_SECOND,       //walk-force
      240/Core.TICKS_PER_SECOND,        //max-velocity
      100,                              //mass
      160,                              //hitpoints
      0                                 //elasticity
    );

    // held = new Gun(this, 40, 1, 30, 168, 15, 0.96, true);
    held = new Gun(this, 1200, 1, 1000, 100, 20, 0.96, true, new Gun(this, 1200, 10, 150, 500, 16, 0.8, false));
    // held = new Gun(this, 40, 1, 30, 160, 30, 0.96, true, new GunLauncher(this, new ItemUnit(0, 0, null), 20, 1, 500, 0.96, true));
    // held = new BuildTool(this);
  }

  public void input(boolean[] keys, boolean[] mouse, Vector2 mousePos) {
    if (!alive) {return;}
    //movement
    double x = 0, y = 0;
    if(keys[KeyEvent.VK_W]) {
      y -= 1;
    }
    if(keys[KeyEvent.VK_A]) {
      x -= 1;
    }
    if(keys[KeyEvent.VK_S]) {
      y += 1;
    }
    if(keys[KeyEvent.VK_D]) {
      x += 1;
    }
    direction = new Vector2(x, y);

    //mouse
    if (mouse[1]) {
      held.primeUse(mousePos);
      mouse[1] = held.getAutoType();
    }

    if (mouse[3] && held.hasSecondary()) {
      held.secondUse(mousePos);
      mouse[3] = held.getAutoType2();
    }

    //triggering
    if (!triggering.isEmpty() && keys[KeyEvent.VK_E]) {
      triggering.get(triggering.size()-1).toggle(this);
      keys[KeyEvent.VK_E] = false;
    }
    if(keys[KeyEvent.VK_V]) {
      collider.toggle();
      keys[KeyEvent.VK_V] = false;
    }
  }

  public Unit summon(double x, double y, Scene s) {return new Player(x, y, s);}

  public void trigger(List<WorldObject> objects) {
    for (WorldObject obj : objects) {
      for (Collider other : obj.getColls()) {
        if (collider.collide(other)!=null && other.isTrigger()) {
          if (!triggering.contains(obj)) {
            triggering.add(obj);
            obj.doTrigger();
          }
        }
        else if (triggering.contains(obj)) {
          triggering.remove(obj);
          obj.undoTrigger();
        }
      }
    }
  }

  public void drawReticle(Graphics2D g) {
    if (alive) held.drawReticle(g, scene.getCursorWorldPos());
  }
}
