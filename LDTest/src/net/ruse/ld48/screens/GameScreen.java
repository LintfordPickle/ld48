package net.ruse.ld48.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;
import net.ruse.ld48.controllers.CameraFollowController;
import net.ruse.ld48.controllers.CameraZoomController;
import net.ruse.ld48.controllers.LevelController;
import net.ruse.ld48.controllers.MobController;
import net.ruse.ld48.controllers.PlayerController;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.data.MobManager;
import net.ruse.ld48.renderers.LevelRenderer;
import net.ruse.ld48.renderers.MobRenderer;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private MobManager mMobManager;
	private Level mLevel;

	// Controllers
	private MobController mMobController;
	private PlayerController mPlayerController;
	private LevelController mLevelController;
	private CameraFollowController mCameraFollowController;
	private CameraZoomController mCameraZoomController;

	// Renderers
	private LevelRenderer mLevelRenderer;
	private MobRenderer mMobRenderer;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mLevel = new Level();
		mMobManager = new MobManager();

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
		addPlayerMob();

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
		mCameraFollowController = new CameraFollowController(lControllerManager, mGameCamera, entityGroupID());

		mLevelController = new LevelController(lControllerManager, mLevel, entityGroupID());

		mMobController = new MobController(lControllerManager, mMobManager, entityGroupID());
		mPlayerController = new PlayerController(lControllerManager, entityGroupID());

	}

	private void initializeControllers(LintfordCore pCore) {
		mCameraZoomController.initialize(pCore);
		mCameraFollowController.initialize(pCore);
		mPlayerController.initialize(pCore);
		mLevelController.initialize(pCore);
		mMobController.initialize(pCore);

	}

	private void createRenderers(LintfordCore pCore) {
		mLevelRenderer = new LevelRenderer(rendererManager, entityGroupID());
		mLevelRenderer.initialize(pCore);

		mMobRenderer = new MobRenderer(rendererManager, entityGroupID());
		mMobRenderer.initialize(pCore);

	}

	private void addPlayerMob() {
		final var lPlayerMob = mMobManager.getFreePooledItem();
		lPlayerMob.isPlayerControlled = true;
		mMobManager.addMobInstance(lPlayerMob);

		mPlayerController.playerMobInstance(lPlayerMob);

		mCameraFollowController.setFollowEntity(lPlayerMob);

	}

}
