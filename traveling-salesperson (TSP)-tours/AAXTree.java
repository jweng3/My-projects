import java.util.ArrayList;

/** AAXTree (skeleton)
 *
 * MODIFY THE FOLLOWING CLASS.
 *
 * You are free to make whatever changes you like or to create additional
 * classes and files, but avoid reusing/modifying the other files given in this
 * folder.
 */

public class AAXTree<Key extends Comparable<Key>, Value> {

	private Node root;
	private abstract class Node {
		
		// -----------------------------------------------------------------
		// Standard dictionary helpers
		// -----------------------------------------------------------------
		
		abstract Value find(Key x);
		
		abstract Node insert(Key x, Value v) throws Exception;
		
		abstract Node delete(Key x) throws Exception;
		
		abstract void replace(Key x, Value v) throws Exception;
		
		abstract ArrayList<String> getPreorderList(); // list entries in preorder
		
		
		// -----------------------------------------------------------------
		// Rebalancing utilities
		// -----------------------------------------------------------------
		
		abstract Node skew();
		
		abstract Node split();
		
		abstract void updateLevel(); // update level (ignored if external)

		abstract Node fixAfterDelete(); // fix structures after deletion (ignored if external)
		
		// -----------------------------------------------------------------
		// Accessors
		// -----------------------------------------------------------------
		
		abstract void setLeft(Node p);
		
		abstract void setRight(Node p);
		
		abstract void setLevel(int level);
		
		abstract Node getRight();
		
		abstract Node getLeft();
		
		abstract int getLevel();
		
		abstract Key getKey();
		
		abstract Value getValue();
	}

	private class InternalNode extends Node {
		private Key key;
		private Node left, right;
		private int level;

		/**
		 * Basic constructor.
		 */
		
		InternalNode (Key x, int level, Node left, Node right) {
			key = x;
			this.level = level;
			this.left = left;
			this.right = right;
		}

		Value find(Key x) {
			if (this.equals(left) && this.equals(right)) {
				return this.find(x);
			}
			if (x.compareTo(key) < 0) {
				return getLeft().find(x);
			} else 
				return getRight().find(x);
		}

		@Override
		Node insert(Key x, Value v) throws Exception {
			if (x.compareTo(this.key) < 0) 
				setLeft (getLeft().insert (x, v));
			else if (x.compareTo(key) >= 0)
				setRight (getRight().insert (x, v));
			return skew().split();
		}

		/**
		 * Right skew a node. If the left child has same level as us, perform a right
		 * rotation.
		 */
		Node skew() {
			if (getLeft().getLevel() == getLevel()) {
				Node q = getLeft();
				setLeft (q.getRight());
				q.setRight(this);
				return q;
			} else
				return this;
		}

		/**
		 * Split a node. If the right-right grandchild is at the same level, promote our
		 * right child to the next higher level.
		 */
		Node split() {
			if (right.getRight().getLevel() == level) {
				Node q = getRight();
				setRight (q.getLeft());
				q.setLeft(this);
				q.setLevel(this.level + 1);
				return q;
			} else 
				return this;
		}
		
		/**
		 * Get a list of the nodes in preorder.
		 *
		 * Adds current key in parentheses, followed by left and right subtrees.
		 */
		ArrayList<String> getPreorderList() {
			ArrayList<String> list = new ArrayList<String>();
			list.add(toString()); // add this node
			list.addAll(left.getPreorderList()); // add left
			list.addAll(right.getPreorderList()); // add right
			return list;
		}
		
		Node delete(Key x) throws Exception {
			Node result;
			if (x.compareTo(getKey()) < 0) 
				result = getLeft().delete(x);
			else 
				result = getRight().delete(x);
			if (result == null) {
				if (x.compareTo(getKey()) < 0)
					return getRight();
				else
					return getLeft();
			} else {
				if (x.compareTo(getKey()) < 0) {
					setLeft(result);
					return fixAfterDelete();
				}
				else {
					setRight(result);
					return fixAfterDelete();
				}
			}
		}

		/**
		 * Fix local structure after deletion. (See references on AA trees for an
		 * explanation of this combination of operations.)
		 */
		Node fixAfterDelete() {	
			updateLevel();
			Node node = this;
			node = skew();
			node.setRight(node.getRight().skew());
			node.getRight().setRight(node.getRight().getRight().skew());
			node = node.split();
			node.setRight(node.getRight().split());
			return node;
		}

		/**
		 * Update node's level from its children.
		 */
		void updateLevel() {
			int idealLevel = 1 + Math.min(getLeft().getLevel(), getRight().getLevel());
			if (getLevel() > idealLevel) {
				setLevel(idealLevel);
				if(getRight().getLevel() > idealLevel)
					getRight().setLevel(idealLevel);
			}
		}
		
		void replace(Key x, Value v) throws Exception {
			if (x.compareTo(key) < 0) // x < key
				left.replace(x, v); // ... search left
			else // x >= key
				right.replace(x, v);
		}
		
		/*
		* Getter and Setter
		*/

		Node getLeft() {
			return left;
		}

		void setLeft(Node left) {
			this.left = left;
		}

		Node getRight() {
			return right;
		}

		void setRight(Node right) {
			this.right = right;
		}

		int getLevel() {
			return level;
		}

		void setLevel(int level) {
			this.level = level;
		}


		@Override
		Key getKey() {
			return this.key;
		}
		Value getValue() {
			return null;
		}
		
		
		/**
		 * toString method.
		 */
		public String toString() {
			String s = "(" + key + ") " + level;
			return s;
		}

	}
	
	// -----------------------------------------------------------------
	// External node
	// -----------------------------------------------------------------
	
	private class ExternalNode extends Node {
		
		private Key key;
		private Value value;

		ExternalNode (Key key, Value value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * Find a key.
		 */
		Value find(Key x) {
			if (x.compareTo(this.key) == 0)
				return this.value;
			else
				return null;
		}
		

		/**
		 * Insert key-value pair. If the keys match, we throw a duplicate-key exception.
		 * Otherwise, we create a three-node combination with the two keys and a level-1
		 * internal node between them.
		 */
		@Override
		Node insert(Key x, Value v) throws Exception{
			if (x.compareTo(this.key) == 0) 
				throw new Exception("Insertion of duplicate key");
			else if (x.compareTo(this.key) < 0) {
				InternalNode p = new InternalNode(this.getKey(), 1, null, null);
				ExternalNode q = new ExternalNode(x,v);
				p.setLeft(q);
				p.setRight(this);
				return p;
			} else {
				InternalNode p = new InternalNode(x, 1, null, null);
				ExternalNode q = new ExternalNode(x,v);
				p.setRight(q);
				p.setLeft(this);
				return p;
			}
		}
		
		/**
		 * Get preorder list. This just returns an encoding of the data in this node.
		 */
		ArrayList<String> getPreorderList() {
			ArrayList<String> list = new ArrayList<String>();
			list.add(toString()); // add this node
			return list;
		}
		
		/**
		 * Delete a key. We simply unlink this node and return null;
		 */
		Node delete(Key x) throws Exception{
			if(x.compareTo(getKey()) != 0)
				throw new Exception("Deletion of nonexistent key");
			else {
				return null;
			}
		}

		@Override
		Value getValue() {
			return this.value;
		}
		
		void replace(Key x, Value v) throws Exception {
			if (x.compareTo(key) == 0)
				this.value = v;
			else
				throw new Exception("Replacement of nonexistent key");
		}
		

		
		// -----------------------------------------------------------------
		// Rebalancing utilities - Do nothing for external nodes
		// -----------------------------------------------------------------
		@Override
		Node skew() {
			return this;
		}

		@Override
		Node split() {
			return this;
		}
		
		void updateLevel() {
			return;
		}

		Node fixAfterDelete() {
			return this;
		}
		
		// -----------------------------------------------------------------
		// Getter and setter
		// -----------------------------------------------------------------

		ExternalNode getLeft() {
			return this;
		}

		void setLeft(Node left) {
			return;
		}

		ExternalNode getRight() {
			return this;
		}

		void setRight(Node right) {
			return;
		}

		int getLevel() {
			return 0;
		}

		void setLevel(int level) {
			return;
		}

		public String toString() {
			String s = "[" + key + " " + value + "]";
			return s;
		}

		@Override
		Key getKey() {
			return this.key;
		}


	}

	
	// -----------------------------------------------------------------
	// Public methods
	// -----------------------------------------------------------------

	
	public AAXTree() { 
		root = null;
	}

	
	/**
	 * Find value of a given key.
	 *
	 * @param x The key to find
	 * @return The associated value if found
	 */
	public Value find(Key k) {
		if (root == null)
			return null;
		else
			return root.find(k);
	}
	
	/**
	 * Insert a key-value pair. Throws an exception if duplicate key found.
	 *
	 * @param x The key to insert
	 * @param v The associated value
	 * @throws Exception If the key is already in the tree
	 */
	public void insert(Key x, Value v) throws Exception { 
		if (root == null) 
			this.root = new ExternalNode(x,v);
		else {
			this.root = root.insert(x,v);
		}
	}
	
	/**
	 * Clear the tree, removing all entries.
	 *
	 */
	public void clear() { 
		root = null;
	}
	
	/**
	 * Get a list of entries in preorder
	 *
	 * @return ArrayList of string encoded items in preorder
	 */
	public ArrayList<String> getPreorderList() { 
		ArrayList<String> result = new ArrayList<String>();
		if (root != null) {
			preOrder(root, result);
		}
		return result;
	}

	/**
	 * helper method for PreorderList
	 */
	public void preOrder(Node node, ArrayList<String> result) {
		result.add(node.toString());
		if (node.getKey() != node.getLeft().getKey() && node != node.getRight().getKey()) {
			preOrder(node.getLeft(), result);
			preOrder(node.getRight(), result);
		}
	}

	/**
	 * Delete a key.
	 *
	 * @param x The key to delete
	 * @throws Exception If the key is not in the tree
	 */
	public void delete(Key x) throws Exception {
		if (root == null)
			throw new Exception("Deletion of nonexistent key");
		else
			root = root.delete(x);
	}
	
	/**
	 * Number of entries in the dictionary.
	 *
	 * @return Number of entries in the dictionary
	 */
	public int size() { 
		return findLeaf(root);
	}
	
	public int findLeaf(Node node) {
		if (node == null)
			return 0;
		else if (node.getLeft().getKey() == node.getKey() && node.getRight().getKey() == node.getKey())
			return 1;
		else
			return findLeaf(node.getLeft()) + findLeaf(node.getRight());
	}
	
	/**
	 * Get value associated with minimum key.
	 *
	 * @return A reference to the associated value, or null if tree empty
	 */
	public Value getMin() {
		if(root == null) 
			return null;
		Node node = root;
		while (node.getKey().compareTo(node.getLeft().getKey()) != 0) 
			node = node.getLeft();
		return node.getValue(); 
	}
	
	/**
	 * Get value associated with maximum key.
	 *
	 * @return A reference to the associated value, or null if tree empty
	 */
	public Value getMax() {
		if(root == null) 
			return null;
		Node node = root;
		while (node.getKey().compareTo(node.getLeft().getKey()) != 0) 
			node = node.getRight();
		return node.getValue(); 
	}
	
	/**
	 * Find next value with key lesser or equal.
	 *
	 * @param x The key being sought
	 * @return A reference to the associated value
	 */
	public Value findSmaller(Key x) { 
		ArrayList<String> path = new ArrayList<String>();
		int backtracking_stop = -1;
		Node node = root;
		if (root == null)
			return null;
		// primary search
		for (int i = 0; node.getKey().compareTo(node.getLeft().getKey()) != 0; i++) {
			if (x.compareTo(node.getKey()) < 0) {
				node = node.getLeft();
				path.add("Left");
			} else {
				node = node.getRight();
				path.add("Right");
				backtracking_stop = i;
			}
		}
		if (backtracking_stop < 0)
			return null;
		if (x.compareTo(node.getKey()) > 0)
			return node.getValue();
		node = root;
		// secondary search
		for (int i = 0; node.getKey().compareTo(node.getLeft().getKey()) != 0; i++) {
			if (i < backtracking_stop) {
				if (path.get(i).equals("Left"))
					node = node.getLeft();
				else
					node = node.getRight();
			}
			else if (i == backtracking_stop) {
				node = node.getLeft();
			}
			else 
				node = node.getRight();
		}
		return node.getValue();
	}

	/**
	 * Find next value of strictly greater key.
	 *
	 * @param x The key being sought
	 * @return A reference to the associated value
	 */
	public Value findLarger(Key x) { 
		ArrayList<String> path = new ArrayList<String>();
		int backtracking_stop = -1;
		Node node = root;
		if (root == null)
			return null;
		// primary search
		for (int i = 0; node.getKey().compareTo(node.getLeft().getKey()) != 0; i++) {
			if (x.compareTo(node.getKey()) < 0) {
				node = node.getLeft();
				path.add("Left");
				backtracking_stop = i;
			} else {
				node = node.getRight();
				path.add("Right");
			}
		}
		if (backtracking_stop < 0)
			return null;
		if (x.compareTo(node.getKey()) < 0)
			return node.getValue();
		node = root;
		// secondary search
		for (int i = 0; node.getKey().compareTo(node.getLeft().getKey()) != 0; i++) {
			if (i < backtracking_stop) {
				if (path.get(i).equals("Left"))
					node = node.getLeft();
				else
					node = node.getRight();
			}
			else if (i == backtracking_stop) {
				node = node.getRight();
			}
			else 
				node = node.getLeft();
		}
		return node.getValue();
	}
	
	/**
	 * Delete the entry with the smallest key.
	 *
	 * @return A reference to the associated value
	 */
	public Value removeMin() { 
		Node node = root;
		Value result;
		if (root == null)
			return null;
		while(node.getKey().compareTo(node.getLeft().getKey()) != 0) {
			node = node.getLeft();
		}
		result = node.getValue();
		try {
			delete(node.getKey());
		} catch (Exception e) {
			return null;
		}
		return result;
	}
	
	/**
	 * Delete the entry with the largest key.
	 *
	 * @return A reference to the associated value
	 */
	public Value removeMax() { 
		Node node = root;
		Value result;
		if (root == null)
			return null;
		while(node.getKey().compareTo(node.getLeft().getKey()) != 0) {
			node = node.getRight();
		}
		result = node.getValue();
		try {
			delete(node.getKey());
		} catch (Exception e) {
			return null;
		}
		return result;
	}
	
	
	/**
	 *  Replace value for key k. Throw exception if key not found.
	 *
	 */
	public void replace(Key k, Value v) throws Exception { 
		if (root == null) {
			throw new Exception("Replacement of nonexistent key");
		} else {
			root.replace(k, v);
		}
	}

}
