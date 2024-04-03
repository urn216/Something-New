package code.world.scene;

import mki.io.FileIO;

import mki.math.MathHelp;
import mki.math.vector.Vector2;
import mki.math.vector.Vector3;
import mki.math.vector.Vector3I;
import mki.world.Camera3D;
import mki.world.Material;
import mki.world.RigidBody;
import mki.world.object.primitive.Face;
import code.core.Core;
import code.world.Bullet;
import code.world.Camera;
import code.world.Tile;

import code.world.fixed.Decal;
import code.world.fixed.Direction;
import code.world.fixed.dividers.Door;
import code.world.fixed.Light;
import code.world.fixed.dividers.Wall;
import code.world.inv.Gun;
import code.world.inv.GunLauncher;
import code.world.fixed.WorldObject;

import code.world.unit.Dud;
import code.world.unit.ItemUnit;
import code.world.unit.Player;
import code.world.unit.TestAI;
import code.world.unit.Unit;

import java.util.*;
import java.util.function.BiConsumer;
import java.awt.Graphics2D;

/**
* Scene class
*/
public class Scene {

  protected String saveName;
  protected String sceneName;
  
  protected final int mapSX;
  protected final int mapSY;
  protected final Tile[][] map;
  
  protected Player player;
  
  protected final Collection<Unit> units;
  protected final Collection<Bullet> bullets;
  protected final Collection<WorldObject> fixedObj;
  protected final Collection<Decal> bgDecals;
  protected final Collection<Decal> fgDecals;

  protected final Camera cam = new Camera(new Vector2(32, 32), new Vector2(), 2);

  protected boolean drawInterior = true;

  protected Decal shadowMap;
  
  /**
  * @return the main menu scene singleton
  */
  public static final Menu mainMenu() {return Menu.MENU;}
  
  Scene(Tile[][] map, Collection<WorldObject> fixedObj, Collection<Unit> units, Collection<Decal> bgDecals, Collection<Decal> fgDecals) {
    this.map = map;
    this.mapSX = map.length;
    this.mapSY = mapSX > 0 ? map[0].length : 0;
    
    this.fixedObj = fixedObj;
    this.units = units;
    this.bullets = new HashSet<>();
    this.bgDecals = bgDecals;
    this.fgDecals = fgDecals;

    generateFloor(mapSX, mapSY);
  }
  
  public void reset() {
    units.clear();
    fixedObj.clear();
    bullets.clear();
    RigidBody.clearBodies();
    generateFloor(mapSX, mapSY);
    loadAssets("../saves/"+saveName+"/scenes/"+sceneName, this, false);
  }

  public static final void generateFloor(int mapSX, int mapSY) {
    //FLOOR
    new Face(
      new Vector3(), 
      mapSX*Tile.TILE_SIZE*Tile.UNIT_SCALE_DOWN, 
      mapSY*Tile.TILE_SIZE*Tile.UNIT_SCALE_DOWN, 
      new Material(new Vector3I(100), 0f, new Vector3())
    ).setPitch(90);

    //CEILING
    new Face(
      new Vector3(0, Tile.TILE_SIZE*Tile.UNIT_SCALE_DOWN/2, 0), 
      mapSX*Tile.TILE_SIZE*Tile.UNIT_SCALE_DOWN, 
      mapSY*Tile.TILE_SIZE*Tile.UNIT_SCALE_DOWN, 
      new Material(new Vector3I(100), 0f, new Vector3())
    ).setPitch(-90);
  }
  
  public String getSceneName() {
    return sceneName;
  }
  
  public boolean equals(String other) {
    if (other==null){return false;}
    return this.sceneName.equals(other);
  }
  
  public Unit getPlayer() {
    return player;
  }

  public Camera3D getPlayerViewPort() {
    return player.getViewPort();
  }

  public Camera getCam() {
    return cam;
  }

  public int getMapSX() {
    return mapSX;
  }

  public int getMapSY() {
    return mapSY;
  }

  public Vector2 getCursorWorldPos() {
    return Core.getCursorScreenPos().add(cam.conX(), cam.conY()).scale(1/cam.getZoom());
  }
  
  public Tile getTile(Vector2 p) {
    int x = MathHelp.clamp((int)(p.x/Tile.TILE_SIZE+mapSX/2), 0, mapSX-1);
    int y = MathHelp.clamp((int)(p.y/Tile.TILE_SIZE+mapSY/2), 0, mapSY-1);
    return map[x][y];
  }
  
  public void addBullet(Bullet b) {
    bullets.add(b);
    getTile(b.getPosition()).passOff(b);
  }
  
  public void addUnit(Unit u) {
    units.add(u);
    getTile(u.getPosition()).passOff(u);
  }

  public void removeUnit(Unit u) {
    units.remove(u);
    getTile(u.getPosition()).remove(u);
    RigidBody.removeBody(u.getRenderedBody());
  }

  public void setDrawInterior(boolean drawInterior) {
    this.drawInterior = drawInterior;
  }

  public void bigUpdate() {
    onEachTile(0, mapSX, 0, mapSY, (i, j) -> {
      map[j][i].bigUpdate(units, fixedObj);
      map[j][i].getNeighbours(map, j, i, mapSX, mapSY);
    });
    this.shadowMap = (Light.createShadowMap(fixedObj, map, mapSX*Tile.TILE_SIZE, mapSY*Tile.TILE_SIZE));
  }
  
  public void update(boolean[] keys, boolean[] mouse, Vector2 mousePos, Vector2 mouseOff) {
    mousePos = mousePos.add(cam.conX(), cam.conY()).scale(1/cam.getZoom());
    cam.follow();
    player.input(keys, mouse, mousePos, mouseOff);
    
    for (int i = 0; i < mapSX; i++) {
      for (int j = 0; j < mapSY; j++) {
        map[i][j].update();
      }
    }
    Collection<Bullet> toRemove = new HashSet<>();
    for (Bullet b : bullets) {
      b.undone();
      if(!b.isAlive()) {toRemove.add(b); RigidBody.removeBody(b.renderedBullet);}
    }
    bullets.removeAll(toRemove);
    for (Unit unit : units) {
      unit.undone();
    }

    if (Core.isRender3D()) player.getViewPort().draw();
  }
  
  public void draw(Graphics2D g) {
    for (Decal d : bgDecals) {
      d.draw(g);
    }
    double halfW = Core.WINDOW.screenWidth()/(2*cam.getZoom());
    int left = Math.max((int)((cam.getPos().x-halfW)/Tile.TILE_SIZE + mapSX/2), 0);
    int right = Math.min((int)((cam.getPos().x+halfW)/Tile.TILE_SIZE + mapSX/2)+1, mapSX);
    double halfH = Core.WINDOW.screenHeight()/(2*cam.getZoom());
    int top = Math.max((int)((cam.getPos().y-halfH)/Tile.TILE_SIZE + mapSY/2), 0);
    int bottom = Math.min((int)((cam.getPos().y+halfH)/Tile.TILE_SIZE + mapSY/2)+1, mapSY);
    
    if (drawInterior) {
      onEachTile(left, right, top, bottom, (i, j) -> map[i][j].draw(g, cam));
      onEachTile(left, right, top, bottom, (i, j) -> map[i][j].drawLowerObjects(g));
      onEachTile(left, right, top, bottom, (i, j) -> map[i][j].drawUnits(g));

      if (shadowMap != null) shadowMap.draw(g);
      
      onEachTile(left, right, top, bottom, (i, j) -> map[i][j].drawBullets(g));
      onEachTile(left, right, top, bottom, (i, j) -> map[i][j].drawHigherObjects(g));

      for (Decal d : fgDecals) {
        d.draw(g);
      }

      player.drawReticle(g);
    }
  }

  private void onEachTile(int left, int right, int top, int bottom, BiConsumer<Integer, Integer> action) {
    for (int i = left; i < right; i++) {
      for (int j = top; j < bottom; j++) {
        action.accept(i, j);
      }
    }
  }



  //  RNGRNGRNGRNGRNGRNGRNGRNGRNGRNG  //
  //RNGRNG - Scene Generation - RNGRNG//
  //  RNGRNGRNGRNGRNGRNGRNGRNGRNGRNG  //



  public static Scene generateRandom(RandomGenerator generator) {
    return generator.generate();
  }



  //  IOIOIOIOIOIOIOIOIOIOIOIOIOIOIO  //
  //IOIOIOIO - File Actions - IOIOIOIO//
  //  IOIOIOIOIOIOIOIOIOIOIOIOIOIOIO  //



  public static Scene load(String saveName, String sceneName) {
    RigidBody.clearBodies();

    Scene res = load("../saves/"+saveName+"/scenes/"+sceneName, false);
    if (res != null) {
      res.saveName = saveName;
      res.sceneName = sceneName;
      return res;
    }

    res = load("../data/scenes/"+sceneName, false);
    if (res == null) res = generateRandom(new DungeonGenerator());

    res.saveName = saveName;
    res.sceneName = sceneName;
    save(saveName, res);
    res.reset();
    return res;
  }
  
  private static Scene load(String directory, boolean fromJar) {
    if (!FileIO.exists(directory)) return null;
    
    List<String> allLines = FileIO.readAllLines(directory+"/Tiles.txt", fromJar);
    int mapSX = 0;
    int mapSY = allLines.size();
    Scanner tScan = new Scanner(allLines.get(0));
    while (tScan.hasNext()) {
      tScan.next();
      mapSX++;
    }
    tScan.close();
    Tile[][] map = new Tile[mapSX][mapSY];
    for (int y = 0; y < mapSY; y++) {
      Scanner scan = new Scanner(allLines.get(y));
      for (int x = 0; x < mapSX; x++) {
        map[x][y] = new Tile((int) (x-mapSX/2), (int) (y-mapSY/2), scan.nextInt());
      }
      scan.close();
    }

    Scene result = new Scene(map, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    
    for (String line : FileIO.readAllLines(directory+"/BGDecals.txt", fromJar)) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (!type.equals("gap")) {
        result.bgDecals.add(new Decal(scan.nextDouble(), scan.nextDouble(), type+"/"+scan.next(), scan.nextBoolean(), result));
      }
      scan.close();
    }

    for (String line : FileIO.readAllLines(directory+"/FGDecals.txt", fromJar)) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (!type.equals("gap")) {
        result.fgDecals.add(new Decal(scan.nextDouble(), scan.nextDouble(), type+"/"+scan.next(), scan.nextBoolean(), result));
      }
      scan.close();
    }

    loadAssets(directory, result, fromJar);

    return result;
  }
  
  private static void loadAssets(String directory, Scene scene, boolean fromJar) {
    if (!FileIO.exists(directory)) return;

    for (String line : FileIO.readAllLines(directory+"/Fixed.txt", fromJar)) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (type.equals("Wall")) {scene.fixedObj.add(new Wall(scan.nextDouble(), scan.nextDouble(), Direction.valueOf(scan.next()), scene));}
      else if (type.equals("Door")) {scene.fixedObj.add(new Door(scan.nextDouble(), scan.nextDouble(), Direction.valueOf(scan.next()), scene));}
      else if (type.equals("Light")) {scene.fixedObj.add(new Light(scan.nextDouble(), scan.nextDouble(), scan.nextBoolean(), scene));}
      scan.close();
    }
    
    for (String line : FileIO.readAllLines(directory+"/Units.txt", fromJar)) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (type.equals("Player")) {
        scene.player = new Player(
          scene,
          // new Gun(1200, 1, 1000, 100, 20, 0.96, true, new Gun(1200, 10, 150, 500, 16, 0.8, false)),
          new Gun(1200, 1, 1000, 100, 20, 0.96, true, new GunLauncher((p, v) -> new ItemUnit(p.getScene(), null, p.getPosition(), p.getVelocity().add(v)), 1200, 1, 500, 0.96, true)),
          new Vector2(scan.nextDouble(), scan.nextDouble()), 
          new Vector2(scan.nextDouble(), scan.nextDouble()), 
          scan.nextDouble(), 
          scan.nextDouble(), 
          scan.nextDouble()
        );
        scene.cam.setTarU(scene.player);
        scene.units.add(scene.player);
      }
      else if (type.equals("Dud")) {scene.units.add(new Dud(scene, null, new Vector2(scan.nextDouble(), scan.nextDouble()), new Vector2(scan.nextDouble(), scan.nextDouble())));}
      else if (type.equals("TestAI")) {scene.units.add(new TestAI(scene, new Gun(1200, 1, 1000, 160, 30, 0.96, true), new Vector2(scan.nextDouble(), scan.nextDouble()), new Vector2(scan.nextDouble(), scan.nextDouble())));}
      scan.close();
    }

    scene.bigUpdate();
  }
  
  public static void save(String saveName, Scene scene) {
    String directory = "../saves/"+saveName+"/scenes/"+scene.sceneName;
    FileIO.createDir(directory);

    StringBuilder b = new StringBuilder();
    for (int y = 0; y < scene.mapSY; y++) {
      for (int x = 0; x < scene.mapSX; x++) {
        b.append(scene.map[x][y].isActive() ? 1+" " : 0+" ");
      }
      b.append("\n");
    }
    FileIO.saveToFile(directory+"/Tiles.txt", b.toString());

    b = new StringBuilder();
    for (Decal img : scene.bgDecals) {
      b.append(img+"\n");
    }
    FileIO.saveToFile(directory+"/BGDecals.txt", b.toString());

    b = new StringBuilder();
    for (Decal img : scene.fgDecals) {
      b.append(img+"\n");
    }
    FileIO.saveToFile(directory+"/FGDecals.txt", b.toString());

    b = new StringBuilder();
    for (WorldObject obj : scene.fixedObj) {
      b.append(obj+"\n");
    }
    FileIO.saveToFile(directory+"/Fixed.txt", b.toString());

    b = new StringBuilder();
    for (Unit unit : scene.units) {
      b.append(unit+"\n");
    }
    FileIO.saveToFile(directory+"/Units.txt", b.toString());

    b = new StringBuilder();
    for (Bullet bullet : scene.bullets) {
      b.append(bullet+"\n");
    }
    FileIO.saveToFile(directory+"/Proj.txt", b.toString());
  }
}
