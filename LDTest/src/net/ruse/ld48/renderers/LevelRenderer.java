package net.ruse.ld48.renderers;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.controllers.LevelController;
import net.ruse.ld48.data.Level;

public class LevelRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "Level Renderer";

	private static final Rectangle NO_SRC_RECT = null;
	private static final Rectangle DIRT_SRC_RECT = new Rectangle(0, 0, 32, 32);
	private static final Rectangle DIRT_DAMAGED_SRC_RECT = new Rectangle(160, 0, 32, 32);

	private static final Rectangle DIRT_TOP_SRC_RECT = new Rectangle(64, 0, 32, 32);
	private static final Rectangle DIRT_TOP_DAMAGED_SRC_RECT = new Rectangle(192, 0, 32, 32);
	private static final Rectangle AIR_DEBUG_SRC_RECT = new Rectangle(32, 0, 32, 32);

	private static final Rectangle GOLD_SRC_RECT = new Rectangle(224, 0, 32, 32);
	private static final Rectangle GOLD_TOP_SRC_RECT = new Rectangle(64, 32, 32, 32);

	private static final Rectangle BACKGROUND_FILL_SRC_RECT = new Rectangle(0, 32, 32, 32);
	private static final Rectangle BACKGROUND_TOP_FILL_SRC_RECT = new Rectangle(32, 32, 32, 32);

	// --------------------------------------
	// Variables
	// --------------------------------------

	private LevelController mLevelController;
	private Texture mLevelTexture;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public LevelRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public boolean isInitialized() {
		return mLevelController != null;
	}

	@Override
	public void initialize(LintfordCore pCore) {
		mLevelController = (LevelController) pCore.controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mLevelTexture = pResourceManager.textureManager().loadTexture("TEXTURE_LEVEL", "res/textures/textureLevel.png", entityGroupID());

	}

	@Override
	public void draw(LintfordCore pCore) {

		drawBackground(pCore);
		drawForeground(pCore);

	}

	private void drawBackground(LintfordCore pCore) {
		final var lLevel = mLevelController.level();

		if (lLevel == null)
			return;

		final var lTextureBatch = rendererManager().uiTextureBatch();

		lTextureBatch.begin(pCore.gameCamera());

		final float lBlockSize = GameConstants.BLOCK_SIZE;
		final int floorHeight = 3;

		for (int y = floorHeight; y < GameConstants.LEVEL_TILES_HIGH; y++) {

			final float lModAmt = 1.f - (float) ((float) y / (float) GameConstants.LEVEL_TILES_HIGH);
			final var lColorConstant = ColorConstants.getColor(lModAmt, lModAmt, lModAmt, 1.f);

			for (int x = 0; x < GameConstants.LEVEL_TILES_WIDE; x++) {

				if (y == floorHeight) {
					lTextureBatch.draw(mLevelTexture, BACKGROUND_TOP_FILL_SRC_RECT, x * lBlockSize, y * lBlockSize, lBlockSize, lBlockSize, -0.01f, lColorConstant);

				} else {
					lTextureBatch.draw(mLevelTexture, BACKGROUND_FILL_SRC_RECT, x * lBlockSize, y * lBlockSize, lBlockSize, lBlockSize, -0.01f, lColorConstant);

				}

			}

		}

		lTextureBatch.end();
	}

	private void drawForeground(LintfordCore pCore) {
		final var lLevel = mLevelController.level();

		if (lLevel == null)
			return;

		final var lTextureBatch = rendererManager().uiTextureBatch();

		lTextureBatch.begin(pCore.gameCamera());

		final float lBlockSize = GameConstants.BLOCK_SIZE;

		for (int y = 0; y < GameConstants.LEVEL_TILES_HIGH; y++) {
			final float lModAmt = 1.f - (float) ((float) y / (float) GameConstants.LEVEL_TILES_HIGH * 0.5f);
			final var lColorConstant = ColorConstants.WHITE; // ColorConstants.getColor(lModAmt, lModAmt, lModAmt, 1.f);

			for (int x = 0; x < GameConstants.LEVEL_TILES_WIDE; x++) {
				final int lBlockTypeIndex = lLevel.getLevelBlockType(x, y);
				if (lBlockTypeIndex == Level.LEVEL_TILE_COORD_INVALID)
					continue;

				final int lTileIndex = lLevel.getLevelTileCoord(x, y);
				final int lTopBlockIndex = lLevel.getTopBlockIndex(lTileIndex);

				final byte lBlockHealth = lLevel.getBlockHealth(x, y);

				var lSrcRect = NO_SRC_RECT;
				switch (lBlockTypeIndex) {
				case Level.LEVEL_TILE_INDEX_GOLD:
					if (lTopBlockIndex != -1 && lLevel.getLevelBlockType(lTopBlockIndex) == 0) {
						if (lBlockHealth < 3) {
							lSrcRect = GOLD_TOP_SRC_RECT;
						} else
							lSrcRect = GOLD_TOP_SRC_RECT;

					} else {
						if (lBlockHealth < 3) {
							lSrcRect = GOLD_SRC_RECT;
						} else
							lSrcRect = GOLD_SRC_RECT;

					}

					break;

				case Level.LEVEL_TILE_INDEX_DIRT:
					if (lTopBlockIndex != -1 && lLevel.getLevelBlockType(lTopBlockIndex) == 0) {
						if (lBlockHealth < 3) {
							lSrcRect = DIRT_TOP_DAMAGED_SRC_RECT;
						} else
							lSrcRect = DIRT_TOP_SRC_RECT;

					} else {
						if (lBlockHealth < 3) {
							lSrcRect = DIRT_DAMAGED_SRC_RECT;
						} else
							lSrcRect = DIRT_SRC_RECT;

					}

					break;
				default:
					// lSrcRect = AIR_DEBUG_SRC_RECT;
				}

				lTextureBatch.draw(mLevelTexture, lSrcRect, x * lBlockSize, y * lBlockSize, lBlockSize, lBlockSize, -0.01f, lColorConstant);

			}

		}

		lTextureBatch.end();
	}

}
