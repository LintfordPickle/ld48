package net.lintford.library.core.graphics.fonts;

import net.lintford.library.core.graphics.Color;
import net.lintford.library.core.graphics.ColorConstants;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatchPCT;

// TODO: Need to implement the spritebatch like TileSetRendererVBO, i.e. with separate shaders and 
public class PixelFontSpriteBatch extends TextureBatchPCT {

	// --------------------------------------
	// Constants
	// --------------------------------------

	// TODO(John): Get rid of this
	protected String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!\"§$%&/()=?<>,.-_*+[]{}#äöü~^°:@€| ";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private float mCharImageSize = 16; // The dimension of each character
	private Texture mTexture;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public void setImageCharacterSequence(String pValue) {
		CHARACTERS = pValue;
	}

	public String getImageCharacterSequence() {
		return CHARACTERS;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public PixelFontSpriteBatch(Texture pTexture) {
		super();

		mTexture = pTexture;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void draw(String pText, float pX, float pY, float pScale) {
		draw(pText, pX, pY, -0.5f, ColorConstants.WHITE, pScale);

	}

	// TODO(John): Need to do both local scaling and center text scaling.
	public void draw(String pText, float pX, float pY, float pZ, Color pTint, float pScale) {
		float lPosX = pX;
		float lPosY = pY;

		for (int i = 0; i < pText.length(); i++) {
			int ci = CHARACTERS.indexOf(pText.charAt(i));
			int xx = ci % 16;
			int yy = ci / 16;

			float u = xx * mCharImageSize;
			float v = yy * mCharImageSize;

			draw(mTexture, u, v, mCharImageSize, mCharImageSize, lPosX, lPosY, mCharImageSize, mCharImageSize, pZ, pTint);
			lPosX += mCharImageSize * pScale;
		}

	}

}
