package code.world.unit;

import code.core.Scene;

import code.math.Vector2;

import code.world.Collider;

import code.world.fixed.WorldObject;

import java.util.*;

/**
* Write a description of class Dud here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class Dud extends Unit
{
  /**
  * Constructor for objects of class Dud
  */
  public Dud(double X, double Y, Scene scene) {
    position = new Vector2(X, Y);

    triggering = new ArrayList<WorldObject>();
    this.size = 16;
    this.scene = scene;
    this.hitPoints = size*10;
    walkF = 0;
    m = 100;
    vMax = 8;

    collider = new Collider(new Vector2(), size, true, this);
  }

  public Unit summon(double x, double y, Scene s) {return new Dud(x, y, s);}
}
