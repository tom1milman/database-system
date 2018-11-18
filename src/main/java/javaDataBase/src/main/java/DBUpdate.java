/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnValuePair;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.UpdateQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription.DataType;

public class DBUpdate {
	private DBTable table;
	private Condition where;
	private ColumnValuePair [] sortedColVal;

	public DBUpdate (UpdateQuery queryObject, DBTable table) {
		this.table = table;
		this.where = queryObject.getWhereCondition();
		this.sortedColVal = columnSorter(queryObject.getColumnValuePairs());
	}

	//Public Update method - the main method (called by DBExecuter
	@SuppressWarnings("unchecked")
	public void update () {
		if (this.where != null) {
			DBWhere whereCondition = new DBWhere(this.where, this.table);

			for (int i = 1; i < table.size(); i++) {
				if (whereCondition.isMatch(this.table.getRow(i))) 
					updateRow(this.table.getRow(i));
			}
		}

		else {
			for (int i = 1; i < table.size(); i++) {
				updateRow(this.table.getRow(i));
			}
		}

		indexUpdate();
	}

	//Updates the value in the given row
	private void updateRow (ArrayList<String> row) {
		for (int i = 0; i < this.sortedColVal.length; i++) {
			if (sortedColVal[i] == null)
				continue;

			ColumnDescription tableColumn = (ColumnDescription) this.table.getColumnDescription().get(i);
			valueCheck(tableColumn, sortedColVal[i].getValue(), row.get(i));
			row.set(i, sortedColVal[i].getValue());
		}
	}

	//Sorts the columnValuePair to match the order of the columns in the table
	private ColumnValuePair [] columnSorter (ColumnValuePair [] array) {
		ColumnValuePair [] sorted = new ColumnValuePair[this.table.getNumColumns()];

		for (int i = 0; i < this.table.getNumColumns(); i++) {
			ColumnDescription column = (ColumnDescription) this.table.getColumnDescription().get(i);
			for (ColumnValuePair colVal : array) {
				if (this.table.getColumn(colVal.getColumnID().getColumnName()) == null)
					throw new NoSuchElementException("Error 404: " + colVal.getColumnID().getColumnName() + " column not found in table " + this.table.getTableName());

				if (column.getColumnName().equals(colVal.getColumnID().getColumnName()))
					sorted[i] = colVal;
			}
		}

		return sorted;
	}

	//Checks if the given value is appropriate to be updated
	private void valueCheck (ColumnDescription colDesc, String value, String rowValue) {
		if (rowValue != null && rowValue.equals(value)) 
			return;	

		isRightLength(value, colDesc);

		if (!isRightValue(value, colDesc)) 
			throw new IllegalArgumentException("The value: " + value + " isn't " + colDesc.getColumnType());

		if (this.table.getPrimaryKey().equals(colDesc))
			if (this.table.isPrimaryValue(value))
				throw new IllegalArgumentException(value + " was already used as a primary value");

		if (colDesc.isUnique())
			if (this.table.isUniqueVale(colDesc, value))
				throw new IllegalArgumentException(value + " was already used as a unique value");

		if (colDesc.isNotNull() && value == null)
			throw new IllegalArgumentException("Value can't be null");

		if (colDesc.getColumnType() == DataType.VARCHAR && value.length() > colDesc.getVarCharLength())
			throw new IllegalArgumentException(value + " is longer than the allowed length");
	}

	//Checks for the right value
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

	//Checks for the right length of a value
	private void isRightLength (String value, ColumnDescription column) {
		DataType type = column.getColumnType();

		if (column.getVarCharLength() == 0 || column.getWholeNumberLength() == 0 || column.getFractionLength() == 0)
			return;

		if (type == DataType.VARCHAR) {
			if (value.length() > column.getVarCharLength())
				throw new IllegalArgumentException(value + " is longer than the allowed length");
		}

		if (type == DataType.DECIMAL) {
			String [] values = value.split(".");

			if (values[0].length() > column.getWholeNumberLength())
				throw new IllegalArgumentException(value + " is longer than the allowed length");

			if (values[1].length() > column.getFractionLength())
				throw new IllegalArgumentException(value + " is longer than the allowed length");
		}
	}

	//Updates the indices
	private void indexUpdate () {
		HashMap<String, DBIndex> trees = this.table.getIndexTrees();

		for (String indexName : trees.keySet()) {
			DBIndex index = trees.get(indexName);

			DBIndex newIndex = new DBIndex(indexName, index.getTable(), index.getColumn());
			newIndex.createIndex();
			trees.replace(indexName, index, newIndex);
		}
	}
}
