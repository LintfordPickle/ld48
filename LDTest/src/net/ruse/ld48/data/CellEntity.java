package net.ruse.ld48.data;

import net.lintford.library.core.entity.WorldEntity;

public abstract class CellEntity extends WorldEntity {

	private static final long serialVersionUID = 8037755641541119234L;

	public int cellX;
	public int cellY;

	public float fractionX;
	public float fractionY;

	public float velocityX;
	public float velocityY;

	public CellEntity(int pPoolUid) {
		super(pPoolUid);
	}

}