package net.ruse.ld48.screens;

import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.particles.ParticleFrameworkData;
import net.lintford.library.renderers.particles.ParticleFrameworkRenderer;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.controllers.CameraFollowController;
import net.ruse.ld48.controllers.CameraZoomController;
import net.ruse.ld48.controllers.GameStateController;
import net.ruse.ld48.controllers.LevelController;
import net.ruse.ld48.controllers.MobController;
import net.ruse.ld48.controllers.PlayerController;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.data.MobInstance;
import net.ruse.ld48.data.MobManager;
import net.ruse.ld48.renderers.HudRenderer;
import net.ruse.ld48.renderers.LevelRenderer;
import net.ruse.ld48.renderers.MobRenderer;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private static final int DEBUG_TARGET_GOLD = 120;
	private static final int DEBUG_PLAYER_HEALTH = 4;
	private static final int DEBUG_NUM_ENEMIES = 6;

	// Data
	private MobManager mMobManager;
	private Level mLevel;
	private ParticleFrameworkData mParticleData;

	// Controllers
	private MobController mMobController;
	private PlayerController mPlayerController;
	private LevelController mLevelController;
	private CameraFollowController mCameraFollowController;
	private CameraZoomController mCameraZoomController;
	private ParticleFrameworkController mParticleFrameworkController;
	private GameStateController mGameStateController;

	// Renderers
	private LevelRenderer mLevelRenderer;
	private MobRenderer mMobRenderer;
	private ParticleFrameworkRenderer mParticleFrameworkRenderer;
	private HudRenderer mHudRenderer;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mLevel = new Level();
		mMobManager = new MobManager();
		mParticleData = new ParticleFrameworkData();

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

		mLevelController.loadLevelFromFile("");

		addPlayerMob();
		addEnemyMobs();

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
		super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

		if (mGameStateController.hasGameEnded())
			return;

		if (mGameStateController.isPlayerDead() && !pCoveredByOtherScreen) {
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

		mGameStateController = new GameStateController(lControllerManager, entityGroupID());

	}

	private void initializeControllers(LintfordCore pCore) {
		mCameraZoomController.initialize(pCore);
		mCameraFollowController.initialize(pCore);
		mPlayerController.initialize(pCore);
		mLevelController.initialize(pCore);
		mMobController.initialize(pCore);
		mParticleFrameworkController.initialize(pCore);
		mGameStateController.initialize(pCore);

	}

	private void createRenderers(LintfordCore pCore) {
		mLevelRenderer = new LevelRenderer(rendererManager, entityGroupID());
		mLevelRenderer.initialize(pCore);

		mMobRenderer = new MobRenderer(rendererManager, entityGroupID());
		mMobRenderer.initialize(pCore);

		mParticleFrameworkRenderer = new ParticleFrameworkRenderer(rendererManager, entityGroupID());
		mParticleFrameworkRenderer.initialize(pCore);

		mHudRenderer = new HudRenderer(rendererManager, entityGroupID());
		mHudRenderer.initialize(pCore);

	}

	private void addPlayerMob() {
		final var lPlayerMob = mMobManager.getFreePooledItem();

		lPlayerMob.initialise(MobInstance.MOB_TYPE_DWARF, DEBUG_PLAYER_HEALTH);
		lPlayerMob.isPlayerControlled = true;
		lPlayerMob.swingAttackEnabled = true;
		lPlayerMob.damagesOnCollide = false;
		lPlayerMob.setPosition(32.f, 0.f);
		lPlayerMob.swingRange = 32.f;

		mMobManager.addMobInstance(lPlayerMob);

		mPlayerController.playerMobInstance(lPlayerMob);

		mCameraFollowController.setFollowEntity(lPlayerMob);

	}

	private void addEnemyMobs() {
		for (int i = 0; i < DEBUG_NUM_ENEMIES; i++) {
			final var lEnemyMob = mMobManager.getFreePooledItem();

			lEnemyMob.initialise(MobInstance.MOB_TYPE_GOBLIN, 3);
			lEnemyMob.damagesOnCollide = false;
			lEnemyMob.swingAttackEnabled = true;
			lEnemyMob.swingRange = 48.f;
			lEnemyMob.isPlayerControlled = false;

			final float lWorldPositionX = 256.f;//RandomNumbers.random(32.f, 96.f);
			final float lWorldPositionY = 0;

			lEnemyMob.setPosition(lWorldPositionX, lWorldPositionY);

			// TODO :Get valid platform from level

			mMobManager.addMobInstance(lEnemyMob);

		}

	}

	private void startNewGame() {
		mGameStateController.setupNewGame(DEBUG_TARGET_GOLD);

	}

}
