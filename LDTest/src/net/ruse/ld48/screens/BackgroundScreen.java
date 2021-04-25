package net.ruse.ld48.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;

public class BackgroundScreen extends Screen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Texture mBackgroundTexture;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BackgroundScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mShowInBackground = true;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mBackgroundTexture = pResourceManager.textureManager().loadTexture("TEXTURE_MAINMENU", "res//textures//textureMainMenu.png", entityGroupID());

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mBackgroundTexture = null;

	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		final var lHudRect = pCore.HUD().boundingRectangle();
		final var lTextureBatch = rendererManager.uiTextureBatch();

		lTextureBatch.begin(pCore.HUD());
		lTextureBatch.draw(mBackgroundTexture, 0, 0, 640, 480, lHudRect, -0.9f, ColorConstants.WHITE);
		lTextureBatch.end();

	}

}
