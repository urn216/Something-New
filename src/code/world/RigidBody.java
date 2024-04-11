package code.world;

import mki.math.vector.Vector2;

import java.util.*;

import code.world.scene.Scene;
import code.world.unit.Unit;

/**
* Write a description of class WorldObject here.
*
* @author (your name)
* @version (a version number or a date)
*/
public interface RigidBody {
  
  public Vector2 getPosition();
  
  public Vector2 getVelocity();
  
  public List<Collider> getColliders();

  public Scene getScene();

  public mki.world.RigidBody getRenderedBody();

  public void setPosition(Vector2 pos);

  public void takeDamage(double damage, Vector2 location);

  public void use(Unit user);

}
