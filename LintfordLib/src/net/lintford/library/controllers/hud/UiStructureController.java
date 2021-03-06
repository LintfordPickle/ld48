package net.lintford.library.controllers.hud;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.Rectangle;
import net.lintford.library.options.DisplayManager;

public class UiStructureController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Ui Structure Controller";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private DisplayManager mDisplayManager;

	private Rectangle mGameHUDRectangle;
	private Rectangle mGameHeaderRectangle;
	private Rectangle mGameFooterRectangle;

	private Rectangle mMenuTitleRectangle;
	private Rectangle mMenuMainRectangle;
	private Rectangle mMenuFooterRectangle;

	private boolean mIsinitialized;

	private float mWindowAutoScaleFactorX;
	private float mWindowAutoScaleFactorY;
	private float mUITransparencyFactorActual;
	private float mUIScaleFactorActual;
	private float mUITextScaleFactorActual;

	private float mWindowPaddingH;
	private float mWindowPaddingV;

	// --------------------------------------
	// Properties
	// --------------------------------------

	/** The windowAutoScaleFactorX is the factor between the current window width and the base window width.*/
	public float windowAutoScaleFactorX() {
		return mWindowAutoScaleFactorX;
	}

	/** The windowAutoScaleFactorY is the factor between the current window height and the base window height.*/
	public float windowAutoScaleFactorY() {
		return mWindowAutoScaleFactorY;
	}

	/** Represents the padding space at the edge of the screen (horizontal). */
	public float windowPaddingH() {
		return mWindowPaddingH;
	}

	/** Represents the padding space at the edge of the screen (vertical). */
	public float windowPaddingV() {
		return mWindowPaddingV;
	}

	public float uiTransparencyFactor() {
		return mUITransparencyFactorActual;
	}

	public float uiScaleFactor() {
		return mUIScaleFactorActual;
	}

	public float uiTextScaleFactor() {
		return mUITextScaleFactorActual;
	}

	@Override
	public boolean isInitialized() {
		return mIsinitialized;
	}

	public Rectangle gameHeaderRectangle() {
		return mGameHeaderRectangle;
	}

	public Rectangle gameHUDRectangle() {
		return mGameHUDRectangle;
	}

	public Rectangle gameFooterRectangle() {
		return mGameFooterRectangle;
	}

	public Rectangle menuTitleRectangle() {
		return mMenuTitleRectangle;
	}

	public Rectangle menuMainRectangle() {
		return mMenuMainRectangle;
	}

	public Rectangle menuFooterRectangle() {
		return mMenuFooterRectangle;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UiStructureController(DisplayManager pDisplayManager, ControllerManager pControllerManager, final int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mDisplayManager = pDisplayManager;

		mGameHeaderRectangle = new Rectangle();
		mGameHUDRectangle = new Rectangle();
		mGameFooterRectangle = new Rectangle();

		mMenuTitleRectangle = new Rectangle();
		mMenuMainRectangle = new Rectangle();
		mMenuFooterRectangle = new Rectangle();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void initialize(LintfordCore pCore) {
		updateHUDAreas(pCore);
		updateWindowUiComponentStructures(pCore);

		mIsinitialized = true;

	}

	@Override
	public void unload() {

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final float lBaseWindowWidth = pCore.config().display().baseGameResolutionWidth();
		final float lBaseWindowHeight = pCore.config().display().baseGameResolutionHeight();

		mWindowAutoScaleFactorX = pCore.config().display().windowWidth() / lBaseWindowWidth;
		mWindowAutoScaleFactorY = pCore.config().display().windowHeight() / lBaseWindowHeight;

		updateHUDAreas(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void updateHUDAreas(LintfordCore pCore) {
		updateGameUiStructure(pCore);

		updateMenuUiStructure(pCore);

	}

	private void updateWindowUiComponentStructures(LintfordCore pCore) {
		mUIScaleFactorActual = mDisplayManager.graphicsSettings().UIScale();
		mUITextScaleFactorActual = mDisplayManager.graphicsSettings().UITextScale();
		mUITransparencyFactorActual = mDisplayManager.graphicsSettings().UITransparencyScale();

		mWindowPaddingH = 40;
		mWindowPaddingV = 30;

	}

	private void updateMenuUiStructure(LintfordCore pCore) {
		updateWindowUiComponentStructures(pCore); // FIXME: remove from update when finished

		final float lWindowWidth = pCore.config().display().windowWidth();
		final float lWindowHeight = pCore.config().display().windowHeight();

		final float lVerticalInnerPadding = 1.f * mWindowAutoScaleFactorY;
		final float lModWidth = lWindowWidth - mWindowPaddingH * 2.f;
		final float lModHeight = lWindowHeight - mWindowPaddingV * 2.f;

		float lRemainingHeight = lModHeight;
		final float lMinimumTitleHeight = 250.f * mWindowAutoScaleFactorY;
		final float lMinimumFooterHeight = 100 * mWindowAutoScaleFactorY;
		final float lTitleHeight = (float) Math.max(lMinimumTitleHeight, lModHeight * .15f - lVerticalInnerPadding);
		final float lFooterHeight = (float) Math.max(lMinimumFooterHeight, lModHeight * .10f - lVerticalInnerPadding);
		lRemainingHeight -= lTitleHeight;
		lRemainingHeight -= lFooterHeight;

		final float lMainHeight = lRemainingHeight;

		mMenuTitleRectangle.set(-lModWidth / 2, -lModHeight / 2f, lModWidth, lTitleHeight);
		mMenuMainRectangle.set(-lModWidth / 2, -lModHeight / 2f + lTitleHeight + lVerticalInnerPadding * 2f, lModWidth, lMainHeight);
		mMenuFooterRectangle.set(-lModWidth / 2, lModHeight / 2f - lFooterHeight + lVerticalInnerPadding * 4f, lModWidth, lFooterHeight);

	}

	private void updateGameUiStructure(LintfordCore pCore) {
		final float lWindowWidth = pCore.config().display().windowWidth();
		final float lWindowHeight = pCore.config().display().windowHeight();

		final float lGameHeaderHeight = 96f * windowAutoScaleFactorY();
		final float lGameFooterHeight = 96f * windowAutoScaleFactorY();

		final float lMaxGameHudWidth = 1280.f;
		final float lMinGameHudWidth = 800.f;
		final float lGameHudHorizontalPadding = 50.f;
		final float lGameHudVerticalPadding = 10.f;

		final var lHUDWidth = Math.min(lMaxGameHudWidth, Math.max(lMinGameHudWidth, lWindowWidth)) - lGameHudHorizontalPadding;
		final var lHUDHeight = lWindowHeight - lGameFooterHeight - lGameHeaderHeight - lGameHudVerticalPadding;

		mGameHeaderRectangle.set(-lWindowWidth / 2f, -lWindowHeight / 2, lWindowWidth, lGameHeaderHeight);
		mGameHUDRectangle.set(-lHUDWidth / 2f, mGameHeaderRectangle.y() + mGameHeaderRectangle.h() + lGameHudVerticalPadding * .5f, lHUDWidth, lHUDHeight);
		mGameFooterRectangle.set(-lWindowWidth / 2f, lWindowHeight / 2 - lGameFooterHeight, lWindowWidth, lGameFooterHeight);
	}

}