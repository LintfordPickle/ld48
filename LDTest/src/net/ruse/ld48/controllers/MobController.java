package net.ruse.ld48.controllers;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.data.MobManager;

public class MobController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Mob Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MobManager mMobManager;

	private LevelController mLevelController;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public MobManager mobManager() {
		return mMobManager;
	}

	@Override
	public boolean isInitialized() {
		return false;

	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MobController(ControllerManager pControllerManager, MobManager pMobManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mMobManager = pMobManager;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void unload() {

	}

	// TODO: Time permitting - refactor
	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lLevel = mLevelController.level();

		final var lMobList = mMobManager.mobInstances();
		final int lNumMobs = lMobList.size();

		for (int i = 0; i < lNumMobs; i++) {
			final var lMobInstance = lMobList.get(i);

			lMobInstance.update(pCore);

			// Digging (in mob controller?)
			// TODO: LEFT / RIGHT
			if (lMobInstance.diggingFlag) {
				System.out.println("digging");
				lLevel.digBlock(lMobInstance.cellX, lMobInstance.cellY + 1, (byte) 1);

			}

			// gravity
			final float lGravAmt = (float) GameConstants.BLOCK_SIZE / 9.6f;
			lMobInstance.velocityY += 0.0096f; //lGravAmt;
			lMobInstance.velocityX = MathHelper.clamp(lMobInstance.velocityX, -0.05f, 0.05f);

			lMobInstance.fractionX += lMobInstance.velocityX;
			lMobInstance.fractionY += lMobInstance.velocityY;

			// grid based collision check
			if (lMobInstance.fractionX < .3f && lLevel.hasCollision(lMobInstance.cellX - 1, lMobInstance.cellY)) {
				lMobInstance.fractionX = 0.3f;

			}

			if (lMobInstance.fractionX > .7f && lLevel.hasCollision(lMobInstance.cellX + 1, lMobInstance.cellY)) {
				lMobInstance.fractionX = 0.7f;

			}

			if (lMobInstance.fractionY < .3f && lLevel.hasCollision(lMobInstance.cellX, lMobInstance.cellY - 1)) {
				lMobInstance.fractionY = 0.3f;

			}

			lMobInstance.groundFlag = false;
			if (lMobInstance.fractionY > .7f && lLevel.hasCollision(lMobInstance.cellX, lMobInstance.cellY + 1)) {
				lMobInstance.fractionY = 0.7f;
				lMobInstance.groundFlag = true;
			}

			while (lMobInstance.fractionX < 0) {
				lMobInstance.fractionX++;
				lMobInstance.cellX--;
			}

			while (lMobInstance.fractionX > 1) {
				lMobInstance.fractionX--;
				lMobInstance.cellX++;
			}

			while (lMobInstance.fractionY < 0) {
				lMobInstance.fractionY++;
				lMobInstance.cellY--;
			}

			while (lMobInstance.fractionY > 1) {
				lMobInstance.fractionY--;
				lMobInstance.cellY++;
			}

			// update mob instance

			lMobInstance.worldPositionX = (float) (lMobInstance.cellX + lMobInstance.fractionX) * GameConstants.BLOCK_SIZE;
			lMobInstance.worldPositionY = (float) (lMobInstance.cellY + lMobInstance.fractionY) * GameConstants.BLOCK_SIZE;

			lMobInstance.velocityX *= .72f;
			lMobInstance.velocityY *= .96f;

		}

	}

}
