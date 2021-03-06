package net.lintford.library.core.debug;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class GLDebug {

	public static boolean checkGLErrorsException() {
		checkGLErrors("GLDebug");

		return false;
	}

	public static boolean checkGLErrorsException(String pCustomTAG) {
		if (!Debug.debugManager().debugManagerEnabled())
			checkGLErrors(pCustomTAG, true);

		return false;
	}

	public static boolean checkGLErrors() {
		return checkGLErrors("GLDebug");

	}

	public static boolean checkGLErrors(String pCustomTAG) {
		return checkGLErrors(pCustomTAG, false);

	}

	private static boolean checkGLErrors(String pCustomTAG, boolean pPrintStackTrace) {
		// FIXME: Need to add some proper handling in all of these cases - there is probably some way to handle each of the
		// cases in a graceful way which SHOULD NOT result in a runtime exception being thrown!

		final int lGLError = GL11.glGetError();
		if (lGLError == GL11.GL_NO_ERROR)
			return false;
		switch (lGLError) {
		case GL11.GL_INVALID_ENUM:
			Debug.debugManager().logger().e(GLDebug.class.getSimpleName(), String.format("GL_INVALID_ENUM (%d)", GL11.GL_INVALID_ENUM));
			if (pPrintStackTrace) {
				throw new RuntimeException("GL_INVALID_ENUM exception occured: " + pCustomTAG);

			}
			return true;
		case GL11.GL_INVALID_VALUE:
			Debug.debugManager().logger().e(GLDebug.class.getSimpleName(), String.format("GL_INVALID_VALUE (%d)", GL11.GL_INVALID_VALUE));
			if (pPrintStackTrace) {
				throw new RuntimeException("GL_INVALID_VALUE exception occured: " + pCustomTAG);

			}
			return true;
		case GL11.GL_INVALID_OPERATION:
			Debug.debugManager().logger().e(GLDebug.class.getSimpleName(), String.format("GL_INVALID_OPERATION (%d)", GL11.GL_INVALID_OPERATION));
			if (pPrintStackTrace) {
				throw new RuntimeException("GL_INVALID_OPERATION exception occured: " + pCustomTAG);
			}

			return true;
		case GL11.GL_OUT_OF_MEMORY:
			Debug.debugManager().logger().e(GLDebug.class.getSimpleName(), String.format("GL_OUT_OF_MEMORY (%d)", GL11.GL_OUT_OF_MEMORY));
			if (pPrintStackTrace) {
				throw new RuntimeException("GL_OUT_OF_MEMORY exception occured: " + pCustomTAG);

			}
			return true;
		case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
			Debug.debugManager().logger().e(GLDebug.class.getSimpleName(), String.format("GL_INVALID_FRAMEBUFFER_OPERATION (%d)", GL30.GL_INVALID_FRAMEBUFFER_OPERATION));
			if (pPrintStackTrace) {
				throw new RuntimeException("GL_INVALID_FRAMEBUFFER_OPERATION exception occured: " + pCustomTAG);

			}
			return true;
		}

		return false;
	}

}
