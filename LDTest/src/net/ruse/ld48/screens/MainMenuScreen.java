package net.ruse.ld48.screens;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class MainMenuScreen extends MenuScreen {

	private static final int BUTTON_START = 0;
	private static final int BUTTON_EXIT = 1;

	public MainMenuScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		final var lListLayout = new ListLayout(this);
		lListLayout.layoutFillType(FILLTYPE.TAKE_WHATS_NEEDED);

		final var lRestartEntry = new MenuEntry(pScreenManager, lListLayout, "Start");
		lRestartEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lRestartEntry.registerClickListener(this, BUTTON_START);
		lRestartEntry.desiredWidth(400.f);

		final var lExitToMenuEntry = new MenuEntry(pScreenManager, lListLayout, "Exit to Desktop");
		lExitToMenuEntry.horizontalFillType(FILLTYPE.TAKE_DESIRED_SIZE);
		lExitToMenuEntry.registerClickListener(this, BUTTON_EXIT);
		lExitToMenuEntry.desiredWidth(400.f);

		lListLayout.menuEntries().add(lRestartEntry);
		lListLayout.menuEntries().add(lExitToMenuEntry);

		layouts().add(lListLayout);

		mESCBackEnabled = false;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_START:
			LoadingScreen.load(screenManager, false, new GameScreen(screenManager, true));
			break;

		case BUTTON_EXIT:
			screenManager.exitGame();
			break;

		}

	}

}
