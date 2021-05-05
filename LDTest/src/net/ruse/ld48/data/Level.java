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
	public static final int LEVEL_TILE_INDEX_HEART = 7;

	public static final byte LEVEL_BLOCK_HEALTH_DIRT = (byte) 2;
	public static final byte LEVEL_BLOCK_HEALTH_GOLD = (byte) 5;
	public static final byte LEVEL_BLOCK_HEALTH_STONE = (byte) 7;
	public static final byte LEVEL_BLOCK_HEALTH_HEART = (byte) 7;

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
		if (pLevelTileCoord < 0 || pLevelTileCoord > (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1)
			return LEVEL_TILE_COORD_INVALID;

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
				mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_DIRT;

			}

			{
				final int lTileCoord = getLevelTileCoord(x, GameConstants.LEVEL_TILES_HIGH - 1);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
				mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_DIRT;

			}

		}

		for (int y = 0; y < GameConstants.LEVEL_TILES_HIGH; y++) {
			{
				final int lTileCoord = getLevelTileCoord(0, y);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
				mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_DIRT;
			}

			{
				final int lTileCoord = getLevelTileCoord(GameConstants.LEVEL_TILES_WIDE - 1, y);
				if (lTileCoord == LEVEL_TILE_COORD_INVALID)
					continue;
				mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
				mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_DIRT;
			}

		}

		// Random blocks
		final int lNumRandomBlocks = (int) (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH * 0.4);
		for (int i = 0; i < lNumRandomBlocks; i++) {
			final int lTileCoord = RandomNumbers.random(lFloorHeight * GameConstants.LEVEL_TILES_WIDE, (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1);

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_DIRT;
			mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_DIRT;

		}

		final int lNumRandomStoneBlocks = (int) (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH * 0.2);
		for (int i = 0; i < lNumRandomStoneBlocks; i++) {
			final int lTileCoord = RandomNumbers.random(lFloorHeight * GameConstants.LEVEL_TILES_WIDE, (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1);

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_STONE;
			mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_STONE;

		}

		// gold blocks
		final int lNumGoldBlocks = 600 / 10;
		for (int i = 0; i < lNumGoldBlocks; i++) {
			final int lTileCoord = RandomNumbers.random((lFloorHeight + 1) * GameConstants.LEVEL_TILES_WIDE, (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1);

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_GOLD;
			mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_GOLD;

		}

		// heart blocks
		final int lNumHeartBlocks = 100; // 20
		for (int i = 0; i < lNumHeartBlocks; i++) {
			final int lTileCoord = RandomNumbers.random((lFloorHeight + 1) * GameConstants.LEVEL_TILES_WIDE, (GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH) - 1);

			mLevelBlockIndices[lTileCoord] = LEVEL_TILE_INDEX_HEART;
			mLevelBlockHealth[lTileCoord] = LEVEL_BLOCK_HEALTH_HEART;

		}

	}

	private void clearLevel() {
		Arrays.fill(mLevelBlockHealth, (byte) 0);
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

	public boolean digBlock(int pTileCoord, byte pDamageAmount) {
		if (pTileCoord < 0 || pTileCoord >= GameConstants.LEVEL_TILES_WIDE * GameConstants.LEVEL_TILES_HIGH)
			return false;

		boolean wasBlockRemoved = false;

		// then deduct damage
		byte lBlockHealth = mLevelBlockHealth[pTileCoord];

		lBlockHealth -= pDamageAmount;
		if (lBlockHealth < 0) {
			lBlockHealth = 0;
			mLevelBlockIndices[pTileCoord] = LEVEL_TILE_INDEX_AIR;
			wasBlockRemoved = true;

		}

		mLevelBlockHealth[pTileCoord] = lBlockHealth;

		return wasBlockRemoved;
	}

	public boolean digBlock(int pTileX, int pTileY, byte pDamageAmount) {
		final int lTileIndex = getLevelTileCoord(pTileX, pTileY);
		if (lTileIndex == LEVEL_TILE_COORD_INVALID)
			return false;

		return digBlock(lTileIndex, pDamageAmount);

	}

	public byte getBlockHealth(int pTileX, int pTileY) {
		final int lTileCoord = getLevelTileCoord(pTileX, pTileY);
		if (lTileCoord == LEVEL_TILE_COORD_INVALID)
			return (byte) 255;

		return mLevelBlockHealth[lTileCoord];
	}

	public boolean placeBlock(int pTileX, int pTileY, int pBlockTypeIndex, byte pBlockHealth) {
		final int lTileCoord = getLevelTileCoord(pTileX, pTileY);
		if (lTileCoord == LEVEL_TILE_COORD_INVALID)
			return false;

		final int lCurrentBlockTypeIndex = getLevelBlockType(lTileCoord);
		if (lCurrentBlockTypeIndex != LEVEL_TILE_INDEX_AIR)
			return false;

		mLevelBlockIndices[lTileCoord] = pBlockTypeIndex;
		mLevelBlockHealth[lTileCoord] = pBlockHealth;

		return true;
	}

}
