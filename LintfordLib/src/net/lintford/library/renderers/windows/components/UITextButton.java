package net.lintford.library.renderers.windows.components;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatchPCT;
import net.lintford.library.renderers.windows.UiWindow;
import net.lintford.library.screenmanager.entries.EntryInteractions;

public class UITextButton extends UIWidget {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -532019333230394347L;

	private static final String NO_LABEL_TEXT = "unlabled";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private EntryInteractions mCallback;
	private int mClickID;
	private String mButtonLabel;
	private boolean mHoveredOver;
	private boolean mIsClicked;
	private float mClickTimer;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public String buttonLabel() {
		return mButtonLabel;
	}

	public void buttonLabel(final String pNewLabel) {
		mButtonLabel = pNewLabel;
	}

	public int buttonListenerID() {
		return mClickID;
	}

	public void buttonListenerID(final int pNewLabel) {
		mClickID = pNewLabel;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public UITextButton(final UiWindow pParentWindow) {
		this(pParentWindow, 0);
	}

	public UITextButton(final UiWindow pParentWindow, final int pClickID) {
		super(pParentWindow);

		mClickID = pClickID;

		mButtonLabel = NO_LABEL_TEXT;
		w = 200;
		h = 25;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (!mIsClicked && intersectsAA(pCore.HUD().getMouseCameraSpace()) && pCore.input().mouse().isMouseOverThisComponent(hashCode())) {
			mHoveredOver = true;

			if (pCore.input().mouse().tryAcquireMouseLeftClickTimed(hashCode(), this)) {
				mIsClicked = true;
				final float MINIMUM_CLICK_TIMER = 200;
				// Callback to the listener and pass our ID
				if (mCallback != null && mClickTimer > MINIMUM_CLICK_TIMER) {
					mClickTimer = 0;
					mCallback.menuEntryOnClick(pCore.input(), mClickID);
					return true;
				}

			}

		} else {
			mHoveredOver = false;
		}

		if (!pCore.input().mouse().isMouseLeftButtonDown()) {
			mIsClicked = false;

		}

		return false;

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mClickTimer += pCore.appTime().elapsedTimeMilli();

	}

	@Override
	public void draw(LintfordCore pCore, TextureBatchPCT pTextureBatch, Texture pUITexture, FontUnit pTextFont, float pComponentZDepth) {
		final float lColorMod = mHoveredOver ? .3f : 1.f;
		final var lColor = ColorConstants.getColorWithRGBMod(ColorConstants.PrimaryColor, lColorMod);

		// Draw the button background
		pTextureBatch.draw(pUITexture, 0, 0, 32, 32, x, y, 32, 32, pComponentZDepth, lColor);

		final var lFontRenderer = mParentWindow.rendererManager().textFont();

		final var lButtonText = mButtonLabel != null ? mButtonLabel : NO_LABEL_TEXT;
		final float lTextWidth = lFontRenderer.bitmap().getStringWidth(lButtonText);

		lFontRenderer.draw(lButtonText, x + w / 2f - lTextWidth / 2f, y + h / 2f - lFontRenderer.bitmap().fontHeight() / 4f, pComponentZDepth, 1f);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void setClickListener(final EntryInteractions pCallbackObject) {
		mCallback = pCallbackObject;
	}

	public void removeClickListener(final EntryInteractions pCallbackObject) {
		mCallback = null;
	}

}
