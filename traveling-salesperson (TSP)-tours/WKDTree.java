import java.util.ArrayList;


/**
 * WKDTree (skeleton)
 *
 * MODIFY THE FOLLOWING CLASS.
 *
 * You are free to make whatever changes you like or to create additional
 * classes and files.
 */

public class WKDTree<LPoint extends LabeledPoint2D> {
	
	private Node root;
	private int size;
	
	private abstract class Node { // generic node (purely abstract)
		
		// -----------------------------------------------------------------
		// Standard dictionary helpers
		// -----------------------------------------------------------------
		abstract LPoint find(Point2D pt);
		
		abstract Node insert(LPoint pt) throws Exception;
		
		abstract Node delete(Point2D pt) throws Exception;
		
		abstract ArrayList<String> getPreorderList();
		
		
		abstract Rectangle2D getWrapper();
		
		abstract LPoint getMinMax(int dim, int sign);
		
		abstract LPoint findSmallerX(float x, LPoint best);
		
		abstract LPoint findLargerX(float x, LPoint best);
		
		abstract LPoint findSmallerY(float y, LPoint best);
		
		abstract LPoint findLargerY(float y, LPoint best);
		
		abstract LPoint fixedRadNN(Point2D q, double sqRadius, LPoint best);
		
		abstract ArrayList<LPoint> circularRange(Point2D center, float sqRadius);
	}
	private class InternalNode extends Node {
		
		int cutDim; // the cutting dimension (0 = x, 1 = y)
		double cutVal; // the cutting value
		Rectangle2D wrapper; // bounding box
		Node left, right; // children
		
		/**
		 * constructor
		 */
		InternalNode (int cutDim, double cutVal, Node left, Node right) {
			this.cutDim = cutDim;
			this.cutVal = cutVal;
			this.left = left;
			this.right = right;
			this.wrapper = Rectangle2D.union(left.getWrapper(), right.getWrapper());
		}
		
		/**
		 * Find point in this subtree
		 */
		LPoint find(Point2D pt) { 
			if (wrapper.contains(pt)) {
				if (pt.get(cutDim) < cutVal)
					return left.find(pt);
				else
					return right.find(pt);
			}
			return null;
		}
		
		/**
		 * Insert into this subtree
		 */
		Node insert(LPoint pt) throws Exception {
			if (pt.get(cutDim) < cutVal) 
				left = left.insert(pt);
			else
				right = right.insert(pt);
			wrapper = Rectangle2D.union(left.getWrapper(), right.getWrapper());
			return this;
		}
		
		/**
		 * Get the wrapper
		 */
		Rectangle2D getWrapper() {
			return wrapper;
		}

		/**
		 * Delete a point from a subtree
		 */
		Node delete(Point2D pt) throws Exception {
			if (pt.get(cutDim) < cutVal) 
				left = left.delete(pt);
			else 
				right = right.delete(pt);
			if (left == null) 
				return right;
			else if (right == null)
				return left;
			wrapper = Rectangle2D.union(left.getWrapper(), right.getWrapper());
			return this;
		}


		/**
		 * Get min/max along the given dim. (sign = -1/+1 for min/max)
		 */
		LPoint getMinMax(int dim, int minMax) {
			if (cutDim == dim) { // need only visit one side
				if (minMax < 0)
					return left.getMinMax(dim, minMax);
				else
					return right.getMinMax(dim, minMax);
			} else {
				LPoint point1 = left.getMinMax(dim, minMax);
				LPoint point2 = right.getMinMax(dim, minMax);
				// minMax < 0 means find the smaller one
				if (minMax < 0) {
					// if equal, return left subtree
					if (point1.get(dim) == point2.get(dim))
						return point1;
					else if (point1.get(dim) < point2.get(dim))
						return point1;
					else
						return point2;
				} else {
					if (point1.get(dim) == point2.get(dim))
						return point2;
					else if (point1.get(dim) < point2.get(dim))
						return point2;
					else
						return point1;
				}
			}
		}
		
		/**
		 * Returns point with smaller X
		 */
		LPoint findSmallerX(float x, LPoint best) {
			if (wrapper.getLow().get(0) < x) {
				double candidate = (best == null ? -(Double.MAX_VALUE) : best.get(0));
				if (wrapper.getHigh().get(0) > candidate) {
					best = right.findSmallerX(x, best);
					best = left.findSmallerX(x, best);
				}
			}
			return best;
		}
		
		/**
		 * Returns point with larger X
		 */
		LPoint findLargerX(float x, LPoint best) {
			if (wrapper.getHigh().get(0) > x) {
				double candidate = (best == null ? Double.MAX_VALUE : best.get(0));
				if (wrapper.getLow().get(0) <= candidate) {
					best = left.findLargerX(x, best);
					best = right.findLargerX(x, best);
				}
			}
			return best;
		}

		/**
		 * Returns point with smaller Y
		 */
		LPoint findSmallerY(float y, LPoint best) {
			System.out.println(wrapper.toString());
			if (wrapper.getLow().get(1) < y) {
				double candidate = (best == null ? -(Double.MAX_VALUE) : best.get(1));
				if (wrapper.getHigh().get(1) > candidate) {
					best = right.findSmallerY(y, best);
					best = left.findSmallerY(y, best);
				}
			}
			return best;
		}

		/**
		 * Returns point with larger Y
		 */
		LPoint findLargerY(float y, LPoint best) {
			if (wrapper.getHigh().get(1) > y) {
				double candidate = (best == null ? Double.MAX_VALUE : best.get(1));
				if (wrapper.getLow().get(1) <= candidate) {
					best = left.findLargerY(y, best);
					best = right.findLargerY(y, best);
				}
			}
			return best;
		}
		
		/**
		 * Circular range reporting for a disk of a given squared radius about a given
		 * center point.
		 */
		ArrayList<LPoint> circularRange(Point2D center, float sqRadius) {
			ArrayList<LPoint> list = new ArrayList<LPoint>();
			if (wrapper.distanceSq(center) <= sqRadius) {
				list.addAll(left.circularRange(center, sqRadius));
				list.addAll(right.circularRange(center, sqRadius));
			}
			return list;
		}
		
		@Override
		ArrayList<String> getPreorderList() {
			ArrayList<String> list = new ArrayList<String>();
			list.add(toString());
			list.addAll(left.getPreorderList());
			list.addAll(right.getPreorderList());
			return list;
		}
		
		public String toString() {
			if (cutDim == 0)
				return "(x=" + cutVal + "): " + wrapper;
			else
				return "(y=" + cutVal + "): " + wrapper;
		}
		
		/* returns a reference to the fixed-radius nearest-neighbor query to q, where the squared
		 * radius of the disk is sqRadius. Among the points whose squared distance to q is strictly
		 * more than zero and strictly less than sqRadius,
		 */
		LPoint fixedRadNN(Point2D q, double sqRadius, LPoint best) {
			if(wrapper.distanceSq(q) >= Math.pow(sqRadius, 2))
				return best;
			if (best != null) {
				if(wrapper.distanceSq(q) > q.distanceSq(best.getPoint2D()))
					return best;
			}
			best = left.fixedRadNN(q, sqRadius, best);
			best = right.fixedRadNN(q, sqRadius, best);
			return best;
		}
	}
	
	
	// -----------------------------------------------------------------
	// External node
	// -----------------------------------------------------------------
	
	
	private class ExternalNode extends Node {
		LPoint thisPt; // the associated point
		
		/**
		 * constructor
		 * @param pt
		 */
		ExternalNode(LPoint pt) {
			thisPt = pt;
		}
		
		/**
		 * Find point in external node.
		 */
		LPoint find(Point2D pt) { 
			if(thisPt.getPoint2D().equals(pt))
				return thisPt;
			else
				return null;
		}
		
		/**
		 * Insert a point into this node.
		 */
		Node insert(LPoint pt) throws Exception {
			if (thisPt.getPoint2D().equals(pt.getPoint2D())) 
				throw new Exception("Insertion of point with duplicate coordinates");
			else {
				Rectangle2D wrapper = new Rectangle2D(thisPt.getPoint2D(), pt.getPoint2D());
				int cutDim;
				if (wrapper.getWidth(0) >= wrapper.getWidth(1))
					cutDim = 0;
				else
					cutDim = 1;
				double cutVal = (pt.get(cutDim) + thisPt.get(cutDim)) / 2;
				ExternalNode node = new ExternalNode(pt);
				if (pt.get(cutDim) < cutVal)
					return new InternalNode(cutDim, cutVal, node, this);
				else
					return new InternalNode(cutDim, cutVal, this, node);
			}
		}
		
		
		Rectangle2D getWrapper() {
			return new Rectangle2D(thisPt.getPoint2D(), thisPt.getPoint2D());
		}
		
		/**
		 * Delete a point from this node.
		 */
		Node delete(Point2D pt) throws Exception {
			if (thisPt.getPoint2D().equals(pt))
				return null;
			else
				throw new Exception("Deletion of nonexistent point");
		}
		
		/**
		 * Get min/max along the given dim. (sign = -1/+1 for min/max)
		 */
		LPoint getMinMax(int dim, int sign) {
			return thisPt;
		}
		
		/**
		 * Returns the point with strictly smaller x coordinate.
		 */
		LPoint findSmallerX(float x, LPoint best) {
			double currentX = thisPt.get(0);
			if (currentX < x) {
				double candidate = (best == null ? -(Double.MAX_VALUE) : best.get(0));
				if (currentX > candidate) {
					best = thisPt;
				}
				else if (currentX == candidate) {
					if (thisPt.get(1) > best.get(1))
						best = thisPt;
				}
			}
			return best;
		}
		
		/**
		 * Returns the point with strictly larger x coordinate.
		 */
		LPoint findLargerX(float x, LPoint best) {
			double currentX = thisPt.get(0);
			if (currentX > x) {
				double candidate = (best == null ? Double.MAX_VALUE : best.get(0));
				if (currentX < candidate) 
					best = thisPt;
				else if (currentX == candidate) {
					if (thisPt.get(1) < best.get(1))
						best = thisPt;
				}
			}
			return best;
		}

		/**
		 * Returns the point with strictly smaller y coordinate.
		 */
		LPoint findSmallerY(float y, LPoint best) {
			double currentY = thisPt.get(1);
			if (currentY < y) {
				double candidate = (best == null ? -(Double.MAX_VALUE) : best.get(1));
				if (currentY > candidate) 
					best = thisPt;
				else if (currentY == candidate) {
					if (thisPt.get(0) > best.get(0))
						best = thisPt;
				}
			}
			return best;
		}

		/**
		 * Returns the point with strictly larger y coordinate.
		 */
		LPoint findLargerY(float y, LPoint best) {
			double currentX = thisPt.get(1);
			if (currentX > y) {
				double candidate = (best == null ? Double.MAX_VALUE : best.get(1));
				if (currentX < candidate) 
					best = thisPt;
				else if (currentX == candidate) {
					if (thisPt.get(0) < best.get(0))
						best = thisPt;
				}
			}
			return best;
		}
		
		/**
		 * Circular range reporting for a disk of a given squared radius about a given
		 * center point.
		 */
		ArrayList<LPoint> circularRange(Point2D center, float sqRadius) {
			ArrayList<LPoint> list = new ArrayList<LPoint>();
			if (center.distanceSq(thisPt.getPoint2D()) <= sqRadius)
				list.add(thisPt);
			return list;
		}

		/**
		 * Add entry to the list.
		 * 
		 * @param list The list into which items are added
		 */
		ArrayList<String> getPreorderList() {
			ArrayList<String> list = new ArrayList<String>();
			list.add(toString()); 
			return list;
		}
		
		public String toString() {
			return "[" + thisPt.toString() + "]";
		}
		
		@Override
		LPoint fixedRadNN(Point2D q, double sqRadius, LPoint best) {
			if (thisPt.getX() != q.getX() || thisPt.getY() != q.getY()) {
				if (q.distanceSq(thisPt.getPoint2D()) < Math.pow(sqRadius, 2)) {
					if (best == null)
						return thisPt;
					if (q.distanceSq(thisPt.getPoint2D()) < q.distanceSq(best.getPoint2D()))
						return thisPt;
					else if (q.distanceSq(thisPt.getPoint2D()) == q.distanceSq(best.getPoint2D())) {
						if (thisPt.getX() < best.getX())
							return thisPt;
						else if (thisPt.getX() == best.getX()) {
							if (thisPt.getY() < best.getY())
								return thisPt;
						}
					}
				}
			}
			return best;
		}
	}

	// -----------------------------------------------------------------
	// Public members - You should not modify the function signatures
	// -----------------------------------------------------------------

	public WKDTree() { 
		root = null;
		size = 0;
	}
	
	/**
	 * Find an point in the tree. Note that the point being deleted does not need to
	 * match fully. It suffices that it has enough information to satisfy the
	 * comparator.
	 *
	 * @param pt The item being sought (only the relevant members are needed)
	 * @return A reference to the element where found or null if not found
	 */
	public LPoint find(Point2D pt) { /* ... */
		if (root == null)
			return null; 
		else
			return root.find(pt);
	}
	
	/**
	 * Insert a point
	 *
	 * @param point The point to be inserted
	 * @throws Exception if point with same coordinates exists in the tree
	 */
	public void insert(LPoint pt) throws Exception { 
		if (root == null) 
			root = new ExternalNode(pt);
		else
			root = root.insert(pt);
		size += 1;
	}
	
	/**
	 * Delete a point. Note that the point being deleted does not need to match
	 * fully. It suffices that it has enough information to satisfy the comparator.
	 *
	 * @param point The point to be deleted
	 * @throws Exception if point with same coordinates exists in the tree
	 */
	public void delete(Point2D pt) throws Exception { 
		if (root == null) 
			throw new Exception("Deletion of nonexistent point");
		else
			root = root.delete(pt);
		size -= 1;
	}
	
	/**
	 * Get a list of entries in preorder
	 *
	 * @return ArrayList of string encoded items in preorder
	 */
	public ArrayList<String> getPreorderList() { 
		if (root == null) 
			return new ArrayList<String>(); 
		else 
			return root.getPreorderList();
	}
	
	/**
	 * Remove all items, resulting in an empty tree
	 */
	public void clear() { 
		root = null;  
		size = 0;
	}
	
	
	public int size() { return size; }
	
	/**
	 * Get point with min/max x/y coordinate. Ties are broken lexicographically.
	 *
	 * @return A reference to the associated value, or null if tree empty
	 */
	public LPoint getMinX() {
		if (root == null)
			return null;
		else 
			return root.getMinMax(0,-1);
	}
	
	public LPoint getMaxX() { 
		if (root == null)
			return null;
		else 
			return root.getMinMax(0,1);
	}
	public LPoint getMinY() { 
		if (root == null)
			return null;
		else 
			return root.getMinMax(1, -1);
	}
	public LPoint getMaxY() { 
		if (root == null)
			return null;
		else 
			return root.getMinMax(1, 1);
	}
	
	/**
	 * Get point with strictly smaller/larger x/y coordinate. Ties are broken
	 * lexicographically.
	 *
	 * @param x The threshold value
	 * @return A reference to the associated value, or null if tree empty
	 */
	public LPoint findSmallerX(float x) { 
		if (root == null)
			return null;
		else
			return root.findSmallerX(x, null);
	}
	
	public LPoint findLargerX(float x)  { 
		if (root == null)
			return null;
		else
			return root.findLargerX(x, null);
	}
	
	public LPoint findSmallerY(float y) { 
		if (root == null)
			return null;
		else
			return root.findSmallerY(y, null);
	}
	
	public LPoint findLargerY(float y)  { 
		if (root == null)
			return null;
		else
			return root.findLargerY(y, null);
	}
	
	/**
	 * Find the nearest neighbor of a query point.
	 * 
	 * @param q The query point.
	 * @return The nearest neighbor to q or null if the tree is empty.
	 */
	public ArrayList<LPoint> circularRange(Point2D center, float sqRadius) { 
		if (root == null)
			return new ArrayList<LPoint>();
		else
			return root.circularRange(center, sqRadius);
	}
	
	public LPoint fixedRadNN(Point2D center, double sqRadius) { 
		if (root == null)
			return null;
		else 
			return root.fixedRadNN(center, sqRadius, null);
	}

}
