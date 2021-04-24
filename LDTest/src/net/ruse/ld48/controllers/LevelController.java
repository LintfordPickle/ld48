package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.particles.particlesystems.ParticleSystemInstance;
import net.ruse.ld48.data.Level;

public class LevelController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Level Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ParticleFrameworkController mParticleFrameworkController;

	private Level mLevel;

	private ParticleSystemInstance mDigBlockParticles;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Level level() {
		return mLevel;
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
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(LintfordCore pCore) {
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

		return super.handleInput(pCore);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void digLevel(int pTileX, int pTileY, byte pDamageAmt) {
		mLevel.digBlock(pTileX, pTileY, pDamageAmt);

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

}
