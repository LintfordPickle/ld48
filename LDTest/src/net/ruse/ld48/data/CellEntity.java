package net.ruse.ld48.data;

import net.lintford.library.core.entity.WorldEntity;
import net.ruse.ld48.GameConstants;

public abstract class CellEntity extends WorldEntity {

	private static final long serialVersionUID = 8037755641541119234L;

	public float radius;

	public int cellX;
	public int cellY;

	public float fractionX;
	public float fractionY;

	public float velocityX;
	public float velocityY;

	public CellEntity(int pPoolUid) {
		super(pPoolUid);

		radius = 16.f;

	}

	public void setPosition(float pWorldPositionX, float pWorldPositionY) {

		worldPositionX = pWorldPositionX;
		worldPositionY = pWorldPositionY;

		cellX = (int) (worldPositionX / GameConstants.BLOCK_SIZE);
		cellY = (int) (worldPositionY / GameConstants.BLOCK_SIZE);

		fractionX = (worldPositionX - (cellX * GameConstants.BLOCK_SIZE)) / GameConstants.BLOCK_SIZE;
		fractionY = (worldPositionY - (cellY * GameConstants.BLOCK_SIZE)) / GameConstants.BLOCK_SIZE;

		velocityX = 0.f;
		velocityY = 0.f;

	}

}
