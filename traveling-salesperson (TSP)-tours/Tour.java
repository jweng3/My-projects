import java.util.ArrayList;

/**
 * Tour (skeleton)
 *
 * MODIFY THE FOLLOWING CLASS.
 *
 * You are free to make whatever changes you like or to create additional
 * classes and files.
 */

public class Tour<LPoint extends LabeledPoint2D> {
	
	private AAXTree<String, Integer> locator; // locator structure
	private ArrayList<LPoint> tour; // the tour
	private WKDTree<LPoint> spatial;
	
	/**
	 * constructor
	 */
	public Tour() { 
		// This structure is used for locating the index of an airport in the tour from its code (e.g., “LAX”)
		// It is a dictionary (implemented as an AAXTree) storing key-value pairs,
		// where the keys are strings and the values are indices
		locator = new AAXTree<String, Integer>();
		
		tour = new ArrayList<LPoint>();
		
		// This is a 2-dimensional spatial index (implemented as a WKDTree) storing
		// the points (LPoint).
		spatial = new WKDTree<LPoint>();
	}
	
	/*
	 * Appends the labeled point pt to the end of
	 * the tour. If there exists a point with this label, an exception with the error message
	 * “Duplicate label” is thrown. If there already exists a point with the same coordinates,
	 * an exception with the error message “Duplicate coordinates” is thrown. Otherwise,
	 * the point is added to the tour, its index is added to the locator, and the point is added
	 * to the spatial index.
	 */
	public void append(LPoint pt) throws Exception { 
		String label = pt.getLabel();
		if (locator.find(label) != null) {
			throw new Exception("Duplicate label");
		} else if (spatial.find(pt.getPoint2D()) != null) {
			throw new Exception("Duplicate coordinates");
		}
		else {
			int index = tour.size(); // index where append will occur
			tour.add(pt); // append to tour
			locator.insert(label, index); // save the location
			spatial.insert(pt);
		}
	}
	
	public ArrayList<LPoint> list() { return tour; }
	
	/*
	 * clear all three data structure
	 */
	public void clear() { 
		tour.clear();
		locator.clear();
		spatial.clear();
	}
	
	/*
	 * calculate the cost from a place to another
	 * */
	public double cost() { 
		int i = 0;
		double sum = 0.0;
		if (tour.size() == 0)
			return sum;
		for (i = 1; i < tour.size(); i++) {
			sum += tour.get(i-1).getPoint2D().distanceSq(tour.get(i).getPoint2D());
		}
		sum += tour.get(i-1).getPoint2D().distanceSq(tour.get(0).getPoint2D());
		return sum;
	}
	
	/*
	 * helper method for reverse method
	 */
	private void reverseSubtour(int loc1, int loc2) throws Exception {
		if (loc1 > loc2) { // swap so that loc1 < loc2
			int temp = loc1;
			loc1 = loc2;
			loc2 = temp;
		}
		int i = loc1 + 1; // indices of current items
		int j = loc2;
		while (i < j) { // swap tour[i] with tour[j]
			LPoint pi = tour.get(i);
			LPoint pj = tour.get(j);
			tour.set(i, pj);
			tour.set(j, pi);
			locator.replace(pi.getLabel(), j); // update locators
			locator.replace(pj.getLabel(), i);
			i++;
			j--;
		}
	}
	
	/*
	 * reverse two location
	 */
	public void reverse(String label1, String label2) throws Exception { 
		if (locator.find(label1) == null || locator.find(label2) == null)
			throw new Exception("Label not found");
		if (locator.find(label1) == locator.find(label2)) {
			throw new Exception("Duplicate label");
		}
		reverseSubtour(locator.find(label1), locator.find(label2));
	}
	
	/*
	 * This is the same
	 * as reverse above, but after checking the validity of the arguments, instead of reverse,
	 * the operation 2-Opt(i, j) is performed on the tour.
	 */
	public boolean twoOpt(String label1, String label2) throws Exception { 
		double newDistance = 0.0, oldDistance = 0.0;
		if (locator.find(label1) == null || locator.find(label2) == null)
			throw new Exception("Label not found");
		int i = locator.find(label1), j = locator.find(label2);
		newDistance = tour.get(i).getPoint2D().distanceSq(tour.get(j).getPoint2D());
		newDistance += tour.get((i + 1) % tour.size()).getPoint2D().distanceSq(tour.get((j + 1) % tour.size()).getPoint2D());
		oldDistance = tour.get(i).getPoint2D().distanceSq(tour.get((i + 1) % tour.size()).getPoint2D());
		oldDistance += tour.get(j).getPoint2D().distanceSq(tour.get((j + 1) % tour.size()).getPoint2D());
		if (newDistance - oldDistance < 0) {
			reverse(label1, label2);
			return true;
		}
		else
			return false;
	} 
	
	/*
	 * This first locates the index i for the
	 * tour point with label label.
	 */
	public LPoint twoOptNN(String label) throws Exception { 
		Point2D center = null;
		int j = 0;
		String label2 = "";
		LPoint result = null;
		if (locator.find(label) == null)
			throw new Exception("Label not found");
		for (int i = 0; i < tour.size(); i++) {
			if (tour.get(i).getLabel().equals(label)) {
				center = tour.get(i).getPoint2D();
				j = i;
			}
		}
		if (center != null) {
			double radius = center.distance(tour.get((j + 1) % tour.size()).getPoint2D());
			result = spatial.fixedRadNN(center, radius);
			if (result == null)
				return null;
			label2 = result.getLabel();
			if (twoOpt(label, label2))
				return result;
		}
		if (result == null)
			return null;
		else {
			label2 = result.getLabel();
			if (twoOpt(label, label2))
				return result;
		}
		return null;
	}
	
	// This performs the operation all-2-Opt() on the tour. 
	public int allTwoOpt() throws Exception { 
		int result = 0;
		for (int i = 0; i < tour.size(); i++) {
			for (int j = i + 1; j < tour.size(); j ++) {
				if (twoOpt(tour.get(i).getLabel(), tour.get(j).getLabel()))
					result += 1;
			}
		}
		return result;
	}

}