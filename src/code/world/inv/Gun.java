package code.world.inv;

import mki.math.MathHelp;

import mki.math.vector.Vector2;

import code.world.RigidBody;
import code.world.Bullet;

/**
* Gun class
*/
public class Gun extends Item {

  private final Item secondary;

  private final double accuracy;
  private final double projVelocity;
  private final int projCount;
  private final int projLifetime;
  private final int cooldown;
  private final double damage;

  private final boolean fullAuto;

  private long lastShot = System.currentTimeMillis();

  /**
  * Constructor for Guns with no secondary fire
  *
  * @param parent The rigidbody holding the gun
  * @param v the speed of the projectile shot by the gun
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot bullet exists for
  * @param coold The number of milliseconds before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the gun is full auto
  */
  public Gun(RigidBody parent, double v, int num, int lifetime, int coold, double dmg, double acc, boolean auto) {
    this(parent, v, num, lifetime, coold, dmg, acc, auto, null);
  }

  /**
  * Constructor for Guns with a secondary fire mode
  *
  * @param parent The rigidbody holding the gun
  * @param v the speed of the projectile shot by the gun
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot bullet exists for
  * @param coold The number of milliseconds before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the gun is full auto
  * @param second The secondary fire mode for the gun
  */
  public Gun(RigidBody parent, double v, int num, int lifetime, int coold, double dmg, double acc, boolean auto, Item second) {
    this.parent = parent;

    projVelocity = v;
    projCount = num;
    projLifetime = lifetime;
    cooldown = coold;
    damage = dmg;
    accuracy = MathHelp.clamp(0.5-(acc/2), 0, 1);
    fullAuto = auto;
    secondary = second;
  }

  public boolean getAutoType() {return fullAuto;}

  public boolean getAutoType2() {return secondary.getAutoType();}

  public int getCooldown() {return cooldown;}

  public int getCooldown2() {return secondary.getCooldown();}

  public boolean hasSecondary() {return secondary!=null;}

  public void primeUse(Vector2 usePos) {
    long currentShot = System.currentTimeMillis();
    if (currentShot - lastShot < cooldown) return;
    lastShot = currentShot;

    Vector2 position = parent.getPos();
    Vector2 bDir = new Vector2(usePos.x-position.x, usePos.y-position.y).unitize();
    for (int i = 0; i < projCount; i++) {
      Vector2 bRan = Vector2.fromAngle(Math.atan2(bDir.y, bDir.x)+(Math.random()*2-1)*Math.PI*accuracy, Math.random()*(projVelocity/10)+projVelocity-(projVelocity/20));
      parent.getScene().addBullet(new Bullet(parent, bRan, projLifetime, damage));
    }
  }

  public void secondUse(Vector2 usePos) {
    secondary.primeUse(usePos);
  }
}
