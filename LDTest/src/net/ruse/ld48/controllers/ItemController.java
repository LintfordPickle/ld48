package net.ruse.ld48.controllers;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;
import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.particles.particlesystems.ParticleSystemInstance;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.data.ItemInstance;
import net.ruse.ld48.data.ItemManager;
import net.ruse.ld48.data.Level;

public class ItemController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Item Controller";

	public static final byte TNT_BLOCK_DAMAGE = 6;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private SoundFxController mSoundFxController;
	private GameStateController mGameStateController;
	private ScreenShakeController mScreenShakeController;
	private MobController mMobController;
	private PlayerController mPlayerController;
	private LevelController mLevelController;
	private ItemManager mItemManager;

	private ParticleFrameworkController mParticleFrameworkController;
	private ParticleSystemInstance mSmokeParticles;
	private ParticleSystemInstance mTntParticles;

	private final List<ItemInstance> itemsToUpdate = new ArrayList<>();

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public boolean isInitialized() {
		return mLevelController != null;
	}

	public ItemManager itemManager() {
		return mItemManager;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ItemController(ControllerManager pControllerManager, ItemManager pItemManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mItemManager = pItemManager;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, entityGroupID());
		mMobController = (MobController) pCore.controllerManager().getControllerByNameRequired(MobController.CONTROLLER_NAME, entityGroupID());
		mScreenShakeController = (ScreenShakeController) pCore.controllerManager().getControllerByNameRequired(ScreenShakeController.CONTROLLER_NAME, entityGroupID());
		mPlayerController = (PlayerController) pCore.controllerManager().getControllerByNameRequired(PlayerController.CONTROLLER_NAME, entityGroupID());
		mParticleFrameworkController = (ParticleFrameworkController) pCore.controllerManager().getControllerByNameRequired(ParticleFrameworkController.CONTROLLER_NAME, entityGroupID());
		mGameStateController = (GameStateController) pCore.controllerManager().getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
		mSoundFxController = (SoundFxController) pCore.controllerManager().getControllerByNameRequired(SoundFxController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);

		mSmokeParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_SMOKE");
		mTntParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_TNT");
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_I)) {
			final float lWorldPositionX = pCore.gameCamera().getMouseWorldSpaceX();
			final float lWorldPositionY = pCore.gameCamera().getMouseWorldSpaceY();

			addCoins(lWorldPositionX, lWorldPositionY, RandomNumbers.random(-.1f, .1f), RandomNumbers.random(-0.3f, 0.f));

		}

		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lLevel = mLevelController.level();
		final var lItemManager = itemManager();

		final var lItemList = lItemManager.instances();
		if (lItemList == null || lItemList.size() == 0)
			return;

		itemsToUpdate.clear();
		final int lItemCount = lItemList.size();
		for (int i = 0; i < lItemCount; i++) {
			itemsToUpdate.add(lItemList.get(i));

		}

		for (int i = 0; i < lItemCount; i++) {
			final var lItemInstance = itemsToUpdate.get(i);

			if (lItemInstance.isPickedUp) {
				itemManager().removeItemInstance(lItemInstance);
				continue;
			}

			lItemInstance.timeAlive += pCore.gameTime().elapsedTimeMilli();
			lItemInstance.flashTimer += pCore.gameTime().elapsedTimeMilli();

			if (lItemInstance.physicsEnabled)
				updateItemPhysics(pCore, lLevel, lItemInstance);

			switch (lItemInstance.itemTypeIndex) {
			case ItemManager.ITEM_TYPE_INDEX_TNT:
				updateTntItem(pCore, lLevel, lItemInstance);
				break;

			case ItemManager.ITEM_TYPE_INDEX_COIN:
				updateCoinItem(pCore, lLevel, lItemInstance);
				break;

			}

			// detect player/item collisions
			handleItemInteraction(pCore, lItemInstance);

		}

	}

	// --------------------------------------

	private void handleItemInteraction(LintfordCore pCore, ItemInstance pItemInstance) {
		if (!pItemInstance.interactsWithMobs)
			return;

		final var lPlayerMobInstance = mPlayerController.playerMobInstance();

		// fast
		if (Math.abs(lPlayerMobInstance.cellX - pItemInstance.cellX) >= 2 || Math.abs(lPlayerMobInstance.cellY - pItemInstance.cellY) >= 2) {
			return;

		}

		final float lMaxDist = lPlayerMobInstance.radius + pItemInstance.radius;

		final float lMobAX = lPlayerMobInstance.worldPositionX;
		final float lMobAY = lPlayerMobInstance.worldPositionY;

		final float lMobBX = pItemInstance.worldPositionX;
		final float lMobBY = pItemInstance.worldPositionY;

		final float lDistSq = Vector2f.distance2(lMobAX, lMobAY, lMobBX, lMobBY);

		if (lDistSq < lMaxDist * lMaxDist) {
			switch (pItemInstance.itemTypeIndex) {
			case ItemManager.ITEM_TYPE_INDEX_LEVEL_EXIT:
				mGameStateController.exitReached();
				break;

			case ItemManager.ITEM_TYPE_INDEX_COIN:
				mGameStateController.addGold(1);
				pItemInstance.isPickedUp = true;
				break;

			case ItemManager.ITEM_TYPE_INDEX_HEALTH:
				mPlayerController.playerMobInstance().tryAddHealth();
				pItemInstance.isPickedUp = true;
				break;
			}

		}

	}

	private void updateItemPhysics(LintfordCore pCore, Level pLevel, ItemInstance pItemInstance) {
		final float lRadiusInItemSpace = 1.f / pItemInstance.radius;

		// gravity
		pItemInstance.velocityY += 0.0096f;

		pItemInstance.fractionX += pItemInstance.velocityX;
		pItemInstance.fractionY += pItemInstance.velocityY;

		// grid based collision check
		if (pItemInstance.fractionX < lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX - 1, pItemInstance.cellY)) {
			pItemInstance.fractionX = lRadiusInItemSpace;

			if (pItemInstance.velocityX < 0)
				pItemInstance.velocityX = -pItemInstance.velocityX * pItemInstance.velocityDecayOnHitX;

		}

		if (pItemInstance.fractionX > 1.f - lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX + 1, pItemInstance.cellY)) {
			pItemInstance.fractionX = 1.f - lRadiusInItemSpace;

			if (pItemInstance.velocityX > 0)
				pItemInstance.velocityX = -pItemInstance.velocityX * pItemInstance.velocityDecayOnHitX;

		}

		if (pItemInstance.fractionY < lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX, pItemInstance.cellY - 1)) {
			pItemInstance.fractionY = lRadiusInItemSpace;

			if (pItemInstance.velocityY < 0)
				pItemInstance.velocityY *= 0.7f;

		}

		if (pItemInstance.fractionY > 1.f - lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX, pItemInstance.cellY + 1)) {
			pItemInstance.fractionY = 1.f - lRadiusInItemSpace;

			if (pItemInstance.velocityY > 0)
				pItemInstance.velocityY = -pItemInstance.velocityY * pItemInstance.velocityDecayOnHitY;

		}

		while (pItemInstance.fractionX < 0) {
			pItemInstance.fractionX++;
			pItemInstance.cellX--;
		}

		while (pItemInstance.fractionX > 1) {
			pItemInstance.fractionX--;
			pItemInstance.cellX++;
		}

		while (pItemInstance.fractionY < 0) {
			pItemInstance.fractionY++;
			pItemInstance.cellY--;
		}

		while (pItemInstance.fractionY > 1) {
			pItemInstance.fractionY--;
			pItemInstance.cellY++;
		}

		// update mob instance

		pItemInstance.worldPositionX = (float) (pItemInstance.cellX + pItemInstance.fractionX) * GameConstants.BLOCK_SIZE;
		pItemInstance.worldPositionY = (float) (pItemInstance.cellY + pItemInstance.fractionY) * GameConstants.BLOCK_SIZE;

		pItemInstance.velocityX *= .97f;
		pItemInstance.velocityY *= .94f;

	}

	private void updateTntItem(LintfordCore pCore, Level pLevel, ItemInstance pItemInstance) {
		float flashSpeed = 700;
		pItemInstance.isFlashOn = false;
		if (!(pItemInstance.flashTimer % flashSpeed < flashSpeed * .5f)) {
			pItemInstance.isFlashOn = true;
		}

		final float lSignum = pItemInstance.velocityX > 0 ? 1.f : -1.f;
		pItemInstance.rotationInRadians += pItemInstance.velocityX * pCore.gameTime().elapsedTimeMilli() * lSignum * .5f;

		// BANG
		if (pItemInstance.timeAlive > 2000) {
			pItemInstance.isPickedUp = true;

			mScreenShakeController.shakeCamera(400.f, 3.f);

			final int lSoundVariationIndex = RandomNumbers.random(0, 4);
			switch (lSoundVariationIndex) {
			default:
			case 1:
				mSoundFxController.playSound(SoundFxController.SOUND_TnT1);
				break;

			case 2:
				mSoundFxController.playSound(SoundFxController.SOUND_TnT2);
				break;

			case 3:
				mSoundFxController.playSound(SoundFxController.SOUND_TnT3);
				break;
			}

			final int lTileX = pItemInstance.cellX;
			final int lTileY = pItemInstance.cellY;
			final int lTileCoord = pLevel.getLevelTileCoord(lTileX, lTileY);
			pLevel.digBlock(lTileCoord, (byte) 50);

			final int lNumFlashes = RandomNumbers.random(1, 3);
			for (int i = 0; i < lNumFlashes; i++) {
				mTntParticles.spawnParticle(pItemInstance.worldPositionX + RandomNumbers.random(-8.f, 8.f), pItemInstance.worldPositionY + RandomNumbers.random(-8.f, 8.f), 0, 0);

			}

			for (int i = 0; i < 2; i++) {
				mSmokeParticles.spawnParticle(pItemInstance.worldPositionX, pItemInstance.worldPositionY, 0, 0);

			}

			final int lLeftTileCoord = pLevel.getLeftBlockIndex(lTileCoord);
			tntDigTile(lLeftTileCoord);
			tntDigTile(pLevel.getTopBlockIndex(lLeftTileCoord));
			tntDigTile(pLevel.getBottomBlockIndex(lLeftTileCoord));

			final int lRightTileCoord = pLevel.getRightBlockIndex(lTileCoord);
			tntDigTile(lRightTileCoord);
			tntDigTile(pLevel.getTopBlockIndex(lRightTileCoord));
			tntDigTile(pLevel.getBottomBlockIndex(lRightTileCoord));

			tntDigTile(pLevel.getTopBlockIndex(lTileCoord));
			tntDigTile(pLevel.getBottomBlockIndex(lTileCoord));

			// extended

			final float lWorldPositionX = pItemInstance.worldPositionX;
			final float lWorldPositionY = pItemInstance.worldPositionY;
			final float lBlastRadius = 96.f;

			mMobController.dealDamageToMobsInRange(lWorldPositionX, lWorldPositionY, lBlastRadius, 2, true);

		}

	}

	private void updateCoinItem(LintfordCore pCore, Level pLevel, ItemInstance pItemInstance) {
		pItemInstance.spriteFrameTimer -= pCore.gameTime().elapsedTimeMilli();

		if (pItemInstance.spriteFrameTimer <= 0) {
			pItemInstance.spriteFrameTimer = 100; // animation speed
			pItemInstance.spriteFrame++;

			if (pItemInstance.spriteFrame > 4) { // max frames
				pItemInstance.spriteFrame = 0;

			}

		}

	}

	private void tntDigTile(int pTileCoord) {
		final boolean lBlockRemoved = mLevelController.digLevel(pTileCoord, TNT_BLOCK_DAMAGE);

		if (!lBlockRemoved)
			return;

		final int lWorldPositionX = (pTileCoord % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;
		final int lWorldPositionY = (pTileCoord / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;

		for (int i = 0; i < 3; i++) {
			mSmokeParticles.spawnParticle(lWorldPositionX, lWorldPositionY, 0, 0);

		}

	}

	// --------------------------------------

	public void addTnt(float pWorldX, float pWorldY, float pVelX, float pVelY) {
		if (!mGameStateController.canThrowTnt())
			return;

		final var lFreeItemInstance = itemManager().getFreePooledItem();

		mSoundFxController.playSound(SoundFxController.SOUND_TnTFuse);

		lFreeItemInstance.setupItem(ItemManager.ITEM_TYPE_INDEX_TNT);
		lFreeItemInstance.setPosition(pWorldX, pWorldY);
		lFreeItemInstance.isPickUpAble = false;
		lFreeItemInstance.physicsEnabled = true;
		lFreeItemInstance.velocityX = pVelX;
		lFreeItemInstance.velocityY = pVelY;
		lFreeItemInstance.radius = 16.f;
		lFreeItemInstance.interactsWithMobs = false;
		lFreeItemInstance.spriteFrame = 0;

		itemManager().addItemInstance(lFreeItemInstance);
		mGameStateController.resetTntCooldown();

	}

	public void addCoinSplash(float pWorldX, float pWorldY, int pAmt) {
		for (int j = 0; j < pAmt; j++) {
			addCoins(pWorldX, pWorldY, RandomNumbers.random(-.1f, .1f), RandomNumbers.random(-0.3f, 0.f));

		}

	}

	public void addCoins(float pWorldX, float pWorldY, float pVelX, float pVelY) {
		if (!mGameStateController.canThrowTnt())
			return;

		final var lFreeItemInstance = itemManager().getFreePooledItem();

		// TODO: Play a decent sound fx for heart drop
		// mSoundFxController.playSound(SoundFxController.SOUND_TnTFuse);

		lFreeItemInstance.setupItem(ItemManager.ITEM_TYPE_INDEX_COIN);
		lFreeItemInstance.setPosition(pWorldX, pWorldY);
		lFreeItemInstance.velocityX = pVelX;
		lFreeItemInstance.velocityY = pVelY;
		lFreeItemInstance.velocityDecayOnHitX = 0.9f;
		lFreeItemInstance.velocityDecayOnHitY = 0.95f;
		lFreeItemInstance.isPickUpAble = true;
		lFreeItemInstance.physicsEnabled = true;
		lFreeItemInstance.radius = 8.f;
		lFreeItemInstance.interactsWithMobs = true;
		lFreeItemInstance.isFlashOn = true;
		lFreeItemInstance.value = 1;
		lFreeItemInstance.spriteFrame = 0;

		itemManager().addItemInstance(lFreeItemInstance);

	}

	public void addHealth(float pWorldX, float pWorldY) {
		if (!mGameStateController.canThrowTnt())
			return;

		final var lFreeItemInstance = itemManager().getFreePooledItem();

		// TODO: Play a decent sound fx for heart drop
		// mSoundFxController.playSound(SoundFxController.SOUND_TnTFuse);

		lFreeItemInstance.setupItem(ItemManager.ITEM_TYPE_INDEX_HEALTH);
		lFreeItemInstance.setPosition(pWorldX, pWorldY);
		lFreeItemInstance.isPickUpAble = true;
		lFreeItemInstance.physicsEnabled = true;
		lFreeItemInstance.velocityX = 0;
		lFreeItemInstance.velocityY = 0;
		lFreeItemInstance.velocityDecayOnHitX = 0.4f;
		lFreeItemInstance.velocityDecayOnHitY = 0.8f;
		lFreeItemInstance.radius = 16.f;
		lFreeItemInstance.interactsWithMobs = true;
		lFreeItemInstance.isFlashOn = false;
		lFreeItemInstance.value = 1;
		lFreeItemInstance.spriteFrame = 0;

		itemManager().addItemInstance(lFreeItemInstance);

	}

	public void startNewGame(int pLevelNumber) {
		itemManager().instances().clear();

		// level exit
		{
			final var lItemInstance = itemManager().getFreePooledItem();
			lItemInstance.isPickUpAble = false;
			lItemInstance.physicsEnabled = false;
			lItemInstance.rotationInRadians = 0.f;
			lItemInstance.radius = 16.f;
			lItemInstance.isFlashOn = false;
			lItemInstance.interactsWithMobs = true;
			lItemInstance.setupItem(ItemManager.ITEM_TYPE_INDEX_LEVEL_EXIT);

			final int lExitTileWorldX = RandomNumbers.random(1, GameConstants.LEVEL_TILES_WIDE - 2) * GameConstants.BLOCK_SIZE + (int) (GameConstants.BLOCK_SIZE * 0.5f);
			final int lExitTileWorldY = (GameConstants.LEVEL_TILES_HIGH - 2) * GameConstants.BLOCK_SIZE + (int) (GameConstants.BLOCK_SIZE * 0.5f);
			lItemInstance.setPosition(lExitTileWorldX, lExitTileWorldY);

			itemManager().addItemInstance(lItemInstance);

		}

	}

}
