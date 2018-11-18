/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnID;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition.Operator;

public class DBWhere {
	private DBTable table;
	private ArrayList <ColumnDescription> columns;
	private Condition where;

	public DBWhere (Condition where, DBTable table) {
		this.table = table;
		this.columns = table.getColumnDescription();
		this.where = where;
	}

	public DBTable getTableName () {
		return this.table;
	}

	//Main method
	//Note the where checks one column at the time, hence there's no way of implementing the index
	public boolean isMatch (ArrayList<String> row) {
		return match(row, where.getLeftOperand(), where.getOperator(), where.getRightOperand());
	}

	//Checks if the given row matches the condition or not
	private boolean match (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {
		switch (operator) {
		case EQUALS: 
			return equals(row, leftOperand, operator, rightOperand);

		case NOT_EQUALS: 
			return notEquals(row, leftOperand, operator, rightOperand);

		case LESS_THAN:
			return lessThan(row, leftOperand, operator, rightOperand);

		case LESS_THAN_OR_EQUALS:
			return lessThanOrEquals(row, leftOperand, operator, rightOperand);

		case GREATER_THAN:
			return greaterThan(row, leftOperand, operator, rightOperand);

		case GREATER_THAN_OR_EQUALS:
			return greaterThanOrEquals(row, leftOperand, operator, rightOperand);

		case AND:
			Condition leftAND = (Condition) leftOperand;
			Condition rightAND = (Condition) rightOperand;

			return match(row, leftAND.getLeftOperand(), leftAND.getOperator(), leftAND.getRightOperand()) && match(row, rightAND.getLeftOperand(), rightAND.getOperator(), rightAND.getRightOperand());

		case OR:
			Condition leftOR = (Condition) leftOperand;
			Condition rightOR = (Condition) rightOperand;

			return match(row, leftOR.getLeftOperand(), leftOR.getOperator(), leftOR.getRightOperand()) || match(row, rightOR.getLeftOperand(), rightOR.getOperator(), rightOR.getRightOperand());
		}

		return false;
	}
	
	//Checks if the 2 given values are equal
	private boolean equals (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {
		String left = null;

		if (leftOperand instanceof ColumnID) 
			left = ((ColumnID) leftOperand).getColumnName();

		if (this.table.getColumn(left) == null)
			throw new NoSuchElementException("Error 404: " + left + " column not found in table " + this.table.getTableName());

		if (leftOperand instanceof String)
			left = (String) leftOperand;

		for (int i = 0; i < this.columns.size(); i++) {
			if (left.equalsIgnoreCase(this.columns.get(i).getColumnName())) {		
				switch (this.columns.get(i).getColumnType()) {
				case INT:
					return numberComparison(row.get(i), rightOperand.toString()) == 0;

				case DECIMAL: 
					return numberComparison(row.get(i), rightOperand.toString()) == 0;

				case BOOLEAN: 
					return varCharComperison(row.get(i), rightOperand.toString()) == 0;

				case VARCHAR: 
					return (varCharComperison(row.get(i), rightOperand.toString()) == 0);
				}
			}
		}
		
		return false;
	}

	//Checks if the 2 given values are not Equal
	private boolean notEquals (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {
		String left = null;

		if (leftOperand instanceof ColumnID) 
			left = ((ColumnID) leftOperand).getColumnName();

		if (this.table.getColumn(left) == null)
			throw new NoSuchElementException("Error 404: " + left + " column not found in table " + this.table.getTableName());

		if (leftOperand instanceof String)
			left = (String) leftOperand;

		for (int i = 0; i < this.columns.size(); i++) {
			if (left.equalsIgnoreCase(this.columns.get(i).getColumnName())) {		
				switch (this.columns.get(i).getColumnType()) {
				case INT:
					return numberComparison(row.get(i), rightOperand.toString()) != 0;

				case DECIMAL: 
					return numberComparison(row.get(i), rightOperand.toString()) != 0;

				case BOOLEAN: 
					return varCharComperison(row.get(i), rightOperand.toString()) != 0;

				case VARCHAR: 
					return (varCharComperison(row.get(i), rightOperand.toString()) != 0);
				}
			}
		}

		return false;
	}

	//Checks if the firstValue < secondValue
	private boolean lessThan (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {		
		String left = null;

		if (leftOperand instanceof ColumnID) 
			left = ((ColumnID) leftOperand).getColumnName();

		if (this.table.getColumn(left) == null)
			throw new NoSuchElementException("Error 404: " + left + " column not found in table " + this.table.getTableName());

		if (leftOperand instanceof String)
			left = (String) leftOperand;

		for (int i = 0; i < this.columns.size(); i++) {
			if (left.equalsIgnoreCase(this.columns.get(i).getColumnName())) {		
				switch (this.columns.get(i).getColumnType()) {
				case INT:
					return numberComparison(row.get(i), rightOperand.toString()) < 0;

				case DECIMAL: 
					return numberComparison(row.get(i), rightOperand.toString()) < 0;

				case BOOLEAN: 
					return varCharComperison(row.get(i), rightOperand.toString()) < 0;

				case VARCHAR: 
					return (varCharComperison(row.get(i), rightOperand.toString()) < 0);
				}
			}
		}

		return false;
	}

	//Checks if the firstValue <= secondValue
	private boolean lessThanOrEquals (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {		
		String left = null;

		if (leftOperand instanceof ColumnID) 
			left = ((ColumnID) leftOperand).getColumnName();

		if (this.table.getColumn(left) == null)
			throw new NoSuchElementException("Error 404: " + left + " column not found in table " + this.table.getTableName());

		if (leftOperand instanceof String)
			left = (String) leftOperand;

		for (int i = 0; i < this.columns.size(); i++) {
			if (left.equalsIgnoreCase(this.columns.get(i).getColumnName())) {		
				switch (this.columns.get(i).getColumnType()) {
				case INT:
					return numberComparison(row.get(i), rightOperand.toString()) <= 0;

				case DECIMAL: 
					return numberComparison(row.get(i), rightOperand.toString()) <= 0;

				case BOOLEAN: 
					return varCharComperison(row.get(i), rightOperand.toString()) <= 0;

				case VARCHAR: 
					return (varCharComperison(row.get(i), rightOperand.toString()) <= 0);
				}
			}
		}

		return false;
	}

	//Checks if the firstValue > secondValue
	private boolean greaterThan (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {		
		String left = null;

		if (leftOperand instanceof ColumnID) 
			left = ((ColumnID) leftOperand).getColumnName();

		if (this.table.getColumn(left) == null)
			throw new NoSuchElementException("Error 404: " + left + " column not found in table " + this.table.getTableName());

		if (leftOperand instanceof String)
			left = (String) leftOperand;

		for (int i = 0; i < this.columns.size(); i++) {
			if (left.equalsIgnoreCase(this.columns.get(i).getColumnName())) {		
				switch (this.columns.get(i).getColumnType()) {
				case INT:
					return numberComparison(row.get(i), rightOperand.toString()) > 0;

				case DECIMAL: 
					return numberComparison(row.get(i), rightOperand.toString()) > 0;

				case BOOLEAN: 
					return varCharComperison(row.get(i), rightOperand.toString()) > 0;

				case VARCHAR: 
					return (varCharComperison(row.get(i), rightOperand.toString()) > 0);
				}
			}
		}

		return false;
	}

	//Checks if the firstValue >= secondValue
	private boolean greaterThanOrEquals (ArrayList<String> row, Object leftOperand, Operator operator, Object rightOperand) {		
		String left = null;

		if (leftOperand instanceof ColumnID) 
			left = ((ColumnID) leftOperand).getColumnName();

		if (this.table.getColumn(left) == null)
			throw new NoSuchElementException("Error 404: " + left + " column not found in table " + this.table.getTableName());

		if (leftOperand instanceof String)
			left = (String) leftOperand;

		for (int i = 0; i < this.columns.size(); i++) {
			if (left.equalsIgnoreCase(this.columns.get(i).getColumnName())) {		
				switch (this.columns.get(i).getColumnType()) {
				case INT:
					return numberComparison(row.get(i), rightOperand.toString()) >= 0;

				case DECIMAL: 
					return numberComparison(row.get(i), rightOperand.toString()) >= 0;

				case BOOLEAN: 
					return varCharComperison(row.get(i), rightOperand.toString()) >= 0;

				case VARCHAR: 
					return (varCharComperison(row.get(i), rightOperand.toString()) >= 0);
				}
			}
		}

		return false;
	}

	//Compares non digit values
	private int varCharComperison (String rowValue, String rightValue) {
		rowValue = rowValue.toLowerCase();
		rightValue = rightValue.toLowerCase();
		return rowValue.compareTo(rightValue);
	}
	
	//Compares digit values
	private int numberComparison (String rowValue, String rightValue) {
		if (rightValue == null && rowValue != null)
			return -1;
		
		else if (rightValue != null && rowValue == null)
			return 1;
		
		else if (rightValue == null && rowValue == null)
			return 0;
		
		
		
		float rowInt = Float.parseFloat(rowValue);
		float rightInt;
	
		try {
			rightInt = Float.parseFloat(rightValue);
			
			if (rowInt == rightInt)
				return 0;
			if (rowInt < rightInt)
				return -1;
			else
				return 1;
		} catch (Exception e) {
			String temp = "" + rowInt;
			return temp.compareTo(rightValue);
		}
	}
}
