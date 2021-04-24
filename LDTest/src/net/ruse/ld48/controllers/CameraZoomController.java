package net.ruse.ld48.controllers;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.ICamera;
import net.ruse.ld48.GameConstants;

public class CameraZoomController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Camera Zoom Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ICamera mGameCamera;

	private final int mTargetPixelsWide = 15 * GameConstants.BLOCK_SIZE;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CameraZoomController(ControllerManager pControllerManager, ICamera pGameCamera, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mGameCamera = pGameCamera;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return false;
	}

	@Override
	public void initialize(LintfordCore pCore) {

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final float lCameraWidth = mGameCamera.windowWidth();
		final float lTargetZoomFactor = lCameraWidth / mTargetPixelsWide;

		mGameCamera.setZoomFactor(lTargetZoomFactor);

	}

}
