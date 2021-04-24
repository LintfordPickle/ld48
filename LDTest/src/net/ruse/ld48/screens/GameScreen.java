package net.ruse.ld48.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;
import net.ruse.ld48.controllers.CameraFollowController;
import net.ruse.ld48.controllers.CameraZoomController;
import net.ruse.ld48.controllers.LevelController;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.renderers.LevelRenderer;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private Level mLevel;

	// Controllers
	private LevelController mLevelController;
	private CameraFollowController mCameraFollowController;
	private CameraZoomController mCameraZoomController;

	// Renderers
	private LevelRenderer mLevelRenderer;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mLevel = new Level();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		final var lCore = screenManager.core();

		createControllers(lCore);
		initializeControllers(lCore);

		createRenderers(lCore);

		mLevelController.loadLevelFromFile("");

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

	}

	@Override
	public void handleInput(LintfordCore pCore) {
		super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void createControllers(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mCameraZoomController = new CameraZoomController(lControllerManager, mGameCamera, entityGroupID());
		mCameraFollowController = new CameraFollowController(lControllerManager, mGameCamera, null, entityGroupID());

		mLevelController = new LevelController(lControllerManager, mLevel, entityGroupID());

	}

	private void initializeControllers(LintfordCore pCore) {
		mCameraZoomController.initialize(pCore);
		mCameraFollowController.initialize(pCore);
		mLevelController.initialize(pCore);

	}

	private void createRenderers(LintfordCore pCore) {
		mLevelRenderer = new LevelRenderer(rendererManager, entityGroupID());
		mLevelRenderer.initialize(pCore);

	}

}
