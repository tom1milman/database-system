/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

//Execution class
public class JavaDataBase {
	
	public ResultSet execute (String SQL) {
		return new ResultSet(SQL);
	}
}
