package net.lintford.library.core.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lintford.library.core.maths.Vector2f;

// SAT Ref: http://www.dyn4j.org/2010/01/sat/
public class Polygon extends Shape {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final long serialVersionUID = -1795671904806528834L;

	// --------------------------------------
	// Variables
	// --------------------------------------

	protected final List<Vector2f> mVertices = new ArrayList<>();
	protected Vector2f[] mAxes;
	protected final Vector2f mCentroid;
	protected boolean mDirty;
	public float x;
	public float y;

	// --------------------------------------
	// Properties
	// --------------------------------------

	/**
	 * Vertices are defined locally around the center point (x,y)
	 */
	@Override
	public List<Vector2f> getVertices() {
		return mVertices;

	}

	public List<Vector2f> getCopyOfVertices() {
		return new ArrayList<>(mVertices);
	}

	public float centerX() {
		return x;
	}

	public float centerY() {
		return y;
	}

	public Vector2f centroid() {
		return mCentroid;
	}

	// --------------------------------------
	// Constructors
	// --------------------------------------

	public Polygon() {
		this(0, 0);

	}

	public Polygon(float pCenterX, float pCenterY) {
		mCentroid = new Vector2f();

		x = pCenterX;
		y = pCenterY;

	}

	public Polygon(Polygon pOther) {
		mCentroid = new Vector2f();

		if (pOther != null) {
			List<Vector2f> lOtherVerts = pOther.getVertices();
			final int lOtherVertCount = lOtherVerts.size();

			for (int i = 0; i < lOtherVertCount; i++) {
				mVertices.add(new Vector2f(lOtherVerts.get(i)));

			}

			Vector2f[] lOtherAxes = pOther.getAxes();
			final int lOtherAxesCount = lOtherAxes.length;
			mAxes = new Vector2f[lOtherAxesCount];
			for (int i = 0; i < lOtherAxesCount; i++) {
				mAxes[i] = new Vector2f(lOtherAxes[i]);
			}

			x = pOther.x;
			y = pOther.y;
			mCentroid.set(pOther.centroid());

			mDirty = pOther.mDirty;

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	/**
	 * This axis-aligned Rectangle contains that rectangle. n.b. If you have applied a rotation to this Rectangle, then it is no longer axis-aligned
	 * 
	 * @param otherRect
	 * @Returns True if this rectangle instance entirely contains the given rectangle. False otherwise.
	 */
	public boolean intersects(Polygon pOtherPoly) {
		return false;
	}

	public boolean intersects(Rectangle pOtherRect) {
		return false;
	}

	/**
	 * This axis-aligned Rectangle contains that point. n.b. If you have applied a rotation to this Rectangle, then it is no longer axis-aligned
	 * 
	 * @param otherRect
	 * @Returns True if this rectangle instance entirely contains the given point. False otherwise.
	 */
	public boolean intersects(Vector2f pOtherPoint) {
		return intersects(pOtherPoint.x, pOtherPoint.y);
	}

	/**
	 * This Rectangle contains that point.
	 * 
	 * @param otherRect
	 * @Returns True if this rectangle instance entirely contains the given point. False otherwise.
	 */
	public boolean intersects(float pX, float pY) {
		return false;
	}

	@Override
	public Vector2f[] getAxes() {
		if (mDirty || mAxes == null) {
			if (mAxes == null || mAxes.length != mVertices.size())
				mAxes = new Vector2f[mVertices.size()];

			// FIXME: Garbage created
			for (int i = 0; i < mVertices.size(); i++) {
				int nextIndex = i < mVertices.size() - 1 ? i + 1 : 0;

				if (mVertices.get(i) == null || mVertices.get(nextIndex) == null)
					continue;

				if (mAxes[i] == null)
					mAxes[i] = new Vector2f();

				// This could cause problems later
				mAxes[i].x = (mVertices.get(i).y - mVertices.get(nextIndex).y);
				mAxes[i].y = -(mVertices.get(i).x - mVertices.get(nextIndex).x);
				mAxes[i].nor();

			}

			mDirty = false;

		}

		return mAxes;

	}

	@Override
	public Vector2f project(Vector2f pAxis, Vector2f pToFill) {
		if (pAxis == null)
			return pToFill;

		float min = Vector2f.dot(mVertices.get(0).x, mVertices.get(0).y, pAxis.x, pAxis.y);
		float max = min;
		for (int i = 1; i < mVertices.size(); i++) {
			if (mVertices.get(i) == null)
				continue;

			float p = Vector2f.dot(mVertices.get(i).x, mVertices.get(i).y, pAxis.x, pAxis.y);
			if (p < min) {
				min = p;

			} else if (p > max) {
				max = p;

			}

		}

		if (pToFill == null)
			pToFill = new Vector2f();

		pToFill.x = min;
		pToFill.y = max;

		return pToFill;

	}

	public boolean overlaps(Vector2f p1, Vector2f p2) {
		return !(p1.x > p2.y || p2.x > p1.y);

	}

	/**
	 * Returns true if this polygon has no vertices assigned.
	 * 
	 * @Returs True if this polygon has no vertices.
	 */
	public boolean isEmpty() {
		return mVertices == null || mVertices.size() == 0;
	}

	public void setCenterPosition(float pNewCenterX, float pNewCenterY) {
		x = pNewCenterX;
		y = pNewCenterY;

	}

	@Override
	public void rotateRel(float pRotAmt) {
		mDirty = true;
		rotation += pRotAmt;

	}

	@Override
	public void rotateAbs(float pRotAmt) {
		mDirty = true;
		rotation = pRotAmt;

	}

	public void addVertices(Vector2f... pNewVertices) {
		if (pNewVertices == null)
			return;

		final int lLength = pNewVertices.length;
		for (int i = 0; i < lLength; i++) {
			mVertices.add(pNewVertices[i]);
		}

		calculateCentroid();
		mDirty = true;

	}

	/**
	 * Returns true if the polygon winding order is CCW, otherwise false.
	 * 
	 * @Note This requires that t he Y axis is from top-to-bottom (- to +).
	 * 
	 * @Reference https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
	 */
	public boolean isPolygonCCW() {
		float lSignedArea = 0;
		float x1, y1, x2, y2 = 0;
		final int lVertexCount = mVertices.size();
		for (int i = 0; i < lVertexCount; i++) {
			// last point

			x1 = mVertices.get(i).x;
			y1 = mVertices.get(i).y;

			if (i == lVertexCount - 1) { // last point
				x2 = mVertices.get(0).x;
				y2 = mVertices.get(0).y;
			} else {
				x2 = mVertices.get(i + 1).x;
				y2 = mVertices.get(i + 1).y;
			}

			lSignedArea += (x1 * y2 - x2 * y1);

		}

		return lSignedArea > 0;

	}

	public void reverseWinding() {
		Collections.reverse(mVertices);
	}

	public void addVertex(Vector2f pNewVertex) {
		mVertices.add(pNewVertex);

		calculateCentroid();

	}

	public void clearVertices() {
		mVertices.clear();
		calculateCentroid();
		mDirty = true;

	}

	// Sutherland-Hodgmann Al
	public static Shape getIntersection(Polygon pClipper, Polygon pSubject) {
		if (pClipper == null || pSubject == null)
			return null;

		Polygon pResult = new Polygon(pSubject);

		int len = pClipper.getVertices().size();
		for (int i = 0; i < len; i++) {

			int len2 = pResult.getVertices().size();
			Polygon lInputPolygon = pResult;
			pResult = new Polygon(); // TODO: Garbage

			Vector2f A = pClipper.getVertices().get((i + len - 1) % len);
			Vector2f B = pClipper.getVertices().get(i);

			for (int j = 0; j < len2; j++) {

				Vector2f P = lInputPolygon.getVertices().get((j + len2 - 1) % len2);
				Vector2f Q = lInputPolygon.getVertices().get(j);

				if (isInside(A, B, Q)) {
					if (!isInside(A, B, P))
						pResult.addVertex(intersection(A, B, P, Q));
					pResult.addVertex(Q);
				} else if (isInside(A, B, P))
					pResult.addVertex(intersection(A, B, P, Q));
			}
		}

		return pResult;
	}

	private static boolean isInside(Vector2f a, Vector2f b, Vector2f c) {
		return (a.x - c.x) * (b.y - c.y) > (a.y - c.y) * (b.x - c.x);

	}

	private static Vector2f intersection(Vector2f a, Vector2f b, Vector2f p, Vector2f q) {
		float A1 = b.y - a.y;
		float B1 = a.x - b.x;
		float C1 = A1 * a.x + B1 * a.y;

		float A2 = q.y - p.y;
		float B2 = p.x - q.x;
		float C2 = A2 * p.x + B2 * p.y;

		float det = A1 * B2 - A2 * B1;
		float x = (B2 * C1 - B1 * C2) / det;
		float y = (A1 * C2 - A2 * C1) / det;

		return new Vector2f(x, y);

	}

	private void calculateCentroid() {
		if (mVertices.size() == 0) {
			mCentroid.set(0, 0);
			return;

		}

		mCentroid.set(0, 0);

		final var lVerticesCount = mVertices.size();
		for (int i = 0; i < lVerticesCount; i++) {
			mCentroid.x += mVertices.get(i).x;
			mCentroid.y += mVertices.get(i).y;

		}

		mCentroid.x /= lVerticesCount;
		mCentroid.y /= lVerticesCount;

	}

}
