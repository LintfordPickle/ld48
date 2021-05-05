package net.lintford.library.core.box2d.definition;

import org.jbox2d.dynamics.FixtureDef;

import net.lintford.library.core.box2d.instance.ShapeInstance;

public class Box2dFixtureDefinition {

	// --------------------------------------
	// Variables
	// --------------------------------------

	public String name;
	public FixtureDef fixtureDef = new FixtureDef();

	// Fixture data is stored directly in the FixtureDef instance above (including the shape)
	public ShapeInstance shape;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public Box2dFixtureDefinition() {

	}

}
