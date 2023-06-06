package code.world.inv;

import mki.math.MathHelp;

import mki.math.vector.Vector2;

import code.world.RigidBody;
import code.world.unit.Unit;

/**
* Gun class
*/
public class GunLauncher extends Item {

  private final Item secondary;

  private final Unit projectile;

  private final double accuracy;
  private final double projVelocity;
  private final int projCount;
  //private int projLifetime;
  private final int cooldown;

  private final boolean fullAuto;

  /**
  * Constructor for Launcher Guns with no secondary fire
  *
  * @param parent The rigidbody holding the launcher
  * @param proj The Unit shot by the launcher
  * @param v the speed of the projectile shot by the launcher
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot Unit exists for
  * @param coold The number of frames before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the launcher is full auto
  */
  public GunLauncher(RigidBody parent, Unit proj, double v, int num, int lifetime, int coold, double acc, boolean auto) {
    this(parent, proj, v, num, lifetime, coold, acc, auto, null);
  }

  /**
  * Constructor for Launcher Guns with a secondary fire mode
  *
  * @param parent The rigidbody holding the launcher
  * @param proj The Unit shot by the launcher
  * @param v the speed of the projectile shot by the launcher
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot Unit exists for
  * @param coold The number of frames before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the launcher is full auto
  * @param second The secondary fire mode for the launcher
  */
  public GunLauncher(RigidBody parent, Unit proj, double v, int num, int lifetime, int coold, double acc, boolean auto, Item second) {
    this.parent = parent;

    projectile = proj;

    projVelocity = v;
    projCount = num;
    //projLifetime = lifetime;
    cooldown = coold;
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
    Vector2 position = parent.getPos().add(parent.getVel());
    Vector2 bDir = new Vector2(usePos.x-position.x, usePos.y-position.y).unitize();
    for (int i = 0; i < projCount; i++) {
      Vector2 bRan = Vector2.fromAngle(Math.atan2(bDir.y, bDir.x)+(Math.random()*2-1)*Math.PI*accuracy, Math.random()*(projVelocity/10)+projVelocity-(projVelocity/20));
      Unit b = projectile.summon(position.x, position.y, parent.getScene());
      b.setVel(bRan.add(parent.getVel()));
      parent.getScene().addUnit(b);
    }
  }

  public void secondUse(Vector2 usePos) {
    secondary.primeUse(usePos);
  }
}
