package org.catais.brw

import groovy.util.logging.Log4j2
import groovy.sql.Sql
import java.sql.SQLException

import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;

/**
 * Class that does provide all methods we need to make the transition of reference frame in a 
 * postgresql database.
 * Except the transformation algorithm itself.
 */
@Log4j2
class PostgresqlDatabase {
	def rangeX = 100000..1000000
	def rangeY = 10000..1000000
	
	def dbhost
	def dbport
	def dbdatabase
	def dbusr
	def dbpwd
	def dburl
	
	// This is only needed because of the dburl variable. Without it,
	// we could use the standard map constructor.
	public PostgresqlDatabase(Map dbparams) {
		dbparams?.each { k, v -> this[k] = v }
		dburl = "jdbc:postgresql://${dbhost}:${dbport}/${dbdatabase}"
	}
	
	/**
	 * Grabs all the tables from a database and puts them into a table.
	 * Tables from the following schemas are not considered:<br>
	 * - information_schema<br>
	 * - pg_%<br>
	 *
	 * @return List of all 'user' tables in the dbschema.
	 */
	public List getUserTables() {
		return getUserTables('')
	}
	
	/**
	 * Grabs all the tables from a database schema and puts them into a table.
	 * Tables from the following schemas are not considered:<br>
	 * - information_schema<br>
	 * - pg_%<br>
	 * 
	 * @param dbschema The database schema name we want to transform.
	 * @return List of all 'user' tables in the dbschema.
	 */
	public List getUserTables(String dbschema) {
		def query = '''\
SELECT * 
FROM information_schema.tables 
WHERE table_type = 'BASE TABLE'
AND table_schema NOT IN ('information_schema') 
AND table_schema NOT ILIKE 'pg_%'
'''
	
		if (dbschema) {
			query = query + "AND table_schema = '$dbschema'\n" // Why do I not need ${Sql.expand(dbschema)}?
		}
		
		query = query + "ORDER BY table_schema, table_name;"
		
		def tables = []
		
		def sql = Sql.newInstance(dburl)

		try {
			sql.eachRow(query) {row ->
				tables << row.table_schema + "." + row.table_name
			}
		} catch (SQLException e) {
			log.error e.getMessage()
			throw new SQLException(e)
		} finally {
			sql.connection.close()
			sql.close()
		}

		return tables
	}
	
	public void prepareList(List tableNames) {
		
		// If Postgis Version is < 2.0 we need to take of of CHECK constraints: 
		// (st_srid(.....) = -1))
		// (srid(.....) = -1))
		// (st_srid(.....) = 21781))
		// CHECK (srid(.....) = 21781))
		// There are other combinations I guess...
		// 
		// Probably it is best to get an idea of the crs if we REALLY read the first
		// geometry of a table and the decide what to do. If we choose to transform,
		// the we want to set the constraint again (to EPSG:2056). If we to not transform
		// we should not delete the constraint.
		
		// 1) Read the first geometry.
		// 2) Try to figure out the srid.
		// 3) If the geometry has no srid (?? <> ekwb ????), try to guess if it is EPSG:21781 
		//    by checking the coordinates.
		// 4) If we transform, check if the table has a CHECK constraint.
		// 5) Unset this constraint.
		// 6) Transform geometry.
		// 7) Set correct constraint.
		
		// This IS not necessary with Postgis 2.x since the geometry srid is "mod typed": 'geometry(LineString,21781)'
		// instead of only 'geometry'.
		
		// We fake a version prior 2.0 by substringing the data_type column to only the 'geometry'.
	
		// Ah: perhaps UpdateGeometrySRID() can help at the end?
		
			
		log.debug "Postgis Version: " + postgisVersion()
		
		// Remember, remember: a table can have multiple geometry columns...
		
		
		def tranformableTables = [:]
		
		tableNames.each { tableName ->
			
			log.debug "****** ${tableName} ******"
			
			def columns = geometryColumns(tableName)
			
			if (columns.size() == 0) {
				log.debug "This table has no geometry columns"
				return
			}			
			
			// Loop through every column of this table.
			def tables = []
			columns.each { columnName, dataType ->
				log.debug "Do we need to transform geometry column: '${columnName}'?"
				
				// Check if we need to transform this geometry.
				// Sometimes we need some fuzzy logic to figure it out.
				// See below.
				def transformableGeometry =  hasTransformableGeometry(tableName, columnName)
				
				if (transformableGeometry) {
					log.debug "Yes!"
					
					
					// TODO: Check for check constraints...
					
					def table = [:]
					table['schema_name'] = tableName.tokenize('.')[0]
					table['table_name'] = tableName.tokenize('.')[1]
					table['geometry_column'] = columnName
					tables << table

				} else {
					log.debug "No!"
				}
				
			}
			tranformableTables[tableName] = tables // TODO: Test if this is empty when there is no geometry columns.
		}
		
		log.debug "These are the tables (geometry_columns) we need to transform: " + tranformableTables
	}
	
	/**
	 * Checks if we need to transform this geometry (columnName) of this
	 * table (tableName).
	 * Strategy: Get SRID of this geometry column. If it is not 21781, check
	 * the coordinate range of the a coordinate of the first geometry. If 
	 * the range is plausible, we will transform this geometry.
	 * 
	 * @param tableName
	 * @param columnName
	 * @return boolean
	 */
	private boolean hasTransformableGeometry(String tableName, String columnName) {				
		def query = """\
SELECT ST_SRID(${Sql.expand(columnName)}) as srid, 
       ST_X((ST_DumpPoints(${Sql.expand(columnName)})).geom) as x, 
       ST_Y((ST_DumpPoints(${Sql.expand(columnName)})).geom) as y
FROM ${Sql.expand(tableName)}
LIMIT 1;
"""
		def sql = Sql.newInstance(dburl)
		try {
			def row = sql.firstRow(query)
			def srid = row.srid
			def x = row.x
			def y = row.y
			
			if (srid == 21781) {
				return true
			} else if (rangeX.contains(x as int) && rangeY.contains(y as int)) {
				return true
			} else {
				println "gaga"
				return false
			}	
		} catch (SQLException e) {
			log.error e.getMessage()
			throw new SQLException(e)
		} finally {
			sql.connection.close()
			sql.close()
		}		
	}
	
	/**
	 * Figures out all the geometry columns of a table.
	 * 
	 * @param tableName
	 * @return Map (column name : data type) with all geometry columns of the table. 
	 */
	private Map geometryColumns(String tableName) {
		def schema_name = tableName.tokenize('.')[0] // But split() returns an empty array!
		def table_name = tableName.tokenize('.')[1]
		
		def query = """\
SELECT c.nspname, a.attname as column_name, format_type(a.atttypid, a.atttypmod) AS data_type
FROM pg_attribute a
JOIN pg_class b ON (a.attrelid = b.relfilenode)
JOIN pg_namespace c ON (c.oid = b.relnamespace)
WHERE b.relname = '${Sql.expand(table_name)}' AND a.attstattarget = -1
AND c.nspname = '${Sql.expand(schema_name)}';
"""
		def columns = [:]

		def sql = Sql.newInstance(dburl)
		try {
			sql.eachRow(query) {row ->
				def column_name = row.column_name
				def data_type = row.data_type
				
				// This is the postgis 1.5 fake....
				if (data_type.length() > 7) {
					if (row.data_type.substring(0,8) == 'geometry') {
						columns[column_name] = data_type
					}
				}
			}
		} catch (SQLException e) {
			log.error e.getMessage()
			throw new SQLException(e)
		} finally {
			sql.connection.close()
			sql.close()
		}	
		return columns	
	}
	
	/**
	 * Get the installed postgis version.
	 * 
	 * @return Postgis Version
	 */
	
	private String postgisVersion() {
		def sql = Sql.newInstance(dburl)
		try {
			return sql.firstRow("SELECT PostGIS_Lib_Version();")[0]
		} catch (SQLException e) {
			log.error e.getMessage()
			throw new SQLException(e)
		} finally {
			sql.connection.close()
			sql.close()
		}

	}
	

}
