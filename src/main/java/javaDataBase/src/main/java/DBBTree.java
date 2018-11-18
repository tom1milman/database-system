/**
 * Java Data Base - semester project
 * Based on the BTree algorithm from https://piazza.com/class/jc9w38vw4ly7io?cid=162
 * Changes by Tom Milman
 */
package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.Arrays;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription.DataType;

public class DBBTree<Key extends Comparable<Key>, Value>  {
	//max children per B-tree node = MAX-1 (must be an even number and greater than 2)
	private static final int MAX = 4;
	private Node root; //root of the B-tree
	private Node leftMostExternalNode;
	private int height; //height of the B-tree
	private int n; //number of key-value pairs in the B-tree
	private DataType type;

	//B-tree node data type
	private static final class Node
	{
		private int entryCount; // number of entries
		private Entry[] entries = new Entry[DBBTree.MAX]; // the array of children
		private Node next;
		private Node previous;

		// create a node with k entries
		private Node(int k)
		{
			this.entryCount = k;
		}

		private void setNext(Node next)
		{
			this.next = next;
		}
		private Node getNext()
		{
			return this.next;
		}
		private void setPrevious(Node previous)
		{
			this.previous = previous;
		}
		private Node getPrevious()
		{
			return this.previous;
		}	

		private Entry[] getEntries()
		{
			return Arrays.copyOf(this.entries, this.entryCount);
		}
	}

	//internal nodes: only use key and child
	//external nodes: only use key and value
	public static class Entry
	{
		private Comparable key;
		private Object val;
		private Node child;

		public Entry(Comparable key, Object val, Node child)
		{
			this.key = key;
			this.val = val;
			this.child = child;
		}
		public Object getValue()
		{
			return this.val;
		}
		public Comparable getKey()
		{
			return this.key;
		}
	}

	/**
	 * Initializes an empty B-tree.
	 */
	public DBBTree(DataType type) {
		this.root = new Node(0);
		this.leftMostExternalNode = this.root;
		this.type = type;
	}

	/**
	 * Returns true if this symbol table is empty.
	 * 
	 * @return {@code true} if this symbol table is empty; {@code false}
	 *         otherwise
	 */
	public boolean isEmpty()
	{
		return this.size() == 0;
	}

	public int size()
	{
		return this.n;
	}

	public int height()
	{
		return this.height;
	}

	public ArrayList<Entry> getOrderedEntries()
	{
		Node current = this.leftMostExternalNode;
		ArrayList<Entry> entries = new ArrayList<Entry>();
		while(current != null)
		{
			for(Entry e : current.getEntries())
			{
				if(e.val != null)
				{
					entries.add(e);
				}
			}
			current = current.getNext();
		}
		return entries;
	}

	public Entry getMinEntry()
	{
		Node current = this.leftMostExternalNode;
		while(current != null)
		{
			for(Entry e : current.getEntries())
			{
				if(e.val != null)
				{
					return e;
				}
			}
		}
		return null;	
	}

	public Entry getMaxEntry()
	{
		ArrayList<Entry> entries = this.getOrderedEntries();
		return entries.get(entries.size()-1);
	}

	public Value get(Key key)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("argument to get() is null");
		}
		Entry entry = this.get(this.root, key, this.height);
		if(entry != null)
		{
			return (Value)entry.val;
		}
		return null;
	}

	private Entry get(Node currentNode, Key key, int height)
	{
		Entry[] entries = currentNode.entries;

		//current node is external (i.e. height == 0)
		if (height == 0)
		{
			for (int j = 0; j < currentNode.entryCount; j++)
			{
				if(isEqual(key, entries[j].key, this.type))
				{
					//found desired key. Return its value
					return entries[j];
				}
			}
			//didn't find the key
			return null;
		}

		//current node is internal (height > 0)
		else
		{
			for (int j = 0; j < currentNode.entryCount; j++)
			{
				//if (we are at the last key in this node OR the key we 
				//are looking for is less than the next key, i.e. the
				//desired key must be in the subtree below the current entry),
				//then recurse into the current entry’s child
				if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key, this.type))
				{
					return this.get(entries[j].child, key, height - 1);
				}
			}
			//didn't find the key
			return null;
		}
	}

	public void delete(Key key)
	{
		put(key, null);
	}

	public void put(Key key, Value val)
	{
		if (key == null)
		{
			throw new IllegalArgumentException("argument key to put() is null");
		}
		//if the key already exists in the b-tree, simply replace the value
		Entry alreadyThere = this.get(this.root, key, this.height);
		if(alreadyThere != null)
		{
			alreadyThere.val = val;
			return;
		}

		Node newNode = this.put(this.root, key, val, this.height);
		this.n++;
		if (newNode == null)
		{
			return;
		}

		//split the root:
		//Create a new node to be the root.
		//Set the old root to be new root's first entry.
		//Set the node returned from the call to put to be new root's second entry
		Node newRoot = new Node(2);
		newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
		newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
		this.root = newRoot;
		//a split at the root always increases the tree height by 1
		this.height++;
	}

	private Node put(Node currentNode, Key key, Value val, int height)
	{
		int j;
		Entry newEntry = new Entry(key, val, null);

		//external node
		if (height == 0)
		{
			//find index in currentNode’s entry[] to insert new entry
			for (j = 0; j < currentNode.entryCount; j++)
			{
				if (less(key, currentNode.entries[j].key, this.type))
				{
					break;
				}
			}
		}

		// internal node
		else
		{
			//find index in node entry array to insert the new entry
			for (j = 0; j < currentNode.entryCount; j++)
			{
				//if (we are at the last key in this node OR the key we 
				//are looking for is less than the next key, i.e. the
				//desired key must be added to the subtree below the current entry),
				//then do a recursive call to put on the current entry’s child
				if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key, this.type))
				{
					//increment j (j++) after the call so that a new entry created by a split
					//will be inserted in the next slot 
					Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
					if (newNode == null)
					{
						return null;
					}
					//if the call to put returned a node, it means I need to add a new entry to
					//the current node
					newEntry.key = newNode.entries[0].key;
					newEntry.val = null;
					newEntry.child = newNode;
					break;
				}
			}
		}
		//shift entries over one place to make room for new entry
		for (int i = currentNode.entryCount; i > j; i--)
		{
			currentNode.entries[i] = currentNode.entries[i - 1];
		}
		//add new entry
		currentNode.entries[j] = newEntry;
		currentNode.entryCount++;
		if (currentNode.entryCount < DBBTree.MAX)
		{
			//no structural changes needed in the tree
			//so just return null
			return null;
		}
		else
		{
			//will have to create new entry in the parent due
			//to the split, so return the new node, which is 
			//the node for which the new entry will be created
			return this.split(currentNode, height);
		}
	}

	private Node split(Node currentNode, int height)
	{
		Node newNode = new Node(DBBTree.MAX / 2);
		//by changing currentNode.entryCount, we will treat any value
		//at index higher than the new currentNode.entryCount as if
		//it doesn't exist
		currentNode.entryCount = DBBTree.MAX / 2;
		//copy top half of h into t
		for (int j = 0; j < DBBTree.MAX / 2; j++)
		{
			newNode.entries[j] = currentNode.entries[DBBTree.MAX / 2 + j];
		}
		//external node
		if (height == 0)
		{
			newNode.setNext(currentNode.getNext());
			newNode.setPrevious(currentNode);
			currentNode.setNext(newNode);
		}
		return newNode;
	}

	// comparison functions - make Comparable instead of Key to avoid casts
	private static boolean less(Comparable k1, Comparable k2, DataType type) {
		return compareKeys(k1, k2, type) < 0;
	}

	private static boolean isEqual(Comparable k1, Comparable k2 , DataType type) {
		return compareKeys(k1, k2, type) == 0;
	}

	private static int compareKeys (Comparable k1, Comparable k2 , DataType type) {
		if (type == DataType.DECIMAL) {
			Float key1 = Float.parseFloat((String) k1);
			Float key2 = Float.parseFloat((String) k2);
			
			if (key1 > key2)
				return 1;
			else if (key1 < key2)
				return -1;
			else
				return 0;
		}
		
		else if (type == DataType.INT) {
			Integer key1 = Integer.parseInt((String) k1);
			Integer key2 = Integer.parseInt((String) k2);
			
			if (key1 > key2)
				return 1;
			else if (key1 < key2)
				return -1;
			else
				return 0;
		}
		
		else 
			return k1.compareTo(k2);
	}
	
	public String toString() {
		return toString(root, height, "") + "\n";
	}
	
	private String toString(Node h, int ht, String indent) {
		StringBuilder s = new StringBuilder();
		Entry[] children = h.entries;

		if (ht == 0) {
			for (int j = 0; j < h.entryCount; j++) {
				s.append(indent + children[j].key + " " + children[j].val + "\n");
			}
		}
		else {
			for (int j = 0; j < h.entryCount; j++) {
				if (j > 0) s.append(indent + "(" + children[j].key + ")\n");
				s.append(toString(children[j].child, ht-1, indent + "     "));
			}
		}
		return s.toString();
	}	
}


