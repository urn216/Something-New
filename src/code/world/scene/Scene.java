package code.world.scene;

import mki.io.FileIO;

import mki.math.MathHelp;
import mki.math.vector.Vector2;
import code.core.Core;
import code.world.Bullet;
import code.world.Camera;
import code.world.Tile;

import code.world.fixed.Decal;
import code.world.fixed.dividers.Door;
import code.world.fixed.Light;
import code.world.fixed.dividers.Wall;
import code.world.fixed.WorldObject;

import code.world.unit.Dud;
import code.world.unit.Player;
import code.world.unit.TestAI;
import code.world.unit.Unit;

import java.util.*;
import java.awt.Graphics2D;

/**
* Scene class
*/
public class Scene
{
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

  protected final Camera cam = new Camera(new Vector2(32, 32), new Vector2(), 2);

  protected boolean drawInterior = true;
  
  /**
  * @return the main menu scene singleton
  */
  public static final Menu mainMenu() {return Menu.MENU;}
  
  Scene(Tile[][] map, Collection<WorldObject> fixedObj, Collection<Unit> units, Collection<Decal> bgDecals) {
    this.map = map;
    this.mapSX = map.length;
    this.mapSY = mapSX > 0 ? map[0].length : 0;
    
    this.fixedObj = fixedObj;
    this.units = units;
    this.bullets = new HashSet<>();
    this.bgDecals = bgDecals;
  }
  
  public void reset() {
    units.clear();
    fixedObj.clear();
    bullets.clear();
    loadAssets("../saves/"+saveName+"/scenes/"+sceneName, this, false);
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

  public Camera getCam() {
    return cam;
  }
  
  public Tile getTile(Vector2 p) {return map[(int)(p.x/Tile.TILE_SIZE+mapSX/2)][(int)(p.y/Tile.TILE_SIZE+mapSY/2)];}
  
  public void addBullet(Bullet b) {
    bullets.add(b);
    Vector2 p = b.getPos();
    map[(int)MathHelp.clamp((p.x/Tile.TILE_SIZE)+mapSX/2, 0, mapSX-1)][(int)MathHelp.clamp((p.y/Tile.TILE_SIZE)+mapSY/2, 0, mapSY-1)].passOff(b);
  }
  
  public void addUnit(Unit u) {
    units.add(u);
    Vector2 p = u.getPos();
    map[(int)(p.x/Tile.TILE_SIZE)+mapSX/2][(int)(p.y/Tile.TILE_SIZE)+mapSY/2].passOff(u);
  }

  public void setDrawInterior(boolean drawInterior) {
    this.drawInterior = drawInterior;
  }

  public void bigUpdate() {
    for (int i = 0; i < mapSY; i++) {
      for (int j = 0; j < mapSX; j++) {
        map[j][i].bigUpdate(units, fixedObj);
        map[j][i].getNeighbours(map, j, i, mapSX, mapSY);
      }
    }
  }
  
  public void update(boolean[] keys, boolean[] mouse, Vector2 mousePos) {
    mousePos = mousePos.add(cam.conX(), cam.conY()).scale(1/cam.getZoom());
    cam.follow();
    player.input(keys, mouse, mousePos);
    
    for (int i = 0; i < mapSX; i++) {
      for (int j = 0; j < mapSY; j++) {
        map[i][j].update();
      }
    }
    Collection<Bullet> toRemove = new HashSet<>();
    for (Bullet b : bullets) {
      b.undone();
      if(!b.isAlive()) {toRemove.add(b);}
    }
    bullets.removeAll(toRemove);
    for (Unit unit : units) {
      unit.undone();
    }
  }
  
  public void draw(Graphics2D g) {
    for (Decal d : bgDecals) {
      d.draw(g, cam);
    }
    double halfW = Core.WINDOW.screenWidth()/(2*cam.getZoom());
    int left = Math.max((int)((cam.getPos().x-halfW)/Tile.TILE_SIZE + mapSX/2), 0);
    int right = Math.min((int)((cam.getPos().x+halfW)/Tile.TILE_SIZE + mapSX/2)+1, mapSX);
    double halfH = Core.WINDOW.screenHeight()/(2*cam.getZoom());
    int top = Math.max((int)((cam.getPos().y-halfH)/Tile.TILE_SIZE + mapSY/2), 0);
    int bottom = Math.min((int)((cam.getPos().y+halfH)/Tile.TILE_SIZE + mapSY/2)+1, mapSY);
    
    if (drawInterior) {
      for (int i = left; i < right; i++) {
        for (int j = top; j < bottom; j++) {
          map[i][j].draw(g, cam);
        }
      }
      for (int i = left; i < right; i++) {
        for (int j = top; j < bottom; j++) {
          map[i][j].drawDecor(g, cam);
        }
      }
    }
  }



  //  IOIOIOIOIOIOIOIOIOIOIOIOIOIOIO  //
  //IOIOIOIO - File Actions - IOIOIOIO//
  //  IOIOIOIOIOIOIOIOIOIOIOIOIOIOIO  //



  public static Scene load(String saveName, String sceneName) {
    Scene res = load("../saves/"+saveName+"/scenes/"+sceneName, false);
    if (res != null) {
      res.saveName = saveName;
      res.sceneName = sceneName;
      return res;
    }

    res = load("../data/scenes/"+sceneName, false);
    if (res == null) res = GenerateRandom.generate();

    res.saveName = saveName;
    res.sceneName = sceneName;
    save(saveName, res);
    return res;
  }
  
  static Scene load(String directory, boolean fromJar) {
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

    Scene result = new Scene(map, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    
    for (String line : FileIO.readAllLines(directory+"/Decals.txt", fromJar)) {
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

    loadAssets(directory, result, fromJar);

    return result;
  }
  
  static void loadAssets(String directory, Scene scene, boolean fromJar) {
    if (!FileIO.exists(directory)) return;

    for (String line : FileIO.readAllLines(directory+"/Fixed.txt", fromJar)) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (type.equals("Wall")) {scene.fixedObj.add(new Wall(scan.nextDouble(), scan.nextDouble(), scan.next(), scan.next(), scene));}
      else if (type.equals("Door")) {scene.fixedObj.add(new Door(scan.nextDouble(), scan.nextDouble(), scan.next(), scan.next(), scene));}
      else if (type.equals("Light")) {scene.fixedObj.add(new Light(scan.nextDouble(), scan.nextDouble(), scan.next(), scene));}
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
        scene.player = new Player(scan.nextDouble(), scan.nextDouble(), scene);
        scene.cam.setTarU(scene.player);
        scene.units.add(scene.player);
      }
      else if (type.equals("Dud")) {scene.units.add(new Dud(scan.nextDouble(), scan.nextDouble(), scene));}
      else if (type.equals("TestAI")) {scene.units.add(new TestAI(scan.nextDouble(), scan.nextDouble(), scene));}
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
    FileIO.saveToFile(directory+"/Decals.txt", b.toString());

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
