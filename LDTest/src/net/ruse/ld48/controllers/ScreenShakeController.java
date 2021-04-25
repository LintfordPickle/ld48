package net.ruse.ld48.controllers;

import java.util.Random;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.core.maths.Vector2f;

public class ScreenShakeController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Screen Shake Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Camera mGameCamera;

	protected Random mRandom = new Random();
	protected float mShakeMag;
	protected float mShakeDur;
	protected float mShakeTimer;
	private boolean mNeedsCleanup;

	protected final Vector2f mOffsetPosition = new Vector2f();

	// ---------------------------------------------
	// Constructor 
	// ---------------------------------------------

	public ScreenShakeController(ControllerManager pControllerManager, Camera pGameCamera, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mGameCamera = pGameCamera;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mGameCamera != null;
	}

	@Override
	public void initialize(LintfordCore pCore) {

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		if (mGameCamera == null)
			return;

		if (mShakeTimer > 0.f) {
			mNeedsCleanup = true;
			mShakeTimer -= pCore.appTime().elapsedTimeMilli();

			// normal time
			float progress = mShakeTimer / mShakeDur;

			float lMagnitude = mShakeMag * (1f - (progress * progress));

			mOffsetPosition.x = mRandom.nextFloat() * lMagnitude;
			mOffsetPosition.y = mRandom.nextFloat() * lMagnitude;

			mGameCamera.setCameraOffset(mOffsetPosition.x, mOffsetPosition.y);

		} else if (mNeedsCleanup) {
			mNeedsCleanup = false;
			mOffsetPosition.x = 0.f;
			mOffsetPosition.y = 0.f;

			mGameCamera.setCameraOffset(mOffsetPosition.x, mOffsetPosition.y);
		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void shakeCamera(float pDuration, float pMagnitude) {
		// don't interrupt large shakes with little ones
		if (mShakeTimer > 0.f) {
			if (mShakeMag > pMagnitude)
				return;
		}

		mShakeMag = pMagnitude;
		mShakeDur = Math.max(pDuration, mShakeDur);

		mShakeTimer = pDuration;

	}

}
