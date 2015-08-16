package org.catais.brw

import groovy.util.logging.Log4j2

@Log4j2
class Utils {
	
	
	public static List readBlackOrWhiteListFile(String filename) {		
		return new File(filename).collect {it}
	}

}
