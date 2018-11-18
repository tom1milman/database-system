/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateIndexQuery;

public class DBIndex {
	private String name;
	private DBTable table;
	private DBBTree<String, ArrayList<String>> BTree;
	private ColumnDescription column;

	public DBIndex (CreateIndexQuery queryObject, DBTable table) {
		this.name = queryObject.getIndexName();
		this.table = table;
		this.column = getColumn(queryObject);
		this.BTree = new DBBTree<String, ArrayList<String>>(this.column.getColumnType());
	}

	public DBIndex (String name, DBTable table, ColumnDescription column) {
		this.name = name;
		this.table = table;
		this.column = column;
		this.BTree = new DBBTree<String, ArrayList<String>>(this.column.getColumnType());
	}

	public String getName () {
		return this.name;
	}

	public DBTable getTable () {
		return this.table;
	}

	public ColumnDescription getColumn () {
		return this.column;
	}

	public ArrayList<String> get (String key) {
		return this.BTree.get(key);
	}

	public void put (ArrayList<String> row) {
		int column = this.table.getColumnIndex(this.column.getColumnName());
		String key = row.get(column);
		this.BTree.put(key, row);		
	}

	public void delete (ArrayList<String> row) {
		int column = this.table.getColumnIndex(this.column.getColumnName());
		String key = row.get(column);

		this.BTree.delete(key);
	}

	//creates the index
	@SuppressWarnings("unchecked")
	public void createIndex () {
		int index = table.getColumnIndex(column.getColumnName());

		for (int row = 1; row < this.table.size(); row++) {
			ArrayList<String> tempRow = this.table.getRow(row);
			String key = (String) this.table.getRow(row).get(index);

			this.BTree.put(key, tempRow);
		}
	}

	private ColumnDescription getColumn (CreateIndexQuery queryObject) {
		ColumnDescription col = this.table.getColumn(queryObject.getColumnName());

		if (col == null) 
			throw new NoSuchElementException("Error 404: " + queryObject.getColumnName() + " column not found in table " + this.table.getTableName());

		return col;
	}

	//prints the tree
	//The toString of the BTrees is takes from the Sedgewick BTree algorithm
	public void printTree () {
		System.out.println(this.BTree.toString());
	}
}
