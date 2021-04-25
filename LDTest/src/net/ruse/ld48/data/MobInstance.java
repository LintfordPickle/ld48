package net.ruse.ld48.data;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.sprites.SpriteInstance;
import net.ruse.ld48.GameConstants;

public class MobInstance extends CellEntity {

	public static final boolean GOD_MODE = false;

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

	public String mCurrentAnimationName;
	public transient SpriteInstance currentSprite;
	public boolean isPlayerControlled;

	public boolean groundFlag;
	public boolean diggingFlag;
	public boolean swingingFlag;

	public float inputCooldownTimer;
	private String mMobTypeName;

	public boolean isLeftFacing;

	public int health;
	public float damageCooldownTimer;

	public float attackPointWorldX;
	public float attackPointWorldY;

	/* some mob swing for the enemies (e.g. goblins), and some deal damage on touch (e.g. spiders) */
	public boolean damagesOnCollide;
	public boolean swingAttackEnabled;
	public float swingRange;

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

	public boolean isDamageCooldownElapsed() {
		return damageCooldownTimer <= 0;
	}

	// --------------------------------------
	// Constructor 
	// --------------------------------------

	public MobInstance(int pPoolUid) {
		super(pPoolUid);

		mMobTypeName = null;

		radius = 13.f;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void initialise(String pMobTypeName, int pHealth) {
		mMobTypeName = pMobTypeName;
		health = pHealth;

	}

	public void update(LintfordCore pCore) {
		if (damageCooldownTimer > 0.0f) {
			damageCooldownTimer -= pCore.gameTime().elapsedTimeMilli();

		}

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

	public void dealDamage(int pAmt, boolean respectCooldown) {
		if (respectCooldown && !isDamageCooldownElapsed() || GOD_MODE)
			return;

		health -= pAmt;

		if (isPlayerControlled)
			damageCooldownTimer = 1000.f;
		else {
			damageCooldownTimer = 200.f;
		}

	}

}
