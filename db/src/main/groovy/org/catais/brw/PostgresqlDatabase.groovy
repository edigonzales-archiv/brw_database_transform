package org.catais.brw

import groovy.util.logging.Log4j2
import groovy.sql.Sql
import java.sql.SQLException

@Log4j2
class PostgresqlDatabase {
	def dbhost
	def dbport
	def dbdatabase
	def dbusr
	def dbpwd
	def dburl
	
	// This is only need because of the dburl. Without it,
	// we could use the standard map constructor.
	public PostgresqlDatabase(Map dbparams) {
		dbparams?.each { k, v -> this[k] = v }
		dburl = "jdbc:postgresql://${dbhost}:${dbport}/${dbdatabase}"
	}
	
	public List getUserTables(String dbschema) {
		
		return []
	}
	
	def fubar() {
		
		println dburl
		println dbpwd
	}

}
