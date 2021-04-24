package net.ruse.ld48.data;

import java.util.ArrayList;
import java.util.List;

import net.lintford.library.core.entity.instances.PoolInstanceManager;

public class MobManager extends PoolInstanceManager<MobInstance> {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = 5752884544640603349L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private int mPoolUidCounter = 0;
	private final List<MobInstance> mMobInstances = new ArrayList<>();

	// --------------------------------------
	// Properties
	// --------------------------------------

	public List<MobInstance> mobInstances() {
		return mMobInstances;

	}

	// --------------------------------------
	// Constructor 
	// --------------------------------------

	public MobManager() {

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	public void addMobInstance(MobInstance pMobToAdd) {
		if (!mMobInstances.contains(pMobToAdd)) {
			mMobInstances.add(pMobToAdd);

		}

	}

	@Override
	protected MobInstance createPoolObjectInstance() {
		return new MobInstance(mPoolUidCounter++);

	}
}
