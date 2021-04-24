package net.ruse.ld48.controllers;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.ruse.ld48.data.MobInstance;

public class PlayerController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Player Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MobInstance mPlayerMobInstance;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public MobInstance playerMobInstance() {
		return mPlayerMobInstance;
	}

	public void playerMobInstance(MobInstance pPlayerMobInstance) {
		mPlayerMobInstance = pPlayerMobInstance;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public PlayerController(ControllerManager pControllerManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	public boolean isInitialized() {
		return mPlayerMobInstance != null;

	}

	@Override
	public void initialize(LintfordCore pCore) {

	}

	@Override
	public void unload() {

	}

}
