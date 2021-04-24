package net.ruse.ld48.data;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.sprites.SpriteInstance;
import net.ruse.ld48.GameConstants;

public class MobInstance extends CellEntity {

	public static final String MOB_TYPE_DWARF = "DWARF";
	public static final String MOB_TYPE_GOBLIN = "GOBLIN";

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -4225793842972451100L;

	public static final float COOLDOWN_DIG = 300; // ms

	// --------------------------------------
	// Variables
	// --------------------------------------

	public transient SpriteInstance currentSprite;
	public boolean isPlayerControlled;

	public boolean groundFlag;
	public boolean diggingFlag;

	public float inputCooldownTimer;
	private String mMobTypeName;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean isAssigned() {
		return mMobTypeName != null;
	}

	public String mobTypeName() {
		return mMobTypeName;
	}

	public boolean isInputCooldownElapsed() {
		return inputCooldownTimer <= 0.f;
	}

	// --------------------------------------
	// Constructor 
	// --------------------------------------

	public MobInstance(int pPoolUid) {
		super(pPoolUid);

		mMobTypeName = null;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void initialise(String pMobTypeName) {
		mMobTypeName = pMobTypeName;

	}

	public void update(LintfordCore pCore) {
		if (inputCooldownTimer > 0.0f)
			inputCooldownTimer -= pCore.gameTime().elapsedTimeMilli();

	}

	public void setPosition(float pWorldPositionX, float pWorldPositionY) {

		worldPositionX = pWorldPositionX;
		worldPositionY = pWorldPositionY;

		cellX = (int) (worldPositionX / GameConstants.BLOCK_SIZE);
		cellY = (int) (worldPositionY / GameConstants.BLOCK_SIZE);

		fractionX = worldPositionX - (cellX * GameConstants.BLOCK_SIZE);
		fractionY = worldPositionY - (cellY * GameConstants.BLOCK_SIZE);

		velocityX = 0.f;
		velocityY = 0.f;

	}

}
