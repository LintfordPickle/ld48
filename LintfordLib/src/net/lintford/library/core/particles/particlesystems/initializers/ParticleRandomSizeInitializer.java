package net.lintford.library.core.particles.particlesystems.initializers;

import net.lintford.library.core.particles.Particle;

public class ParticleRandomSizeInitializer extends ParticleSingleValueInitializer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -2507259383291058254L;

	public static final String INITIALIZER_NAME = "ParticleRandomSizeInitializer";

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public ParticleRandomSizeInitializer() {
		super(INITIALIZER_NAME);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	public void onIntialiseParticle(Particle pParticle, float pValue) {
		pParticle.scale = pValue;

	}

}
