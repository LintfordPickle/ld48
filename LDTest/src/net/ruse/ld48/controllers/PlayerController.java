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

	private ItemController mItemController;
	private MobInstance mPlayerMobInstance;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public boolean isInitialized() {
		return mPlayerMobInstance != null;

	}

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
	public void initialize(LintfordCore pCore) {
		mItemController = (ItemController) pCore.controllerManager().getControllerByNameRequired(ItemController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {

		final var lKeyboard = pCore.input().keyboard();

		// Digging
		mPlayerMobInstance.diggingFlag = false;
		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_J)) {
			mPlayerMobInstance.diggingFlag = true;

		}

		mPlayerMobInstance.swingingFlag = false;
		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_K)) {
			mPlayerMobInstance.swingingFlag = true;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_G) && mPlayerMobInstance.isInputCooldownElapsed()) {
			final float lWorldPositionX = mPlayerMobInstance.worldPositionX;
			final float lWorldPositionY = mPlayerMobInstance.worldPositionY;
			final float lSignum = mPlayerMobInstance.isLeftFacing ? -1.f : 1.f;

			if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_S)) {
				mItemController.addTnt(lWorldPositionX + lSignum * 32.f, lWorldPositionY, 0.f, -.01f);

			} else {
				mItemController.addTnt(lWorldPositionX, lWorldPositionY, lSignum * .15f, -.2f);

			}

			mPlayerMobInstance.inputCooldownTimer = 300;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_A)) {
			if (!mPlayerMobInstance.diggingFlag)
				mPlayerMobInstance.velocityX -= .1f;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_D)) {
			if (!mPlayerMobInstance.diggingFlag)
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
