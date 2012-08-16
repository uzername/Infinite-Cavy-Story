package main;

import net.slashie.libjcsi.CSIColor;

////////////////////////////////////////////////////////////////////////
/////// BASIC CLASS TO HANDLE ENTITIES SUCH AS AIComponent OR PLAYER ///////
////////////////////////////////////////////////////////////////////////
public class Entity {
	// //// LOGGER INSTANCE //////
	// private static Logger log = Logger.getLogger(Entity.class.getName());

	// //// PRIVATE PARAMETERS //////
	private int x, y; // position
	private char key; // char symbol
	private String name; // entity name
	private CSIColor color; // entity color
	private boolean blocks; // blocks movement or not
	private FighterComponent fighter; // fighter component
	private AIComponent ai; // ai component

	// //// CREATE ENTITY WITH BOTH COMPONENTS /////
	public Entity(int x, int y, char key, String name, CSIColor color,
			Boolean blocks, FighterComponent fighter, AIComponent ai) {
		this.x = x;
		this.y = y;
		this.key = key;
		this.name = name;
		this.color = color;
		this.blocks = blocks;
		this.fighter = fighter;
		this.ai = ai;
	}

	// ///// CREATE ENTITY WITH FIGHTER COMPONENT //////
	public Entity(int x, int y, char key, String name, CSIColor color,
			Boolean blocks, FighterComponent fighter) {
		this.x = x;
		this.y = y;
		this.key = key;
		this.name = name;
		this.color = color;
		this.blocks = blocks;
		this.fighter = fighter;
	}

	// /////// CREATE ENTITY WITHOUT COMPONENTS /////////
	public Entity(int x, int y, char key, String name, CSIColor color,
			Boolean blocks) {
		this.x = x;
		this.y = y;
		this.key = key;
		this.name = name;
		this.color = color;
		this.blocks = blocks;
	}

	// //// GETTERS /////////
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public char getKey() {
		return key;
	}

	public boolean blocks() {
		return blocks;
	}

	public CSIColor getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public FighterComponent getFighterComponent() {
		return this.fighter;
	}

	public AIComponent getAIComponent() {
		return this.ai;
	}

	// ////////////////////////

	// /// SETTERS/////////////

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setFighterComponent(FighterComponent fighter) {
		this.fighter = fighter;
	}

	public void setAIComponent(AIComponent ai) {
		this.ai = ai;
	}

	// ////////////////////////

	// // FUNCTION TO MOVE ENTITY /////
	public void move(int dx, int dy) {
		if (!MapGenerator.getInstance().isBlocked(this.x + dx, this.y + dy)) {
			this.x += dx;
			this.y += dy;
		}
	}

	// // FUNCTION TO DRAW ENTITY /////
	public void draw() {
		MainGame.getCSI().print(this.x, this.y, this.key, this.color);
	}

	// // CREARING THE ENTITIES SPACE
	public void clear() {
		MainGame.getCSI().print(this.x, this.y, ' ', this.color);
	}

	// // FUNCTIONS TO MOVE ENTITY TOWARS TARGET LOCATION ////
	public void moveTowards(int target_x, int target_y) {
		int dx = target_x - this.x;
		int dy = target_y - this.y;
		double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

		// normalize it to length 1 (preserving direction), then round it and
		// convert to integer so the movement is restricted to the map grid
		dx = (int) (Math.round(dx / distance));
		dy = (int) (Math.round(dy / distance));
		this.move(dx, dy);
	}

	// /// FUNCTION TO COUNT DISTANCE TO OTHER ENTITY //////
	public double distanceTo(Entity other) {
		int dx = other.x - this.x;
		int dy = other.y - this.y;

		return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
	}

	// //// ACTIONS FOR ENTITIES DEATH /////
	public void deathFunction() {
		String name = this.getName();
		if (name.equals("player")) {
			MainGame.getInstance().setGameState("dead");
			this.key = '%';
			this.color = CSIColor.DARK_RED;
		} else if ((name.equals("troll")) || (name.equals("orc"))) {
			this.key = '%';
			this.color = CSIColor.DARK_RED;
			this.blocks = false;
			this.fighter = null;
			this.ai = null;
			MainGame.getInstance().newMessage("--" + this.name + " dies");
			this.name = "remains";
		}
	}
}