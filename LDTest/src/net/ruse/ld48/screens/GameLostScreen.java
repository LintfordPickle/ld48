package net.ruse.ld48.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameLostScreen extends MenuScreen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final int BUTTON_RESTART = 0;
	private static final int BUTTON_EXIT_TO_MENU = 1;

	// --------------------------------------
	// Variables
	// --------------------------------------

	protected Texture mUITexture;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameLostScreen(ScreenManager pScreenManager, String pMenuTitle) {
		super(pScreenManager, pMenuTitle);

		final var lListLayout = new ListLayout(this);
		lListLayout.layoutFillType(FILLTYPE.TAKE_WHATS_NEEDED);

		final var lRestartEntry = new MenuEntry(pScreenManager, lListLayout, "Restart");
		lRestartEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lRestartEntry.registerClickListener(this, BUTTON_RESTART);
		lRestartEntry.desiredWidth(400.f);

		final var lExitToMenuEntry = new MenuEntry(pScreenManager, lListLayout, "Exit to Menu");
		lExitToMenuEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lExitToMenuEntry.registerClickListener(this, BUTTON_EXIT_TO_MENU);
		lExitToMenuEntry.desiredWidth(400.f);

		lListLayout.menuEntries().add(lRestartEntry);
		lListLayout.menuEntries().add(lExitToMenuEntry);

		layouts().add(lListLayout);

		mIsPopup = true;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mUITexture = pResourceManager.textureManager().textureCore();

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mUITexture = null;

	}

	@Override
	public void draw(LintfordCore pCore) {
		final float lWindowWidth = pCore.HUD().boundingRectangle().w();
		final float lWindowHeight = pCore.HUD().boundingRectangle().h();

		final var lTextureBatch = textureBatch();
		final var lColor = ColorConstants.getBlackWithAlpha(.85f);

		lTextureBatch.begin(pCore.HUD());
		lTextureBatch.draw(mUITexture, 0, 0, 32, 32, -lWindowWidth * 0.5f, -lWindowHeight * 0.5f, lWindowWidth, lWindowHeight, -.9f, lColor);
		lTextureBatch.end();

		super.draw(pCore);
	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_RESTART:
			LoadingScreen.load(screenManager, false, new GameScreen(screenManager));
			break;

		case BUTTON_EXIT_TO_MENU:
			// LoadingScreen.load(screenManager, false, new BackgroundScreen(screenManager), new MainMenuClientScreen(screenManager));

			break;

		}
	}
}
