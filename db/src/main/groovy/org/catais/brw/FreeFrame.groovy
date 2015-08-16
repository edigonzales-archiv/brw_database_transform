package org.catais.brw

class FreeFrame {
	
	public FreeFrame() {
		init()
	}

	private void init() {
		URL url = this.class.classLoader.getResource("chenyx06.tri")
		new File(url.toURI()).eachLine { line ->
		
//			println line
			
			def list = line.split(";") // WTF: split() seems to work here?!
			println list.size()
			
			if (list.size() != 3) {
				println "fuuu"
			}
		}
	
	}
	

}
