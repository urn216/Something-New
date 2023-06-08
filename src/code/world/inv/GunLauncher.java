package code.world.inv;

import mki.math.vector.Vector2;

import code.world.RigidBody;
import code.world.unit.Unit;

/**
* Gun class
*/
public class GunLauncher extends Gun {

  private final Unit projectile;

  /**
  * Constructor for Launcher Guns with no secondary fire
  *
  * @param parent The rigidbody holding the launcher
  * @param proj The Unit shot by the launcher
  * @param v the speed of the projectile shot by the launcher
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot Unit exists for
  * @param coold The number of milliseconds before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the launcher is full auto
  */
  public GunLauncher(RigidBody parent, Unit proj, double v, int num, int coold, double acc, boolean auto) {
    this(parent, proj, v, num, coold, acc, auto, null);
  }

  /**
  * Constructor for Launcher Guns with a secondary fire mode
  *
  * @param parent The rigidbody holding the launcher
  * @param proj The Unit shot by the launcher
  * @param v the speed of the projectile shot by the launcher
  * @param num the number of projectiles per shot
  * @param lifetime The number of frames the shot Unit exists for
  * @param coold The number of milliseconds before another shot can fire
  * @param dmg The damage done by each projectile
  * @param acc The percent accuracy of each projectile
  * @param auto Whether or not the launcher is full auto
  * @param second The secondary fire mode for the launcher
  */
  public GunLauncher(RigidBody parent, Unit proj, double v, int num, int coold, double acc, boolean auto, Item second) {
    super(parent, v, num, 0, coold, 0, acc, auto, second);

    projectile = proj;
  }

  @Override
  public void primeUse(Vector2 usePos) {
    long currentShot = System.currentTimeMillis();
    if (currentShot - lastShot < cooldown) return;
    lastShot = currentShot;

    Vector2 position = parent.getPos().add(parent.getVel());
    Vector2 bDir = new Vector2(usePos.x-position.x, usePos.y-position.y).unitize();
    for (int i = 0; i < projCount; i++) {
      Vector2 bRan = Vector2.fromAngle(Math.atan2(bDir.y, bDir.x)+(Math.random()*2-1)*Math.PI*accuracy, Math.random()*(projVelocity/10)+projVelocity-(projVelocity/20));
      Unit b = projectile.summon(position.x, position.y, parent.getScene());
      b.setVel(bRan.add(parent.getVel()));
      parent.getScene().addUnit(b);
    }
  }
}
