package org.classfoo.tools.jdbc;



public interface SQLFactory {

	InsertDDL createInsertDDL();

	String getSql(InsertDDL insertDDL, DbMetaData dbMetaData);

	DeleteDDL createDeleteDDL();

	String getSql(DeleteDDL deleteDDL, DbMetaData dbMetaData);

	UpdateDDL createUpdateDDL();

	String getSql(UpdateDDL update, DbMetaData dbMetaData);

}
