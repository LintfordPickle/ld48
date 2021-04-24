package net.ruse.ld48;

import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;

public class BaseGame extends LintfordCore {

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseGame(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs, false);

	}

	// ---------------------------------------------
	// Entry
	// ---------------------------------------------

	public static void main(String[] pArgs) {
		final var lGameInfo = new GameInfo() {
			@Override
			public int baseGameResolutionWidth() {
				return GameConstants.WINDOW_WIDTH;
			}

			@Override
			public int baseGameResolutionHeight() {
				return GameConstants.WINDOW_HEIGHT;
			}

			@Override
			public int minimumWindowWidth() {
				return GameConstants.WINDOW_WIDTH;
			}

			@Override
			public int minimumWindowHeight() {
				return GameConstants.WINDOW_HEIGHT;
			}

			@Override
			public boolean stretchGameResolution() {
				return true;
			}

			@Override
			public boolean windowResizeable() {
				return true;
			}

			@Override
			public String windowTitle() {
				return GameConstants.WINDOW_TITLE;
			}
		};

		final var lBaseGame = new BaseGame(lGameInfo, pArgs);
		lBaseGame.createWindow();
	}

}
