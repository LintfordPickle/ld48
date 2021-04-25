package net.ruse.ld48.data;

public class ItemInstance extends CellEntity {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -3634257026339016844L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	public int itemTypeIndex;
	public float timeAlive;

	public boolean interactsWithMobs;
	public boolean physicsEnabled;
	public boolean isPickUpAble;
	public boolean isPickedUp;
	public boolean isFlashOn;
	public float flashTimer;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ItemInstance(int pPoolUid) {
		super(pPoolUid);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------	

	public void setupItem(int pItemTypeIndex) {
		itemTypeIndex = pItemTypeIndex;
		timeAlive = 0;
		isPickedUp = false;

	}

	public void kill() {
		itemTypeIndex = -1;
		isPickedUp = true;
	}

}
