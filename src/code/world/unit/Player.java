package code.world.unit;

import code.core.Scene;

import code.math.Vector2;

import code.world.Collider;

import code.world.fixed.WorldObject;

import code.world.inv.Gun;
import code.world.inv.GunLauncher;

import java.util.*;
import java.awt.event.KeyEvent;

/**
* Player Object
*/
public class Player extends Unit
{
  /**
  * Constructor for objects of class Player
  */
  public Player(double X, double Y, Scene scene) {
    position = new Vector2(X, Y);
    direction = new Vector2();
    v = new Vector2();

    m = 100;
    walkF = 200;
    triggering = new ArrayList<WorldObject>();
    this.size = 16;
    this.scene = scene;
    this.hitPoints = size*10;
    vMax = 8;

    collider = new Collider(new Vector2(), size, true, this);

    //held = new Gun(this, 40, 1, 30, 5, 15, 0.96, true);
    //held = new Gun(this, 40, 1, 30, 5, 15, 0.96, true, new Gun(this, 40, 10, 5, 30, 10, 0.8, false));
    held = new Gun(this, 40, 1, 30, 5, 30, 0.96, true, new GunLauncher(this, new ItemUnit(0, 0, null), 20, 1, 30, 15, 0.96, true));
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
    if (mouse[1] && heldCool1 <= 0) {
      held.primeUse(mousePos);
      heldCool1 = held.getCooldown();
      mouse[1] = held.getAutoType();
    }

    if (mouse[3] && held.hasSecondary() && heldCool2 <= 0) {
      held.secondUse(mousePos);
      heldCool2 = held.getCooldown2();
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
        if (collider.touching(other)!=null && other.isTrigger()) {
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
}
