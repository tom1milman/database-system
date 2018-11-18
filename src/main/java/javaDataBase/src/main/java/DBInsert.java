/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription.DataType;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnValuePair;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.InsertQuery;

public class DBInsert {
	private DBTable table;
	private ColumnValuePair [] newEntery;
	private String primaryValue;

	public DBInsert (InsertQuery queryObject, DBTable table) {
		this.table = table;
		this.newEntery = queryObject.getColumnValuePairs();
	}

	//Constructor used by DBSelect for temp table
	public DBInsert (DBTable table, ArrayList<String> row) {
		this.table = table;
	}

	//Main insert method - used by the DBExecuter
	public void insertValues () {	
		columnsChecker();
		ArrayList<ColumnDescription> columns = this.table.getColumnDescription();
		ArrayList<String> values = new ArrayList<String>();	
		ColumnDescription primaryKeyColumn = this.table.getPrimaryKey();
		this.primaryValue = null;

		for (ColumnDescription tableColumn : columns) {	
			this.newRow(tableColumn, values, primaryKeyColumn);
		}

		this.table.newEntery(values, this.primaryValue);
		
		updateIndex(values);
	}

	//Constructs the new row
	private void newRow (ColumnDescription tableColumn, ArrayList<String> values, ColumnDescription primaryKeyColumn) {
		boolean isNull = true;
		String unique = null;
		
		for (ColumnValuePair value : newEntery) {
			//Checks if the value belongs to the looped column
			//if not, then it continues in the loop to the next value
			if (!tableColumn.getColumnName().equalsIgnoreCase(value.getColumnID().getColumnName()))
				continue;

			String valueString = value.getValue();

			this.exceptionChecker(tableColumn, value, primaryKeyColumn, valueString);

			if (tableColumn.getColumnType() == DataType.DECIMAL)
				valueString = Float.parseFloat(valueString) + "";

			isNull = false;
			values.add(valueString);
			unique = valueString;
		}

		if (isNull && tableColumn.isNotNull())
			throw new IllegalArgumentException("Value for '" + tableColumn.getColumnName() + "' can't be null");

		else if (isNull && !tableColumn.isNotNull() && tableColumn.getHasDefault())
			values.add(tableColumn.getDefaultValue());

		else if (isNull && !tableColumn.isNotNull())
			values.add(null);
		
		if (!isNull && tableColumn.isUnique()) {
			DBIndex tree = this.table.getIndexTrees().get(tableColumn.getColumnName().toLowerCase());
			tree.put(values);
		}
	}

	//Updates the indices
	private void updateIndex (ArrayList<String> row) {
		HashMap <String, DBIndex> map = this.table.getIndexTrees();
		
		for (DBIndex index : map.values()) {
			index.put(row);
		}
	}
	
	//Checks if all the columns exists in the table
	private void columnsChecker () {
		for (ColumnValuePair column : newEntery) {
			String columnName = column.getColumnID().getColumnName();
			if (!columnName.equalsIgnoreCase("*") && this.table.getColumn(columnName) == null) {
				throw new NoSuchElementException("Error 404: " + columnName + " column not found in table " + this.table.getTableName());
			}
		}
	}
	
	//Checks for exceptions
	private void exceptionChecker (ColumnDescription tableColumn, ColumnValuePair value, ColumnDescription primaryKeyColumn, String valueString) {
		isLengthRight(value.getValue(), tableColumn);
		
		//Checks if the value is the appropriate type for the column
		if (!isRightValue(value.getValue(), tableColumn)) {
			String s = String.format("The value %s for column '%s' isn't a %s type", value.getValue(), tableColumn.getColumnName(), tableColumn.getColumnType());
			throw new IllegalArgumentException(s);
		}

		//Checks for primary column and values
		if (tableColumn.equals(primaryKeyColumn)) {					
			if (this.table.isPrimaryValue(valueString))
				throw new IllegalArgumentException("Primary value'" +  valueString + "' was already used");

			this.primaryValue = valueString;
		}

		//Checks for unique column and values
		if (tableColumn.isUnique()) { 
			if (this.table.isUniqueVale(tableColumn, valueString))
				throw new IllegalArgumentException("UNIQUE value was already used");
		}
	}

	//Checks if the value is the right type
	private boolean isRightValue (String value, ColumnDescription column) {
		switch (column.getColumnType()) {
		case INT: 
			try {
				Integer.parseInt(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		case DECIMAL: 
			try {
				Double.parseDouble(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		case BOOLEAN:
			if (value.toLowerCase().equals("true") || value.toLowerCase().equals("false"))
				return true;
			else
				return false;
		case VARCHAR:
			return true;
		default: 
			return false;
		}
	}
	//Checks for the values length
	private void isLengthRight (String value, ColumnDescription column) {
		DataType type = column.getColumnType();

		if (column.getVarCharLength() == 0 || column.getWholeNumberLength() == 0 || column.getFractionLength() == 0)
			return;

		if (type == DataType.VARCHAR) {
			if (value.length() > column.getVarCharLength())
				throw new IllegalArgumentException(value + " is longer than the allowed length");
		}

		if (type == DataType.DECIMAL) {
			Float temp = Float.parseFloat(value);
			String [] values = temp.toString().split(".");

			if (values[0].length() > column.getWholeNumberLength())
				throw new IllegalArgumentException(value + " is longer than the allowed length");

			if (values[1].length() > column.getFractionLength())
				throw new IllegalArgumentException(value + " is longer than the allowed length");
		}
	}
}