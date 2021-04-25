package net.ruse.ld48.screens;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.core.particles.ParticleFrameworkData;
import net.lintford.library.renderers.particles.ParticleFrameworkRenderer;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;
import net.ruse.ld48.controllers.CameraFollowController;
import net.ruse.ld48.controllers.CameraZoomController;
import net.ruse.ld48.controllers.GameStateController;
import net.ruse.ld48.controllers.ItemController;
import net.ruse.ld48.controllers.LevelController;
import net.ruse.ld48.controllers.MobController;
import net.ruse.ld48.controllers.PlayerController;
import net.ruse.ld48.controllers.ScreenShakeController;
import net.ruse.ld48.data.ItemManager;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.data.MobManager;
import net.ruse.ld48.renderers.HudRenderer;
import net.ruse.ld48.renderers.ItemRenderer;
import net.ruse.ld48.renderers.LevelRenderer;
import net.ruse.ld48.renderers.MobRenderer;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private static final int DEBUG_TARGET_GOLD = 120;

	// Data
	private MobManager mMobManager;
	private ItemManager mItemManager;
	private Level mLevel;
	private ParticleFrameworkData mParticleData;

	// Controllers
	private ScreenShakeController mScreenShakeController;
	private MobController mMobController;
	private ItemController mItemController;
	private PlayerController mPlayerController;
	private LevelController mLevelController;
	private CameraFollowController mCameraFollowController;
	private CameraZoomController mCameraZoomController;
	private ParticleFrameworkController mParticleFrameworkController;
	private GameStateController mGameStateController;

	// Renderers
	private LevelRenderer mLevelRenderer;
	private ItemRenderer mItemRenderer;
	private MobRenderer mMobRenderer;
	private ParticleFrameworkRenderer mParticleFrameworkRenderer;
	private HudRenderer mHudRenderer;

	private boolean mShowHelpOnOpen;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager, boolean pShowHelpOnOpen) {
		super(pScreenManager);

		mLevel = new Level();
		mMobManager = new MobManager();
		mItemManager = new ItemManager();
		mParticleData = new ParticleFrameworkData();
		mShowHelpOnOpen = pShowHelpOnOpen;

		mShowInBackground = true;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		final var lCore = screenManager.core();

		mParticleData.initialize(lCore);

		createControllers(lCore);
		initializeControllers(lCore);

		createRenderers(lCore);

		startNewGame();

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

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE)) {
			screenManager.addScreen(new PauseScreen(screenManager, "paused"));

		}

		super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

		if (mGameStateController.hasGameEnded())
			return;

		if (mGameStateController.hasPlayerWon() && !pCoveredByOtherScreen) {
			screenManager.addScreen(new GameLostScreen(screenManager, "You Won!"));

			mGameStateController.endGame();
		}

		else if (mGameStateController.isPlayerDead() && !pCoveredByOtherScreen) {
			screenManager.addScreen(new GameLostScreen(screenManager, "You Died!"));

			mGameStateController.endGame();

		}

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
		mParticleFrameworkController = new ParticleFrameworkController(lControllerManager, mParticleData, entityGroupID());
		mItemController = new ItemController(lControllerManager, mItemManager, entityGroupID());
		mGameStateController = new GameStateController(lControllerManager, entityGroupID());
		mScreenShakeController = new ScreenShakeController(lControllerManager, (Camera) mGameCamera, entityGroupID());

	}

	private void initializeControllers(LintfordCore pCore) {
		mCameraZoomController.initialize(pCore);
		mCameraFollowController.initialize(pCore);
		mPlayerController.initialize(pCore);
		mLevelController.initialize(pCore);
		mMobController.initialize(pCore);
		mParticleFrameworkController.initialize(pCore);
		mGameStateController.initialize(pCore);
		mItemController.initialize(pCore);
		mScreenShakeController.initialize(pCore);

	}

	private void createRenderers(LintfordCore pCore) {
		mLevelRenderer = new LevelRenderer(rendererManager, entityGroupID());
		mLevelRenderer.initialize(pCore);

		mMobRenderer = new MobRenderer(rendererManager, entityGroupID());
		mMobRenderer.initialize(pCore);

		mParticleFrameworkRenderer = new ParticleFrameworkRenderer(rendererManager, entityGroupID());
		mParticleFrameworkRenderer.initialize(pCore);

		mItemRenderer = new ItemRenderer(rendererManager, entityGroupID());
		mItemRenderer.initialize(pCore);

		mHudRenderer = new HudRenderer(rendererManager, entityGroupID(), mShowHelpOnOpen);
		mHudRenderer.initialize(pCore);

	}

	// ---------------------------------------------
	// REFACTOR

	private void startNewGame() {
		mGameStateController.setupNewGame(DEBUG_TARGET_GOLD);

	}

}
