package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.IProcessMouseInput;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.particles.particlesystems.ParticleSystemInstance;
import net.ruse.ld48.data.Level;

public class LevelController extends BaseController implements IProcessMouseInput {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Level Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameStateController mGameStateController;
	private ParticleFrameworkController mParticleFrameworkController;
	private ParticleSystemInstance mDigBlockParticles;

	private Level mLevel;
	private float mMouseCooldownTimer;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isCoolDownElapsed() {
		return mMouseCooldownTimer <= 0;
	}

	@Override
	public void resetCoolDownTimer() {
		mMouseCooldownTimer = 200;

	}

	public Level level() {
		return mLevel;
	}

	@Override
	public boolean isInitialized() {
		return mGameStateController != null;

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public LevelController(ControllerManager pControllerManager, Level pLevel, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mLevel = pLevel;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mGameStateController = (GameStateController) pCore.controllerManager().getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
		mParticleFrameworkController = (ParticleFrameworkController) pCore.controllerManager().getControllerByNameRequired(ParticleFrameworkController.CONTROLLER_NAME, entityGroupID());

		mDigBlockParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_DIG");

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_R)) {
			mLevel.loadLevel();

		}

		if (pCore.input().mouse().isMouseLeftButtonDownTimed(this)) {
			final float lMouseWorldPositionX = pCore.gameCamera().getMouseWorldSpaceX();
			final float lMouseWorldPositionY = pCore.gameCamera().getMouseWorldSpaceY();

			final int lTileX = (int) (lMouseWorldPositionX / 32.f);
			final int lTileY = (int) (lMouseWorldPositionY / 32.f);

			final int lSelectedTileCoord = mLevel.getLevelTileCoord(lTileX, lTileY);

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				mLevel.levelBlocks()[lSelectedTileCoord] = 1;

			} else {
				digLevel(lTileX, lTileY, (byte) 10);

			}

		}

		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		if (mMouseCooldownTimer > 0) {
			mMouseCooldownTimer -= pCore.gameTime().elapsedTimeMilli();

		}
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void digLevel(int pTileX, int pTileY, byte pDamageAmt) {
		final int lBLockTypeIndex = mLevel.getLevelBlockType(pTileX, pTileY);
		final boolean lWasBlockedRemoved = mLevel.digBlock(pTileX, pTileY, pDamageAmt);

		if (lBLockTypeIndex == Level.LEVEL_TILE_INDEX_AIR || lBLockTypeIndex == Level.LEVEL_TILE_INDEX_STONE)
			return;

		final int lTileIndex = mLevel.getLevelTileCoord(pTileX, pTileY);
		if (lTileIndex == Level.LEVEL_TILE_COORD_INVALID)
			return;

		if (lWasBlockedRemoved) {
			if (lBLockTypeIndex == Level.LEVEL_TILE_INDEX_GOLD) {
				mGameStateController.addGold(10);

			}

		}

		final int lBlockType = mLevel.getLevelBlockType(pTileX, pTileY);
		if (lBlockType <= 0)
			return;

		if (mDigBlockParticles != null) {
			final float lTileCenterX = pTileX * 32.f + 16f;
			final float lTileCenterY = pTileY * 32.f + 16f;

			final float lVelocityX = RandomNumbers.random(-5.f, 5.f);
			final float lVelocityY = -10.f + RandomNumbers.random(-5.f, 5.f);

			mDigBlockParticles.spawnParticle(lTileCenterX, lTileCenterY, lVelocityX, lVelocityY, 0, 0, 16, 16, 64.f, 64.f);

		}

	}

	public void loadLevelFromFile(String pFilename) {
		mLevel.loadLevel();
	}

	public void startNewGame(long pSeed) {
		mLevel.loadLevel();

	}

}
