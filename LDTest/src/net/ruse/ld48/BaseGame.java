package net.ruse.ld48;

import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

import org.lwjgl.opengl.GL11;

import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatchPCT;
import net.lintford.library.screenmanager.ScreenManager;
import net.ruse.ld48.screens.GameScreen;

public class BaseGame extends LintfordCore {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ScreenManager mScreenManager;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ScreenManager screenManager() {
		return mScreenManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseGame(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs, false);

		mIsFixedTimeStep = true;

		mScreenManager = new ScreenManager(this);

		Debug.debugManager().logger().mirrorLogToConsole(true);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void showStartUpLogo(long pWindowHandle) {
		// Show a mini-splash screen
		Texture lTexture = mResourceManager.textureManager().loadTexture("TEXTURE_SPLASH", "res/textures/textureSplash.png", LintfordCore.CORE_ENTITY_GROUP_ID);

		GL11.glClearColor(0f, 0f, 0f, 1f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		TextureBatchPCT lTB = new TextureBatchPCT();
		lTB.loadGLContent(mResourceManager);
		lTB.begin(mHUD);
		lTB.draw(lTexture, 0, 0, 640, 480, -320, -240, 640, 480, -0.1f, ColorConstants.WHITE);
		lTB.end();

		glfwSwapBuffers(pWindowHandle);

	}

	@Override
	protected void onInitializeApp() {
		super.onInitializeApp();

		mScreenManager.addScreen(new GameScreen(mScreenManager));

		mScreenManager.initialize();

	}

	@Override
	protected void oninitializeGL() {
		super.oninitializeGL();

	}

	@Override
	protected void onLoadGLContent() {
		super.onLoadGLContent();

		mScreenManager.loadGLContent(mResourceManager);

	}

	@Override
	protected void onUnloadGLContent() {
		super.onUnloadGLContent();

		mScreenManager.unloadGLContent();

	}

	@Override
	protected void onHandleInput() {
		super.onHandleInput();

		mScreenManager.handleInput(this);

	}

	@Override
	protected void onUpdate() {
		super.onUpdate();

		mScreenManager.update(this);

	}

	@Override
	protected void onDraw() {
		super.onDraw();

		mScreenManager.draw(this);

	}

	// ---------------------------------------------
	// Entry Point
	// ---------------------------------------------

	public static void main(String[] pArgs) {
		final var lGameInfo = new GameInfo() {
			@Override
			public int baseGameResolutionWidth() {
				return GameConstants.WINDOW_WIDTH;
			}

			@Override
			public int baseGameResolutionHeight() {
				return GameConstants.WINDOW_HEIGHT;
			}

			@Override
			public int minimumWindowWidth() {
				return GameConstants.WINDOW_WIDTH;
			}

			@Override
			public int minimumWindowHeight() {
				return GameConstants.WINDOW_HEIGHT;
			}

			@Override
			public boolean stretchGameResolution() {
				return false;
			}

			@Override
			public boolean windowResizeable() {
				return true;
			}

			@Override
			public String windowTitle() {
				return GameConstants.WINDOW_TITLE;
			}
		};

		final var lBaseGame = new BaseGame(lGameInfo, pArgs);
		lBaseGame.createWindow();
	}

}
