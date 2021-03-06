package net.lintford.library.screenmanager;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.controllers.hud.UiStructureController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.debug.GLDebug;
import net.lintford.library.options.IResizeListener;
import net.lintford.library.screenmanager.Screen.ScreenState;
import net.lintford.library.screenmanager.toast.ToastManager;

public class ScreenManager {

	// --------------------------------------
	// Variables
	// --------------------------------------

	private LintfordCore mLWJGLCore;
	private List<Screen> mScreens;
	private List<Screen> mScreensToUpdate;
	private ToolTip mToolTip;
	private ResourceManager mResourceManager;
	private AudioFireAndForgetManager mUISoundManager;
	private ToastManager mToastManager;
	private boolean mIsinitialized;
	private boolean mIsLoaded;
	private int mScreenUIDCounter;
	private UiStructureController mUiStructureController;
	private IResizeListener mResizeListener;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public ToastManager toastManager() {
		return mToastManager;
	}

	public UiStructureController UiStructureController() {
		return mUiStructureController;
	}

	public AudioFireAndForgetManager uiSounds() {
		return mUISoundManager;
	}

	public ResourceManager resources() {
		return mResourceManager;
	}

	public ToolTip toolTip() {
		return mToolTip;
	}

	public List<Screen> screens() {
		return mScreens;
	}

	public LintfordCore core() {
		return mLWJGLCore;
	}

	/** Returns a new ID for a screen. */
	public int getNewUUID() {
		return mScreenUIDCounter++;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ScreenManager(LintfordCore pCore) {
		mLWJGLCore = pCore;
		mScreenUIDCounter = 100;

		mToastManager = new ToastManager();
		mScreens = new ArrayList<>();
		mScreensToUpdate = new ArrayList<>();

		mToolTip = new ToolTip();

		mIsinitialized = false;
		mIsLoaded = false;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void initialize() {
		final int lCount = mScreens.size();
		for (int i = 0; i < lCount; i++) {
			mScreens.get(i).initialize();
		}

		mUiStructureController = (UiStructureController) mLWJGLCore.controllerManager().getControllerByNameRequired(UiStructureController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);

		mIsinitialized = true;

	}

	public void loadGLContent(final ResourceManager pResourceManager) {
		mResourceManager = pResourceManager;

		int lCount = mScreens.size();
		for (int i = 0; i < lCount; i++) {
			mScreens.get(i).loadGLContent(pResourceManager);
		}

		mUISoundManager = new AudioFireAndForgetManager(pResourceManager.audioManager());
		mUISoundManager.acquireAudioSources(2);

		mToolTip.loadGLContent(pResourceManager);
		mToastManager.loadGLContent(pResourceManager);

		// Add a viewport listener so the screenmanager screens can react to changes in window size
		mResizeListener = new IResizeListener() {

			@Override
			public void onResize(final int pWidth, final int pHeight) {
				onViewportChanged(pWidth, pHeight);

			}

		};

		core().config().display().addResizeListener(mResizeListener);

		Debug.debugManager().logger().i(getClass().getSimpleName(), "Finished loadingGLContent");
		GLDebug.checkGLErrorsException(getClass().getSimpleName());

		mIsLoaded = true;
	}

	public void unloadGLContent() {
		int lCount = mScreens.size();
		for (int i = 0; i < lCount; i++) {

			mScreens.get(i).unloadGLContent();

		}

		mToolTip.unloadGLContent();
		mToastManager.unloadGLContent();

		Debug.debugManager().logger().i(getClass().getSimpleName(), "Finished ScreenManager.unloadGLContent");
		GLDebug.checkGLErrorsException(getClass().getSimpleName());

		mIsLoaded = false;

	}

	public void handleInput(LintfordCore pCore) {
		if (mScreens == null || mScreens.size() == 0)
			return;

		// Top-Most screen processes input
		boolean lInputBlockedByHigherScreen = false;

		final int lScreenCount = mScreens.size() - 1;
		for (int i = lScreenCount; i >= 0; i--) {
			final var lScreen = mScreens.get(i);

			if (lInputBlockedByHigherScreen) {
				pCore.input().mouse().tryAcquireMouseMiddle(hashCode());

			}

			lScreen.acceptMouseInput = !lInputBlockedByHigherScreen;
			lScreen.acceptKeyboardInput = !lInputBlockedByHigherScreen;

			if (!lInputBlockedByHigherScreen && (lScreen.screenState() == ScreenState.TransitionOn || lScreen.screenState() == ScreenState.Active || lScreen.mShowInBackground)) {
				lScreen.handleInput(pCore);

			}

			lInputBlockedByHigherScreen = lInputBlockedByHigherScreen || lScreen.mBlockInputInBackground;

		}

		mToolTip.handleInput(pCore);

	}

	public void update(LintfordCore pCore) {
		if (!mIsinitialized || !mIsLoaded)
			return;

		boolean lOtherScreenHasFocus = false;
		boolean lCoveredByOtherScreen = false;

		mScreensToUpdate.clear();

		int lCount = mScreens.size();
		for (int i = 0; i < lCount; i++) {
			mScreensToUpdate.add(mScreens.get(i));

		}

		while (mScreensToUpdate.size() > 0) {
			Screen lScreen = mScreensToUpdate.get(mScreensToUpdate.size() - 1);

			mScreensToUpdate.remove(mScreensToUpdate.size() - 1);

			// Update the screen
			lScreen.update(pCore, lOtherScreenHasFocus, lCoveredByOtherScreen);

			if (lScreen.screenState() == ScreenState.TransitionOn || lScreen.screenState() == ScreenState.Active) {
				lOtherScreenHasFocus = true;

				if (!lScreen.isPopup()) {
					lCoveredByOtherScreen = true;

				}

			}

		}

		mToastManager.update(pCore);
		mToolTip.update(pCore);

	}

	public void draw(LintfordCore pCore) {
		if (!mIsinitialized || !mIsLoaded)
			return;

		int lCount = mScreens.size();
		for (int i = 0; i < lCount; i++) {
			if (mScreens.get(i).screenState() == ScreenState.Hidden && !mScreens.get(i).showInBackground())
				continue;

			mScreens.get(i).draw(pCore);

			GLDebug.checkGLErrorsException(getClass().getSimpleName());

		}

		mToastManager.draw(pCore);
		mToolTip.draw(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addScreen(Screen pScreen) {
		if (pScreen.singletonScreen()) {
			final int lScreenCount = mScreens.size();
			final var pNewScreenSImpleName = pScreen.getClass().getSimpleName();
			for (int i = 0; i < lScreenCount; i++) {
				final var lScreen = mScreens.get(i);
				if (lScreen.getClass().getSimpleName().equals(pNewScreenSImpleName)) {
					Debug.debugManager().logger().e(this.getClass().getSimpleName(), "Cannot add second SingletonScreen instance: " + pNewScreenSImpleName);
					return;

				}

			}

		}

		if (!pScreen.isLoaded()) {
			pScreen.isExiting(false);

			if (mIsinitialized && !pScreen.isinitialized()) {// screen manager already initialized? then load this screen manually
				pScreen.initialize();
			}

			if (mIsLoaded) { // screen manager already loaded? then load this screen manually
				pScreen.loadGLContent(mResourceManager);
			}

		}

		int lInsertIndex = 0;
		if (mScreens.size() > 0) {
			mScreens.get(mScreens.size() - 1).onLostFocus();

			lInsertIndex = mScreens.size();
		}
		for (int i = mScreens.size() - 1; i > 0; i--) {
			lInsertIndex = i + 1;
			if (!mScreens.get(i).alwaysOnTop()) {
				break;
			}
		}

		mScreens.add(lInsertIndex, pScreen);

		Debug.debugManager().logger().i(getClass().getSimpleName(), String.format("Added screen '%s'", pScreen.getClass().getSimpleName()));

	}

	public void removeScreen(Screen pScreen) {

		if (mIsinitialized) {
			pScreen.unloadGLContent();

		}

		pScreen.onScreenRemovedFromScreenManager();

		if (mScreens.contains(pScreen)) {
			// if this screen was the top screen, then the screen below gains focus
			if (mScreens.size() > 1 && mScreens.get(mScreens.size() - 1) == pScreen) {
				mScreens.get(mScreens.size() - 2).onGainedFocus();
			}

			mScreens.remove(pScreen);

		}

		if (mScreensToUpdate.contains(pScreen))
			mScreensToUpdate.remove(pScreen);

		Debug.debugManager().logger().i(getClass().getSimpleName(), String.format("Removed screen '%s'", pScreen.getClass().getSimpleName()));

	}

	public void fadeBackBufferToBlack(float pAlpha) {

		// TODO: Render a full screen black quad ...

	}

	public void exitGame() {
		mLWJGLCore.closeApp();

	}

	public void onViewportChanged(float pWidth, float pHeight) {
		final int lScreenCount = mScreens.size();
		for (int i = 0; i < lScreenCount; i++) {
			mScreens.get(i).onViewportChange(pWidth, pHeight);

		}

	}

}