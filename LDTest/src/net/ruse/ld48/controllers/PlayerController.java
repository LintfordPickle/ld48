package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.data.MobInstance;

public class PlayerController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Player Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private SoundFxController mSoundFxController;

	private LevelController mLevelController;
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
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, entityGroupID());
		mSoundFxController = (SoundFxController) pCore.controllerManager().getControllerByNameRequired(SoundFxController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {

		final var lKeyboard = pCore.input().keyboard();

		// Digging / Attacking
		mPlayerMobInstance.swingingFlag = false;
		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
			mPlayerMobInstance.swingingFlag = true;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_G) && mPlayerMobInstance.isInputCooldownElapsed()) {
			final float lWorldPositionX = mPlayerMobInstance.worldPositionX;
			final float lWorldPositionY = mPlayerMobInstance.worldPositionY;
			final float lSignum = mPlayerMobInstance.isLeftFacing ? -1.f : 1.f;

			if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_S)) {
				final var lLevel = mLevelController.level();
				final int lPlayerMobTileCoord = lLevel.getLevelTileCoord(mPlayerMobInstance.cellX, mPlayerMobInstance.cellY);
				boolean lIsAdjacentBlock = false;
				if (lSignum < 0.f) { // left
					if (lLevel.getLevelBlockType(lLevel.getLeftBlockIndex(lPlayerMobTileCoord)) != Level.LEVEL_TILE_INDEX_AIR) {
						lIsAdjacentBlock = true;
					}
				} else {
					if (lLevel.getLevelBlockType(lLevel.getRightBlockIndex(lPlayerMobTileCoord)) != Level.LEVEL_TILE_INDEX_AIR) {
						lIsAdjacentBlock = true;
					}
				}
				final float lOffsetX = lIsAdjacentBlock ? 0.f : lSignum * 32.f;
				mItemController.addTnt(lWorldPositionX + lOffsetX, lWorldPositionY, 0.f, -.01f);

			} else {
				mItemController.addTnt(lWorldPositionX, lWorldPositionY, lSignum * .15f, -.2f);

			}

			mPlayerMobInstance.inputCooldownTimer = 300;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_A)) {
			if (!mPlayerMobInstance.swingingFlag)
				mPlayerMobInstance.velocityX -= .1f;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_D)) {
			if (!mPlayerMobInstance.swingingFlag)
				mPlayerMobInstance.velocityX += .1f;

		}

		if (lKeyboard.isKeyDown(GLFW.GLFW_KEY_SPACE) && mPlayerMobInstance.groundFlag) {
			mPlayerMobInstance.velocityY = -.21f;
			mSoundFxController.playSound(SoundFxController.SOUND_JUMP);
			mPlayerMobInstance.groundFlag = false;

		}

		return super.handleInput(pCore);
	}

}
