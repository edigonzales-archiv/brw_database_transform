package org.catais.brw

import groovy.util.logging.Log4j2

import geoscript.feature.Schema
import geoscript.feature.Feature
import geoscript.geom.Geometry 
import geoscript.layer.Layer
import geoscript.index.STRtree
//import com.vividsolutions.jts.geom.Geometry 

@Log4j2
class FreeFrame {
	Schema schema = new Schema("triangle", "nummer:String,geom_lv03:Polygon:srid=21781,geom_lv95:Polygon:srid=2056")
	Layer layer = new Layer("chenyx06", schema)
	STRtree index = new STRtree()
	
	public FreeFrame() {
		init()
	}

	private void init() {	
		// First we create layer with the triangles and build the spatial index.
		def i = 1
		def features = []
		URL url = this.class.classLoader.getResource("chenyx06.tri")
		new File(url.toURI()).eachLine { line ->
		
			
			def list = line.split(";") // WTF: split() seems to work here?!
			if (list.size() == 3) {
				
//				println list[0]
//				println list[1]
//				println list[2]
				
				String nummer = list[0]
				Geometry geom_lv03 = Geometry.fromWKT(list[1])
				Geometry geom_lv95 = Geometry.fromWKT(list[2])
//				
				Feature f = new Feature(['nummer': nummer, 'geom_lv03': geom_lv03, 'geom_lv95': geom_lv95], i++ as String, schema)
				features << f // Much faster than layer.add(f).
				
				index.insert(geom_lv03.getBounds(), f)
				
//				println geom_lv03.getDimension()
//				println geom_lv03.getCoordinates()
				
				
			}
		}
		layer.add(features)
		
		// TODO: Figure out if this is a good idea.
		assert layer.count() == 11882
	}
	
	public transform(Geometry geom) {
		
		// TODO: FIRST:!!!! what happens with xyzm???
		// vergleich postgis vs geoscript: falles nichts verloren geht, kann man weiterfahren sonst nicht.
		
	}
	
		
}
