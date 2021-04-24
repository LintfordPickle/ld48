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
import net.ruse.ld48.controllers.LevelController;
import net.ruse.ld48.controllers.MobController;
import net.ruse.ld48.controllers.PlayerController;
import net.ruse.ld48.data.Level;
import net.ruse.ld48.data.MobInstance;
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
	private ParticleFrameworkData mParticleData;

	// Controllers
	private MobController mMobController;
	private PlayerController mPlayerController;
	private LevelController mLevelController;
	private CameraFollowController mCameraFollowController;
	private CameraZoomController mCameraZoomController;
	private ParticleFrameworkController mParticleFrameworkController;

	// Renderers
	private LevelRenderer mLevelRenderer;
	private MobRenderer mMobRenderer;
	private ParticleFrameworkRenderer mParticleFrameworkRenderer;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mLevel = new Level();
		mMobManager = new MobManager();
		mParticleData = new ParticleFrameworkData();

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
		mParticleFrameworkController = new ParticleFrameworkController(lControllerManager, mParticleData, entityGroupID());

	}

	private void initializeControllers(LintfordCore pCore) {
		mCameraZoomController.initialize(pCore);
		mCameraFollowController.initialize(pCore);
		mPlayerController.initialize(pCore);
		mLevelController.initialize(pCore);
		mMobController.initialize(pCore);
		mParticleFrameworkController.initialize(pCore);

	}

	private void createRenderers(LintfordCore pCore) {
		mLevelRenderer = new LevelRenderer(rendererManager, entityGroupID());
		mLevelRenderer.initialize(pCore);

		mMobRenderer = new MobRenderer(rendererManager, entityGroupID());
		mMobRenderer.initialize(pCore);

		mParticleFrameworkRenderer = new ParticleFrameworkRenderer(rendererManager, entityGroupID());
		mParticleFrameworkRenderer.initialize(pCore);

	}

	private void addPlayerMob() {
		final var lPlayerMob = mMobManager.getFreePooledItem();

		lPlayerMob.initialise(MobInstance.MOB_TYPE_DWARF);
		lPlayerMob.isPlayerControlled = true;

		mMobManager.addMobInstance(lPlayerMob);

		mPlayerController.playerMobInstance(lPlayerMob);

		mCameraFollowController.setFollowEntity(lPlayerMob);

	}

	private void addEnemyMobs() {
		for (int i = 0; i < 10; i++) {
			final var lEnemyMob = mMobManager.getFreePooledItem();

			lEnemyMob.initialise(MobInstance.MOB_TYPE_GOBLIN);
			lEnemyMob.isPlayerControlled = false;

			final float lWorldPositionX = RandomNumbers.random(0, GameConstants.LEVEL_TILES_WIDE * GameConstants.BLOCK_SIZE);
			final float lWorldPositionY = 0;

			lEnemyMob.setPosition(lWorldPositionX, lWorldPositionY);

			// TODO :Get valid platform from level

			mMobManager.addMobInstance(lEnemyMob);

		}

	}

}
