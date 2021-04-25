package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;

public class GameStateController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Game State Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private PlayerController mPlayerController;

	private LevelController mLevelController;
	private ItemController mItemController;
	private MobController mMobController;

	private boolean mIsGameStarted = false;
	private boolean mHasGameEnded = false;

	private int mCurrentGold;
	private int mTargetGold;
	private boolean mPlayerReachExit;

	// --------------------------------------
	// Properties
	// --------------------------------------

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

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_R)) {
			setupNewGame(10L);

		}

		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void setupNewGame(long pSeed) {
		RandomNumbers.reseed(pSeed);
		mCurrentGold = 0;
		mTargetGold = 120;//RandomNumbers.random(400, 600);

		mLevelController.startNewGame(pSeed);
		mMobController.startNewGame(pSeed);
		mItemController.startNewGame(pSeed);

		mIsGameStarted = true;
		mHasGameEnded = false;
		mPlayerReachExit = false;

	}

	public void endGame() {
		mIsGameStarted = false;
		mHasGameEnded = true;

	}

	public void addGold(int pAmt) {
		mCurrentGold += pAmt;

	}

}
