package code.world;

import code.core.Scene;

import code.math.Vector2;

import java.util.*;

/**
* Write a description of class WorldObject here.
*
* @author (your name)
* @version (a version number or a date)
*/
public interface RigidBody
{

  public Vector2 getPos();

  public Vector2 getVel();

  public List<Collider> getColls();

  public Scene getScene();

  public void setPos(Vector2 pos);

  public void takeDamage(double damage);

}
