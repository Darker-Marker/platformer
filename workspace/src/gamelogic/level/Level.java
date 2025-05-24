package gamelogic.level;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
				else if (values[x][y] == 16)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
				else if (values[x][y] == 17)
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
				else if (values[x][y] == 18)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
				else if (values[x][y] == 19)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
				else if (values[x][y] == 20)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
				else if (values[x][y] == 21)
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
			}

		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
				else
					addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}

			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();
				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!


//Adds gas tiles until the requisite number of squares are filled or there is no more room 
private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {


    Gas g = new Gas (col, row, tileSize, tileset.getImage("GasOne"), this, 0);
    map.addTile(col, row, g);
    placedThisRound.add(g);
    numSquaresToFill--;

    while(placedThisRound.size() > 0 && numSquaresToFill > 0){
        Gas current = placedThisRound.remove(0);
        int currRow = current.getRow();
        int currCol = current.getCol();


        // Up
        if(currRow - 1 < map.getTiles().length && !map.getTiles()[currCol][currRow - 1].isSolid() && !(map.getTiles()[currCol][currRow - 1] instanceof Gas) && numSquaresToFill > 0){
            Gas uG = new Gas (currCol, currRow - 1, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol, currRow - 1, uG);
            placedThisRound.add(uG);
            numSquaresToFill--;
        }

        // Up-right
        if(currCol + 1 < map.getTiles().length && !map.getTiles()[currCol + 1][currRow - 1].isSolid() && !(map.getTiles()[currCol + 1][currRow - 1] instanceof Gas) && numSquaresToFill > 0){
            Gas rUG = new Gas (currCol + 1, currRow - 1, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol + 1, currRow - 1, rUG);
            placedThisRound.add(rUG);
            numSquaresToFill--;
        }

        // Up-left
        if(currCol - 1 < map.getTiles().length && !map.getTiles()[currCol - 1][currRow - 1].isSolid() && !(map.getTiles()[currCol - 1][currRow - 1] instanceof Gas) && numSquaresToFill > 0){
            Gas lUG = new Gas (currCol - 1, currRow - 1, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol - 1, currRow - 1, lUG);
            placedThisRound.add(lUG);
            numSquaresToFill--;
        }

        // Right
        if(currCol + 1 < map.getTiles().length && !map.getTiles()[currCol + 1][currRow].isSolid() && !(map.getTiles()[currCol + 1][currRow] instanceof Gas) && numSquaresToFill > 0){
            Gas rG = new Gas (currCol + 1, currRow, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol + 1, currRow, rG);
            placedThisRound.add(rG);
            numSquaresToFill--;
        }

        // Left
        if(currCol - 1 < map.getTiles().length && !map.getTiles()[currCol - 1][currRow].isSolid() && !(map.getTiles()[currCol - 1][currRow] instanceof Gas) && numSquaresToFill > 0){
            Gas lG = new Gas (currCol - 1, currRow, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol - 1, currRow, lG);
            placedThisRound.add(lG);
            numSquaresToFill--;
        }

        // Down
        if(currRow + 1 < map.getTiles().length && !map.getTiles()[currCol][currRow + 1].isSolid() && !(map.getTiles()[currCol][currRow + 1] instanceof Gas) && numSquaresToFill > 0){
            Gas dG = new Gas (currCol, currRow + 1, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol, currRow + 1, dG);
            placedThisRound.add(dG);
            numSquaresToFill--;
        }

        // Down-right
        if(currRow + 1 < map.getTiles().length && !map.getTiles()[currCol + 1][currRow + 1].isSolid() && !(map.getTiles()[currCol + 1][currRow + 1] instanceof Gas) && numSquaresToFill > 0){
            Gas dRG = new Gas (currCol + 1, currRow + 1, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol + 1, currRow + 1, dRG);
            placedThisRound.add(dRG);
            numSquaresToFill--;
        }

        // Down-left
        if(currRow + 1 < map.getTiles().length && !map.getTiles()[currCol - 1][currRow + 1].isSolid() && !(map.getTiles()[currCol - 1][currRow + 1] instanceof Gas) && numSquaresToFill > 0){
            Gas dLG = new Gas (currCol - 1, currRow + 1, tileSize, tileset.getImage("GasOne"), this, 0);
            map.addTile(currCol - 1, currRow + 1, dLG);
            placedThisRound.add(dLG);
            numSquaresToFill--;
        }
    }


	


	// #############################################################################################################
    // Your code goes here!
    // Please make sure you read the rubric/directions carefully and implement the
    // solution recursively!
    private void water(int col, int row, Map map, int fullness) {


        String img = "Full_water";
        if (fullness == 2) {
            img = "Half_water";
        }
        if (fullness == 1) {
            img = "Quarter_water";
        }
        if (fullness == 0) {
            img = "Falling_water";
        }
        // creates a new water in the spot indicated
        Water w = new Water(col, row, tileSize, tileset.getImage(img), this, fullness);
        map.addTile(col, row, w);


        // down if possible
        if (row + 1 < map.getTiles()[0].length && !(map.getTiles()[row + 1][row] instanceof Water)
                && !map.getTiles()[col][row + 1].isSolid()) {
            if(row + 2 < map.getTiles()[0].length && !map.getTiles()[col][row + 2].isSolid()){
                    water(col, row + 1, map, 0);
            }
            else if (row + 2 < map.getTiles()[0].length){
                    water(col, row + 1, map, 3);
            }
             else if(!(row + 2 < map.getTiles()[0].length)){
                    water(col, row + 1, map, 0);
            }
        }
        // right continuously
        else if(row + 1 < map.getTiles()[0].length && map.getTiles()[col][row + 1].isSolid()){ // HELP IT BROKE
            if (col + 1 < map.getTiles().length && !(map.getTiles()[col + 1][row] instanceof Water)
                    && !map.getTiles()[col + 1][row].isSolid()) {
                        if(fullness>1)
                water(col + 1, row, map, fullness -1);
                else{
                    water(col + 1, row, map, fullness);
                }
            }
       
            // left continuously
            if (col - 1 < map.getTiles().length && !(map.getTiles()[col - 1][row] instanceof Water)
                    && !map.getTiles()[col - 1][row].isSolid()) {
                        if(fullness>1)
                water(col - 1, row, map, fullness -1);
                else{
                    water(col - 1, row, map, fullness);
            }
        }
    }
    }





public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }


	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);




	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());
	    }




	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}
}