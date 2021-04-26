package net.ruse.ld48.controllers;

import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.audio.AudioFireAndForgetManager;
import net.lintford.library.core.audio.AudioManager;

public class SoundFxController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Sound Fx Controller";

	public static final String SOUND_DIG_DIRT_1 = "SOUND_DIG_DIRT_1";
	public static final String SOUND_DIG_DIRT_2 = "SOUND_DIG_DIRT_2";
	public static final String SOUND_DIG_STONE = "SOUND_DIG_STONE";
	public static final String SOUND_JUMP = "SOUND_JUMP";
	public static final String SOUND_GOLD_1 = "SOUND_GOLD_1";
	public static final String SOUND_GOLD_2 = "SOUND_GOLD_2";
	public static final String SOUND_JUMP_BIG = "SOUND_JUMP_BIG";
	public static final String SOUND_HURT_1 = "SOUND_HURT_1";
	public static final String SOUND_HURT_2 = "SOUND_HURT_2";
	public static final String SOUND_HURT_3 = "SOUND_HURT_3";
	public static final String SOUND_HURT_4 = "SOUND_HURT_4";
	public static final String SOUND_SPIDER_HURT = "SOUND_SPIDER_HURT";
	public static final String SOUND_SWORD = "SOUND_SWORD";
	public static final String SOUND_TnT1 = "SOUND_TnT1";
	public static final String SOUND_TnT2 = "SOUND_TnT2";
	public static final String SOUND_TnT3 = "SOUND_TnT3";
	public static final String SOUND_TnTFuse = "SOUND_TnTFuse";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private AudioFireAndForgetManager mAudioFireAndForgetManager;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mAudioFireAndForgetManager != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public SoundFxController(ControllerManager pControllerManager, AudioManager pAudioManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mAudioFireAndForgetManager = new AudioFireAndForgetManager(pAudioManager);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mAudioFireAndForgetManager.acquireAudioSources(6);

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void playSound(String pSoundFxName) {
		mAudioFireAndForgetManager.play(pSoundFxName);

	}

}
