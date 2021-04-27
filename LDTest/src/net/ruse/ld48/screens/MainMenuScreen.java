package net.ruse.ld48.screens;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class MainMenuScreen extends MenuScreen {

	private static final int BUTTON_START_EASY = 0;
	private static final int BUTTON_START_MEDIUM = 1;
	private static final int BUTTON_START_HARD = 2;
	private static final int BUTTON_EXIT = 3;

	public MainMenuScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		final var lListLayout = new ListLayout(this);
		lListLayout.layoutFillType(FILLTYPE.TAKE_WHATS_NEEDED);

		final var lEasyStartEntry = new MenuEntry(pScreenManager, lListLayout, "Easy");
		lEasyStartEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lEasyStartEntry.registerClickListener(this, BUTTON_START_EASY);
		lEasyStartEntry.desiredWidth(400.f);

		final var lMediumStartEntry = new MenuEntry(pScreenManager, lListLayout, "Medium");
		lMediumStartEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lMediumStartEntry.registerClickListener(this, BUTTON_START_MEDIUM);
		lMediumStartEntry.desiredWidth(400.f);

		final var lHardStartEntry = new MenuEntry(pScreenManager, lListLayout, "Hard");
		lHardStartEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lHardStartEntry.registerClickListener(this, BUTTON_START_HARD);
		lHardStartEntry.desiredWidth(400.f);

		final var lExitToMenuEntry = new MenuEntry(pScreenManager, lListLayout, "Exit to Desktop");
		lExitToMenuEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lExitToMenuEntry.registerClickListener(this, BUTTON_EXIT);
		lExitToMenuEntry.desiredWidth(400.f);

		lListLayout.menuEntries().add(lEasyStartEntry);
		lListLayout.menuEntries().add(lMediumStartEntry);
		lListLayout.menuEntries().add(lHardStartEntry);
		lListLayout.menuEntries().add(lExitToMenuEntry);

		layouts().add(lListLayout);

		mPaddingTopNormalized = -50.f;

		mESCBackEnabled = false;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_START_EASY:
			LoadingScreen.load(screenManager, false, new GameScreen(screenManager, true, 1));
			break;

		case BUTTON_START_MEDIUM:
			LoadingScreen.load(screenManager, false, new GameScreen(screenManager, true, 2));
			break;

		case BUTTON_START_HARD:
			LoadingScreen.load(screenManager, false, new GameScreen(screenManager, true, 3));
			break;

		case BUTTON_EXIT:
			screenManager.exitGame();
			break;

		}

	}

}
