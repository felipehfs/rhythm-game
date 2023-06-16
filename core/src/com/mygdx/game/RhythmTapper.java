package com.mygdx.game;


public class RhythmTapper extends BaseGame {

	@Override
	public void create() {
		super.create();
		setActiveScreen(new RhythmScreen());
	}
}
