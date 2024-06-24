package code.world.unit;

import mki.math.vector.Vector2;
import mki.world.Material;
import code.core.Core;
import code.world.Collider;
import code.world.RigidBody;
import code.world.fixed.WorldObject;
import code.world.inv.Item;
import code.world.scene.Scene;

import java.util.*;
//import java.awt.Graphics2D;
//import java.awt.geom.Rectangle2D;
//import java.awt.Color;

/**
* Write a description of class Unit here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class ItemUnit extends Unit {

  public ItemUnit(Scene scene, Item item, Vector2 position, Vector2 velocity) {
    super(
      scene,                            //scene
      6,                                //size
      position,                         //pos
      velocity,
      new Vector2(),                    //dir
      0,                                //walk-force
      240/Core.TICKS_PER_SECOND,        //max-velocity
      40,                               //mass
      1,                                //hitpoints
      0.7f                              //elasticity
    );

    this.collider.setToTriggerVolume(null, null);
    this.held = item;
    this.renderedBody.getModel().setMat(new Material(Core.FULL_BRIGHT)); //0f , new Vector3(1, 10, 1)
  }

  @Override
  public boolean step(List<Collider> colliders) {
    Vector2 slowAcc = velocity.scale(m/500);
    velocity = velocity.subtract(slowAcc);

    colliders = colliders.stream().filter((c) -> (c.getParent() instanceof WorldObject)).toList();

    return !(!stepX(colliders) && !stepY(colliders));
  }

  @Override
  public void trigger(List<RigidBody> bodies) {}

  @Override
  public void use(Unit user) {
    scene.removeUnit(this);
    user.takeItem(held);
  }
}
