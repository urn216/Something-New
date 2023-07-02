package code.world.unit;

import mki.math.vector.Vector2;

import code.world.Collider;

import code.world.fixed.WorldObject;
import code.world.scene.Scene;

import java.util.*;

/**
* Write a description of class Dud here.
*
* @author (your name)
* @version (a version number or a date)
*/
public class Dud extends Unit {
  /**
  * Constructor for objects of class Dud
  */
  public Dud(double X, double Y, Scene scene) {
    position = new Vector2(X, Y);

    triggering = new ArrayList<WorldObject>();
    this.scene = scene;
    this.hitPoints = 160;
    walkF = 0;
    m = 100;
    vMax = 8;

    collider = new Collider.Round(new Vector2(), 8, true, this);
  }

  public Unit summon(double x, double y, Scene s) {return new Dud(x, y, s);}
}
