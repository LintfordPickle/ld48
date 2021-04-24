package net.ruse.ld48.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.entity.WorldEntity;
import net.lintford.library.core.maths.Vector2f;

public class CameraFollowController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "CameraFollowController";

	private static final float CAMERA_MAN_MOVE_SPEED = 0.05f;
	private static final float CAMERA_MAN_MOVE_SPEED_MAX = 1f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ICamera mGameCamera;
	private WorldEntity mTrackedEntity;
	private boolean mIsTrackingPlayer;
	private Vector2f mVelocity;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ICamera gameCamera() {
		return mGameCamera;
	}

	public boolean trackPlayer() {
		return mIsTrackingPlayer;
	}

	public void trackPlayer(boolean pNewValue) {
		mIsTrackingPlayer = pNewValue;
	}

	@Override
	public boolean isInitialized() {
		return mGameCamera != null;

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CameraFollowController(ControllerManager pControllerManager, ICamera pCamera, int pControllerGroup) {
		super(pControllerManager, CONTROLLER_NAME, pControllerGroup);

		mVelocity = new Vector2f();

		//
		mGameCamera = pCamera;
		mIsTrackingPlayer = true;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {

	}

	public void initialize(ICamera pGameCamera, WorldEntity pTrackedEntity) {
		mGameCamera = pGameCamera;
		mTrackedEntity = pTrackedEntity;

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (mGameCamera == null)
			return false;

		final float speed = CAMERA_MAN_MOVE_SPEED / mGameCamera.getZoomFactor();

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT)) {
			mVelocity.x -= speed;
			mIsTrackingPlayer = false;

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
			mVelocity.x += speed;
			mIsTrackingPlayer = false;

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DOWN)) {
			mVelocity.y += speed;
			mIsTrackingPlayer = false;

		}

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_UP)) {
			mVelocity.y -= speed;
			mIsTrackingPlayer = false;

		}

		return false;

	}

	@Override
	public void update(LintfordCore pCore) {
		if (mGameCamera == null)
			return;

		mIsTrackingPlayer = mTrackedEntity != null;
		if (mIsTrackingPlayer) {
			mGameCamera.setPosition(mTrackedEntity.worldPositionX, mTrackedEntity.worldPositionY);

		} else {
			// Cap
			if (mVelocity.x < -CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.x = -CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.x > CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.x = CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.y < -CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.y = -CAMERA_MAN_MOVE_SPEED_MAX;
			if (mVelocity.y > CAMERA_MAN_MOVE_SPEED_MAX)
				mVelocity.y = CAMERA_MAN_MOVE_SPEED_MAX;

			float elapsed = (float) pCore.appTime().elapsedTimeMilli();

			// Apply
			float lCurX = mGameCamera.getPosition().x;
			float lCurY = mGameCamera.getPosition().y;

			mGameCamera.setPosition(lCurX + mVelocity.x * elapsed, lCurY + mVelocity.y * elapsed);

		}

		// DRAG
		mVelocity.x *= 0.917f;
		mVelocity.y *= 0.917f;

		// There are minimums for the camera

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void setFollowEntity(WorldEntity pFollowEntity) {
		mTrackedEntity = pFollowEntity;
	}

}