/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateTableQuery;

public class DBTable {
	private String tableName;
	private int numColumns;
	private List<ArrayList> table; //Main table of a data base
	private ArrayList<ColumnDescription> columnDescription; //Keeps ColumnDescription for each column of the table
	private HashMap<String, Integer> columnsIndecis;
	private ColumnDescription primaryKey;
	private ArrayList <String> primaryEnteries;
	private HashMap <String, DBIndex> indexTrees;

	//Constructor used by DBSelect for temporary table
	public DBTable () {
		this.table = new ArrayList<ArrayList>();
	}
	
	//Constructor used by the DBExecuter
	public DBTable (CreateTableQuery queryObject) {
		this.tableName = queryObject.getTableName().toLowerCase();
		this.numColumns = queryObject.getColumnDescriptions().length;
		this.table = new ArrayList<ArrayList>();
		this.columnDescription = new ArrayList<ColumnDescription>(); 
		this.columnsIndecis = new HashMap <String, Integer>();
		this.primaryKey = queryObject.getPrimaryKeyColumn();
		this.primaryEnteries = new ArrayList <String>();
		this.indexTrees = new HashMap<String, DBIndex>();
		
		columnBuilder(queryObject);
		
		DBIndex primaryIndex = new DBIndex("Primary Indices", this, this.primaryKey);
		this.indexTrees.put(this.primaryKey.getColumnName().toLowerCase(), primaryIndex);
	}

	public String getTableName () {
		return this.tableName;
	}

	public int size() {
		return this.table.size();
	}
	
	public int rowSize() {
		return this.table.get(0).size();
	}

	public int getNumColumns () {
		return this.numColumns;
	}

	public List<ArrayList> getTable () {
		return this.table;
	}

	public ArrayList<ColumnDescription> getColumnDescription () {
		return this.columnDescription;
	}

	public ColumnDescription getPrimaryKey () {
		return this.primaryKey;
	}

	public ArrayList <String> getPrimaryEntehries () {
		return this.primaryEnteries;
	}

	//Checks if a given column name exists in the table
	public ColumnDescription getColumn (String columnName) {
		for (ColumnDescription column : this.columnDescription) {
			if (columnName.equalsIgnoreCase(column.getColumnName()))
				return column;
		}

		return null;
	}

	//returns the index of a column in the table
	public int getColumnIndex (String name) {
		return columnsIndecis.get(name);
	}

	//returns all the BTrees related to this table
	public HashMap<String, DBIndex> getIndexTrees () {
		return this.indexTrees;
	}
	
	//returns a row
	public ArrayList getRow (int index) {
		try {
			return this.table.get(index);
		} catch (Exception e) {
			return null;
		}
	}

	//Adds a given row (used by DBSelect temporary table)
	public void addRow (ArrayList row) {
		this.table.add(row);
	}

	public void deleteRow (int index) {
		this.table.remove(index);
	}

	//Adds new row to a table
	public void newEntery (ArrayList values, String primary) {
		this.table.add(values);
		//this.primaryEnteries.add(primary);
	}

	//Checks if the given value was already used as a primary value
	public boolean isPrimaryValue (String value) {
		DBIndex tree = this.indexTrees.get(this.primaryKey.getColumnName().toLowerCase());
		
		if (tree.get(value) == null)
			return false;

		return true;
	}

	//Checks if the given value was already used as a unique value
	public boolean isUniqueVale (ColumnDescription column, String string) {
		DBIndex tree = this.indexTrees.get(column.getColumnName().toLowerCase());

		if (tree.get(string) == null)
			return false;

		return true;
		/*
		int temp = 0;

		for (int i = 0; i < this.table.get(0).size(); i++) {
			if (column.equals(this.table.get(0).get(i)))
				break;

			temp++;
		}

		for (int i = 0; i < this.table.size(); i++) {
			if (string.equals(this.table.get(i).get(temp)))
				return true;
		}

		return false;*/
	}
	
	//sets a row in the table 
	public void setRow (ArrayList<String> newRow, int index) {
		this.table.set(index, newRow);
	}

	//checks if the given columns are all indexed (used for the where condition)
	public boolean areIndexed () {
		return false;
	}
	
	//-------Printing Method--------
	public void printTable () {
		System.out.println("Table: " + this.tableName);
		for (int i = 0; i < this.table.size(); i++) {
			System.out.print("Line " + i + ":   ");

			for (int j = 0; j < this.table.get(i).size(); j++) {				
				if (this.table.get(i).get(j) != null && this.table.get(i).get(j).getClass() == ColumnDescription.class) {
					ColumnDescription temp = (ColumnDescription) this.table.get(i).get(j);
					System.out.printf("%-20s", temp.getColumnName());
				}

				else
					System.out.printf("%-20s", this.table.get(i).get(j));
			}

			System.out.println();
		}
		System.out.println();
	}

	@SuppressWarnings("unchecked")
	//Builds a table with one row (0) that represents the name of the column
	//Every row represent a new object in a data base
	// table.getIndex(i).getIndex(j) gets an entry for i-th object(row) at j-th column
	private void columnBuilder (CreateTableQuery queryObject) {	
		ColumnDescription [] columnList = queryObject.getColumnDescriptions();

		ArrayList temp = new ArrayList ();

		for (int i = 0; i < columnList.length; i++) {
			temp.add(columnList[i]);
			columnDescription.add(columnList[i]);
			columnsIndecis.put(columnList[i].getColumnName(), i);
			if (!queryObject.getPrimaryKeyColumn().equals(columnList[i]) && columnList[i].isUnique()) {
				DBIndex tempIndex = new DBIndex(columnList[i].getColumnName(), this, columnList[i]);
				this.indexTrees.put(columnList[i].getColumnName().toLowerCase(), tempIndex);
			}
		}

		table.add(temp);
	}

}
