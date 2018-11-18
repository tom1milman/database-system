/**
 * @author Tom Milman
 * Java Data Base - semester project
 * Testing code
 */

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javaDataBase.src.main.java.JavaDataBase;
import javaDataBase.src.main.java.ResultSet;

public class DBJUnit {
	private JavaDataBase db;
	private String [][] expectedTrueResultSet = new String [1][1];
	private String [][] expectedFalseResultSet = new String [1][1];
	private String [][] originalTable = {
			{"true", "3.45", "'Tom'", "1", "'Milman'"},
			{"true", "4.0", "'Aaron'", "2", "'Cohen'"},
			{"false", "2.0", "'Bun'", "3", "'Cinnamon'"},
			{"true", "3.12", "'Pizza'", "4", "'Dairy'"},
	};

	@Before
	public void beforeSetup () {
		db = new JavaDataBase();
		expectedTrueResultSet [0][0] = "true";
		expectedFalseResultSet [0][0] = "false";
		ResultSet createTable = db.execute("CREATE TABLE Students (ID int NOT NULL, LastName varchar(255) NOT NULL UNIQUE,FirstName varchar(255), GPA decimal(1,2) DEFAULT 2.0, isAlive boolean DEFAULT true, PRIMARY KEY (ID))");
		ResultSet insurtStudent1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (1, 'Milman', 'Tom', 3.45, true)");
		ResultSet insurtStudent2 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (2, 'Cohen', 'Aaron', 4.00, true)");
		ResultSet insurtStudent3 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (3, 'Cinnamon', 'Bun', 2.00, false)");
		ResultSet insurtStudent4 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (4, 'Dairy', 'Pizza', 3.12, true)");
	}

	@After
	public void afterSetup () {
		ResultSet deleteAll = db.execute("DELETE FROM Students");
	}

	@Test
	public void testCreatingTables() {	
		String [][] expectedEmptyTableReturn = new String [0][0];
		String [] expectedColumnsSet2 = {"isAlive (BOOLEAN)", "GPA (DECIMAL)", "FirstName (VARCHAR)", "ID (INT)", "LastName (VARCHAR)"};

		ResultSet set2 = db.execute("CREATE TABLE Student (ID int NOT NULL, LastName varchar(255) NOT NULL UNIQUE,FirstName varchar(255), GPA decimal(1,2) DEFAULT 2.0, isAlive boolean DEFAULT true, PRIMARY KEY (ID))");
		ResultSet set3 = db.execute("CREATE TABLE Student (ID int NOT NULL, LastName varchar(255) NOT NULL UNIQUE,FirstName varchar(255), GPA decimal(1,2) DEFAULT 2.0, isAlive boolean DEFAULT true, PRIMARY KEY (ID))");
		ResultSet set4 = db.execute("CREATE TABLE table (ID int NOT NULL");
		ResultSet set5 = db.execute("CREATE TABLE table (ID int NOT NULL, LastName varchar(255) NOT NULL UNIQUE, GPA decimal(1,2) DEFAULT 2.0, PRIMARY KEY (ID))");

		assertArrayEquals(expectedEmptyTableReturn, set2.getResult()); //Creating table Student, returns true
		assertArrayEquals(expectedColumnsSet2, set2.getColumns()); //checking that the correct columns were created
		assertArrayEquals(expectedFalseResultSet, set3.getResult()); //Creating the same table second time, returns false
		assertArrayEquals(expectedFalseResultSet, set4.getResult()); //creating table without primary key returns false		
		assertArrayEquals(expectedEmptyTableReturn, set5.getResult()); //trying to create the same table but with valid sql string
	}


	@Test
	public void testInsert_HappyPath () {
		ResultSet insertTest1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 3.56, true)");
		ResultSet insertTest2 = db.execute("INSERT INTO Students (ID, LastName, FirstName) VALUES (6, 'Some', 'one')");

		assertArrayEquals(expectedTrueResultSet, insertTest1.getResult()); //Good insert
		assertArrayEquals(expectedTrueResultSet, insertTest2.getResult()); //Good insert - isAlive + GPA are not specified

		String [] insertTest1Expected = {"5", "'Matza'", "'Ball'", "3.56", "true"};
		String [] insertTest2Expected = {"6", "'Some'", "'one'", "2.0", "true"};
		ResultSet select1 = db.execute("Select ID, LastName, FirstName, GPA, isAlive FROM Students");

		assertArrayEquals(insertTest1Expected, select1.getResult()[4]); //Checks the insert is proper
		assertArrayEquals(insertTest2Expected, select1.getResult()[5]); //Checks the insert is proper
	}

	@Test
	public void testInsert_BadPath () {
		ResultSet insurtTest1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (4, 'Some', 'one', 2.56, false)");
		ResultSet insurtTest2 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (7, 'Cohen', 'one', 2.56, false)");
		ResultSet insurtTest3 = db.execute("INSERT INTO Students (ID, LastName, FirstName) VALUES ('55', 'Cohen', 'one')");

		assertArrayEquals(expectedFalseResultSet, insurtTest1.getResult()); //Bad insert - Same ID
		assertArrayEquals(expectedFalseResultSet, insurtTest2.getResult()); //Bad insert - Same LastName (Unique column)
		assertArrayEquals(expectedFalseResultSet, insurtTest3.getResult()); //Bad insert - ID (int) given is String

		ResultSet select1 = db.execute("Select * FROM Students");

		assertEquals(4, select1.getResult().length); //Checks that the sie of the table didn't actually changed
	}

	@Test
	public void testSimpleUpdate_HappyPath () {
		//Shows that the returned column nothing is equal to 4.1
		ResultSet select1 = db.execute("Select FirstName, GPA FROM Students");

		for (int i = 0; i < select1.getResult().length; i++) {
			assertNotEquals("'Bad First Name'", select1.getResult()[i][0]);
			assertNotEquals("4.1", select1.getResult()[i][1]);

		}

		ResultSet update1 = db.execute("UPDATE Students SET GPA = 4.1, FirstName = 'Bad First Name'");

		assertArrayEquals(expectedTrueResultSet, update1.getResult());

		//Shows that after return, all the previous values are equal
		ResultSet select2 = db.execute("Select FirstName, GPA FROM Students");

		for (int i = 0; i < select1.getResult().length; i++) {
			assertEquals("'Bad First Name'", select2.getResult()[i][0]);
			assertEquals("4.1", select2.getResult()[i][1]);
		}
	}

	@Test
	//Shows the size before and after delete all
	public void testSimpleDelete_HappyPath () {
		ResultSet select1 = db.execute("Select * FROM Students");

		assertEquals(4, select1.getResult().length);

		ResultSet deleteAll = db.execute("DELETE FROM Students");

		assertArrayEquals(expectedTrueResultSet, deleteAll.getResult());

		ResultSet select2 = db.execute("Select * FROM Students");

		assertEquals(0, select2.getResult().length);
	}

	@Test
	//Shows a bad delete sql string
	public void testSimpleDelete_BadPath () {
		ResultSet select1 = db.execute("Select * FROM Students");

		assertEquals(4, select1.getResult().length);

		ResultSet deleteAll = db.execute("DELETE some FROM Students");

		assertArrayEquals(expectedFalseResultSet, deleteAll.getResult());

		ResultSet select2 = db.execute("Select * FROM Students");

		assertEquals(4, select2.getResult().length);
	}

	@Test
	//Tests Delete with some where conditions
	public void testDeleteWhere_HappyPath () {
		ResultSet select1 = db.execute("Select * FROM Students");

		assertEquals(4, select1.getResult().length);

		ResultSet delete1 = db.execute("DELETE FROM Students WHERE GPA < 3.2");

		assertArrayEquals(expectedTrueResultSet, delete1.getResult());

		ResultSet select2 = db.execute("Select * FROM Students");

		assertEquals(2, select2.getResult().length);

		ResultSet insertTest2 = db.execute("INSERT INTO Students (ID, LastName, FirstName) VALUES (6, 'Some', 'one')");

		ResultSet delete2 = db.execute("DELETE FROM Students WHERE ID = 6");

		assertArrayEquals(expectedTrueResultSet, delete2.getResult());

		ResultSet select3 = db.execute("Select * FROM Students");

		assertEquals(2, select3.getResult().length);
	}

	@Test
	//Checks the Updated length and values
	public void testWhereCondition_HappyPath () {
		ResultSet select1 = db.execute("Select GPA FROM Students");

		assertEquals(4, select1.getResult().length);
		int temp = 0;
		for (int i = 0; i < select1.getResult().length; i++) {
			if (select1.getResult()[i][0].equals("4.0"))
				temp++;
		}

		assertEquals(1, temp);

		ResultSet delete1 = db.execute("DELETE FROM Students WHERE LastName = 'Milman'");
		ResultSet update1 = db.execute("UPDATE Students SET GPA = 4.0 Where GPA < 3.00");

		assertArrayEquals(expectedTrueResultSet, delete1.getResult());
		assertArrayEquals(expectedTrueResultSet, update1.getResult());

		ResultSet select2 = db.execute("Select GPA FROM Students");

		assertEquals(3, select2.getResult().length);

		temp = 0;
		for (int i = 0; i < select2.getResult().length; i++) {
			if (select2.getResult()[i][0].equals("4.0"))
				temp++;
		}

		assertEquals(2, temp);
	}

	@Test
	public void testIndex () {
		ResultSet index1 = db.execute("CREATE INDEX myIndex on Students (GPA)");
		ResultSet index2 = db.execute("CREATE INDEX myIndex2 on Students (FirstName)");
		
		assertArrayEquals(expectedTrueResultSet, index1.getResult());
		assertArrayEquals(expectedTrueResultSet, index2.getResult());
	}
	
	@Test
	public void testIndex_BadPath () {
		ResultSet index1 = db.execute("CREATE INDEX myIndex on Students (GPAA)");
		ResultSet index2 = db.execute("CREATE INDEX myIndex2 on YUStudents (FirstName)");
		
		assertArrayEquals(expectedFalseResultSet, index1.getResult());
		assertArrayEquals(expectedFalseResultSet, index2.getResult());
	}
	
	@Test
	//Order by test
	public void testOrderBy () {
		String[][] expectedTable1 = {
				{"1", "'Milman'", "3.45"},
				{"4", "'Dairy'", "4.0"},
				{"3", "'Cinnamon'", "4.0"},
				{"2", "'Cohen'", "4.0"},
		};

		String[][] expectedTable2 = {
				{"2", "'Cohen'", "4.0"},
				{"3", "'Cinnamon'", "4.0"},
				{"4", "'Dairy'", "4.0"},
				{"1", "'Milman'", "3.45"}
		};

		ResultSet update1 = db.execute("UPDATE Students SET GPA = 4.0 Where GPA < 3.20");
		ResultSet select1 = db.execute("Select ID, LastName, GPA FROM Students ORDER BY GPA asc, ID DESC");
		ResultSet select2 = db.execute("Select ID, LastName, GPA FROM Students ORDER BY GPA Desc, ID asc");

		assertArrayEquals(expectedTable1, select1.getResult());
		assertArrayEquals(expectedTable2, select2.getResult());
	}

	@Test
	//Tests delete function with complicated where condition
	public void testDelete_ComplicatedWhere () {
		ResultSet update1 = db.execute("DELETE Students Where FirstName = 'Bun' AND LastName = 'Cinnamon' OR ID <=2 AND GPA = 4.0");
		assertArrayEquals(expectedTrueResultSet, update1.getResult());

		String [][] expectedTable1 = {
				{"true", "3.45", "'Tom'", "1", "'Milman'"},
				{"true", "3.12", "'Pizza'", "4", "'Dairy'"},
		};

		ResultSet selectAll = db.execute("Select * from students");

		assertNotSame(originalTable, selectAll.getResult());
		assertArrayEquals(expectedTable1, selectAll.getResult());
	}

	@Test
	//tests update function with complicated where condition
	public void testUpdate_ComplicatedWhere () {
		ResultSet update1 = db.execute("Update Students set GPA = 2.0, FirstName = 'Not a Name', isAlive = true Where FirstName = 'Bun' AND LastName = 'Cinnamon' OR ID <=2 AND GPA = 4.0");
		assertArrayEquals(expectedTrueResultSet, update1.getResult());

		String [][] expectedTable1 = {
				{"true", "3.45", "'Tom'", "1", "'Milman'"},
				{"true", "2.0", "'Not a Name'", "2", "'Cohen'"},
				{"true", "2.0", "'Not a Name'", "3", "'Cinnamon'"},
				{"true", "3.12", "'Pizza'", "4", "'Dairy'"},
		};

		ResultSet selectAll = db.execute("Select * from students");
		assertArrayEquals(expectedTable1, selectAll.getResult());
	}

	@Test
	//tests update function with complicated where condition that doesn't match anything
	public void testUpdate_ComplicatedWhere2 () {
		ResultSet update1 = db.execute("Update Students set GPA = 2.0, FirstName = 'Not a Name', isAlive = true Where FirstName = 'Bun' AND LastName = 'Pizza' OR ID = 1 AND isAlive = false");
		assertArrayEquals(expectedTrueResultSet, update1.getResult());

		ResultSet selectAll = db.execute("Select * from students");
		assertArrayEquals(originalTable, selectAll.getResult());
	}


	@Test
	//Tests avg function in select
	public void testSelectAVG_1 () {
		String [][] expectedTable1 = {
				{"1", "2.5", "'Milman'"},
				{"2", "2.5", "'Cohen'"},
				{"3", "2.5", "'Cinnamon'"},
				{"4", "2.5", "'Dairy'"},
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"AVG (id)",
				"LastName (VARCHAR)"
		};

		ResultSet selectAVG1 = db.execute("Select ID, AVG(id), Lastname from students");


		assertArrayEquals(expectedColumns1, selectAVG1.getColumns());
		assertArrayEquals(expectedTable1, selectAVG1.getResult());
	}

	@Test
	//Tests avg function in select
	public void testSelectAVG_2 () {
		String [][] expectedTable2 = {
				{"'Milman'", "3.1425", "3.45"},
				{"'Cohen'", "3.1425", "4.0"},
				{"'Cinnamon'", "3.1425", "2.0"},
				{"'Dairy'", "3.1425", "3.12"},
		};

		String[] expectedColumns2 = {
				"LastName (VARCHAR)",
				"AVG (GPA)",
				"GPA (DECIMAL)"
		};

		ResultSet selectAVG2 = db.execute("Select LastName, AVG(GPA), GPA from students");

		assertArrayEquals(expectedColumns2, selectAVG2.getColumns());
		assertArrayEquals(expectedTable2, selectAVG2.getResult());
	}
	
	@Test
	//Tests bad avg function in select
	public void testSelectAVG_BadPath () {
		ResultSet selectAVG1 = db.execute("Select ID, AVG(LastName), Lastname from students");
		assertArrayEquals(expectedFalseResultSet, selectAVG1.getResult()); // can't do AVG on string columns
		assertEquals("Can't do AVG on a VARCHAR type", selectAVG1.getErrorMessage());

		ResultSet selectAVG2 = db.execute("Select ID, AVG(isAlive), Lastname from students");
		assertArrayEquals(expectedFalseResultSet, selectAVG2.getResult()); // can't do AVG on boolean columns
		assertEquals("Can't do AVG on a BOOLEAN type", selectAVG2.getErrorMessage());
	}

	@Test
	//Tests count function in select
	public void testSelectCOUNT_1 () {
		String [][] expectedTable1 = {
				{"1", "4.0", "'Milman'"},
				{"2", "4.0", "'Cohen'"},
				{"3", "4.0", "'Cinnamon'"},
				{"4", "4.0", "'Dairy'"},
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"COUNT (id)",
				"LastName (VARCHAR)"
		};

		ResultSet selectCount1 = db.execute("Select ID, COUNT(id), Lastname from students");

		assertArrayEquals(expectedColumns1, selectCount1.getColumns());
		assertArrayEquals(expectedTable1, selectCount1.getResult());
	}

	@Test
	//select with count function on non existing column
	public void testSelectCOUNT_BadPath () {
		ResultSet selectCount1 = db.execute("Select ID, COUNT(Somecolumn), Lastname from students where LastName < 'Levi'");
		assertArrayEquals(expectedFalseResultSet, selectCount1.getResult());
	}

	@Test
	//Tests max function twice with a change
	public void testSelectMAX_1 () {
		String [][] expectedTable1 = {
				{"1", "4.0", "'Milman'", "3.45"},
				{"2", "4.0", "'Cohen'", "4.0"},
				{"3", "4.0", "'Cinnamon'", "2.0"},
				{"4", "4.0", "'Dairy'", "3.12"},
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"MAX (id)",
				"LastName (VARCHAR)",
				"GPA (DECIMAL)"
		};

		String [][] expectedTable2 = {
				{"1", "5.0", "'Milman'", "3.45"},
				{"2", "5.0", "'Cohen'", "4.0"},
				{"3", "5.0", "'Cinnamon'", "2.0"},
				{"4", "5.0", "'Dairy'", "3.12"},
				{"5", "5.0", "'Matza'", "3.56"},
		};

		ResultSet selectMAX1 = db.execute("Select ID, MAX(id), Lastname, GPA from students");
		ResultSet insert1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 3.56, true)");
		ResultSet selectMAX2 = db.execute("Select ID, MAX(id), Lastname, GPA from students");

		assertArrayEquals(expectedColumns1, selectMAX1.getColumns());
		assertArrayEquals(expectedTable1, selectMAX1.getResult());
		assertArrayEquals(expectedTrueResultSet, insert1.getResult());
		assertArrayEquals(expectedColumns1, selectMAX2.getColumns());
		assertArrayEquals(expectedTable2, selectMAX2.getResult());
	}

	@Test
	//Tests max function twice with a change
	public void testSelectMAX_2 () {
		String [][] expectedTable1 = {
				{"1", "4.0", "'Milman'", "3.45"},
				{"2", "4.0", "'Cohen'", "4.0"},
				{"3", "4.0", "'Cinnamon'", "2.0"},
				{"4", "4.0", "'Dairy'", "3.12"}
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"MAX (GPA)",
				"LastName (VARCHAR)",
				"GPA (DECIMAL)"
		};

		String [][] expectedTable2 = {
				{"1", "5.35", "'Milman'", "3.45"},
				{"2", "5.35", "'Cohen'", "4.0"},
				{"3", "5.35", "'Cinnamon'", "2.0"},
				{"4", "5.35", "'Dairy'", "3.12"},
				{"5", "5.35", "'Matza'", "5.35"}
		};
		
		ResultSet selectMAX1 = db.execute("Select ID, MAX(GPA), Lastname, GPA from students");
		ResultSet insert1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 5.35, true)");
		ResultSet selectMAX2 = db.execute("Select ID, MAX(GPA), Lastname, GPA from students");

		assertArrayEquals(expectedColumns1, selectMAX1.getColumns());
		assertArrayEquals(expectedTable1, selectMAX1.getResult());
		assertArrayEquals(expectedTrueResultSet, insert1.getResult());
		assertArrayEquals(expectedColumns1, selectMAX2.getColumns());
		assertArrayEquals(expectedTable2, selectMAX2.getResult());
	}

	@Test
	//select with bad column inputs
	public void testSelectMAX_BadPath () {
		ResultSet selectMAX1 = db.execute("Select ID, MAX(LastName), Lastname from students");
		ResultSet selectMAX2 = db.execute("Select ID, MAX(NonExistingColumn), Lastname from students");

		assertArrayEquals(expectedFalseResultSet, selectMAX1.getResult()); //Can't do functions on varchar
		assertArrayEquals(expectedFalseResultSet, selectMAX2.getResult()); //Column doesn't exists
	}

	@Test
	//Tests MIN function twice with a change
	public void testSelectMIN_1 () {
		String [][] expectedTable1 = {
				{"1", "2.0", "'Milman'", "3.45"},
				{"2", "2.0", "'Cohen'", "4.0"},
				{"3", "2.0", "'Cinnamon'", "2.0"},
				{"4", "2.0", "'Dairy'", "3.12"}
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"MIN (GPA)",
				"LastName (VARCHAR)",
				"GPA (DECIMAL)"
		};

		String [][] expectedTable2 = {
				{"1", "1.1", "'Milman'", "3.45"},
				{"2", "1.1", "'Cohen'", "4.0"},
				{"3", "1.1", "'Cinnamon'", "2.0"},
				{"4", "1.1", "'Dairy'", "3.12"},
				{"5", "1.1", "'Matza'", "1.1"}
		};
		
		ResultSet selectMIN1 = db.execute("Select ID, MIN(GPA), Lastname, GPA from students");
		ResultSet insert1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 1.1, true)");
		ResultSet selectMIN2 = db.execute("Select ID, MIN(GPA), Lastname, GPA from students");

		assertArrayEquals(expectedColumns1, selectMIN1.getColumns());
		assertArrayEquals(expectedTable1, selectMIN1.getResult());
		assertArrayEquals(expectedTrueResultSet, insert1.getResult());
		assertArrayEquals(expectedColumns1, selectMIN2.getColumns());
		assertArrayEquals(expectedTable2, selectMIN2.getResult());
	}
	
	@Test
	//select with bad column inputs
	public void testSelectMIN_BadPath () {
		ResultSet selectMIN1 = db.execute("Select ID, MIN(isAlive), Lastname from students");
		ResultSet selectMIN2 = db.execute("Select ID, MIN(NonExistingColumn), Lastname from students");

		assertArrayEquals(expectedFalseResultSet, selectMIN1.getResult()); //Can't do functions on boolean
		assertArrayEquals(expectedFalseResultSet, selectMIN2.getResult()); //Column doesn't exists
	}
	
	@Test
	//Tests SUM function twice with a change
	public void testSelectSUM_1 () {
		String [][] expectedTable1 = {
				{"1", "12.57", "'Milman'", "3.45"},
				{"2", "12.57", "'Cohen'", "4.0"},
				{"3", "12.57", "'Cinnamon'", "2.0"},
				{"4", "12.57", "'Dairy'", "3.12"}
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"SUM (GPA)",
				"LastName (VARCHAR)",
				"GPA (DECIMAL)"
		};

		String [][] expectedTable2 = {
				{"1", "13.67", "'Milman'", "3.45"},
				{"2", "13.67", "'Cohen'", "4.0"},
				{"3", "13.67", "'Cinnamon'", "2.0"},
				{"4", "13.67", "'Dairy'", "3.12"},
				{"5", "13.67", "'Matza'", "1.1"}
		};
		
		ResultSet selectSUM1 = db.execute("Select ID, SUM(GPA), Lastname, GPA from students");
		ResultSet insert1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 1.1, true)");
		ResultSet selectSUM2 = db.execute("Select ID, SUM(GPA), Lastname, GPA from students");

		assertArrayEquals(expectedColumns1, selectSUM1.getColumns());
		assertArrayEquals(expectedTable1, selectSUM1.getResult());
		assertArrayEquals(expectedTrueResultSet, insert1.getResult());
		assertArrayEquals(expectedColumns1, selectSUM2.getColumns());
		assertArrayEquals(expectedTable2, selectSUM2.getResult());
	}
	
	@Test
	//Tests SUM function twice with a change
	public void testSelectSUM_2 () {
		String [][] expectedTable1 = {
				{"1", "10.0", "'Milman'", "3.45"},
				{"2", "10.0", "'Cohen'", "4.0"},
				{"3", "10.0", "'Cinnamon'", "2.0"},
				{"4", "10.0", "'Dairy'", "3.12"}
		};

		String[] expectedColumns1 = {
				"ID (INT)",
				"SUM (ID)",
				"LastName (VARCHAR)",
				"GPA (DECIMAL)"
		};

		String [][] expectedTable2 = {
				{"1", "15.0", "'Milman'", "3.45"},
				{"2", "15.0", "'Cohen'", "4.0"},
				{"3", "15.0", "'Cinnamon'", "2.0"},
				{"4", "15.0", "'Dairy'", "3.12"},
				{"5", "15.0", "'Matza'", "1.1"}
		};
		
		ResultSet selectSUM1 = db.execute("Select ID, SUM(ID), Lastname, GPA from students");
		ResultSet insert1 = db.execute("INSERT INTO Students (ID, LastName, FirstName, GPA, isAlive) VALUES (5, 'Matza', 'Ball', 1.1, true)");
		ResultSet selectSUM2 = db.execute("Select ID, SUM(ID), Lastname, GPA from students");

		assertArrayEquals(expectedColumns1, selectSUM1.getColumns());
		assertArrayEquals(expectedTable1, selectSUM1.getResult());
		assertArrayEquals(expectedTrueResultSet, insert1.getResult());
		assertArrayEquals(expectedColumns1, selectSUM2.getColumns());
		assertArrayEquals(expectedTable2, selectSUM2.getResult());
	}
	
	@Test
	//select SUM with bad column inputs
	public void testSelectSUM_BadPath () {
		ResultSet selectSUM1 = db.execute("Select ID, SUM(isAlive), Lastname from students");
		ResultSet selectSUM2 = db.execute("Select ID, SUM(NonExistingColumn), Lastname from students");

		assertArrayEquals(expectedFalseResultSet, selectSUM1.getResult()); //Can't do functions on boolean
		assertArrayEquals(expectedFalseResultSet, selectSUM2.getResult()); //Column doesn't exists
	}
}