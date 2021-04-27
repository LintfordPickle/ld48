package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;
import net.ruse.ld48.GameConstants;

public class GameStateController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Game State Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private PlayerController mPlayerController;

	private SoundFxController mSoundfxController;
	private LevelController mLevelController;
	private ItemController mItemController;
	private MobController mMobController;

	private boolean mIsGameStarted = false;
	private boolean mHasGameEnded = false;

	private float mTnTCooldownTimer;
	public static final float TNT_COOLDOWN_TIME = 1500;

	private int mCurrentLevel;
	private int mTargetGold;
	private float mMobTileDiscardChance;

	private int mCurrentGold;
	private boolean mPlayerReachExit;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public float mobTileDiscardChance() {
		return mMobTileDiscardChance;
	}

	public int levelNumber() {
		return mCurrentLevel;
	}

	public boolean canThrowTnt() {
		return mTnTCooldownTimer <= 0;
	}

	public float tntCooldownTimer() {
		return mTnTCooldownTimer;
	}

	public void resetTntCooldown() {
		mTnTCooldownTimer = TNT_COOLDOWN_TIME;
	}

	public boolean isGameStarted() {
		return mIsGameStarted;
	}

	public boolean hasGameEnded() {
		return mHasGameEnded;
	}

	public int targetGold() {
		return mTargetGold;
	}

	public int currentGold() {
		return mCurrentGold;
	}

	@Override
	public boolean isInitialized() {
		return false;
	}

	// EXIT CONDITIONS ----------------------

	public boolean isFullGoldCollected() {
		return mCurrentGold >= mTargetGold;
	}

	public void exitReached() {
		if (!isFullGoldCollected())
			return;

		mPlayerReachExit = true;

	}

	public boolean hasPlayerWon() {
		return mPlayerReachExit && isFullGoldCollected();
	}

	public boolean isPlayerDead() {
		return mPlayerController.playerMobInstance().health <= 0;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameStateController(ControllerManager pControllerManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mPlayerController = (PlayerController) pCore.controllerManager().getControllerByNameRequired(PlayerController.CONTROLLER_NAME, entityGroupID());

		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, entityGroupID());
		mItemController = (ItemController) pCore.controllerManager().getControllerByNameRequired(ItemController.CONTROLLER_NAME, entityGroupID());
		mMobController = (MobController) pCore.controllerManager().getControllerByNameRequired(MobController.CONTROLLER_NAME, entityGroupID());
		mSoundfxController = (SoundFxController) pCore.controllerManager().getControllerByNameRequired(SoundFxController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (GameConstants.DEBUG_MODE && pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R)) {
			setupNewGame(mCurrentLevel);

		}

		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		if (mTnTCooldownTimer > 0) {
			mTnTCooldownTimer -= pCore.gameTime().elapsedTimeMilli();

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void setupNewGame(int pLevelNumber) {
		mCurrentLevel = pLevelNumber;
		mCurrentGold = 0;

		switch (pLevelNumber) {
		case 1:
			mTargetGold = 120;
			mMobTileDiscardChance = 80;
			break;

		default:
		case 2:
			mTargetGold = 200;
			mMobTileDiscardChance = 65;
			break;

		case 3:
			mTargetGold = 250;
			mMobTileDiscardChance = 50F;
			break;
		}

		mLevelController.startNewGame(mCurrentLevel);
		mMobController.startNewGame(mCurrentLevel);
		mItemController.startNewGame(mCurrentLevel);

		mIsGameStarted = true;
		mHasGameEnded = false;
		mPlayerReachExit = false;

	}

	public void endGame() {
		mIsGameStarted = false;
		mHasGameEnded = true;

	}

	public void addGold(int pAmt) {

		final int lSoundVariation = RandomNumbers.random(0, 2);
		switch (lSoundVariation) {
		default:
		case 0:
			mSoundfxController.playSound(SoundFxController.SOUND_GOLD_1);
			break;

		case 1:
			mSoundfxController.playSound(SoundFxController.SOUND_GOLD_2);
			break;

		}

		mCurrentGold += pAmt;

	}

}
