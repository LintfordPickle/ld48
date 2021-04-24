package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

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

	@Override
	public boolean handleInput(LintfordCore pCore) {

		final var lKeyboard = pCore.input().keyboard();

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_A)) {
			mPlayerMobInstance.velocityX -= .1f;
		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_D)) {
			mPlayerMobInstance.velocityX += .1f;
		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_SPACE) && mPlayerMobInstance.groundFlag) {
			mPlayerMobInstance.velocityY = -.21f;
			mPlayerMobInstance.groundFlag = false;

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_R)) {
			mPlayerMobInstance.setPosition(0, 0);

		}

		return super.handleInput(pCore);
	}

}
