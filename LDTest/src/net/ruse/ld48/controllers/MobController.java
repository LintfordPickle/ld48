package net.ruse.ld48.controllers;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.data.CellEntity;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.data.MobInstance;
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

	private PlayerController mPlayerController;
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
		mPlayerController = (PlayerController) pCore.controllerManager().getControllerByNameRequired(PlayerController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lLevel = mLevelController.level();

		final var lMobList = mMobManager.mobInstances();
		final int lNumMobs = lMobList.size();

		for (int i = 0; i < lNumMobs; i++) {
			final var lMobInstance = lMobList.get(i);

			lMobInstance.update(pCore);

			// TODO: LEFT / RIGHT
			if (lMobInstance.diggingFlag) {
				mLevelController.digLevel(lMobInstance.cellX, lMobInstance.cellY + 1, (byte) 1);

			}

			if (!lMobInstance.isPlayerControlled) {
				updateEnemyAi(pCore, lLevel, lMobInstance);

				updateEnemyPlayerCollision(pCore, lLevel, lMobInstance);

			}

			updateMobPhysics(pCore, lLevel, lMobInstance);

		}

	}

	private void updateMobPhysics(LintfordCore pCore, Level pLevel, MobInstance pMobInstance) {

		// gravity
		pMobInstance.velocityY += 0.0096f;
		pMobInstance.velocityY = MathHelper.clamp(pMobInstance.velocityY, -0.2f, 0.4f);
		pMobInstance.velocityX = MathHelper.clamp(pMobInstance.velocityX, -0.05f, 0.05f);

		pMobInstance.fractionX += pMobInstance.velocityX;
		pMobInstance.fractionY += pMobInstance.velocityY;

		// grid based collision check
		if (pMobInstance.fractionX < .3f && pLevel.hasCollision(pMobInstance.cellX - 1, pMobInstance.cellY)) {
			pMobInstance.fractionX = 0.3f;

			if (pMobInstance.velocityX < 0)
				pMobInstance.velocityX = 0;

		}

		if (pMobInstance.fractionX > .7f && pLevel.hasCollision(pMobInstance.cellX + 1, pMobInstance.cellY)) {
			pMobInstance.fractionX = 0.7f;

			if (pMobInstance.velocityX > 0)
				pMobInstance.velocityX = 0;

		}

		if (pMobInstance.fractionY < .3f && pLevel.hasCollision(pMobInstance.cellX, pMobInstance.cellY - 1)) {
			pMobInstance.fractionY = 0.3f;

			if (pMobInstance.velocityY < 0)
				pMobInstance.velocityY = 0;

		}

		pMobInstance.groundFlag = false;
		if (pMobInstance.fractionY > .7f && pLevel.hasCollision(pMobInstance.cellX, pMobInstance.cellY + 1)) {
			pMobInstance.fractionY = 0.7f;
			pMobInstance.groundFlag = true;
			if (pMobInstance.velocityY > 0)
				pMobInstance.velocityY = 0;

		}

		while (pMobInstance.fractionX < 0) {
			pMobInstance.fractionX++;
			pMobInstance.cellX--;
		}

		while (pMobInstance.fractionX > 1) {
			pMobInstance.fractionX--;
			pMobInstance.cellX++;
		}

		while (pMobInstance.fractionY < 0) {
			pMobInstance.fractionY++;
			pMobInstance.cellY--;
		}

		while (pMobInstance.fractionY > 1) {
			pMobInstance.fractionY--;
			pMobInstance.cellY++;
		}

		// update mob instance

		pMobInstance.worldPositionX = (float) (pMobInstance.cellX + pMobInstance.fractionX) * GameConstants.BLOCK_SIZE;
		pMobInstance.worldPositionY = (float) (pMobInstance.cellY + pMobInstance.fractionY) * GameConstants.BLOCK_SIZE;

		pMobInstance.velocityX *= .72f;
		pMobInstance.velocityY *= .96f;
	}

	private void updateEnemyAi(LintfordCore pCore, Level pLevel, MobInstance pMobInstance) {
		final var lPlayerMobInstance = mPlayerController.playerMobInstance();

		if (pMobInstance.worldPositionX - 16.f < lPlayerMobInstance.worldPositionX) {
			pMobInstance.velocityX += 0.005f;
		}

		if (pMobInstance.worldPositionX + 16.f > lPlayerMobInstance.worldPositionX) {
			pMobInstance.velocityX -= 0.005f;
		}

	}

	private void updateEnemyPlayerCollision(LintfordCore pCore, Level pLevel, MobInstance pMobInstance) {
		final var lPlayerMobInstance = mPlayerController.playerMobInstance();

		// first check the cells
		if (Math.abs(lPlayerMobInstance.cellX - pMobInstance.cellX) >= 2 || Math.abs(lPlayerMobInstance.cellY - pMobInstance.cellY) >= 2) {
			return;

		}

		// then check the circus colls
		if (overlaps(pMobInstance, lPlayerMobInstance)) {
			final float lAngle = (float) Math.atan2(lPlayerMobInstance.worldPositionY - pMobInstance.worldPositionY, lPlayerMobInstance.worldPositionX - pMobInstance.worldPositionX);

			final float lRepelPower = 0.03f;

			lPlayerMobInstance.velocityX += Math.cos(lAngle) * lRepelPower;
			lPlayerMobInstance.velocityY += Math.sin(lAngle) * lRepelPower * 0.025f;

			pMobInstance.velocityX -= Math.cos(lAngle) * lRepelPower;
			pMobInstance.velocityY -= Math.sin(lAngle) * lRepelPower;

		}

	}

	private boolean overlaps(CellEntity pEntityA, CellEntity pEntityB) {
		final float lMaxDist = pEntityA.radius + pEntityB.radius;

		final float lMobAX = pEntityA.worldPositionX;
		final float lMobAY = pEntityA.worldPositionY;

		final float lMobBX = pEntityB.worldPositionX;
		final float lMobBY = pEntityB.worldPositionY;

		final float lDistSq = Vector2f.distance2(lMobAX, lMobAY, lMobBX, lMobBY);

		return lDistSq < lMaxDist * lMaxDist;
	}

}
