/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.HashMap;
import java.util.NoSuchElementException;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateIndexQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateTableQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.DeleteQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.InsertQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.UpdateQuery;

public enum DBExecuter {
	MYDS();

	private HashMap <String, DBTable> tables;	//Keeps all the created tables. <table name, DBTable object>.

	private DBExecuter () {
		this.tables = new HashMap<String, DBTable>();
	}

	//Executes the received queryObject from the parser
	public DBTable executer (Object queryObject) {		
		if (queryObject.getClass().equals(CreateTableQuery.class)) 
			return createTable((CreateTableQuery) queryObject);

		if (queryObject.getClass().equals(InsertQuery.class))
			tableInsert((InsertQuery) queryObject);

		if (queryObject.getClass().equals(UpdateQuery.class))
			update((UpdateQuery) queryObject); 

		if (queryObject.getClass().equals(DeleteQuery.class))
			delete((DeleteQuery) queryObject);

		if (queryObject.getClass().equals(SelectQuery.class))
			return select((SelectQuery) queryObject);

		if (queryObject.getClass().equals(CreateIndexQuery.class))
			createIndex((CreateIndexQuery) queryObject);

		return null;
	}

	//returns all the tables in the given data base
	public HashMap <String, DBTable> getTables () {
		return this.tables;
	}

	//Testing method for printing tables
	public void printTable (String table) {
		getTable(table).printTable();
	}

	//method that handles the Create Table Query
	private DBTable createTable (CreateTableQuery queryObject) {		
		if (this.getTable(queryObject.getTableName()) != null)
			throw new IllegalArgumentException("Table already exists");

		DBTable table = new DBTable(queryObject);

		tables.put(table.getTableName(), table);
		
		return table;
	}

	//method that handles the Insert Query
	private void tableInsert (InsertQuery queryObject) {
		DBInsert insert = null;

		DBTable table = getTable(queryObject.getTableName());

		if (table == null)
			throw new NoSuchElementException("Table '" + queryObject.getTableName() + "' doesn't exist");

		insert = new DBInsert(queryObject, table);

		insert.insertValues();
	}

	//method that handles the Update Query
	private void update (UpdateQuery queryObject) {
		DBUpdate update = null;

		DBTable table = getTable(queryObject.getTableName());

		if (table == null)
			throw new NoSuchElementException("Table '" + queryObject.getTableName() + "' doesn't exist");

		update = new DBUpdate(queryObject, table);

		update.update();
	}

	//method that handles the Delete Query
	private void delete (DeleteQuery queryObject) {
		DBDelete delete = null;

		DBTable table = getTable(queryObject.getTableName());


		if (table == null) 
			throw new NoSuchElementException("Table '" + queryObject.getTableName() + "' doesn't exist");

		delete = new DBDelete(queryObject, table);

		delete.delete();
	}

	//method that handles the Select Query
	private DBTable select (SelectQuery queryObject) {
		DBSelect select = null;

		String [] tables = queryObject.getFromTableNames();	

		DBTable table = getTable(tables[0]);


		if (table == null) 
			throw new NoSuchElementException("Table '" + queryObject.getFromTableNames()[0] + "' doesn't exist");

		select = new DBSelect(queryObject, table);

		return select.select();
	}

	//method that handles the Create Index Query
	private void createIndex (CreateIndexQuery queryObject) {
		DBIndex index = null;

		DBTable table = getTable(queryObject.getTableName());

		if (table == null)
			throw new NoSuchElementException("Table '" + queryObject.getTableName() + "' doesn't exist");

		index = new DBIndex(queryObject, table);

		index.createIndex();
	}

	
	private DBTable getTable (String tableName) {
		return tables.get(tableName.toLowerCase());
	}
}
