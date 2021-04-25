package net.ruse.ld48.renderers;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;
import net.ruse.ld48.controllers.ItemController;
import net.ruse.ld48.data.ItemManager;

public class ItemRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "Item Renderer";

	private static final float FULL_FLASH_DUR = 150;

	private static final Rectangle TNT_SRC_RECT = new Rectangle(0, 0, 32, 32);
	private static final Rectangle TNT_PICKUP_SRC_RECT = new Rectangle(32, 0, 32, 32);
	private static final Rectangle EXIT_SRC_RECT = new Rectangle(64, 0, 32, 32);
	private static final Rectangle GOLD_SRC_RECT = new Rectangle(96, 0, 32, 32);

	// --------------------------------------
	// Variables
	// --------------------------------------

	private Texture mItemTexture;

	private ItemController mItemController;

	@Override
	public boolean isInitialized() {
		return mItemController != null;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ItemRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mItemController = (ItemController) pCore.controllerManager().getControllerByNameRequired(ItemController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mItemTexture = pResourceManager.textureManager().loadTexture("TEXTURE_ITEMS", "res//textures//textureItems.png", entityGroupID());

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lItemManager = mItemController.itemManager();

		final var lItemList = lItemManager.instances();
		if (lItemList == null || lItemList.size() == 0)
			return;

		final var lTextureBatch = rendererManager().uiTextureBatch();

		lTextureBatch.begin(pCore.gameCamera());

		final int lItemCount = lItemList.size();
		for (int i = 0; i < lItemCount; i++) {
			final var lItemInstance = lItemList.get(i);

			final var worldPosX = lItemInstance.worldPositionX;
			final var worldPosY = lItemInstance.worldPositionY;
			final float lItemRadius = lItemInstance.radius;

			var lTintColor = ColorConstants.WHITE;
			if (lItemInstance.isFlashOn) {
				lTintColor = ColorConstants.getColor(100, 100, 100, 1);
			}

			Rectangle lSrcRect = null;
			switch (lItemInstance.itemTypeIndex) {
			case ItemManager.ITEM_TYPE_INDEX_TNT:
				lSrcRect = TNT_SRC_RECT;
				break;

			case ItemManager.ITEM_TYPE_INDEX_TNT_PICKUP:
				lSrcRect = TNT_PICKUP_SRC_RECT;
				break;

			case ItemManager.ITEM_TYPE_INDEX_LEVEL_EXIT:
				lSrcRect = EXIT_SRC_RECT;
				break;

			}

			final float lRot = lItemInstance.rotationInRadians;
			lTextureBatch.drawAroundCenter(mItemTexture, lSrcRect, worldPosX, worldPosY, lItemRadius * 2, lItemRadius * 2.f, -0.1f, lRot, 0.0f, 0.0f, 1.f, lTintColor);

		}

		lTextureBatch.end();

	}

}
