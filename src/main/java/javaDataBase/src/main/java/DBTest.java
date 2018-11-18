/**
 * @author Tom Milman
 * Java Data Base - semester project
 */

package javaDataBase.src.main.java;

public class DBTest {

	public static void main(String[] args) {
		System.out.println("------START------");
		JavaDataBase set = new JavaDataBase();
		
		ResultSet createTable = set.execute("CREATE TABLE Students (ID int NOT NULL, LastName varchar(255) NOT NULL UNIQUE,FirstName varchar(255), GPA decimal(1,2) DEFAULT 2.0, isAlive boolean DEFAULT true, PRIMARY KEY (ID))");
		createTable.printResult();
		printDivider();
		
		ResultSet insurtStudent1 = set.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (1, 'Milman', 'Tom', 3.45, true)");
		insurtStudent1.printResult();
		printDivider();
		
		ResultSet insurtStudent2 = set.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (2, 'Cohen', 'Aaron', 4.00, true)");
		insurtStudent2.printResult();
		printDivider();
		
		ResultSet insurtStudent3 = set.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (3, 'Cinnamon', 'Bun', 2.00, false)");
		insurtStudent3.printResult();
		printDivider();
		
		ResultSet insurtStudent4 = set.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (4, 'Dairy', 'Pizza', 3.12, true)");
		insurtStudent4.printResult();
		printDivider();
		
		ResultSet select1 = set.execute("SELECT * From Students");
		select1.printResult();
		printDivider();
		
		ResultSet index1 = set.execute("CREATE INDEX myIndex on Students (GPA)");
		index1.printResult();
		printDivider();
		
		ResultSet insurtStudent5 = set.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 3.56, true)");
		insurtStudent5.printResult();
		printDivider();
		
		ResultSet insurtStudent6 = set.execute("INSERT INTO Students (ID, LastName, FirstName) VALUES (6, 'Some', 'one')");
		insurtStudent6.printResult();
		printDivider();
		
		ResultSet select2 = set.execute("SELECT ID, Lastname, Firstname, GPA, isAlive From Students");
		select2.printResult();
		printDivider();
		
		ResultSet update1 = set.execute("Update Students set GPA = 4.0 where isAlive = false or lastname = 'Milman'");
		update1.printResult();
		printDivider();
	
		ResultSet delete1 = set.execute("DELETE FROM Students WHERE GPA <= 2.0");
		delete1.printResult();
		printDivider();
		
		ResultSet select3 = set.execute("Select ID, Lastname, GPA, AVG(GPA), MAX(GPA), MIN(GPA), SUM(ID), COUNT(ID) from students order by gpa desc, id desc");
		select3.printResult();
		printDivider();
		
		ResultSet deleteAll = set.execute("DELETE FROM Students");
		deleteAll.printResult();
		printDivider();
		
		ResultSet insurtStudent7 = set.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (100, 'Semester', 'Project', 4.0, true)");
		insurtStudent7.printResult();
		printDivider();
		
		ResultSet select4 = set.execute("Select ID, Lastname, firstname, GPA, isAlive from students");
		select4.printResult();
		
		System.out.println("------END------");
	}
	
	private static void printDivider() {
		System.out.println("------------------------------------------------");
		System.out.println();
	}
}
