package net.ruse.ld48.data;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.entity.instances.PoolInstanceManager;

public class ItemManager extends PoolInstanceManager<ItemInstance> {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 4124443376537008615L;

	public static final int ITEM_TYPE_INDEX_INVALID = -1;
	public static final int ITEM_TYPE_INDEX_TNT = 1;
	public static final int ITEM_TYPE_INDEX_TNT_PICKUP = 5;
	public static final int ITEM_TYPE_INDEX_COIN = 2;
	public static final int ITEM_TYPE_INDEX_HEALTH = 3;

	public static final int ITEM_TYPE_INDEX_LEVEL_EXIT = 100;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private int mPoolUidCounter = 0;
	private final List<ItemInstance> itemInstances = new ArrayList<>();

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected ItemInstance createPoolObjectInstance() {
		return new ItemInstance(mPoolUidCounter++);

	}

	public void addItemInstance(ItemInstance pItemInstance) {
		if (!itemInstances.contains(pItemInstance)) {
			itemInstances.add(pItemInstance);

		}

	}

	public void removeItemInstance(ItemInstance pItemToRemove) {
		if (itemInstances.contains(pItemToRemove)) {
			itemInstances.remove(pItemToRemove);

		}

		returnPooledItem(pItemToRemove);
	}

}
