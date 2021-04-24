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
	private static final Rectangle DIRT_TOP_SRC_RECT = new Rectangle(64, 0, 32, 32);
	private static final Rectangle AIR_DEBUG_SRC_RECT = new Rectangle(32, 0, 32, 32);

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

		final var lLevel = mLevelController.level();

		if (lLevel == null)
			return;

		final var lFontUnit = rendererManager().textFont();
		final var lTextureBatch = rendererManager().uiTextureBatch();

		lTextureBatch.begin(pCore.gameCamera());

		final float lBlockSize = GameConstants.BLOCK_SIZE;

		for (int y = 0; y < GameConstants.LEVEL_TILES_HIGH; y++) {
			for (int x = 0; x < GameConstants.LEVEL_TILES_HIGH; x++) {
				final int lBlockIndex = lLevel.getLevelBlockType(x, y);
				if (lBlockIndex == Level.LEVEL_TILE_COORD_INVALID)
					continue;

				var lSrcRect = NO_SRC_RECT;
				switch (lBlockIndex) {
				case Level.LEVEL_TILE_INDEX_DIRT_TOP:
					lSrcRect = DIRT_TOP_SRC_RECT;
					break;
				case Level.LEVEL_TILE_INDEX_DIRT:
					lSrcRect = DIRT_SRC_RECT;
					break;
				default:
					lSrcRect = AIR_DEBUG_SRC_RECT;
				}

				lTextureBatch.draw(mLevelTexture, lSrcRect, x * lBlockSize, y * lBlockSize, lBlockSize, lBlockSize, -0.01f, ColorConstants.WHITE);

			}

		}

		lTextureBatch.end();

	}
}
