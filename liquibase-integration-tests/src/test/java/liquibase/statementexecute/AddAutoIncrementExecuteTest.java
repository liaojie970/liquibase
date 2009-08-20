package liquibase.statementexecute;

import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.test.DatabaseTestContext;
import liquibase.statement.*;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AddAutoIncrementExecuteTest extends AbstractExecuteTest {

    protected static final String TABLE_NAME = "table_name";
    protected static final String COLUMN_NAME = "column_name";


    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        ArrayList<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
        CreateTableStatement table = new CreateTableStatement(null, TABLE_NAME);
        if (database instanceof MySQLDatabase) {
            table.addPrimaryKeyColumn("id", "int", null, "pk_");
        } else {
            table.addColumn("id", "int", new NotNullConstraint());
        }
        statements.add(table);

        if (database.supportsSchemas()) {
            table = new CreateTableStatement(DatabaseTestContext.ALT_SCHEMA, TABLE_NAME);
            table
                    .addColumn("id", "int", new NotNullConstraint());
            statements.add(table);
        }
        return statements;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noSchema() throws Exception {
        this.statementUnderTest = new AddAutoIncrementStatement(null, TABLE_NAME, COLUMN_NAME, "int");

        assertCorrect("alter table [table_name] modify column_name serial auto_increment", PostgresDatabase.class);
        assertCorrect("alter table `table_name` modify `column_name` int auto_increment", MySQLDatabase.class);
        assertCorrect("ALTER TABLE [table_name] ALTER COLUMN [column_name] SET GENERATED ALWAYS AS IDENTITY", DB2Database.class);
        assertCorrect("alter table table_name alter column column_name int generated by default as identity identity", HsqlDatabase.class, H2Database.class);

        assertCorrect("ALTER TABLE [table_name] MODIFY [column_name] serial", InformixDatabase.class);

        assertCorrectOnRest("ALTER TABLE [table_name] MODIFY [column_name] int AUTO_INCREMENT");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fullNoConstraints() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", TABLE_NAME, COLUMN_NAME, 42);


        assertCorrect("alter table [table_name] add [table_name] column_name null default 42", SybaseDatabase.class);
        assertCorrect("alter table [table_name] add [table_name] column_name constraint df_table_name_table_name default 42", MSSQLDatabase.class);
//        assertCorrect("alter table [table_name] add [column_name] integer default 42", SQLiteDatabase.class);
        assertCorrect("not supported. fixme!!", SQLiteDatabase.class);
        assertCorrect("alter table table_name add table_name column_name default 42", PostgresDatabase.class, InformixDatabase.class, OracleDatabase.class, DerbyDatabase.class, HsqlDatabase.class, DB2Database.class, H2Database.class, CacheDatabase.class, FirebirdDatabase.class, MaxDBDatabase.class);
        assertCorrect("alter table [table_name] add [table_name] column_name null default 42", SybaseASADatabase.class);
        assertCorrect("alter table `table_name` add `table_name` column_name default 42", MySQLDatabase.class);
        assertCorrectOnRest("ALTER TABLE [table_name] ADD [column_name] int DEFAULT 42");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void autoIncrement() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, TABLE_NAME, COLUMN_NAME, "int", null, new AutoIncrementConstraint());

        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int auto_increment_clause", MSSQLDatabase.class);
        assertCorrect("alter table [table_name] add [column_name] int default autoincrement null", SybaseASADatabase.class);
        assertCorrect("alter table [table_name] add [column_name] int identity null", SybaseDatabase.class);
        assertCorrect("alter table [table_name] add [column_name] serial", PostgresDatabase.class, InformixDatabase.class);
        assertCorrect("not supported. fixme!!", SQLiteDatabase.class);
        assertCorrectOnRest("ALTER TABLE [table_name] ADD [column_name] int auto_increment_clause");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notNull() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, TABLE_NAME, COLUMN_NAME, "int", 42, new NotNullConstraint());

        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int NOT NULL DEFAULT 42", SybaseASADatabase.class);
        assertCorrect("alter table table_name add column_name int default 42 not null", InformixDatabase.class);
        assertCorrect("alter table [table_name] add [column_name] int not null constraint df_table_name_column_name default 42", MSSQLDatabase.class);
        assertCorrect("alter table table_name add column_name int default 42 not null", OracleDatabase.class, DerbyDatabase.class, HsqlDatabase.class, DB2Database.class, H2Database.class, FirebirdDatabase.class);
        assertCorrect("not supported. fixme!!", SQLiteDatabase.class);
        assertCorrectOnRest("ALTER TABLE [table_name] ADD [column_name] int NOT NULL DEFAULT 42");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void generateSql_primaryKey() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint());
//      TODO	sqlserver (at least 2000) does not allows add not null column.
//      this type or refactoring should include adding nullable column, updating it to some default value, and final adding primary constraint.
//        assertCorrect(null, MSSQLDatabase.class);
        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int primary key not null", InformixDatabase.class, OracleDatabase.class, FirebirdDatabase.class);
        assertCorrectOnRest("ALTER TABLE [table_name] ADD [column_name] int NOT NULL PRIMARY KEY");

    }

//     protected void setupDatabase(Database database) throws Exception {
//        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME).addColumn("existingCol", "int"), database);
//        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME).addColumn("existingCol", "int"), database);
//    }
//
//    protected AddColumnStatement createGeneratorUnderTest() {
//        return new AddColumnStatement(null, null, null, null, null);
//    }
//
//    @Test
//    public void execute_stringDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals("varchar".toUpperCase(), columnSnapshot.getTypeName().toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
//                        assertEquals(50, columnSnapshot.getColumnSize());
//                        assertEquals("new default", columnSnapshot.getDefaultValue());
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_intDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        if (snapshot.getDatabase() instanceof OracleDatabase) {
//                            assertEquals("NUMBER", columnSnapshot.getTypeName().toUpperCase());
//                        } else {
//                            assertTrue(columnSnapshot.getTypeName().toUpperCase().startsWith("INT"));
//                        }
//                        assertEquals(42, ((Number) columnSnapshot.getDefaultValue()).intValue());
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//
//                }
//
//        );
//    }
//
//    @Test
//    public void execute_floatDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "float", 42.5)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals(new Double(42.5), new Double(((Number) columnSnapshot.getDefaultValue()).doubleValue()));
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_notNull() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42, new NotNullConstraint())) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(false, columnSnapshot.isNullable());
//                    }
//                }
//
//        );
//    }
//
//    @Test
//    public void execute_primaryKey_nonAutoIncrement() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint())) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return (database instanceof DB2Database
//                                || database instanceof DerbyDatabase
//                                || database instanceof H2Database
//                                || database instanceof CacheDatabase);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(false, columnSnapshot.isNullable());
//                        assertTrue(columnSnapshot.isPrimaryKey());
//                        assertEquals(false, columnSnapshot.isAutoIncrement());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals("new default", columnSnapshot.getDefaultValue());
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//
//                });
//    }
//
//    @Test
//      public void execute_primaryKeyAutoIncrement() throws Exception {
//          new DatabaseTestTemplate().testOnAvailableDatabases(
//                  new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint(), new AutoIncrementConstraint())) {
//
//                      protected boolean expectedException(Database database, DatabaseException exception) {
//                          return (database instanceof DB2Database
//                                  || database instanceof DerbyDatabase
//                                  || database instanceof H2Database
//                                  || database instanceof CacheDatabase
//                                    || !database.supportsAutoIncrement());
//                      }
//
//                      protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                          assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                      }
//
//                      protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                          Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                          assertNotNull(columnSnapshot);
//                          assertEquals(false, columnSnapshot.isNullable());
//                          assertTrue(columnSnapshot.isPrimaryKey());
//                          assertEquals(true, columnSnapshot.isAutoIncrement());
//                      }
//                  });
//      }
}
