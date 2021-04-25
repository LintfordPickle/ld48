package net.ruse.ld48.data;

import java.util.Arrays;

import net.lintford.library.core.entity.BaseInstanceData;
import net.lintford.library.core.maths.RandomNumbers;
import net.ruse.ld48.GameConstants;

public class Level extends BaseInstanceData {

	public static final int LEVEL_TILE_COORD_INVALID = -1;

	public static final int LEVEL_TILE_INDEX_AIR = 0;
	public static final int LEVEL_TILE_INDEX_DIRT = 1;
	public static final int LEVEL_TILE_INDEX_STONE = 2;
	public static final int LEVEL_TILE_INDEX_ENTRY = 3;
	public static final int LEVEL_TILE_INDEX_EXIT = 4;

	public static final int LEVEL_TILE_INDEX_GOLD = 6;
	public static final int LEVEL_TILE_INDEX_SOLID = 7;

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = -8298580465691367470L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private final int[] mLevelBlockIndices = new int[GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH];
	private final byte[] mLevelBlockHealth = new byte[GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH];

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public int[] levelBlocks() {
		return mLevelBlockIndices;
	}

	public int getLevelBlockType(int pLevelTileCoord) {
		return mLevelBlockIndices[pLevelTileCoord];
	}

	public int getLevelBlockType(int pTileX, int pTileY) {
		final int lTileCoord = getLevelTileCoord(pTileX, pTileY);
		if (lTileCoord == LEVEL_TILE_COORD_INVALID)
			return LEVEL_TILE_COORD_INVALID;

		return mLevelBlockIndices[lTileCoord];
	}

	public int getLevelTileCoord(int pTileX, int pTileY) {
		final int lTileCoord = pTileY * GameConstants.LEVEL_TILES_WIDE + pTileX;
		if (lTileCoord < 0 || lTileCoord >= GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH)
			return LEVEL_TILE_COORD_INVALID;

		return lTileCoord;

	}

	public int getTopBlockIndex(int pTileIndex) {
		if (pTileIndex < GameConstants.LEVEL_TILES_WIDE)
			return LEVEL_TILE_COORD_INVALID;

		return pTileIndex - GameConstants.LEVEL_TILES_WIDE;
	}

	public int getBottomBlockIndex(int pTileIndex) {
		if (pTileIndex > GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH - GameConstants.LEVEL_TILES_WIDE)
			return LEVEL_TILE_COORD_INVALID;

		return pTileIndex + GameConstants.LEVEL_TILES_WIDE;
	}

	public int getLeftBlockIndex(int pTileIndex) {
		if (pTileIndex % GameConstants.LEVEL_TILES_WIDE == 0)
			return LEVEL_TILE_COORD_INVALID;

		return pTileIndex - 1;
	}

	public int getRightBlockIndex(int pTileIndex) {
		if (pTileIndex % GameConstants.LEVEL_TILES_WIDE == GameConstants.LEVEL_TILES_WIDE - 1)
			return LEVEL_TILE_COORD_INVALID;

		return pTileIndex + 1;
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
			{
				final int lTileCoord = getLevelTileCoord(x, lFloorHeight);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;

			}

			{
				final int lTileCoord = getLevelTileCoord(x, GameConstants.LEVEL_TILES_HIGH - 1);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
			}

		}

		for (int y = 0; y < GameConstants.LEVEL_TILES_HIGH; y++) {
			{
				final int lTileCoord = getLevelTileCoord(0, y);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
			}

			{
				final int lTileCoord = getLevelTileCoord(GameConstants.LEVEL_TILES_WIDE - 1, y);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
			}

		}

		// Random blocks
		final int lNumRandomBlocks = (int) (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH * 0.4);
		for (int i = 0; i < lNumRandomBlocks; i++) {
			final int lTileCoord = RandomNumbers.random(lFloorHeight * GameConstants.LEVEL_TILES_WIDE, (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1);

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;

		}

		// gold blocks
		final int lNumGoldBlocks = 600 / 10;
		for (int i = 0; i < lNumGoldBlocks; i++) {
			final int lTileCoord = RandomNumbers.random((lFloorHeight + 1) * GameConstants.LEVEL_TILES_WIDE, (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1);

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_GOLD;

		}

	}

	private void clearLevel() {
		Arrays.fill(mLevelBlockHealth, (byte) 5);
		Arrays.fill(mLevelBlockIndices, LEVEL_TILE_INDEX_AIR);

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

	public boolean digBlock(int pTileX, int pTileY, byte pDamageAmount) {
		// first check block is present
		final int lTileIndex = getLevelTileCoord(pTileX, pTileY);
		if (lTileIndex == LEVEL_TILE_COORD_INVALID)
			return false;

		boolean wasBlockRemoved = false;

		// then deduct damage
		byte lBlockHealth = mLevelBlockHealth[lTileIndex];

		lBlockHealth -= pDamageAmount;
		if (lBlockHealth < 0) {
			lBlockHealth = 0;
			mLevelBlockIndices[lTileIndex] = LEVEL_TILE_INDEX_AIR;
			wasBlockRemoved = true;

		}

		mLevelBlockHealth[lTileIndex] = lBlockHealth;

		return wasBlockRemoved;

	}

	public byte getBlockHealth(int pTileX, int pTileY) {
		final int lTileCoord = getLevelTileCoord(pTileX, pTileY);
		if (lTileCoord == LEVEL_TILE_COORD_INVALID)
			return (byte) 255;

		return mLevelBlockHealth[lTileCoord];
	}

	public boolean placeBlock() {
		return false;
	}

}
