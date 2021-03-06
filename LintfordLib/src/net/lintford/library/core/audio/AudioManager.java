package net.lintford.library.core.audio;

import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.ALC_FREQUENCY;
import static org.lwjgl.openal.ALC10.ALC_REFRESH;
import static org.lwjgl.openal.ALC10.ALC_SYNC;
import static org.lwjgl.openal.ALC10.ALC_TRUE;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetInteger;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.ALC11.ALC_ALL_DEVICES_SPECIFIER;
import static org.lwjgl.openal.ALC11.ALC_MONO_SOURCES;
import static org.lwjgl.openal.ALC11.ALC_STEREO_SOURCES;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.data.AudioData;
import net.lintford.library.core.audio.data.OGGAudioData;
import net.lintford.library.core.audio.data.WaveAudioData;
import net.lintford.library.core.audio.music.MusicManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.storage.FileUtils;
import net.lintford.library.options.AudioConfig;

public class AudioManager {

	public class AudioNubble {
		private boolean enabled;
		private float nubbleNormalised;
		public int audioType;

		public boolean isEnabled() {
			return enabled;
		}

		void nubbleNormalized(float pNewNubbleNormalized) {
			nubbleNormalised = MathHelper.clamp(pNewNubbleNormalized, 0f, 1f);
		}

		float nubbleNormalized() {
			return nubbleNormalised;
		}

		public AudioNubble(final int pAudioType) {
			audioType = pAudioType;
		}

	}

	public class AudioMetaDataDefinition {
		public String filepath;
		public String soundname;
		public boolean reload;
	}

	public class AudioMetaData {
		public AudioMetaDataDefinition[] AudioMetaDefinitions;

	}

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String META_FILE_LOCATION = "/res/audio/meta.json";

	public static final int AUDIO_SOURCE_TYPE_SOUNDFX = 0;
	public static final int AUDIO_SOURCE_TYPE_MUSIC = 1;

	// --------------------------------------
	// Variables
	// --------------------------------------

	/** A pool of {@link AudioSource}s created for other objects (and can be reused). */
	private List<AudioSource> mAudioSources;
	private Map<String, AudioData> mAudioDataBuffers;
	private AudioListener mAudioListener;

	private long mContext;
	private long mDevice;
	private int mNumberAssignedSources;
	private boolean mOpenALInitialized;

	private int mMaxMonoSourceCount;
	private int mMaxStereoSourceCount;
	private boolean mACL10Supported;
	private boolean mACL11Supported;

	private List<String> mAudioDevices;
	private String mDefaultAudioDevice;

	private List<AudioFireAndForgetManager> mAudioFireAndForgetManagers = new ArrayList<>();
	private MusicManager mMusicManager;

	private AudioConfig mAudioConfig;

	private AudioNubble mSoundFxNubble;
	private AudioNubble mMusicNubble;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public AudioNubble musicNubble() {
		return mMusicNubble;
	}

	public AudioConfig audioConfig() {
		return mAudioConfig;
	}

	public int maxMonoAudioSources() {
		return mMaxMonoSourceCount;
	}

	public int maxStereoAudioSources() {
		return mMaxStereoSourceCount;
	}

	public String defaultAudioDevice() {
		return mDefaultAudioDevice;
	}

	// TODO: Option to set current audio device

	public List<String> audioDevices() {
		return mAudioDevices;
	}

	public MusicManager musicManager() {
		return mMusicManager;
	}

	/** Returns the maxiumum numbers of sources supported by the OpenAL context. */
	public int maxSources() {
		return mMaxMonoSourceCount;
	}

	/** Returns true if OpenAL has be initialized (device and context created). */
	public boolean isInitialized() {
		return mOpenALInitialized;
	}

	public AudioData getAudioDataBufferByName(String pName) {
		return mAudioDataBuffers.get(pName);

	}

	public AudioListener listener() {
		return mAudioListener;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public AudioManager(AudioConfig pAudioConfig) {
		mAudioConfig = pAudioConfig;

		mAudioDataBuffers = new HashMap<>();
		mAudioSources = new ArrayList<>();
		mAudioListener = new AudioListener();

		mSoundFxNubble = new AudioNubble(AUDIO_SOURCE_TYPE_SOUNDFX);
		mMusicNubble = new AudioNubble(AUDIO_SOURCE_TYPE_MUSIC);

		mMusicManager = new MusicManager(this);

		mContext = NULL;
		mDevice = NULL;

		mAudioConfig.loadConfig();
		updateSettings();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void updateSettings() {
		// Read the current values from the audio options menu
		final var lNormalizedMasterVolume = mAudioConfig.masterVolume();
		final var lNormalizedSoundMasterVolume = mAudioConfig.soundFxVolume();
		final var lNormalizedMusicMasterVolume = mAudioConfig.musicVolume();

		mMusicManager.isMusicEnabled(mAudioConfig.masterEnabled());
		if (!mAudioConfig.masterEnabled()) {
			updateKillOfSources(mSoundFxNubble);
			updateKillOfSources(mMusicNubble);

			mSoundFxNubble.enabled = false;
			mMusicNubble.enabled = false;
			return;
		}

		{ // SoundFx
			final var lPreviousSoundFxEnabled = mSoundFxNubble.enabled;
			final var lPreviousSoundFxVolume = mSoundFxNubble.nubbleNormalized();

			mSoundFxNubble.enabled = mAudioConfig.soundFxEnabled();
			if (mSoundFxNubble.enabled) {
				mSoundFxNubble.nubbleNormalized(lNormalizedMasterVolume * lNormalizedSoundMasterVolume);

			}

			if (!mSoundFxNubble.enabled && lPreviousSoundFxEnabled) {
				mSoundFxNubble.nubbleNormalized(0);
				// updateKillOfSources(mSoundFxNubble);
				updateVolumeOfAllSources(mSoundFxNubble);

			} else if (!lPreviousSoundFxEnabled && mSoundFxNubble.enabled) {
				updateVolumeOfAllSources(mSoundFxNubble);

			}

			if (mSoundFxNubble.enabled && lPreviousSoundFxVolume != mSoundFxNubble.nubbleNormalized()) {
				updateVolumeOfAllSources(mSoundFxNubble);

			}
		}

		{ // Music
			final var lPreviousMusicEnabled = mMusicNubble.enabled;
			final var lPreviousVolume = mMusicNubble.nubbleNormalized();

			mMusicNubble.enabled = mAudioConfig.musicEnabled();
			if (mMusicNubble.enabled) {
				mMusicNubble.nubbleNormalized(lNormalizedMasterVolume * lNormalizedMusicMasterVolume);

			}

			if (!mMusicNubble.enabled && lPreviousMusicEnabled) {
				// Music turned off
				mMusicManager.isMusicEnabled(false);
				// updateKillOfSources(mMusicNubble);
				updateVolumeOfAllSources(mMusicNubble);

			} else if (mMusicNubble.enabled && !lPreviousMusicEnabled) {
				// Music turned on
				mMusicManager.isMusicEnabled(true);
				updateVolumeOfAllSources(mMusicNubble);

			}

			if (mMusicNubble.enabled && lPreviousVolume != mMusicNubble.nubbleNormalized()) {
				updateVolumeOfAllSources(mMusicNubble);

			}
		}

	}

	private void updateKillOfSources(AudioNubble pAudioNubble) {
		final int lAudioSourceType = pAudioNubble.audioType;

		final int lNumberOfAudioSources = mAudioSources.size();
		for (int i = 0; i < lNumberOfAudioSources; i++) {
			final var lAudioSource = mAudioSources.get(i);
			if (lAudioSource.audioSourceType() == lAudioSourceType) {
				lAudioSource.stop();

			}

		}

	}

	private void updateVolumeOfAllSources(AudioNubble pAudioNubble) {
		final int lAudioSourceType = pAudioNubble.audioType;

		final int lNumberOfAudioSources = mAudioSources.size();
		for (int i = 0; i < lNumberOfAudioSources; i++) {
			final var lAudioSource = mAudioSources.get(i);
			if (lAudioSource.audioSourceType() == lAudioSourceType) {
				lAudioSource.updateGain();

			}

		}

	}

	public void loadALContent(ResourceManager pResourceManager) {
		if (mOpenALInitialized) {
			Debug.debugManager().logger().i(getClass().getSimpleName(), "AudioManager already initialized.");
			return;

		}

		mDevice = alcOpenDevice((ByteBuffer) null);
		if (mDevice == NULL)
			throw new IllegalStateException("Failed to open the default device.");

		ALCCapabilities deviceCaps = ALC.createCapabilities(mDevice);

		mACL10Supported = deviceCaps.OpenALC10;
		mACL11Supported = deviceCaps.OpenALC11;

		Debug.debugManager().logger().i(getClass().getSimpleName(), "OpenALC10: " + mACL10Supported);
		Debug.debugManager().logger().i(getClass().getSimpleName(), "OpenALC11: " + mACL11Supported);
		Debug.debugManager().logger().i(getClass().getSimpleName(), "caps.ALC_EXT_EFX = " + deviceCaps.ALC_EXT_EFX);

		// Check the caps of the sound devie
		if (deviceCaps.OpenALC11) {
			mAudioDevices = ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);
			if (mAudioDevices == null) {
				// checkALCError(NULL);

			} else {
				for (int i = 0; i < mAudioDevices.size(); i++) {
					Debug.debugManager().logger().i(getClass().getSimpleName(), i + ": " + mAudioDevices.get(i));

				}

			}

		}

		// Chose which sound device to use
		mDefaultAudioDevice = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		Debug.debugManager().logger().i(getClass().getSimpleName(), "Default device: " + mDefaultAudioDevice);
		// Assert true: defaultDeviceName != null

		mContext = alcCreateContext(mDevice, (IntBuffer) null);
		alcSetThreadContext(mContext);
		AL.createCapabilities(deviceCaps);

		mMaxMonoSourceCount = alcGetInteger(mDevice, ALC_MONO_SOURCES);
		mMaxStereoSourceCount = alcGetInteger(mDevice, ALC_STEREO_SOURCES);

		Debug.debugManager().logger().i(getClass().getSimpleName(), "ALC_FREQUENCY: " + alcGetInteger(mDevice, ALC_FREQUENCY) + "Hz");
		Debug.debugManager().logger().i(getClass().getSimpleName(), "ALC_REFRESH: " + alcGetInteger(mDevice, ALC_REFRESH) + "Hz");
		Debug.debugManager().logger().i(getClass().getSimpleName(), "ALC_SYNC: " + (alcGetInteger(mDevice, ALC_SYNC) == ALC_TRUE));
		Debug.debugManager().logger().i(getClass().getSimpleName(), "ALC_MONO_SOURCES: " + mMaxMonoSourceCount);
		Debug.debugManager().logger().i(getClass().getSimpleName(), "ALC_STEREO_SOURCES: " + mMaxStereoSourceCount);

		if (mMaxMonoSourceCount == 0) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "AudioManager not initialized correctly. Unable to assign AudioSources!");

		}

		// Setup some initial listener data
		mAudioListener.setPosition(0, 0, 0);
		mAudioListener.setVelocity(0, 0, 0);

		mOpenALInitialized = true;

		musicManager().loadALContent(pResourceManager);

		// Once all the OpenAL Capabailities have been discovered, move onto loading the audio data
		loadAudioFilesFromMetafile(META_FILE_LOCATION);

	}

	public void unloadALContent() {
		musicManager().unloadALContent();

		// Remove all the sound buffers
		for (AudioData lAudioData : mAudioDataBuffers.values()) {
			lAudioData.unloadAudioData();

		}

		mAudioDataBuffers.clear();

		// Dispose of the assignable AudioSources.
		for (AudioSource lAudioSource : mAudioSources) {
			lAudioSource.dispose();

		}

		alcMakeContextCurrent(NULL);
		alcDestroyContext(mContext);
		alcCloseDevice(mDevice);

		mOpenALInitialized = false;

	}

	public void unloadAudioBuffer(String pAudioName) {
		final var lAudioDataBuffer = mAudioDataBuffers.get(pAudioName);

		if (lAudioDataBuffer == null) {
			return;

		}

		lAudioDataBuffer.unloadAudioData();

		mAudioDataBuffers.remove(pAudioName);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	/** Returns the {@link AudioData} with the given name. */
	public AudioData getSound(String pName) {
		if (pName == null || pName.length() == 0) {
			return null;

		}

		// First, check to see if a sound resource with the given name already exists and if so, return it.
		if (mAudioDataBuffers.containsKey(pName)) {
			return mAudioDataBuffers.get(pName);

		}

		return null;

	}

	public AudioNubble getAudioSourceNubbleBasedOnType(int pAudioSourceType) {
		if (pAudioSourceType == AUDIO_SOURCE_TYPE_MUSIC)
			return mMusicNubble;

		return mSoundFxNubble;

	}

	/** Returns an OpenAL {@link AudioSource} object which can be used to play an OpenAL AudioBuffer. */
	public AudioSource getAudioSource(final int pOwnerHash, int pAudioSourceType) {
		final int lNumberSourcesInPool = mAudioSources.size();

		for (int i = 0; i < lNumberSourcesInPool; i++) {
			if (mAudioSources.get(i).isFree()) {
				final var lAudioSource = mAudioSources.get(i);

				if (lAudioSource.assign(pOwnerHash, getAudioSourceNubbleBasedOnType(pAudioSourceType))) {
					return lAudioSource;

				}

			}

		}

		final AudioSource lNewAudioSource = increaseAudioSourcePool(8);
		if (lNewAudioSource == null) {
			return null;

		}

		lNewAudioSource.assign(pOwnerHash, getAudioSourceNubbleBasedOnType(pAudioSourceType));
		return lNewAudioSource;

	}

	private AudioSource increaseAudioSourcePool(int pAmt) {
		final int lNumberFreeSourceSpaces = mMaxMonoSourceCount - mNumberAssignedSources;
		if (lNumberFreeSourceSpaces <= 0) {
			return null;

		}

		pAmt = Math.min(pAmt, lNumberFreeSourceSpaces);

		for (int i = 0; i < pAmt - 1; i++) {
			createNewAudioSource();

		}

		return createNewAudioSource();

	}

	private AudioSource createNewAudioSource() {
		final var lReturnAudioSource = new AudioSource();
		AL10.alSourcei(lReturnAudioSource.sourceID(), AL10.AL_SOURCE_ABSOLUTE, AL10.AL_TRUE);
		AL10.alSourcei(lReturnAudioSource.sourceID(), AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
		AL10.alSourcef(lReturnAudioSource.sourceID(), AL10.AL_GAIN, 1f);
		AL10.alSourcef(lReturnAudioSource.sourceID(), AL10.AL_MAX_GAIN, 1f);// MathHelper.scaleToRange(mAudioConfig.soundFxVolume(), 0f, 1f, 0f, 100f));
		AL10.alSourcef(lReturnAudioSource.sourceID(), AL10.AL_PITCH, 1f);
		AL10.alSource3f(lReturnAudioSource.sourceID(), AL10.AL_POSITION, 0, 0, 0);

		mAudioSources.add(lReturnAudioSource);

		return lReturnAudioSource;

	}

	// --------------------------------------
	// Loading Methods
	// --------------------------------------

	public void loadAudioFilesFromMetafile(String pMetaFileLocation) {
		Debug.debugManager().logger().i(getClass().getSimpleName(), String.format("Loading audio from meta-file %s", pMetaFileLocation));

		final Gson GSON = new GsonBuilder().create();

		String lMetaFileContentsString = null;
		AudioMetaData lAudioMetaObject = null;

		lMetaFileContentsString = FileUtils.loadString(pMetaFileLocation);
		lAudioMetaObject = GSON.fromJson(lMetaFileContentsString, AudioMetaData.class);

		if (lAudioMetaObject == null || lAudioMetaObject.AudioMetaDefinitions == null || lAudioMetaObject.AudioMetaDefinitions.length == 0) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "There was an error reading the audio meta file");
			return;

		}

		final int lNumberOfFontUnitDefinitions = lAudioMetaObject.AudioMetaDefinitions.length;
		for (int i = 0; i < lNumberOfFontUnitDefinitions; i++) {
			final var lAudioDataDefinition = lAudioMetaObject.AudioMetaDefinitions[i];

			final var lSoundName = lAudioDataDefinition.soundname;
			final var lFilepath = lAudioDataDefinition.filepath;
			final var lReload = lAudioDataDefinition.reload;

			loadAudioFile(lSoundName, lFilepath, lReload);

		}

	}

	public AudioData loadAudioFile(String pSoundName, String pFilepath, boolean pReload) {
		if (!mOpenALInitialized) {
			Debug.debugManager().logger().w(getClass().getSimpleName(), "Cannot load AudioData files until the AudioManager has been loaded");
			return null;

		}

		if (pSoundName == null || pSoundName.length() == 0) {
			return null;

		}

		if (!pReload && mAudioDataBuffers.containsKey(pSoundName)) {
			return mAudioDataBuffers.get(pSoundName);

		}

		final var lSoundName = pSoundName;
		final var lSoundData = loadAudioFile(lSoundName, pFilepath);

		if (lSoundData != null) {
			if (pReload) {
				Debug.debugManager().logger().i(getClass().getSimpleName(), "Re-Loaded AudioData file '" + pFilepath + "' as " + lSoundName);
			} else {
				Debug.debugManager().logger().i(getClass().getSimpleName(), "Loaded AudioData file '" + pFilepath + "' as " + lSoundName);
			}

			mAudioDataBuffers.put(lSoundName, lSoundData);

		}

		return lSoundData;

	}

	private AudioData loadAudioFile(String pName, String pFilepath) {
		if (pFilepath == null || pFilepath.length() == 0) {
			return null;

		}

		InputStream lInputStream = null;
		if (pFilepath.charAt(0) == '/') {
			lInputStream = loadAudioDataFromResource(pFilepath);

		} else {
			lInputStream = loadAudioDataFromFile(pFilepath);

		}

		if (lInputStream == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "Couldn't open the audio file: " + pFilepath);
			return null;

		}

		final var lFileExtension = FileUtils.getFileExtension(pFilepath);
		switch (lFileExtension) {
		case ".wav":
			final var lNewWavData = new WaveAudioData();
			lNewWavData.loadAudioFromInputStream(pName, lInputStream);
			return lNewWavData;

		case ".ogg":
			final var lNewOggAudioData = new OGGAudioData();
			lNewOggAudioData.loadAudioFromInputStream(pName, lInputStream);
			return lNewOggAudioData;

		default:
			Debug.debugManager().logger().w(getClass().getSimpleName(), "Failed to recognize the audio file extension.");
			return null;
		}

	}

	private InputStream loadAudioDataFromResource(String pResourcename) {
		final var lInputStream = FileUtils.class.getResourceAsStream(pResourcename);
		if (lInputStream == null)
			return null;

		return new BufferedInputStream(lInputStream);

	}

	private InputStream loadAudioDataFromFile(String pFilename) {
		final var lNewFile = new File(pFilename);

		if (!lNewFile.exists()) {
			return null;

		}

		try {
			return new BufferedInputStream(new FileInputStream(lNewFile));
		} catch (FileNotFoundException e) {

		}

		return null;
	}

	// --------------------------------------
	// Factory Methods
	// --------------------------------------

	public AudioFireAndForgetManager getFireAndForgetManager(int pNumberSources) {
		final var lNewFireAndForgetManager = getFreeAudioFireAndForgetManager();
		lNewFireAndForgetManager.acquireAudioSources(pNumberSources);

		return lNewFireAndForgetManager;
	}

	public void releaseFireAndForgetManager(AudioFireAndForgetManager pAudioFireAndForgetManager) {
		if (mAudioFireAndForgetManagers.contains(pAudioFireAndForgetManager)) {
			mAudioFireAndForgetManagers.remove(pAudioFireAndForgetManager);

		}
	}

	private AudioFireAndForgetManager getFreeAudioFireAndForgetManager() {
		final int lNumberOfmAudioFireAndForgetManagers = mAudioFireAndForgetManagers.size();

		for (int i = 0; i < lNumberOfmAudioFireAndForgetManagers; i++) {
			if (!mAudioFireAndForgetManagers.get(i).isInUse()) {
				return mAudioFireAndForgetManagers.get(i);
			}
		}

		return createAudioFireAndForgetManager();
	}

	private AudioFireAndForgetManager createAudioFireAndForgetManager() {
		final var lNewFireAndForgetManagear = new AudioFireAndForgetManager(this);
		mAudioFireAndForgetManagers.add(lNewFireAndForgetManagear);

		return lNewFireAndForgetManagear;
	}

}
