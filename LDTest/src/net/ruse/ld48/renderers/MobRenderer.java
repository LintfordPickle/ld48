package net.ruse.ld48.renderers;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.controllers.MobController;

public class MobRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "Mob Renderer";

	private static final float FULL_FLASH_DUR = 150;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private SpriteSheetDefinition mMobSpriteSheet;
	private Texture mLevelTexture;

	private MobController mMobController;

	@Override
	public boolean isInitialized() {
		return mMobController != null;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MobRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mMobController = (MobController) pCore.controllerManager().getControllerByNameRequired(MobController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mMobSpriteSheet = pResourceManager.spriteSheetManager().loadSpriteSheet("res/spritesheets/spritesheetMobs.json", entityGroupID());
		mLevelTexture = pResourceManager.textureManager().loadTexture("TEXTURE_LEVEL", "res//textures//textureLevel.png", entityGroupID());

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lMobManager = mMobController.mobManager();

		final var lMobList = lMobManager.mobInstances();
		if (lMobList == null || lMobList.size() == 0)
			return;

		final var lSpriteBatch = rendererManager().uiSpriteBatch();
		final var lTextureBatch = rendererManager().uiTextureBatch();

		lTextureBatch.begin(pCore.gameCamera());
		lSpriteBatch.begin(pCore.gameCamera());

		final int lMobCount = lMobList.size();
		for (int i = 0; i < lMobCount; i++) {
			final var lMobInstance = lMobList.get(i);

			final float lMobX = lMobInstance.worldPositionX;
			final float lMobY = lMobInstance.worldPositionY;
			final float lMobR = lMobInstance.radius;

			final var lMobSpriteInstance = lMobInstance.currentSprite;

			String lCurrentAnimationName = lMobInstance.mobTypeName() + "_IDLE";

			if (lMobInstance.swingingFlag && lMobInstance.swingAttackEnabled) {
				lCurrentAnimationName = lMobInstance.mobTypeName() + "_SWING";

			} else if (Math.abs(lMobInstance.velocityX) > 0.002f) {
				lCurrentAnimationName = lMobInstance.mobTypeName() + "_WALK";

			}

			if (lMobSpriteInstance == null || lMobInstance.mCurrentAnimationName == null || !lMobInstance.mCurrentAnimationName.equals(lCurrentAnimationName)) {
				lMobInstance.currentSprite = mMobSpriteSheet.getSpriteInstance(lCurrentAnimationName);
				if (lMobInstance.currentSprite != null) {
					lMobInstance.currentSprite.timerMod = lMobInstance.animationTimeSpeedMod;
					lMobInstance.mCurrentAnimationName = lCurrentAnimationName;

				}

			}

			if (lMobSpriteInstance == null)
				continue;

			lMobSpriteInstance.setCenterPosition(lMobX, lMobY);
			lMobSpriteInstance.update(pCore);

			final float lMobWidth = lMobInstance.currentSprite.width();

			var lTintColor = ColorConstants.WHITE;
			if (!lMobInstance.isDamageCooldownElapsed() && lMobInstance.damageCooldownTimer % FULL_FLASH_DUR < FULL_FLASH_DUR * .5f) {
				lTintColor = ColorConstants.getColor(100, 100, 100, 1);
			}

			if (lMobInstance.isLeftFacing) {
				lSpriteBatch.draw(mMobSpriteSheet, lMobSpriteInstance.currentSpriteFrame(), lMobX + 16.f, lMobY - 16.f, -lMobWidth, 32, -0.1f, lTintColor);

			} else {
				lSpriteBatch.draw(mMobSpriteSheet, lMobSpriteInstance.currentSpriteFrame(), lMobX - 16.f, lMobY - 16.f, lMobWidth, 32, -0.1f, lTintColor);

			}

			if (lMobInstance.swingingFlag && lMobInstance.lastSwingTileCoord != -1) {
				final float lTileWorldPosX = (lMobInstance.lastSwingTileCoord % GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;
				final float lTileWorldPosY = (lMobInstance.lastSwingTileCoord / GameConstants.LEVEL_TILES_WIDE) * GameConstants.BLOCK_SIZE;

				lTextureBatch.draw(mLevelTexture, 32, 0, 32, 32, lTileWorldPosX, lTileWorldPosY, 32, 32, -0.01f, ColorConstants.WHITE);

			}

		}

		lSpriteBatch.end();
		lTextureBatch.end();

	}

}
