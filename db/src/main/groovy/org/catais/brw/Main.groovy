package org.catais.brw

import groovy.util.logging.Log4j2
import groovy.util.CliBuilder

@Log4j2
class Main {

	// TODO: Exception Handling!!!!
	
	static main(args) {
		// some defaults
		def dbhost = "localhost"
		def dbport = "5432"
		def dbdatabase = "xanadu2"
		def dbusr = "stefan"
		def dbpwd = "ziegler12"
		
		// program parameters
		def cli = new CliBuilder(
			usage: 'java -jar XYZ.jar',
			header: '\nAvailable options (use --help for help):\n')
		cli.with {
			_ longOpt: 'help', 'Usage Information'
			_ longOpt: 'black', 'File with blacklisted tables.', required: false, args:1, argName:'file'
			_ longOpt: 'white', 'File with whitelisted tables.', required: false, args:1, argName:'file'
			_ longOpt: 'dbschema', 'Transform this dbschema only.', required: false, args:1, argName:'schema'
			_ longOpt: 'dbhost', "The host name of the server. Defaults to ${dbhost}.", required: false, args:1, argName:'host'
			_ longOpt: 'dbport', "The port number the server is listening on. Defaults to ${dbport}.", required: false, args:1, argName:'port'
			_ longOpt: 'dbdatabase', "The database name. Defaults to ${dbdatabase}", required: false, args:1, argName:'username'
			_ longOpt: 'dbusr', "User name to access database. Defaults to ${dbusr}", required: false, args:1, argName:'username'
			_ longOpt: 'dbpwd', "Password of user used to access database. Defaults to ${dbpwd}", required: false, args:1, argName:'password'
			_ longOpt: 'simulate', "Do not transform data.", required: false
		}

		def options= cli.parse(args)

		if (args.size() == 0) {
			cli.usage()
			return
		}

		if (!options) {
			return
		}

		if (options.help) {
			cli.usage()
			return
		}
		
		// Create a dbparams map for convenience.
		def dbparams = [:]
		dbparams['dbhost'] = dbhost
		dbparams['dbport'] = dbport
		dbparams['dbdatabase'] = dbdatabase
		dbparams['dbusr'] = dbusr
		dbparams['dbpwd'] = dbpwd
		
		// ...
		def pg = new PostgresqlDatabase(dbparams)
		pg.fubar()
	
		
		println "Hallo Welt."
	}

}
