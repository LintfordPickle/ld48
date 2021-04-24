package net.ruse.ld48.controllers;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameStateController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Game State Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private PlayerController mPlayerController;

	private boolean mIsGameStarted = false;
	private boolean mHasGameEnded = false;

	private int mCurrentGold;
	private int mTargetGold;

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

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void setupNewGame(int pTargetGold) {
		mTargetGold = pTargetGold;

		mIsGameStarted = true;
		mHasGameEnded = false;

	}

	public void endGame() {
		mTargetGold = 0;
		mCurrentGold = 0;

		mIsGameStarted = false;
		mHasGameEnded = true;

	}

	public void addGold(int pAmt) {
		mCurrentGold += pAmt;

	}

}
