package net.ruse.ld48.controllers;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;
import org.lwjgl.glfw.GLFW;

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

	private SoundFxController mSoundFxController;
	private GameStateController mGameStateController;
	private PlayerController mPlayerController;
	private CameraFollowController mCameraFollowController;
	private LevelController mLevelController;
	private ItemController mItemController;

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
		mCameraFollowController = (CameraFollowController) pCore.controllerManager().getControllerByNameRequired(CameraFollowController.CONTROLLER_NAME, entityGroupID());
		mGameStateController = (GameStateController) pCore.controllerManager().getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
		mSoundFxController = (SoundFxController) pCore.controllerManager().getControllerByNameRequired(SoundFxController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mItemController = (ItemController) pCore.controllerManager().getControllerByNameRequired(ItemController.CONTROLLER_NAME, entityGroupID());

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
				final int lNumberOfCoinsOnDeath = lMobInstance.numberOfCoins;
				final float lWorldPositionX = lMobInstance.worldPositionX;
				final float lWorldPositionY = lMobInstance.worldPositionY;

				mItemController.addCoinSplash(lWorldPositionX, lWorldPositionY, lNumberOfCoinsOnDeath);

				lMobList.remove(lMobInstance);
				continue;

			}

			lMobInstance.update(pCore);

			if (lMobInstance.swingingFlag && lMobInstance.isInputCooldownElapsed()) {

				// calculate the point of the attack (in world space)
				final int lSignum = lMobInstance.isLeftFacing ? -1 : 1;

				boolean lAnyoneHit = false;
				for (int j = 0; j < lNumMobs; j++) {
					final var lOtherMobInstance = mMobInstancesToUpdate.get(j);

					lMobInstance.attackPointWorldX = lMobInstance.worldPositionX + 24.f * lSignum;
					lMobInstance.attackPointWorldY = lMobInstance.worldPositionY;

					if (lMobInstance == lOtherMobInstance)
						continue;

					final boolean lThisMobHit = updateMobAttackCollisions(pCore, lLevel, lMobInstance, lOtherMobInstance);
					lAnyoneHit |= lThisMobHit;

					if (lThisMobHit && lMobInstance.isPlayerControlled) {
						// lMobInstance.targetWorldCoord.set(-1, -1);

						lMobInstance.targetWorldCoord.x = lOtherMobInstance.worldPositionX - GameConstants.BLOCK_SIZE * .5f;
						lMobInstance.targetWorldCoord.y = lOtherMobInstance.worldPositionY - GameConstants.BLOCK_SIZE * .5f;
						lMobInstance.targetTypeIndex = MobInstance.MOB_TARGET_TYPE_MOB;

					}

				}

				if (!lAnyoneHit) {
					final boolean lDigDirectionSet = lMobInstance.swingingFlagDirectionKey != -1;

					if (!lDigDirectionSet) {
						final int lTileCoord = lLevel.getLevelTileCoord(lMobInstance.cellX + lSignum, lMobInstance.cellY);
						mLevelController.digLevel(lTileCoord, (byte) 1);

						lMobInstance.targetWorldCoord.x = (lTileCoord % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;
						lMobInstance.targetWorldCoord.y = (lTileCoord / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;

					} else {
						final boolean lDigDown = lMobInstance.swingingFlagDirectionKey == GLFW.GLFW_KEY_S;
						final boolean lDigUp = lMobInstance.swingingFlagDirectionKey == GLFW.GLFW_KEY_W;

						if (lDigDown) {
							final int lTileCoord = lLevel.getLevelTileCoord(lMobInstance.cellX, lMobInstance.cellY + 1);
							mLevelController.digLevel(lTileCoord, (byte) 1);

							lMobInstance.targetWorldCoord.x = (lTileCoord % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;
							lMobInstance.targetWorldCoord.y = (lTileCoord / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;

						} else if (lDigUp) {
							final int lDigLeft = lMobInstance.swingingFlagDirectionKey == GLFW.GLFW_KEY_A ? -1 : 0;
							final int lDigRight = lMobInstance.swingingFlagDirectionKey == GLFW.GLFW_KEY_D ? +1 : 0;
							final int lTileCoord = lLevel.getLevelTileCoord(lMobInstance.cellX + lDigLeft + lDigRight, lMobInstance.cellY - 1);
							mLevelController.digLevel(lTileCoord, (byte) 1);

							lMobInstance.targetWorldCoord.x = (lTileCoord % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;
							lMobInstance.targetWorldCoord.y = (lTileCoord / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;

						} else {
							final int lDigDownDirection = lMobInstance.swingingFlagDirectionKey == GLFW.GLFW_KEY_A ? -1 : 1;
							final int lTileCoord = lLevel.getLevelTileCoord(lMobInstance.cellX + lDigDownDirection, lMobInstance.cellY + 1);
							mLevelController.digLevel(lTileCoord, (byte) 1);

							lMobInstance.targetWorldCoord.x = (lTileCoord % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;
							lMobInstance.targetWorldCoord.y = (lTileCoord / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;

						}

					}

					lMobInstance.targetTypeIndex = MobInstance.MOB_TARGET_TYPE_BLOCK;

				}

				lMobInstance.inputCooldownTimer = lMobInstance.isPlayerControlled ? 300 : 450;

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
		pMobInstance.velocityY = MathHelper.clamp(pMobInstance.velocityY, -0.3f, 0.4f);
		pMobInstance.velocityX = MathHelper.clamp(pMobInstance.velocityX, -0.05f, 0.05f);

		// don't change face when in combat
		if (!pMobInstance.swingingFlag) {
			pMobInstance.isLeftFacing = pMobInstance.velocityX < 0f;

		}

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

		if (!pMobInstance.groundFlag && pMobInstance.cellY < pMobInstance.lastGroundHeight) {
			pMobInstance.lastGroundHeight = pMobInstance.cellY;

		}

		boolean lPrevGroundFlag = pMobInstance.groundFlag;
		pMobInstance.groundFlag = false;
		if (pMobInstance.fractionY > .7f && pLevel.hasCollision(pMobInstance.cellX, pMobInstance.cellY + 1)) {
			pMobInstance.fractionY = 0.7f;

			if (!lPrevGroundFlag) {
				for (int i = 0; i < 5; i++) {
					mDustBlockParticles.spawnParticle(pMobInstance.worldPositionX + RandomNumbers.random(-8.f, 8.f), pMobInstance.worldPositionY + 16.f, RandomNumbers.random(-100.f, 100.f),
							RandomNumbers.random(-20.f, -60.f));

				}

				final int lFallHeight = Math.abs(pMobInstance.cellY) - Math.abs(pMobInstance.lastGroundHeight);
				if (lFallHeight > GameConstants.MIN_HEIGHT_FALL_DAMAGE && GameConstants.MIN_HEIGHT_FALL_DAMAGE != 0) {

					if (pMobInstance.isPlayerControlled)
						playHurtSound();

					pMobInstance.dealDamage(lFallHeight / GameConstants.MIN_HEIGHT_FALL_DAMAGE, true);

				}

			}

			pMobInstance.groundFlag = true;
			pMobInstance.lastGroundHeight = pMobInstance.cellY;

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

		final var lLevel = mLevelController.level();

		final float lDistSq = getDistSq(lPlayerMobInstance, pMobInstance);
		final float lSeeDistSq = 128 * 128;

		if (lDistSq > lSeeDistSq) {
			pMobInstance.swingingFlag = false;
			return;
		}

		// walk towards player if visible on same layer
		if (Math.abs(lPlayerMobInstance.cellY) - Math.abs(pMobInstance.cellY) < 1) {
			if (pMobInstance.worldPositionX - 16.f < lPlayerMobInstance.worldPositionX) {
				pMobInstance.velocityX += 0.005f;

				if (pMobInstance.groundFlag) {
					final int lMobTileCoord = lLevel.getLevelTileCoord(pMobInstance.cellX, pMobInstance.cellY);
					if (lLevel.getLevelBlockType(lLevel.getRightBlockIndex(lMobTileCoord)) != 0) {
						pMobInstance.velocityY = pMobInstance.jumpVelocity;
						pMobInstance.groundFlag = false;

					}

				}

			}

			if (pMobInstance.worldPositionX + 16.f > lPlayerMobInstance.worldPositionX) {
				pMobInstance.velocityX -= 0.005f;

				if (pMobInstance.groundFlag) {
					final int lMobTileCoord = lLevel.getLevelTileCoord(pMobInstance.cellX, pMobInstance.cellY);
					if (lLevel.getLevelBlockType(lLevel.getLeftBlockIndex(lMobTileCoord)) != 0) {
						pMobInstance.velocityY = pMobInstance.jumpVelocity;
						pMobInstance.groundFlag = false;

					}

				}

			}

		}

		// start attack if close enough to player
		if (lDistSq < 32.f * 32.f)
			pMobInstance.swingingFlag = true;
		else
			pMobInstance.swingingFlag = false;

	}

	private boolean updateMobAttackCollisions(LintfordCore pCore, Level pLevel, MobInstance pAttackingMob, MobInstance pReceivingMob) {
		if (Math.abs(pAttackingMob.cellX - pReceivingMob.cellX) > pAttackingMob.minAttackCellClearanceX) {
			return false;

		}

		// The Y check is a little stricter to ensure no mobs attack up/down
		if (Math.abs(pAttackingMob.cellY - pReceivingMob.cellY) >= pAttackingMob.minAttackCellClearanceY) {
			return false;
		}

		final float lMaxDist = 12.f + pReceivingMob.radius;

		final float lMobAX = pAttackingMob.attackPointWorldX;
		final float lMobAY = pAttackingMob.attackPointWorldY;

		final float lMobBX = pReceivingMob.worldPositionX;
		final float lMobBY = pReceivingMob.worldPositionY;

		final float lDistSq = Vector2f.distance2(lMobAX, lMobAY, lMobBX, lMobBY);

		if (lDistSq <= lMaxDist * lMaxDist) {
			final int lNumBloodSplats = RandomNumbers.random(0, 5);
			for (int i = 0; i < lNumBloodSplats; i++) {
				mBloodBlockParticles.spawnParticle(lMobBX, lMobBY, RandomNumbers.random(-150.f, 150.f), RandomNumbers.random(-200.f, -50.f));

			}

			if (GameConstants.DEBUG_ENABLE_ATTACK_KNOCKBACK) {
				final float lAngle = (float) Math.atan2(pReceivingMob.worldPositionY - pAttackingMob.worldPositionY, pReceivingMob.worldPositionX - pAttackingMob.worldPositionX);
				final float lRepelPower = 0.3f;

				pReceivingMob.velocityX += Math.cos(lAngle) * lRepelPower;
				pReceivingMob.velocityY += Math.sin(lAngle) * lRepelPower * 0.05f;
			}

			playHurtSound();

			pReceivingMob.dealDamage(1, true);

			pAttackingMob.attackPointWorldX = pReceivingMob.worldPositionX;
			pAttackingMob.attackPointWorldY = pReceivingMob.worldPositionY;

			return true;

		}

		return false;

	}

	private void playHurtSound() {
		final int lHurtSoundIndex = RandomNumbers.random(0, 4);
		switch (lHurtSoundIndex) {
		default:
		case 0:
			mSoundFxController.playSound(SoundFxController.SOUND_HURT_1);
			break;
		case 1:
			mSoundFxController.playSound(SoundFxController.SOUND_HURT_2);
			break;
		case 2:
			mSoundFxController.playSound(SoundFxController.SOUND_HURT_3);
			break;
		case 3:
			mSoundFxController.playSound(SoundFxController.SOUND_HURT_4);
			break;
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

			if (GameConstants.DEBUG_ENABLE_ATTACK_KNOCKBACK && !pMobInstanceB.isPlayerControlled) {
				pMobInstanceB.velocityX += Math.cos(lAngle) * lRepelPower;
				pMobInstanceB.velocityY += Math.sin(lAngle) * lRepelPower * 0.025f;

			}

			if (GameConstants.DEBUG_ENABLE_ATTACK_KNOCKBACK && !pMobInstanceA.isPlayerControlled) {
				pMobInstanceA.velocityX -= Math.cos(lAngle) * lRepelPower;
				pMobInstanceA.velocityY -= Math.sin(lAngle) * lRepelPower;

			}

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

	public void dealDamageToMobsInRange(float pWorldX, float pWorldY, float pRadius, int pDamage, boolean pIncludePlayer) {

		final var lMobList = mobManager().instances();
		final int lMobCount = lMobList.size();
		for (int i = 0; i < lMobCount; i++) {
			final var lMobInstance = lMobList.get(i);

			final float lMobBX = lMobInstance.worldPositionX;
			final float lMobBY = lMobInstance.worldPositionY;

			final float lDistSq = Vector2f.distance2(pWorldX, pWorldY, lMobBX, lMobBY);

			if (lDistSq <= pRadius * pRadius) {
				mBloodBlockParticles.spawnParticle(lMobBX, lMobBY, RandomNumbers.random(-150.f, 150.f), RandomNumbers.random(-200.f, -50.f));
				lMobInstance.dealDamage(pDamage, true);

			}

		}

	}

	// --------------------------------------

	public void startNewGame(int pLevelNumber) {
		mobManager().mobInstances().clear();

		addPlayerMob();
		addEnemyMobs();

	}

	private void addPlayerMob() {
		final var lPlayerMob = mMobManager.getFreePooledItem();

		lPlayerMob.initialise(MobInstance.MOB_TYPE_DWARF, GameConstants.STARTING_PLAYER_HEALTH);
		lPlayerMob.isPlayerControlled = true;
		lPlayerMob.swingAttackEnabled = true;
		lPlayerMob.damagesOnCollide = false;
		lPlayerMob.minAttackCellClearanceX = 1;
		lPlayerMob.minAttackCellClearanceY = 1;
		lPlayerMob.setPosition(32.f, 0.f);
		lPlayerMob.swingRange = 32.f;
		lPlayerMob.lastGroundHeight = lPlayerMob.cellY;

		mMobManager.addMobInstance(lPlayerMob);

		mPlayerController.playerMobInstance(lPlayerMob);

		mCameraFollowController.setFollowEntity(lPlayerMob);
	}

	private void addEnemyMobs() {
		final var lLevel = mLevelController.level();
		final int lNumTiles = GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH;

		for (int i = 3 * GameConstants.LEVEL_TILES_WIDE; i < lNumTiles; i++) {
			final int lBlockType = lLevel.getLevelBlockType(i);

			if (lBlockType != Level.LEVEL_TILE_INDEX_AIR)
				continue;

			if (RandomNumbers.getRandomChance(mGameStateController.mobTileDiscardChance()))
				continue;

			MobInstance lEnemyMob = null;

			final int lMobType = RandomNumbers.random(0, 2);

			switch (lMobType) {
			case 0:
				lEnemyMob = getSpiderMob();
				break;
			default:
			case 1:
				lEnemyMob = getGoblinMob();

				final int lBlockBelowType = lLevel.getLevelBlockType(lLevel.getBottomBlockIndex(i));
				if (lBlockBelowType == Level.LEVEL_TILE_INDEX_AIR)
					continue;

				break;
			}

			final float lWorldPositionX = (i % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE + GameConstants.BLOCK_SIZE * .5f;
			final float lWorldPositionY = (i / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE + GameConstants.BLOCK_SIZE * .5f;

			lEnemyMob.setPosition(lWorldPositionX, lWorldPositionY);
			lEnemyMob.lastGroundHeight = lEnemyMob.cellY;

			mMobManager.addMobInstance(lEnemyMob);

		}

	}

	// TODO: ---> Refactor these out

	private MobInstance getSpiderMob() {
		final var lEnemyMob = mMobManager.getFreePooledItem();

		lEnemyMob.initialise(MobInstance.MOB_TYPE_SPIDER, 2);
		lEnemyMob.damagesOnCollide = true;
		lEnemyMob.swingAttackEnabled = false;
		lEnemyMob.swingRange = 32.f;
		lEnemyMob.jumpVelocity = -.27f;
		lEnemyMob.isPlayerControlled = false;
		lEnemyMob.minAttackCellClearanceX = 2;
		lEnemyMob.minAttackCellClearanceY = 1;
		lEnemyMob.dropsCoinsOnDeath = true;
		lEnemyMob.numberOfCoins = RandomNumbers.random(1, 2);

		return lEnemyMob;

	}

	private MobInstance getGoblinMob() {
		final var lEnemyMob = mMobManager.getFreePooledItem();

		lEnemyMob.initialise(MobInstance.MOB_TYPE_GOBLIN, 3);
		lEnemyMob.damagesOnCollide = false;
		lEnemyMob.swingAttackEnabled = true;
		lEnemyMob.swingRange = 48.f;
		lEnemyMob.jumpVelocity = -.21f;
		lEnemyMob.isPlayerControlled = false;
		lEnemyMob.minAttackCellClearanceX = 2;
		lEnemyMob.minAttackCellClearanceY = 1;
		lEnemyMob.dropsCoinsOnDeath = true;
		lEnemyMob.numberOfCoins = RandomNumbers.random(2, 4);

		return lEnemyMob;
	}

}
