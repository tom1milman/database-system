/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription.DataType;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnID;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery.FunctionInstance;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery.OrderBy;

public class DBSelect {
	private DBTable table, returnTable;
	private ArrayList <FunctionInstance> functions;
	private ArrayList <Float> functionResults;
	private ArrayList <ColumnID> columns;
	private Condition where;
	private OrderBy [] orders;
	private boolean isDistinct;
	private int functionIndex;

	public DBSelect (SelectQuery queryObject, DBTable table) {
		this.table = table;
		this.returnTable = returnTableCreator(table);
		this.functions = queryObject.getFunctions();
		this.functionResults = new ArrayList<Float>();
		this.columns = arrayToArrayList(queryObject.getSelectedColumnNames());
		this.where = queryObject.getWhereCondition();
		this.orders = queryObject.getOrderBys();
		this.isDistinct = queryObject.isDistinct();
	}

	//the main method for Select statement
	public DBTable select () {
		columnsChecker();
		orderFunction();
		deleteRows();
		deleteColumns(); 
		selectFunctions();
		tableBuilder(); 
		if (isDistinct) 
			distinctValues();

		return returnTable;
	}

	@SuppressWarnings("unchecked")
	//Builds the table ordering columns and inputing the function columns and results in
	private <T> void tableBuilder () {
		HashMap<String, Integer> returnTableIndices = returnTableColumnIndices();

		for (int tableRow = 0; tableRow < this.returnTable.size(); tableRow++) {
			ArrayList<T> oldRow = this.returnTable.getRow(tableRow);			
			ArrayList<T> newRow = new ArrayList<T>();
			this.functionIndex = 0;

			for (int column = 0; column < columns.size(); column++) {
				if (this.columns.get(0).getColumnName().equals("*"))
					newRowBuilder(column, oldRow, newRow);
				else 
					newRowBuilder(returnTableIndices, tableRow, column, oldRow, newRow);
			}

			updateRow(newRow ,tableRow, oldRow.size());
		}
	}

	//Builds the new row for the return table
	@SuppressWarnings("unchecked")
	public <T> void newRowBuilder (HashMap<String, Integer> returnTableIndices, int tableRow, int column, ArrayList<T> oldRow, ArrayList<T> newRow) {
		int index = 0;
		if (returnTableIndices.get(columns.get(column).getColumnName().toLowerCase()) != null) //???
			index = returnTableIndices.get(columns.get(column).getColumnName().toLowerCase());
		
		T value = null;

		if (functions.size() > 0 && this.functionIndex < functions.size() && columns.get(column) == functions.get(this.functionIndex).column) {
			if (tableRow == 0)
				value = (T) (functions.get(this.functionIndex).function + " (" + functions.get(this.functionIndex).column.getColumnName() + ")");
			else
				value = (T) (functionResults.get(this.functionIndex).toString());

			this.functionIndex++;
		}

		else
			value = oldRow.get(index);

		newRow.add(value);
	}

	//Builds the new row for the return table
	public <T> void newRowBuilder (int column, ArrayList<T> oldRow, ArrayList<T> newRow) {
		newRow.add(oldRow.get(column));
	}

	//Checks for distinct rows and removes the non-distinct ones
	@SuppressWarnings("unchecked")
	private void distinctValues () {
		ArrayList<ArrayList<String>> distincts = new ArrayList<ArrayList<String>>();
		ArrayList<Integer> notDistinctsIndices = new ArrayList<Integer>();

		distincts.add(this.returnTable.getRow(1));

		for (int tableRow = 2; tableRow < this.returnTable.size(); tableRow++) {
			ArrayList<String> row = this.returnTable.getRow(tableRow);
			if (isRowDistinct(distincts, row))
				distincts.add(row);
			else
				notDistinctsIndices.add(tableRow);
		}

		for (int rowIndex = notDistinctsIndices.size() - 1; rowIndex >= 0; rowIndex--)
			this.returnTable.deleteRow(notDistinctsIndices.get(rowIndex));
	}

	//Checks if the given row is distinct relatively to the other distinct rows in the table
	private boolean isRowDistinct (ArrayList<ArrayList<String>> distincts, ArrayList<String> row) {
		boolean distinct = true;

		for (ArrayList<String> otherRow : distincts) {
			int functionIndex = 0;
			for (int column = 0; column < row.size(); column++) {
				if (functions.size() > 0 && functionIndex < functions.size() && columns.get(column) == functions.get(functionIndex).column) {
					functionIndex++;
					continue;
				}

				if (row.get(column).equals(otherRow.get(column)))
					distinct = false;
				else {
					distinct = true;
					break;
				}
			}
		}
		return distinct;
	}

	@SuppressWarnings("unchecked")
	//Deletes rows that do not match the where condition
	private void deleteRows () {
		if (this.where == null) 
			return;

		DBWhere where = new DBWhere(this.where, this.table);

		for (int i = this.table.size() - 1; i > 0; i--) {
			if (!where.isMatch(this.table.getRow(i)))
				this.returnTable.deleteRow(i);
		}


	}

	//Deletes the columns that are not used by the select statement
	private void deleteColumns () {
		if (this.columns.get(0).getColumnName().equals("*"))
			return;

		for (int column = this.table.getColumnDescription().size() - 1; column >= 0; column--) {
			boolean bool = false;
			String columnName = this.table.getColumnDescription().get(column).getColumnName().toLowerCase();

			HashSet<String> columnsFromSelect = columnNames();

			for (String name : columnsFromSelect) {
				if (name.equalsIgnoreCase(columnName))
					bool = true;
			}

			if (!bool) {
				for (int i = 0; i < this.returnTable.size(); i++) 
					this.returnTable.getRow(i).remove(column);
			}
		}
	}

	//Executes the functionInstances
	private void selectFunctions () {
		for (FunctionInstance function : functions) {
			ColumnDescription column = this.table.getColumn(function.column.getColumnName());

			if (column == null)
				throw new NoSuchElementException("Error 404: " + function.column.getColumnName() + " column not found in table " + this.table.getTableName());

			this.functionResults.add(functionAction(function, column));
		}
	}

	//Executes the given function
	private float functionAction (FunctionInstance function, ColumnDescription column) {

		switch (function.function) {
		case AVG:	
			if (function.isDistinct)
				return distinctSumFunction(column, function, true);
			else {
				float [] results = sumFunction(column, function);
				return results[0] / results[1];
			}
		case COUNT:
			if (function.isDistinct)
				return distinctCount(column, function);
			else
				return this.returnTable.size() - 1;
		case MAX:
			return maxFunction(column, function);
		case MIN:
			return minFunction(column, function);
		case SUM:
			if (function.isDistinct)
				return distinctSumFunction(column, function, false);
			else
				return sumFunction(column, function)[0];
		default:
			return 0;
		}
	}

	//The max function
	private float maxFunction (ColumnDescription column, FunctionInstance function) {
		if (column.getColumnType() == DataType.VARCHAR || column.getColumnType() == DataType.BOOLEAN) 
			throw new IllegalArgumentException("Can't do " + function.function + " on a " + column.getColumnType() + " type");

		int columnIndex = this.table.getColumnIndex(column.getColumnName());

		float maxValue = Float.parseFloat((String) this.table.getRow(1).get(columnIndex));

		for (int i = 2; i < this.table.size(); i++) {
			if (this.table.getRow(i).get(columnIndex) == null)
				continue;

			float temp = Float.parseFloat((String) this.table.getRow(i).get(columnIndex));

			if (temp > maxValue)
				maxValue = temp;
		}

		return maxValue;
	}

	//The min function
	private float minFunction (ColumnDescription column, FunctionInstance function) {
		if (column.getColumnType() == DataType.VARCHAR || column.getColumnType() == DataType.BOOLEAN) 
			throw new IllegalArgumentException("Can't do " + function.function + " on a " + column.getColumnType() + " type");

		int columnIndex = this.table.getColumnIndex(column.getColumnName());

		float minValue = Float.parseFloat((String) this.table.getRow(1).get(columnIndex));

		for (int i = 2; i < this.table.size(); i++) {
			if (this.table.getRow(i).get(columnIndex) == null)
				continue;

			float temp = Float.parseFloat((String) this.table.getRow(i).get(columnIndex));

			if (temp < minValue)
				minValue = temp;
		}

		return minValue;
	}

	//The sum function
	private float [] sumFunction(ColumnDescription column, FunctionInstance function) {
		if (column.getColumnType() == DataType.VARCHAR || column.getColumnType() == DataType.BOOLEAN) 
			throw new IllegalArgumentException("Can't do " + function.function + " on a " + column.getColumnType() + " type");

		int columnIndex = this.table.getColumnIndex(column.getColumnName());
		float sum = 0;
		float rows = 0;

		for (int i = 1; i < this.table.size(); i++) {
			if (this.table.getRow(i).get(columnIndex) == null)
				continue;

			float temp = Float.parseFloat((String) this.table.getRow(i).get(columnIndex));
			sum += temp;
			rows++;
		}

		float [] results = {sum, rows};

		return results;
	}

	//The sum function but for distinct values
	private float distinctSumFunction (ColumnDescription column, FunctionInstance function, boolean avg) {
		if (column.getColumnType() == DataType.VARCHAR || column.getColumnType() == DataType.BOOLEAN) 
			throw new IllegalArgumentException("Can't do " + function.function + " on a " + column.getColumnType() + " type");

		HashSet<Float> values = new HashSet<Float>();
		int columnIndex = this.table.getColumnIndex(column.getColumnName());
		float sum = 0;

		for (int i = 1; i < this.table.size(); i++) {
			float temp = Float.parseFloat((String) this.table.getRow(i).get(columnIndex));
			if (values.add(temp))
				sum += temp;
		}

		if (avg)
			return sum / values.size();
		else
			return sum;
	}

	//Counts the amount of distinct values
	private float distinctCount (ColumnDescription column, FunctionInstance function) {
		HashSet<String> values = new HashSet<String>();
		int columnIndex = this.table.getColumnIndex(column.getColumnName());

		for (int i = 1; i < this.table.size(); i++) 
			values.add((String) this.table.getRow(i).get(columnIndex));
		return values.size();
	}

	//Executes the order by functions of the statement
	private void orderFunction () {
		int previousColumnIndex = 0;

		for (int order = 0; order < orders.length; order++) {
			for (int column = 0; column < this.returnTable.getRow(0).size(); column++) {
				ColumnDescription temp = (ColumnDescription) this.returnTable.getRow(0).get(column);

				if (orders[order].getColumnID().getColumnName().equalsIgnoreCase(temp.getColumnName())) {
					if (order == 0) 
						sorting(column, 1, this.returnTable.size()-1, temp.getColumnType(), orders[order].isAscending());					

					else {
						int startPoint = 0, endPoint = 0;

						while (endPoint + 1 != this.returnTable.size()) {

							int [] points = sortingPoints(previousColumnIndex, endPoint);
							startPoint = points[0];
							endPoint = points[1];
							sorting(column, startPoint, endPoint, temp.getColumnType(), orders[order].isAscending());
						}

					}

					previousColumnIndex = column;
				}
			}
		}
	}

	//Algorithm was adapted from https://www.geeksforgeeks.org/bubble-sort/
	@SuppressWarnings("unchecked")
	private void sorting (int column, int startPoint, int endPoint, DataType type, boolean ASC) {
		for (int i = startPoint; i < endPoint; i++) {
			for (int j = startPoint; j < endPoint -i+1; j++) {
				String currentValue = (String) this.returnTable.getRow(j).get(column);
				String nextValue = (String) this.returnTable.getRow(j+1).get(column);

				if (valueComparison(currentValue, nextValue, type) > 0) {
					ArrayList<String> temp = this.returnTable.getRow(j);

					this.returnTable.setRow(this.returnTable.getRow(j+1), j);
					this.returnTable.setRow(temp, j+1);
				}
			}
		}

		if (!ASC) 
			descFlip(startPoint, endPoint);
	}

	//Flips the rows if the order is descending
	@SuppressWarnings("unchecked")
	private void descFlip (int startPoint, int endPoint) {
		while (startPoint < endPoint) {
			ArrayList<String> temp = this.returnTable.getRow(startPoint);

			this.returnTable.setRow(this.returnTable.getRow(endPoint), startPoint);
			this.returnTable.setRow(temp, endPoint);

			startPoint++;
			endPoint--;
		}
	}

	//finds the starting and ending points for the sorting
	private int [] sortingPoints (int previousColumn, int previousEndPoint) {
		int [] points = new int[2];

		points[0] = previousEndPoint + 1;
		String startingValue = (String) this.returnTable.getRow(previousEndPoint + 1).get(previousColumn);

		for (int row = previousEndPoint + 2; row < this.returnTable.size(); row++) {
			String value = (String) this.returnTable.getRow(row).get(previousColumn);

			if (!value.equalsIgnoreCase(startingValue)) {
				points[1] = row - 1;
				break;
			}
		}
		if (points[1] < points[0])
			points[1] = this.returnTable.size() - 1;

		return points;
	}

	//Compares two given values. Used for ordering
	private int valueComparison (String right, String left, DataType type) {
		if (type == DataType.DECIMAL || type == DataType.INT) {
			if (right == null && left != null)
				return -1;

			else if (right != null && left == null)
				return 1;

			else if (right == null && left == null)
				return 0;

			float rightNum = Float.parseFloat(right);
			float leftNum = Float.parseFloat(left);

			if (rightNum == leftNum)
				return 0;
			if (rightNum < leftNum)
				return -1;
			else
				return 1;
		}

		else {
			return right.toLowerCase().compareTo(left.toLowerCase());
		}
	}

	@SuppressWarnings({ "unchecked"})
	//Creates a duplicate of the given table
	//This table will be manipulated and changed according the select statement
	private <T> DBTable returnTableCreator (DBTable table) {
		DBTable tempTable = new DBTable();

		for (int i = 0; i < table.size(); i++) {
			ArrayList<T> temp = new ArrayList<T>();

			for (int j = 0; j < table.getRow(i).size(); j++) {
				temp.add((T) table.getRow(i).get(j));
			}

			tempTable.addRow(temp);
		}

		return tempTable;
	}

	//Checks if all the columns exists in the table
	private void columnsChecker () {
		for (ColumnID column : columns) {
			if (!column.getColumnName().equalsIgnoreCase("*") && this.table.getColumn(column.getColumnName()) == null) {
				throw new NoSuchElementException("Error 404: " + column.getColumnName() + " column not found in table " + this.table.getTableName());
			}
		}

		for (OrderBy order : orders) {
			if (this.table.getColumn(order.getColumnID().getColumnName()) == null)
				throw new NoSuchElementException("Error 404: " + order.getColumnID().getColumnName() + " column not found in table " + this.table.getTableName());
		}
	}

	//used by deleteColumns. Makes a set of the names of columns that are needed
	private HashSet<String> columnNames () {
		HashSet<String> names = new HashSet<String>();
		if (functions.size() == 0) {
			for (ColumnID column : columns) {
				names.add(column.getColumnName().toLowerCase());
			}
		}

		else {
			for (ColumnID column : columns) {
				for (FunctionInstance function : functions) {
					if (column != function.column)
						names.add(column.getColumnName().toLowerCase());
				}
			}
		}
		return names;
	}

	//creates the table that will be used to return at the end of the query
	private HashMap<String, Integer> returnTableColumnIndices () {
		HashMap<String, Integer> indices = new HashMap<String, Integer>();

		for (int i = 0; i < this.returnTable.getRow(0).size(); i++) {
			ColumnDescription temp = (ColumnDescription) this.returnTable.getRow(0).get(i);
			indices.put(temp.getColumnName().toLowerCase(), i);
		}

		return indices;
	}

	//Updates the given row in that then will be inserted in the returnTable
	@SuppressWarnings("unchecked")
	private <T> void updateRow (ArrayList<T> newRow, int row, int oldRowSize) {	
		for (int i = 0; i < newRow.size(); i++) {
			if (i < oldRowSize)
				this.returnTable.getRow(row).set(i, newRow.get(i));
			else
				this.returnTable.getRow(row).add(newRow.get(i));
		}
	}

	//Converts [] to list
	private <T> ArrayList<T> arrayToArrayList (T [] array) {
		ArrayList <T> arrayList = new ArrayList<T>();

		for (T object : array) {
			arrayList.add(object);
		}

		return arrayList;
	}
}
