package code.world.inv;

import mki.math.vector.Vector2;

import java.util.function.BiFunction;

import code.world.RigidBody;
import code.world.unit.Unit;

/**
* Gun class
*/
public class GunLauncher extends Gun {

  private final BiFunction<RigidBody, Vector2, Unit> projectileEmitter;

  /**
   * Constructor for Launcher Guns with no secondary fire
   *
   * @param projectileEmitter The Unit shot by the launcher
   * @param v the speed of the projectile shot by the gun (u/s)
   * @param num the number of projectiles per shot
   * @param coold The number of milliseconds before another shot can fire
   * @param acc The percent accuracy of each projectile
   * @param auto Whether or not the launcher is full auto
   */
  public GunLauncher(BiFunction<RigidBody, Vector2, Unit> projectileEmitter, double v, int num, int coold, double acc, boolean auto) {
    this(projectileEmitter, v, num, coold, acc, auto, null);
  }

  /**
   * Constructor for Launcher Guns with no secondary fire
   *
   * @param projectileEmitter The Unit shot by the launcher
   * @param v the speed of the projectile shot by the gun (u/s)
   * @param num the number of projectiles per shot
   * @param coold The number of milliseconds before another shot can fire
   * @param acc The percent accuracy of each projectile
   * @param auto Whether or not the launcher is full auto
   * @param second The secondary fire mode for this {@code GunLauncher}
   */
  public GunLauncher(BiFunction<RigidBody, Vector2, Unit> projectileEmitter, double v, int num, int coold, double acc, boolean auto, Item second) {
    super(v, num, 0, coold, 0, acc, auto, second);

    this.projectileEmitter = projectileEmitter;
  }

  @Override
  public void primeUse(RigidBody parent, Vector2 usePos) {
    long currentShot = System.currentTimeMillis();
    if (currentShot - lastShot < cooldown) return;
    lastShot = currentShot;

    Vector2 bDir = usePos.subtract(parent.getPosition()).unitize();
    for (int i = 0; i < projCount; i++) {
      Vector2 bRan = Vector2.fromAngle(Math.atan2(bDir.y, bDir.x)+(Math.random()*2-1)*Math.PI*accuracy, Math.random()*(projVelocity/10)+projVelocity-(projVelocity/20));
      parent.getScene().addUnit(projectileEmitter.apply(parent, bRan));
    }
  }
}
