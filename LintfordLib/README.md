# Introduction: LintfordLib
A Java game library containing LWJGL natives and a game state management framework.
The library project and metadata was created in Eclipse Neon (4.6.0). The project was compiled with Java 1.8.0_112 64-bit and contains LGWJL3 jars and native libraries.

The development of this project takes place predominantly during the Ludum Dare game jam events. It therefore misses the benefits of code review, comprehensive unit testing or the application of many clean code principles. It is strongly recommended to **not use this code ina production environment**.

# Getting the repository
You can get the Java-LDLibraryGL project by either by cloning the repository or by adding it as a submodule to an existing git repository.


# Library Usage
Once you have cloned the Java-LDLibraryGL repository, you can import it into your IDE workspace. It should then be added as a project to your game project's classpath. This can be done either using the project properties dialog (e.g. in Eclipse), or by added the following to the .classpath:

```
<classpathentry combineaccessrules="false" kind="src" path="<relative_path_to_ldlibrary_folder>"/>
```

Once you have done this, you should be able to import the packages from the LDLibrary and access the classes within.
To test the setup and get started, you can open a new LWJGL window by adding the following class to your project:

```
// Import the OpenGL bindings from within the LWJGL framework
import org.lwjgl.opengl.GL11;

// import LDLibrary classes
import net.ld.library.GameInfo;
import net.ld.library.core.LWJGLCore;

/** Create a class which extends LWJGLCore. This will be responsible for instantiating and opening an OpenGL window. */
public class GameBase extends LWJGLCore {

/** Main entry point for the game. */
public static void main(String args[]) {
	GameInfo lGameInfo = new GameInfo() { 
		@Override
		public String windowTitle() {
			return "New Game";
		}
	};

	final var lGameBase = new GameBase(lGameInfo);
	lGameBase.createWindow(); 
}
```

LWJGLCore is an abstract class which defines core methods for an OpenGL game. These methods are called automatically, and are:

**void onInitialiseGL()**: This is called once at the start of the application. From here you can set the initial state for OpenGL.

**void onInitialiseApp()**: This method is also called once, and it provides a convenient place to initialise any other classes for your game.

**void onLoadContent()**: This is called once at the beginning of the application and after onInitialiseGL(). Here you can begin loading OpenGL resources and using the OpenGL context.

**boolean onHandleInput()**: Called once per frame, before onUpdate(), and is where you can handle input.

**void onUpdate(GameTime pGameTime)**: Called once per frame.

**void onDraw()**: Called once per frame.


# Resource Files
The LDLibrary also has a couple of resource files that are used as standard by the ScreenManager (such as a 'default' font and texture file). These resources are embedded in the jar and streamed at runtime. When loading media from an embedded resource as a stream, don't forget that the path name should start with a / (which indicates the path is relative to the JAR root.) For example:

Loading a texture from an embedded resource:

```
Texture myTexture = TextureManager.textureManager().loadTexture("world", "/res/textures/world.png");
```

Loading a texture from a file:

```
Texture myTexture = TextureManager.textureManager().loadTexture("world", "res/textures/world.png");
```

You can customise the 'core' texture that the ScreenManager uses by loading a texture in the TextureManager with an ID of ScreenManager.SCREENMANAGER___TEXTURE_NAME. 

For example:

```
TextureManager.textureManager().loadTexture(ScreenManager.SCREENMANAGER_TEXTURE_NAME, "res/textures/screenmanager.png");
```
will load the png file from the res/textures directory.

Similarly you can load a custom font for the screen manager like this:

```
mResourceManager.fontManager().loadNewFont(ToastManager.TOAST_FONT_NAME, "res/fonts/system.ttf", 20);
```

You can specify to the ResourceManager in LWJGLCore to watch a texture directory for changes. Any changes to texture
files at runtime will be automatically reloaded and updated in the running game. To do this, for example, use:

```
mResourceManager.watchTextureDirectory("res/textures");
```


# GameInfo
The GameInfo interface provides default methods (Java 1.8) specifying the behaviour of the LWJGL window to be created. Simply override any of the methods to provide custom behvaiour:

```
GameInfo lGameInfo = new GameInfo() {
	@Override
	public String windowTitle() {
		return "Hello World!";
	}
};
```

# ScreenManager
There is also a ScreenManager framework contained within net.ld.library.screenmanager. This is a stack based menu system where you can push and pop screens on top of the stack to be updated & rendered in a top down fashion.


# Quick Startup
The following elements can be added directly into your game class which extends LWJGLCore, to provide some basic features:

onInitialiseGL:

```
// Enable depth testing
GL11.glEnable(GL11.GL_BLEND);
GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

// Enable depth testing
GL11.glEnable(GL11.GL_DEPTH_TEST);
GL11.glDepthFunc(GL11.GL_LEQUAL);

// Set the clear color to corn flower blue
GL11.glClearColor(100.0f / 255.0f, 149.0f / 255.0f, 237.0f / 255.0f, 1.0f);
```

onDraw():

```
// Clear the depth buffer and color buffer
GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
```

# Build
The project includes an ANT script to build the library project. The LDLibrary is, by default, build into the following directory structure:

```
drive:root / 
	    build /
	        classes / 
	        res /     -- contains resources files which will be copied *into* the LDLIbrary JAR once it is built 
	    dist /
	        docs /    -- contains documentation to be included in the distribution
	        res /     -- contains binary resources to be included in the distribution (not used by LDLibrary)
	        libs /    -- contains dependency JAR files (e.g. LWJGL 3 and gson)
```

All of the resources used by the library are loaded from the jar as ByteStreams, and are loaded from within the LDLibrary JAR. 

