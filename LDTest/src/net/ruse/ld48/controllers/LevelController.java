package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.input.IProcessMouseInput;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.particles.particlesystems.ParticleSystemInstance;
import net.ruse.ld48.GameConstants;
import net.ruse.ld48.data.Level;

public class LevelController extends BaseController implements IProcessMouseInput {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Level Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private SoundFxController mSoundFxController;
	private ItemController mItemController;
	private GameStateController mGameStateController;
	private ParticleFrameworkController mParticleFrameworkController;

	private ParticleSystemInstance mDigBlockParticles;
	private ParticleSystemInstance mDigNoEffectBlockParticles;

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
		mSoundFxController = (SoundFxController) pCore.controllerManager().getControllerByNameRequired(SoundFxController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mItemController = (ItemController) pCore.controllerManager().getControllerByNameRequired(ItemController.CONTROLLER_NAME, entityGroupID());

		mDigBlockParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_DIG");
		mDigNoEffectBlockParticles = mParticleFrameworkController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_DIG_NOEFFECT");

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {

		if (GameConstants.DEBUG_MODE) {
			if (pCore.input().mouse().isMouseLeftButtonDownTimed(this)) {
				final int lMouseTileX = (int) (pCore.gameCamera().getMouseWorldSpaceX() / GameConstants.BLOCK_SIZE);
				final int lMouseTileY = (int) (pCore.gameCamera().getMouseWorldSpaceY() / GameConstants.BLOCK_SIZE);

				if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {

					// place block
					mLevel.placeBlock(lMouseTileX, lMouseTileX, Level.LEVEL_TILE_INDEX_DIRT, (byte) 2);

				} else {
					mLevel.digBlock(lMouseTileX, lMouseTileX, (byte) 50);
				}

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

	public boolean digLevel(int pTileX, int pTileY, byte pDamageAmt) {
		final int lTileIndex = mLevel.getLevelTileCoord(pTileX, pTileY);
		if (lTileIndex == Level.LEVEL_TILE_COORD_INVALID)
			return false;

		return digLevel(lTileIndex, pDamageAmt);

	}

	public boolean digLevel(int pTileCoord, byte pDamageAmt) {
		final int lBlockTypeIndex = mLevel.getLevelBlockType(pTileCoord);

		{
			if (pTileCoord % GameConstants.LEVEL_TILES_WIDE == 0)
				return false;
			if (pTileCoord % GameConstants.LEVEL_TILES_WIDE == GameConstants.LEVEL_TILES_WIDE - 1)
				return false;
			if (pTileCoord > GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH - GameConstants.LEVEL_TILES_WIDE)
				return false;
		}

		final boolean lWasBlockedRemoved = mLevel.digBlock(pTileCoord, pDamageAmt);

		if (lBlockTypeIndex == Level.LEVEL_TILE_INDEX_STONE) {
			mSoundFxController.playSound(SoundFxController.SOUND_DIG_STONE);

			final int lTileX = pTileCoord % GameConstants.LEVEL_TILES_WIDE;
			final int lTileY = pTileCoord / GameConstants.LEVEL_TILES_WIDE;

			final float lTileCenterX = lTileX * 32.f + RandomNumbers.random(4.f, 28.f);
			final float lTileCenterY = lTileY * 32.f + RandomNumbers.random(4.f, 28.f);

			final float lVelocityX = RandomNumbers.random(-5.f, 5.f);
			final float lVelocityY = -10.f + RandomNumbers.random(-5.f, 5.f);

			mDigNoEffectBlockParticles.spawnParticle(lTileCenterX, lTileCenterY, lVelocityX, lVelocityY, 0, 0, 16, 16, 64.f, 64.f);

			return false;
		}

		if (lBlockTypeIndex == Level.LEVEL_TILE_INDEX_HEART && lWasBlockedRemoved) {
			final int lTileX = pTileCoord % GameConstants.LEVEL_TILES_WIDE;
			final int lTileY = pTileCoord / GameConstants.LEVEL_TILES_WIDE;

			final float lTileCenterX = lTileX * 32.f + RandomNumbers.random(4.f, 28.f);
			final float lTileCenterY = lTileY * 32.f + RandomNumbers.random(4.f, 28.f);

			mItemController.addHealth(lTileCenterX, lTileCenterY);

		}

		if (lBlockTypeIndex == Level.LEVEL_TILE_INDEX_AIR)
			return false;

		if (lWasBlockedRemoved) {
			if (lBlockTypeIndex == Level.LEVEL_TILE_INDEX_GOLD) {
				final int lTileX = pTileCoord % GameConstants.LEVEL_TILES_WIDE;
				final int lTileY = pTileCoord / GameConstants.LEVEL_TILES_WIDE;

				final float lWorldPositionX = lTileX * GameConstants.BLOCK_SIZE;
				final float lWorldPositionY = lTileY * GameConstants.BLOCK_SIZE;

				mItemController.addCoinSplash(lWorldPositionX, lWorldPositionY, RandomNumbers.random(7, 11));

			}

		}

		if (lBlockTypeIndex <= 0)
			return false;

		final int lSoundVariationIndex = RandomNumbers.random(0, 2);
		switch (lSoundVariationIndex) {
		default:
		case 0:
			mSoundFxController.playSound(SoundFxController.SOUND_DIG_DIRT_1);
			break;

		case 1:
			mSoundFxController.playSound(SoundFxController.SOUND_DIG_DIRT_2);
			break;
		}

		if (mDigBlockParticles != null) {
			final int lTileX = pTileCoord % GameConstants.LEVEL_TILES_WIDE;
			final int lTileY = pTileCoord / GameConstants.LEVEL_TILES_WIDE;

			final float lTileCenterX = lTileX * 32.f + RandomNumbers.random(4.f, 28.f);
			final float lTileCenterY = lTileY * 32.f + RandomNumbers.random(4.f, 28.f);

			final float lVelocityX = RandomNumbers.random(-5.f, 5.f);
			final float lVelocityY = -10.f + RandomNumbers.random(-5.f, 5.f);

			mDigBlockParticles.spawnParticle(lTileCenterX, lTileCenterY, lVelocityX, lVelocityY, 0, 0, 16, 16, 64.f, 64.f);

		}

		return lWasBlockedRemoved;

	}

	public void loadLevelFromFile(String pFilename) {
		mLevel.loadLevel();
	}

	public void startNewGame(int pLevelNumber) {
		mLevel.loadLevel();

	}

}