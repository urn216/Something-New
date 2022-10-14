package code.core;

import code.math.IOHelp;
import code.math.MathHelp;
import code.math.Vector2;

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


import java.io.*;
import java.nio.file.*;

import java.util.*;
import java.awt.Graphics2D;

/**
* Scene class
*/
public class Scene
{
  private String title;
  //private String saveName;
  private String prefix = "/data/scenes/";

  private boolean inJar = true;

  private int mapSX;
  private int mapSY;
  private Tile[][] map;

  private Player player;

  private List<Unit> units = new ArrayList<Unit>();
  private List<Bullet> bullets = new ArrayList<Bullet>();
  private List<WorldObject> fixedObj = new ArrayList<WorldObject>();
  private List<Decal> bgDecals = new ArrayList<Decal>();

  /**
  * Constructor for Scenes
  */
  public Scene(String name, String saveName)
  {
    this.title = name;
    //this.saveName = saveName;
    if (saveName != null) {
      prefix = "../saves/"+saveName+"/scenes/";
      inJar = false;
      File dest = Paths.get(prefix+title).toFile();
      if (!dest.exists()) {
        dest.mkdirs();
        InputStream source = Scene.class.getResourceAsStream("/data/scenes/"+title);
        if (source==null) {new GenerateRandom().save(title, saveName);}
        else {IOHelp.copyContents(source, dest.toPath());} // This will not work! suggest loading first, then saving from play.
      }
    }
    load(true);

    for (int i = 0; i < mapSY; i++) {
      for (int j = 0; j < mapSX; j++) {
        map[j][i].bigUpdate(units, fixedObj);
        map[j][i].getNeighbours(map, j, i, mapSX, mapSY);
      }
    }
  }

  public void reset() {
    units.clear();
    fixedObj.clear();
    bullets.clear();
    load(false);

    for (int i = 0; i < mapSY; i++) {
      for (int j = 0; j < mapSX; j++) {
        map[j][i].bigUpdate(units, fixedObj);
        map[j][i].getNeighbours(map, j, i, mapSX, mapSY);
      }
    }
  }

  public String getTitle() {
    return title;
  }

  public boolean equals(String other) {
    if (other==null){return false;}
    return this.title.equals(other);
  }

  public Unit getPlayer() {
    return player;
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

  public void update(boolean[] keys, boolean[] mouse, Vector2 mousePos) {
    player.input(keys, mouse, mousePos);

    for (int i = 0; i < mapSX; i++) {
      for (int j = 0; j < mapSY; j++) {
        map[i][j].update();
      }
    }
    for (int b = 0; b < bullets.size(); b++) {
      bullets.get(b).undone();
      if(!bullets.get(b).isAlive()) {bullets.remove(b); b--;}
    }
    for (Unit unit : units) {
      unit.undone();
    }
  }

  public void draw(Graphics2D g, Camera cam, boolean drawInterior) {
    for (Decal d : bgDecals) {
      d.draw(g, cam);
    }
    if (drawInterior) {
      for (int i = 0; i < mapSX; i++) {
        for (int j = 0; j < mapSY; j++) {
          if (map[i][j].onScreen(cam)) {
            //if (map[i][j].onScreen(player, 200, 200)) {
            map[i][j].draw(g, cam);
          }
        }
      }
      for (int i = 0; i < mapSX; i++) {
        for (int j = 0; j < mapSY; j++) {
          if (map[i][j].isVis()) {
            map[i][j].drawDecor(g, cam);
          }
        }
      }
    }
  }

  public void load(boolean init) {
    String filename;
    List<String> allLines;
    if (init) {
      filename = prefix+title+"/Decals.txt";
      allLines = IOHelp.readAllLines(filename, inJar);
      for (String line : allLines) {
        Scanner scan = new Scanner(line);
        String type;
        if (scan.hasNext()) {
          type = scan.next();
        }
        else {type = "gap";}
        if (!type.equals("gap")) {
          bgDecals.add(new Decal(scan.nextDouble(), scan.nextDouble(), type+"/"+scan.next(), scan.nextBoolean(), this));
        }
        scan.close();
      }

      filename = prefix+title+"/Tiles.txt";
      allLines = IOHelp.readAllLines(filename, inJar);
      mapSX = 0;
      mapSY = allLines.size();
      if (allLines.isEmpty()) {return;}
      Scanner tScan = new Scanner(allLines.get(0));
      while (tScan.hasNext()) {
        tScan.next();
        mapSX++;
      }
      tScan.close();
      map = new Tile[mapSX][mapSY];
      for (int y = 0; y < mapSY; y++) {
        Scanner scan = new Scanner(allLines.get(y));
        for (int x = 0; x < mapSX; x++) {
          map[x][y] = new Tile((int) (x-mapSX/2), (int) (y-mapSY/2), scan.nextInt());
        }
        scan.close();
      }
    }
    filename = prefix+title+"/Fixed.txt";
    allLines = IOHelp.readAllLines(filename, inJar);
    for (String line : allLines) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (type.equals("Wall")) {fixedObj.add(new Wall(scan.nextDouble(), scan.nextDouble(), scan.next(), scan.next(), this));}
      else if (type.equals("Door")) {fixedObj.add(new Door(scan.nextDouble(), scan.nextDouble(), scan.next(), scan.next(), this));}
      else if (type.equals("Light")) {fixedObj.add(new Light(scan.nextDouble(), scan.nextDouble(), scan.next(), this));}
      scan.close();
    }

    filename = prefix+title+"/Units.txt";
    allLines = IOHelp.readAllLines(filename, inJar);
    for (String line : allLines) {
      Scanner scan = new Scanner(line);
      String type;
      if (scan.hasNext()) {
        type = scan.next();
      }
      else {type = "gap";}
      if (type.equals("Player")) {
        player = new Player(scan.nextDouble(), scan.nextDouble(), this);
        units.add(player);
      }
      else if (type.equals("Dud")) {units.add(new Dud(scan.nextDouble(), scan.nextDouble(), this));}
      else if (type.equals("TestAI")) {units.add(new TestAI(scan.nextDouble(), scan.nextDouble(), this));}
      scan.close();
    }
  }
}
