package net.ruse.ld48.controllers;

import java.util.ArrayList;
import java.util.List;

import org.joml.Math;

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

	// --------------------------------------
	// Variables
	// --------------------------------------

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

		mSmokeParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_SMOKE");
		mTntParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_TNT");
	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

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
				pItemInstance.velocityX = -pItemInstance.velocityX * .5f;

		}

		if (pItemInstance.fractionX > 1.f - lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX + 1, pItemInstance.cellY)) {
			pItemInstance.fractionX = 1.f - lRadiusInItemSpace;

			if (pItemInstance.velocityX > 0)
				pItemInstance.velocityX = -pItemInstance.velocityX * .5f;

		}

		if (pItemInstance.fractionY < lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX, pItemInstance.cellY - 1)) {
			pItemInstance.fractionY = lRadiusInItemSpace;

			if (pItemInstance.velocityY < 0)
				pItemInstance.velocityY *= 0.7f;

		}

		if (pItemInstance.fractionY > 1.f - lRadiusInItemSpace && pLevel.hasCollision(pItemInstance.cellX, pItemInstance.cellY + 1)) {
			pItemInstance.fractionY = 1.f - lRadiusInItemSpace;

			if (pItemInstance.velocityY > 0)
				pItemInstance.velocityY = 0;

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

			tntDigTile(pLevel.getLeftBlockIndex(lTileCoord));
			tntDigTile(pLevel.getRightBlockIndex(lTileCoord));
			tntDigTile(pLevel.getTopBlockIndex(lTileCoord));
			tntDigTile(pLevel.getBottomBlockIndex(lTileCoord));

			final float lWorldPositionX = pItemInstance.worldPositionX;
			final float lWorldPositionY = pItemInstance.worldPositionY;
			final float lBlastRadius = 64.f;

			mMobController.dealDamageToMobsInRange(lWorldPositionX, lWorldPositionY, lBlastRadius, 2, true);

		}

	}

	private void tntDigTile(int pTileCoord) {
		final boolean lBlockRemoved = mLevelController.digLevel(pTileCoord, (byte) 7);

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

		lFreeItemInstance.setupItem(ItemManager.ITEM_TYPE_INDEX_TNT);
		lFreeItemInstance.setPosition(pWorldX, pWorldY);
		lFreeItemInstance.isPickUpAble = false;
		lFreeItemInstance.physicsEnabled = true;
		lFreeItemInstance.velocityX = pVelX;
		lFreeItemInstance.velocityY = pVelY;
		lFreeItemInstance.radius = 16.f;
		lFreeItemInstance.interactsWithMobs = false;

		itemManager().addItemInstance(lFreeItemInstance);
		mGameStateController.resetTntCooldown();

	}

	public void addCoins(float pWorldX, float pWorldY, float pVelX, float pVelY) {
		// lFreeItemInstance.interactsWithMobs = true;
	}

	public void addHealth(float pWorldX, float pWorldY) {

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
