package main;

import java.util.ArrayList;
import java.util.Random;

import net.slashie.libjcsi.CSIColor;
import net.slashie.libjcsi.ConsoleSystemInterface;

public class MapGenerator {
	// // LOGGET INSTANCE /////
	// private static Logger log =
	// Logger.getLogger(MapGenerator.class.getName());

	// /// SINGLETONE INSTANCE AND GETTER /////
	private static MapGenerator instance = new MapGenerator();

	public static MapGenerator getInstance() {
		return instance;
	}

	// ////////////////////////////////////////

	// //// SIZE OF MAPS
	public final static int MAP_WIDTH = 65;
	public final static int MAP_HEIGHT = 25;

	// ///// DUNGEON GENERATOR PARAMETERS
	private final static int ROOM_MAX_SIZE = 10;
	private final static int ROOM_MIN_SIZE = 6;
	private final static int MAX_ROOMS = 30;
	private final static int MAX_ROOM_MONSTERS = 3;

	// // GETTING INSTANCES FROM MAINGAME CLASS
	private static Tile[][] map = MainGame.getInstance().getMap();
	private static ConsoleSystemInterface csi = MainGame.getCSI();
	private static Entity player = MainGame.getInstance().getPlayer();

	// /// ARRAYLIST FOR MAP OBJECTS /////
	private ArrayList<Entity> objects = new ArrayList<Entity>();

	public ArrayList<Entity> getObjects() {
		return objects;
	}

	// ///////////////////////////////////

	// /// MAIN DUNGEON GENERATOR //////
	public void generateMap() {
		// / fill the map with blocking tiles
		for (int x = 0; x < MAP_WIDTH; x++)
			for (int y = 0; y < MAP_HEIGHT; y++)
				map[x][y] = new Tile(true);

		// initialise array for rooms
		Rect[] rooms = new Rect[MAX_ROOMS];
		Random rand = new Random();

		int num_rooms = 0;

		for (int r = 0; r < MAX_ROOMS; r++) {
			// randomizing room size
			int w = rand.nextInt((ROOM_MAX_SIZE - ROOM_MIN_SIZE))
					+ ROOM_MIN_SIZE;
			int h = rand.nextInt((ROOM_MAX_SIZE - ROOM_MIN_SIZE))
					+ ROOM_MIN_SIZE;

			// randomizing room position
			int x = rand.nextInt(MAP_WIDTH - w - 1);
			int y = rand.nextInt(MAP_HEIGHT - h - 1);

			// Rectangle class makes rectangles easier to work with
			Rect new_room = new Rect(x, y, w, h);

			// check if new room doesn't intersect with older ones
			boolean failed = false;
			for (Rect other_room : rooms) {
				if (other_room != null)
					if (new_room.intersect(other_room)) {
						failed = true;
						break;
					}
			}

			if (!failed) {
				// this means there are no intersections, so this room is valid
				createRoom(new_room); // create rooms
				placeObjects(new_room); // create objects in room

				// save room center for tunnel generating
				int roomCenterX = new_room.getCenterX();
				int roomCenterY = new_room.getCenterY();

				if (num_rooms == 0) {
					// set player in first room
					player.setX(roomCenterX);
					player.setY(roomCenterY);
				} else {

					// get previous room center
					int prevRoomCenterX = rooms[num_rooms - 1].getCenterX();
					int prevRoomCenterY = rooms[num_rooms - 1].getCenterY();

					// connect current and previous room with tunnel
					if (rand.nextInt(1) == 1) {
						create_h_tunnel(prevRoomCenterX, roomCenterX,
								prevRoomCenterY);
						create_v_tunnel(prevRoomCenterY, roomCenterY,
								roomCenterX);
					} else {

						create_v_tunnel(prevRoomCenterY, roomCenterY,
								prevRoomCenterX);
						create_h_tunnel(prevRoomCenterX, roomCenterX,
								roomCenterY);
					}
				}

				// append new room to array
				rooms[num_rooms] = new_room;
				num_rooms++;
			}
		}
	}

	// // FUNCTION TO CREATE ROOM IN THE RECT
	public static void createRoom(Rect room) {
		for (int x = room.getX1() + 1; x < room.getX2(); x++)
			for (int y = room.getY1() + 1; y < room.getY2(); y++) {
				// fillig the room with empty tiles
				map[x][y].setBlocked(false);
				map[x][y].setBlockedSight(false);
			}
	}

	// // CREATE HORIZONTAL TUNNEL
	public static void create_h_tunnel(int x1, int x2, int y) {
		// horizontal tunnel. min() and max() are used in case x1>x2

		for (int x = Math.min(x1, x2); x < Math.max(x1, x2) + 1; x++) {
			map[x][y].setBlocked(false);
			map[x][y].setBlockedSight(false);
		}
	}

	// // CREATE VERTICAL TUNNEL
	public static void create_v_tunnel(int y1, int y2, int x) {
		for (int y = Math.min(y1, y2); y < Math.max(y1, y2) + 1; y++) {
			map[x][y].setBlocked(false);
			map[x][y].setBlockedSight(false);
		}

	}

	// //// FUNCTION TO PLACE OBJECTS IN THE ROOM
	public void placeObjects(Rect room) {
		Random rand = new Random();
		int num_Monsters = rand.nextInt(MAX_ROOM_MONSTERS);
		for (int i = 0; i < num_Monsters; i++) {
			// random object position
			int x = rand.nextInt(room.getX2() - room.getX1()) + room.getX1();
			int y = rand.nextInt(room.getY2() - room.getY1()) + room.getY1();
			if (!isBlocked(x, y))

				// TODO HERE MONSTERS SPAWN
				if (rand.nextInt(100) < 80) { // 80% - orc
					Entity AIComponent = new Entity(x, y, 'O', "orc",
							CSIColor.LIME_GREEN, true);
					FighterComponent fighter_component = new FighterComponent(
							AIComponent, 10, 0, 3);
					AIComponent ai_component = new AIComponent(AIComponent);
					AIComponent.setFighterComponent(fighter_component);
					AIComponent.setAIComponent(ai_component);
					objects.add(AIComponent);
				} else { // 20% - troll
					Entity AIComponent = new Entity(x, y, 'T', "troll",
							CSIColor.DARK_GREEN, true);
					FighterComponent fighter_component = new FighterComponent(
							AIComponent, 16, 1, 4);
					AIComponent ai_component = new AIComponent(AIComponent);
					AIComponent.setFighterComponent(fighter_component);
					AIComponent.setAIComponent(ai_component);
					objects.add(AIComponent);
				}
		}
	}

	// /// CHECK IF THE POSITION X Y IS BLOCKED
	public boolean isBlocked(int x, int y) {
		if (map[x][y].isBlocked())
			return true;

		for (Entity object : objects) {
			if (object.blocks() && (x == object.getX()) && (y == object.getY()))
				return true;
		}
		return false;
	}

	// /// PATTERN FOR LIGHT SPREADING
	private final static int light_pattern[][] = {
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0 },
			{ 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
			{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			{ 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
			{ 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0 } };

	// ////////////////////////////////
	// //// DRAW ALL MAP FUNCTION /////
	// ////////////////////////////////
	public void drawMap() {
		for (int x = 0; x < MAP_WIDTH; x++)
			for (int y = 0; y < MAP_HEIGHT; y++)
				if (map[x][y].isBlockedSight()) {
					if (map[x][y].wasVisited())
						csi.print(x, y, '▓', CSIColor.DARK_GRAY); // walls
				} else if (map[x][y].wasVisited())
					csi.print(x, y, '.', CSIColor.DARK_GRAY); // empty space

		// simple sight view
		int pl_x_l = player.getX() - 6;
		if (pl_x_l < 0)
			pl_x_l = 0;
		int pl_x_r = player.getX() + 6;
		if (pl_x_r > MAP_WIDTH)
			pl_x_r = MAP_WIDTH;
		int pl_y_t = player.getY() - 4;
		if (pl_y_t < 0)
			pl_y_t = 0;
		int pl_y_b = player.getY() + 4;
		if (pl_y_b > MAP_HEIGHT)
			pl_y_b = MAP_HEIGHT;

		boolean is_light;
		int i = 0, j = 0;
		for (int x = pl_x_l; x < pl_x_r; x++) {
			j = 0;
			for (int y = pl_y_t; y < pl_y_b; y++) {
				is_light = (light_pattern[j][i] == 1);
				if (is_light) {
					// draw objects you can see
					map[x][y].setVisited(true);
					for (Entity object : objects)
						if (object.getX() == x && object.getY() == y)
							object.draw();
				}
				if (map[x][y].isBlockedSight())
					if (is_light) {
						csi.print(x, y, '▓', CSIColor.LIGHT_GRAY); // you see
					} else if (map[x][y].wasVisited())
						csi.print(x, y, '▓', CSIColor.DARK_GRAY); // you saw
				j++;
			}
			i++;
		}

	}

	// // FUNCTION TO MOVE PLAYER, OR ATTACK IF TARGET IN THE DIRECTION PLACE
	public void playerMoveOrAttack(int dx, int dy) {
		int x = player.getX() + dx;
		int y = player.getY() + dy;

		Entity target = null;
		for (Entity object : objects)
			if ((object.getX() == x) && (object.getY() == y)) {
				target = object;
				break;
			}

		if (target != null && !target.getName().equals("remains")) {
			player.getFighterComponent().attack(target);
		} else {
			MainGame.getInstance().getPlayer().move(dx, dy);
		}
	}
}