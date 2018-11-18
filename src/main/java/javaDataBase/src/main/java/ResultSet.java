/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SQLParser;

public class ResultSet {	
	private String sql;
	private String errorMessage;
	private String [][] result;
	private String [] columns;
	
	//Constructor
	public ResultSet(String SQL) {
		this.errorMessage = "No error";
		this.sql = SQL;
		SQLParser sqlParser = new SQLParser();	
		Object queryObject = null;

		try	{
			queryObject = sqlParser.parse(SQL);
			this.result = setResult(DBExecuter.MYDS.executer(queryObject));
		} catch (Exception e) {
			//e.printStackTrace();
			this.errorMessage = e.getMessage();
			this.result = new String[1][1];
			this.result[0][0] = "false";
		}	

	}		
	
	public String getSQL () {
		return this.sql;
	}
	
	public String getErrorMessage () {
		return this.errorMessage;
	}

	public String[][] getResult () {
		return this.result;
	}

	public String[] getColumns () {
		return this.columns;
	}
	
	public void printResult () {
		System.out.println("SQL: " + this.sql);
		System.out.println("Columns: ");
		if (this.columns != null)
		for (String s : this.columns) {
			System.out.printf("%-23s", s);
		}
		System.out.println();
		System.out.println("Table: ");
		for (int row = 0; row < result.length; row++) {
			for (int column = 0; column < result[0].length; column++) {
				System.out.printf("%-23s", this.result[row][column]);
			}
			System.out.println();
		}
		System.out.println();
	}

	//Testing method to print tables
	public void print (String table) {
		DBExecuter.MYDS.printTable(table);
	}

	//sets the result for the ResultSet
	@SuppressWarnings("unchecked")
	private String[][] setResult (DBTable table) {
		String[][] result = null;
		
		if (table == null) {
			result = new String[1][1];
			result[0][0] = "true";
			setColumns(null);
		}
		else {
			result = new String[table.size() - 1][table.rowSize()];

			setColumns(table.getRow(0));
			//table.deleteRow(0);

			for (int row = 0; row < result.length; row++) {
				ArrayList<String> tempRow = table.getRow(row + 1);
				for (int column = 0; column < result[0].length; column++) {
					result[row][column] = tempRow.get(column);
				}
			}
		}
		return result;
	}

	//sets columns[] for the result set
	private void setColumns (ArrayList<Object> row) {
		if (row == null) {
			this.columns = new String[1];
			this.columns[0] = "result";
		}

		else {
			this.columns = new String[row.size()];

			for (int i = 0; i < row.size(); i++) {
				if (row.get(i).getClass() == String.class)
					this.columns[i] = (String) row.get(i);
				else {
					ColumnDescription col = (ColumnDescription) row.get(i);
					this.columns[i] = col.getColumnName() + " (" + col.getColumnType() + ")";
				}
			}
		}
	}
}
