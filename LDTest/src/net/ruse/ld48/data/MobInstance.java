package net.ruse.ld48.data;

import net.lintford.library.core.graphics.sprites.SpriteInstance;

public class MobInstance extends CellEntity {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -4225793842972451100L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	public transient SpriteInstance currentSprite;
	public boolean isPlayerControlled;

	// --------------------------------------
	// Constructor 
	// --------------------------------------

	public MobInstance(int pPoolUid) {
		super(pPoolUid);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

}
