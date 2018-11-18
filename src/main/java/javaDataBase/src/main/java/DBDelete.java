/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

import java.util.ArrayList;
import java.util.HashMap;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.DeleteQuery;

public class DBDelete {
	private DBTable table;
	private Condition where;

	public DBDelete (DeleteQuery queryObject, DBTable table) {
		this.table = table;
		this.where = queryObject.getWhereCondition();
	}

	//Main method used by the DBExecuter
	@SuppressWarnings("unchecked")
	public void delete () {
		DBWhere whereCondition = new DBWhere(this.where, this.table);

		if (this.where != null) {
			for (int i = table.size() -  1; i > 0; i--) {
				if (whereCondition.isMatch(this.table.getRow(i))) {
					this.table.deleteRow(i);			
				}
			}
		}

		else {
			for (int i = table.size() -  1; i > 0; i--) {
				this.table.deleteRow(i);			
			}
		}
		
		indexUpdate();
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
	
	private void primaryUpdate (int i) {
		ArrayList<String> pk = this.table.getPrimaryEntehries();
		
		
		
		//for (String s : pk)
			
	}
}
