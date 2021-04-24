package net.ruse.ld48.controllers;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.particles.particlesystems.ParticleSystemInstance;
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
	private final List<MobInstance> mMobInstancesToUpdate = new ArrayList<>();

	private PlayerController mPlayerController;
	private LevelController mLevelController;

	private ParticleFrameworkController mParticleFrameworkController;
	private ParticleSystemInstance mBloodBlockParticles;
	private ParticleSystemInstance mDustBlockParticles;

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
		mParticleFrameworkController = (ParticleFrameworkController) pCore.controllerManager().getControllerByNameRequired(ParticleFrameworkController.CONTROLLER_NAME, entityGroupID());

		mBloodBlockParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_BLOOD");
		mDustBlockParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_DUST");

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

		mMobInstancesToUpdate.clear();
		for (int i = 0; i < lNumMobs; i++) {
			final var lMobInstance = lMobList.get(i);
			mMobInstancesToUpdate.add(lMobInstance);

		}

		for (int i = 0; i < lNumMobs; i++) {
			final var lMobInstance = mMobInstancesToUpdate.get(i);

			if (lMobInstance.health <= 0) {
				lMobList.remove(lMobInstance);
				continue;

			}

			lMobInstance.update(pCore);

			if (lMobInstance.diggingFlag && lMobInstance.isInputCooldownElapsed()) {
				final int lSignum = lMobInstance.isLeftFacing ? -1 : 1;

				final boolean lSideWaysDigging = GameConstants.GAME_SIDEWAYS_DIGGING;

				mLevelController.digLevel(lMobInstance.cellX + (lSideWaysDigging ? lSignum : 0), lMobInstance.cellY + 1, (byte) 1);
				lMobInstance.inputCooldownTimer = 300;

			}

			if (lMobInstance.swingingFlag && lMobInstance.isInputCooldownElapsed()) {

				// calculate the point of the attack (in world space)
				final float lSignum = lMobInstance.isLeftFacing ? -1 : 1;
				lMobInstance.attackPointWorldX = lMobInstance.worldPositionX + 32.f * lSignum;
				lMobInstance.attackPointWorldY = lMobInstance.worldPositionY;

				for (int j = 0; j < lNumMobs; j++) {
					final var lOtherMobInstance = mMobInstancesToUpdate.get(j);

					if (lMobInstance == lOtherMobInstance || !lOtherMobInstance.swingAttackEnabled)
						continue;

					updateMobAttackCollisions(pCore, lLevel, lMobInstance, lOtherMobInstance);

				}

				lMobInstance.inputCooldownTimer = 300;
			}

			if (!lMobInstance.isPlayerControlled) {
				updateEnemyAi(pCore, lLevel, lMobInstance);

			}

			for (int j = i + 0; j < lNumMobs; j++) {
				final var lOtherMobInstance = mMobInstancesToUpdate.get(j);

				if (lMobInstance == lOtherMobInstance)
					continue;

				updateEnemyPlayerCollision(pCore, lLevel, lMobInstance, lOtherMobInstance);

			}

			updateMobPhysics(pCore, lLevel, lMobInstance);

		}

	}

	private void updateMobPhysics(LintfordCore pCore, Level pLevel, MobInstance pMobInstance) {

		// gravity
		pMobInstance.velocityY += 0.0096f;
		pMobInstance.velocityY = MathHelper.clamp(pMobInstance.velocityY, -0.2f, 0.4f);
		pMobInstance.velocityX = MathHelper.clamp(pMobInstance.velocityX, -0.05f, 0.05f);

		pMobInstance.isLeftFacing = pMobInstance.velocityX < 0f;

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
				pMobInstance.velocityY *= 0.7f;

		}

		pMobInstance.groundFlag = false;
		if (pMobInstance.fractionY > .7f && pLevel.hasCollision(pMobInstance.cellX, pMobInstance.cellY + 1)) {
			pMobInstance.fractionY = 0.7f;
			pMobInstance.groundFlag = true;

			if (Math.abs(pMobInstance.velocityX) > 0.01f && RandomNumbers.getRandomChance(33.f)) {
				mDustBlockParticles.spawnParticle(pMobInstance.worldPositionX + RandomNumbers.random(-8.f, 8.f), pMobInstance.worldPositionY + 16.f, pMobInstance.velocityX * RandomNumbers.random(0.f, 150.f),
						RandomNumbers.random(0.f, 0.f));

			}

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

		final float lDistSq = getDistSq(lPlayerMobInstance, pMobInstance);
		final float lSeeDistSq = 128 * 128;

		if (lDistSq > lSeeDistSq) {
			pMobInstance.swingingFlag = false;
			return;
		}

		if (pMobInstance.worldPositionX - 16.f < lPlayerMobInstance.worldPositionX) {
			pMobInstance.velocityX += 0.005f;
		}

		if (pMobInstance.worldPositionX + 16.f > lPlayerMobInstance.worldPositionX) {
			pMobInstance.velocityX -= 0.005f;
		}

		if (lDistSq < 32.f * 32.f)
			pMobInstance.swingingFlag = true;
		else
			pMobInstance.swingingFlag = false;

	}

	private void updateMobAttackCollisions(LintfordCore pCore, Level pLevel, MobInstance pAttackingMob, MobInstance pReceivingMob) {
		final int lMinCellClearance = 3;
		if (Math.abs(pAttackingMob.cellX - pReceivingMob.cellX) >= lMinCellClearance || Math.abs(pAttackingMob.cellY - pReceivingMob.cellY) >= lMinCellClearance) {
			return;

		}

		final float lMaxDist = 12.f + pReceivingMob.radius;

		final float lMobAX = pAttackingMob.attackPointWorldX;
		final float lMobAY = pAttackingMob.attackPointWorldY;

		final float lMobBX = pReceivingMob.worldPositionX;
		final float lMobBY = pReceivingMob.worldPositionY;

		final float lDistSq = Vector2f.distance2(lMobAX, lMobAY, lMobBX, lMobBY);

		if (lDistSq <= lMaxDist * lMaxDist) {
			mBloodBlockParticles.spawnParticle(lMobBX, lMobBY, RandomNumbers.random(-150.f, 150.f), RandomNumbers.random(-200.f, -50.f));
			pReceivingMob.dealDamage(1, true);

		}

	}

	private void updateEnemyPlayerCollision(LintfordCore pCore, Level pLevel, MobInstance pMobInstanceA, MobInstance pMobInstanceB) {
		if (Math.abs(pMobInstanceB.cellX - pMobInstanceA.cellX) >= 2 || Math.abs(pMobInstanceB.cellY - pMobInstanceA.cellY) >= 2) {
			return;

		}

		final float lMaxDist = pMobInstanceA.radius + pMobInstanceB.radius;

		// then check the circus colls
		if (getDistSq(pMobInstanceA, pMobInstanceB) < lMaxDist * lMaxDist) {
			final float lAngle = (float) Math.atan2(pMobInstanceB.worldPositionY - pMobInstanceA.worldPositionY, pMobInstanceB.worldPositionX - pMobInstanceA.worldPositionX);
			final float lRepelPower = 0.03f;

			pMobInstanceB.velocityX += Math.cos(lAngle) * lRepelPower;
			pMobInstanceB.velocityY += Math.sin(lAngle) * lRepelPower * 0.025f;

			pMobInstanceA.velocityX -= Math.cos(lAngle) * lRepelPower;
			pMobInstanceA.velocityY -= Math.sin(lAngle) * lRepelPower;

			// assumes the player is always the first mob index (probably correct)
			if (pMobInstanceA.isPlayerControlled) {
				if (pMobInstanceB.damagesOnCollide)
					pMobInstanceA.dealDamage(1, true);

				if (pMobInstanceA.damagesOnCollide)
					pMobInstanceB.dealDamage(1, true);

			}

		}

	}

	private float getDistSq(CellEntity pEntityA, CellEntity pEntityB) {
		final float lMobAX = pEntityA.worldPositionX;
		final float lMobAY = pEntityA.worldPositionY;

		final float lMobBX = pEntityB.worldPositionX;
		final float lMobBY = pEntityB.worldPositionY;

		final float lDistSq = Vector2f.distance2(lMobAX, lMobAY, lMobBX, lMobBY);

		return lDistSq;
	}

}
