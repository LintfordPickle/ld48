package net.ruse.ld48.controllers;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
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

	private MobController mMobController;
	private LevelController mLevelController;
	private ItemManager mItemManager;

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

			updateItemPhysics(pCore, lLevel, lItemInstance);

			switch (lItemInstance.itemTypeIndex) {
			case ItemManager.ITEM_TYPE_INDEX_TNT:
				updateTntItem(pCore, lLevel, lItemInstance);
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
		pItemInstance.rotationInRadians += pCore.gameTime().elapsedTimeMilli() * 0.01f * lSignum;

		// BANG
		if (pItemInstance.timeAlive > 2000) {
			pItemInstance.isPickedUp = true;

			final int lTileX = pItemInstance.cellX;
			final int lTileY = pItemInstance.cellY;
			final int lTileCoord = pLevel.getLevelTileCoord(lTileX, lTileY);
			pLevel.digBlock(lTileCoord, (byte) 50);
			pLevel.digBlock(pLevel.getLeftBlockIndex(lTileCoord), (byte) 50);
			pLevel.digBlock(pLevel.getRightBlockIndex(lTileCoord), (byte) 50);
			pLevel.digBlock(pLevel.getTopBlockIndex(lTileCoord), (byte) 50);
			pLevel.digBlock(pLevel.getBottomBlockIndex(lTileCoord), (byte) 50);

			final float lWorldPositionX = pItemInstance.worldPositionX;
			final float lWorldPositionY = pItemInstance.worldPositionY;
			final float lBlastRadius = 64.f;

			mMobController.dealDamageToMobsInRange(lWorldPositionX, lWorldPositionY, lBlastRadius, 2, true);

		}

	}

	// --------------------------------------

	public void addTnt(float pWorldX, float pWorldY, float pVelX, float pVelY) {
		final var lFreeItemInstance = itemManager().getFreePooledItem();

		lFreeItemInstance.setupItem(ItemManager.ITEM_TYPE_INDEX_TNT);
		lFreeItemInstance.setPosition(pWorldX, pWorldY);
		lFreeItemInstance.velocityX = pVelX;
		lFreeItemInstance.velocityY = pVelY;
		lFreeItemInstance.radius = 8.f;

		itemManager().addItemInstance(lFreeItemInstance);

	}

	public void addCoins(float pWorldX, float pWorldY, float pVelX, float pVelY) {

	}

	public void addHealth(float pWorldX, float pWorldY) {

	}

}
