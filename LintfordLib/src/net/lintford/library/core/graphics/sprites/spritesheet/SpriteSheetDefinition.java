package net.lintford.library.core.graphics.sprites.spritesheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.sprites.SpriteDefinition;
import net.lintford.library.core.graphics.sprites.SpriteFrame;
import net.lintford.library.core.graphics.sprites.SpriteInstance;
import net.lintford.library.core.graphics.textures.Texture;

/** A {@link SpriteSheetDefinition} contains a collecetion of {@link SpriteFrame}s (which each define a source rectangle) and an associated {@link Texture} instance. */
public class SpriteSheetDefinition {

	// --------------------------------------
	// Variables
	// --------------------------------------

	/** Textures referenced within this sheetsheet need to be referenced with the correct entity group Id. */
	private transient int mEntityGroupID;

	/** The unique name given to this {@link SpriteSheetDefinition}. */
	public String spriteSheetName;

	/** The name of the {@link Texture} associated to this {@link SpriteSheetDefinition} */
	protected String textureName;

	/** The {@link Texture} instance associated with this {@link SpriteSheetDefinition} */
	private transient Texture texture;

	/** The filename of the {@link Texture} associated to this {@link SpriteSheetDefinition} */
	protected String textureFilename;
	protected boolean reloadable;
	protected String spriteSheetFilename;

	/**
	 * In order to detect changes to the SpriteSheet when trying to reload textures, we will store the file size of the texture each time it is loaded.
	 */
	private long mFileSizeOnLoad;

	/** A collection of {@link ISprite} instances contained within this {@link SpriteSheetDefinition} */
	protected Map<String, SpriteFrame> frameMap;
	protected Map<String, SpriteDefinition> spriteMap;
	protected List<SpriteInstance> spriteInstancePool;

	/** The width of the associated texture. */
	public transient float textureWidth;

	/** The height of the associated texture. */
	public transient float textureHeight;

	/** Which nodes should this spritesheet be tied to if asssociated with a SpriteGraphInstance */
	public String spriteGraphNodeName;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public long fileSizeOnLoad() {
		return mFileSizeOnLoad;
	}

	public void fileSizeOnLoad(long v) {
		mFileSizeOnLoad = v;
	}

	/** Returns true if this {@link SpriteSheetDefinition}'s GL resources have been laoded, false otherwise. */
	public boolean isLoaded() {
		return texture != null;
	}

	/** Returns the texture loaded for this {@link SpriteSheetDefinition}. */
	public Texture texture() {
		return texture;
	}

	/** Returns the number of {@link SpriteFrame}s assigned to the {@link SpriteSheetDefinition}. */
	public int getSpriteCount() {
		return spriteMap.size();
	}

	public boolean reloadable() {
		return reloadable;
	}

	public void reloadable(boolean pNewValue) {
		reloadable = pNewValue;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public SpriteSheetDefinition() {
		frameMap = new HashMap<>();
		spriteMap = new HashMap<>();
		spriteInstancePool = new ArrayList<>();

	}

	/** Creates a new instance of {@link SpriteSheetDefinition} as assigns it the given name. */
	public SpriteSheetDefinition(final String pSpriteSheetName) {
		this();
		spriteSheetName = pSpriteSheetName;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void loadGLContent(ResourceManager pResourceManager) {
		loadGLContent(pResourceManager, mEntityGroupID);

	}

	/** Loads the associated texture. */
	public void loadGLContent(ResourceManager pResourceManager, int pEntityGroupID) {
		// All SpriteSheets require a valid texture
		if (textureName == null || textureName.length() == 0 || textureFilename == null || textureFilename.length() == 0) {
			System.err.println("SpriteSheet texture name and filename cannot be null!");
			return;

		}

		mEntityGroupID = pEntityGroupID;

		// If the texture has already been loaded, the TextureManager will return the texture instance so we can store it in this SpriteSheet instance.
		texture = pResourceManager.textureManager().loadTexture(textureName, textureFilename, mEntityGroupID);

		// Check that the texture was loaded correctly.
		if (texture == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), String.format("Cannot load SpriteSheetDef '%s' texture %s for EntityGroupID %d.", spriteSheetName, textureName, pEntityGroupID));
			return;

		}

		textureWidth = texture.getTextureWidth();
		textureHeight = texture.getTextureHeight();

		if (frameMap == null) {
			frameMap = new HashMap<>();
		}

		if (spriteInstancePool == null) {
			spriteInstancePool = new ArrayList<>();
		}

		if (spriteMap == null) {
			spriteMap = new HashMap<>();

		} else {
			// If the SpriteSheet definition had animations, then interate them
			// Resolve the Sprite references in the Animations
			for (Map.Entry<String, SpriteDefinition> entry : spriteMap.entrySet()) {
				final var lSpriteDefinition = entry.getValue();
				lSpriteDefinition.name = entry.getKey();
				lSpriteDefinition.loadContent(this);

			}

		}

		// Finally, create a single SpriteDefinition for each SpriteFrame, in case there isn't one already
		for (Entry<String, SpriteFrame> entry : frameMap.entrySet()) {
			if (spriteMap.containsKey(entry.getKey())) {
				continue;
			}

			SpriteDefinition lNewSprite = new SpriteDefinition();
			lNewSprite.name = entry.getKey();
			lNewSprite.addFrame(entry.getValue());

			spriteMap.put(entry.getKey(), lNewSprite);

		}

	}

	/** unloads the GL Content created by this SpriteSheet. */
	public void unloadGLContent() {
		texture = null;
		textureWidth = -1;
		textureHeight = -1;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	/** Adds a new sprite definition to this SpriteSheet. */
	public void addSpriteDefinition(final String pNewName, final SpriteDefinition pNewSprite) {
		spriteMap.put(pNewName, pNewSprite);

	}

	public SpriteDefinition getSpriteDefinition(final String pSpriteName) {
		return spriteMap.get(pSpriteName);

	}

	/** Returns a new {@link SpriteInstance} based on the {@link ISpriteDefinition} of the name provided. Null is returned if the {@link SpriteSheetDefinition} doesn*t contains a Sprite instance of the given name. */
	public SpriteInstance getSpriteInstance(final String pSpriteName) {
		if (spriteMap.containsKey(pSpriteName)) {
			return getSpriteInstance(spriteMap.get(pSpriteName));

		}

		return null;

	}

	/** Returns a new {@link SpriteInstance} based on the {@link ISpriteDefinition} provided. Null is returned if the {@link SpriteSheetDefinition} doesn*t contains a Sprite instance of the given name. */
	public SpriteInstance getSpriteInstance(final SpriteDefinition pSpriteDefinition) {
		if (pSpriteDefinition == null) {
			return null;

		}

		final var lReturnSpriteInstance = getFreeInstance();
		lReturnSpriteInstance.init(pSpriteDefinition);

		return lReturnSpriteInstance;

	}

	public SpriteFrame getSpriteFrame(final String pFrameName) {
		return frameMap.get(pFrameName);

	}

	public void releaseInistance(SpriteInstance pInstance) {
		if (pInstance == null)
			return;

		pInstance.kill();

		if (!spriteInstancePool.contains(pInstance)) {
			spriteInstancePool.add(pInstance);

		}

	}

	private SpriteInstance getFreeInstance() {
		SpriteInstance lReturnInstance = null;

		if (spriteInstancePool == null)
			spriteInstancePool = new ArrayList<>();

		final int POOL_SIZE = spriteInstancePool.size();
		for (int i = 0; i < POOL_SIZE; i++) {
			SpriteInstance lSprite = spriteInstancePool.get(i);

			if (lSprite.isFree()) {
				lReturnInstance = lSprite;
				break;

			}

		}

		if (lReturnInstance != null) {
			spriteInstancePool.remove(lReturnInstance);
			return lReturnInstance;

		}

		return extendInstancePool(4);

	}

	private SpriteInstance extendInstancePool(int pAmt) {
		for (int i = 0; i < pAmt; i++) {
			spriteInstancePool.add(new SpriteInstance());

		}

		return new SpriteInstance();

	}

}