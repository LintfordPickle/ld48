package net.ruse.ld48.data;

import net.lintford.library.core.entity.BaseInstanceData;
import net.ruse.ld48.GameConstants;

public class Level extends BaseInstanceData {

	public static final int LEVEL_TILE_COORD_INVALID = -1;

	public static final int LEVEL_TILE_INDEX_AIR = 0;
	public static final int LEVEL_TILE_INDEX_DIRT = 1;
	public static final int LEVEL_TILE_INDEX_DIRT_TOP = 5;
	public static final int LEVEL_TILE_INDEX_STONE = 2;
	public static final int LEVEL_TILE_INDEX_ENTRY = 3;
	public static final int LEVEL_TILE_INDEX_EXIT = 4;

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -8298580465691367470L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private final int[] mLevelBlockIndices = new int[GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH];

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int[] levelBlocks() {
		return mLevelBlockIndices;
	}

	public int getLevelBlockType(int pTileX, int pTileY) {
		final int lTileCoord = getLevelTileCoord(pTileX, pTileY);
		if (lTileCoord == LEVEL_TILE_COORD_INVALID)
			return LEVEL_TILE_COORD_INVALID;

		return mLevelBlockIndices[lTileCoord];
	}

	public int getLevelTileCoord(int pTileX, int pTileY) {
		return pTileY * GameConstants.LEVEL_TILES_WIDE + pTileX;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Level() {
		clearLevel();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void loadLevel() {
		createTestLevel();

	}

	private void createTestLevel() {
		clearLevel();

		final int lFloorHeight = 3;
		for (int x = 1; x < GameConstants.LEVEL_TILES_WIDE; x++) {
			final int lTileCoord = getLevelTileCoord(x, lFloorHeight);

			if (lTileCoord == LEVEL_TILE_COORD_INVALID)
				continue;

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT_TOP;
			mLevelBlockIndices[lTileCoord + GameConstants.LEVEL_TILES_WIDE] = LEVEL_TILE_INDEX_DIRT;

		}

	}

	private void clearLevel() {
		for (int y = 0; y < GameConstants.LEVEL_TILES_HIGH; y++) {
			for (int x = 0; x < GameConstants.LEVEL_TILES_WIDE; x++) {
				final int lTileCoord = getLevelTileCoord(x, y);

				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;

				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_AIR;

			}
		}
	}

	public boolean hasCollision(int pTileX, int pTileY) {
		if (pTileX < 0 || pTileY < 0)
			return true;
		if (pTileX >= GameConstants.LEVEL_TILES_WIDE || pTileY >= GameConstants.LEVEL_TILES_HIGH)
			return true;

		final int lTileIndex = getLevelTileCoord(pTileX, pTileY);
		if (lTileIndex == LEVEL_TILE_COORD_INVALID)
			return true;

		return mLevelBlockIndices[lTileIndex] > LEVEL_TILE_INDEX_AIR;

	}

}
