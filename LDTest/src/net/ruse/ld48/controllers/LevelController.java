package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.ruse.ld48.data.Level;

public class LevelController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Level Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Level mLevel;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Level level() {
		return mLevel;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public LevelController(ControllerManager pControllerManager, Level pLevel, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mLevel = pLevel;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(LintfordCore pCore) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_R)) {
			mLevel.loadLevel();

		}

		return super.handleInput(pCore);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void loadLevelFromFile(String pFilename) {
		mLevel.loadLevel();
	}

}
