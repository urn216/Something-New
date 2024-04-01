package code.world.inv;

import java.awt.Graphics2D;

import mki.math.MathHelp;

import mki.math.vector.Vector2;

import java.util.function.BiConsumer;

import code.core.Core;
import code.world.Bullet;
import code.world.RigidBody;

/**
* Gun class
*/
public class Gun extends Item {

  protected final Item secondary;

  protected final BiConsumer<Graphics2D, Vector2> reticleDrawer;

  protected final double accuracy;
  protected final double projVelocity;
  protected final int projCount;
  protected final int projLifetime;
  protected final int cooldown;
  protected final double damage;

  protected final boolean fullAuto;

  protected long lastShot = 0;

  /**
  * Constructor for Guns with no secondary fire
  *
  * @param v the speed of the projectile shot by the gun (u/s)
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot bullet exists for
  * @param coold The number of milliseconds before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the gun is full auto
  */
  public Gun(double v, int num, int lifetime, int coold, double dmg, double acc, boolean auto) {
    this(v, num, lifetime, coold, dmg, acc, auto, null);
  }

  /**
  * Constructor for Guns with a secondary fire mode
  *
  * @param v the speed of the projectile shot by the gun (u/s)
  * @param num the number of projectiles per shot
  * @param lifetime The number of milliseconds the shot bullet exists for
  * @param coold The number of milliseconds before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the gun is full auto
  * @param second The secondary fire mode for the gun
  */
  public Gun(double v, int num, int lifetime, int coold, double dmg, double acc, boolean auto, Item second) {
    this((g, u)->{}, v, num, lifetime, coold, dmg, acc, auto, second);
  }

  /**
   * Constructor for Guns with a custom reticle
   * 
   * @param reticleDrawer The reticle of this gun
   * @param v the speed of the projectile shot by the gun (u/s)
   * @param num the number of projectiles per shot
   * @param lifetime The number of milliseconds the shot bullet exists for
   * @param coold The number of milliseconds before another shot can fire
   * @param dmg The damage done by each projectile
   * @param acc The percent accuracy of each projectile
   * @param auto Whether or not the gun is full auto
   */
  public Gun(BiConsumer<Graphics2D, Vector2> reticleDrawer, double v, int num, int lifetime, int coold, double dmg, double acc, boolean auto) {
    this(reticleDrawer, v, num, lifetime, coold, dmg, acc, auto, null);
  }

  /**
   * Constructor for Guns with a secondary fire mode and a custom reticle
   * 
   * @param reticleDrawer The reticle of this gun
   * @param v the speed of the projectile shot by the gun (u/s)
   * @param num the number of projectiles per shot
   * @param lifetime The number of milliseconds the shot bullet exists for
   * @param coold The number of milliseconds before another shot can fire
   * @param dmg The damage done by each projectile
   * @param acc The percent accuracy of each projectile
   * @param auto Whether or not the gun is full auto
   * @param second The secondary fire mode for the gun
   */
  public Gun(BiConsumer<Graphics2D, Vector2> reticleDrawer, double v, int num, int lifetime, int coold, double dmg, double acc, boolean auto, Item second) {

    this.reticleDrawer = reticleDrawer;

    this.projVelocity = v/Core.TICKS_PER_SECOND;
    this.projCount = num;
    this.projLifetime = (int)(lifetime/Core.MILLISECONDS_PER_TICK);
    this.cooldown = (int)(coold/Core.MILLISECONDS_PER_TICK);
    this.damage = dmg;
    this.accuracy = MathHelp.clamp(0.5-(acc/2), 0, 1);
    this.fullAuto = auto;
    this.secondary = second;
  }

  @Override
  public boolean getAutoType() {return fullAuto;}

  @Override
  public boolean getAutoType2() {return secondary.getAutoType();}

  @Override
  public int getCooldown() {return cooldown;}

  @Override
  public int getCooldown2() {return secondary.getCooldown();}

  @Override
  public boolean hasSecondary() {return secondary!=null;}

  @Override
  public void primeUse(RigidBody parent, Vector2 usePos) {
    long currentShot = Core.currentTicks();
    if (currentShot - lastShot < cooldown) return;
    lastShot = currentShot;

    Vector2 bDir = usePos.subtract(parent.getPosition()).unitize();
    for (int i = 0; i < projCount; i++) {
      Vector2 bRan = Vector2.fromAngle(Math.atan2(bDir.y, bDir.x)+(Math.random()*2-1)*Math.PI*accuracy, Math.random()*(projVelocity/10)+projVelocity-(projVelocity/20));
      parent.getScene().addBullet(new Bullet(parent, bRan, projLifetime, damage));
    }
  }

  @Override
  public void secondUse(RigidBody parent, Vector2 usePos) {
    secondary.primeUse(parent, usePos);
  }

  @Override
  public void drawReticle(Graphics2D g, RigidBody parent, Vector2 usePos) {
    reticleDrawer.accept(g, usePos);
  }
}
