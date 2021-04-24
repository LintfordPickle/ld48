package net.ruse.ld48.renderers;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.sprites.spritesheet.SpriteSheetDefinition;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;
import net.ruse.ld48.controllers.MobController;

public class MobRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "Mob Renderer";

	// --------------------------------------
	// Variables
	// --------------------------------------

	SpriteSheetDefinition mDwarfSpriteSheet;

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

		mDwarfSpriteSheet = pResourceManager.spriteSheetManager().loadSpriteSheet("res/spritesheets/spritesheetDwarf.json", entityGroupID());

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lMobManager = mMobController.mobManager();

		final var lMobList = lMobManager.mobInstances();
		if (lMobList == null || lMobList.size() == 0)
			return;

		final var lSpriteBatch = rendererManager().uiSpriteBatch();

		lSpriteBatch.begin(pCore.gameCamera());

		final int lMobCount = lMobList.size();
		for (int i = 0; i < lMobCount; i++) {
			final var lMobInstance = lMobList.get(i);

			final float lMobX = lMobInstance.worldPositionX;
			final float lMobY = lMobInstance.worldPositionY;

			final var lMobSpriteInstance = lMobInstance.currentSprite;

			if (lMobSpriteInstance == null) {
				lMobInstance.currentSprite = mDwarfSpriteSheet.getSpriteInstance("idle");
				continue;

			}

			lMobSpriteInstance.setPosition(lMobX, lMobY);
			lMobSpriteInstance.update(pCore);

			lSpriteBatch.draw(mDwarfSpriteSheet, lMobSpriteInstance, -0.1f, ColorConstants.WHITE);

		}

		lSpriteBatch.end();

	}
}
