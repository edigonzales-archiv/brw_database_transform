import geoscript.layer.Shapefile
import geoscript.geom.Geometry


def dreiecke = [:]
def shpPath = "/home/stefan/Projekte/brw_database_transform/chenyx06/"

// LV03
Shapefile shp = new Shapefile(shpPath + 'chenyx06lv03.shp')
shp.getFeatures().each() { dreiecke
  def g = it.getGeom()
  def wkt = g.getGeometryN(0).reducePrecision(['scale':1000],'fixed').getWkt()

  def nummer = it.nummer

  def dreieck = [:]
  dreieck.put('nummer', it.nummer)
  dreieck.put('wkt_lv03', wkt)

  dreiecke[nummer] = dreieck
}

// LV95
shp = new Shapefile(shpPath + 'chenyx06lv95.shp')
shp.getFeatures().each() { dreiecke
  def g = it.getGeom()
  def wkt = g.getGeometryN(0).reducePrecision(['scale':1000],'fixed').getWkt()

  def nummer = it.nummer

  if (dreiecke[nummer]) {
    dreieck = dreiecke[nummer]
    dreieck.put('wkt_lv95', wkt)

    dreiecke[nummer] = dreieck
  }
}

// Output
dreiecke.each() {
  def value = it.value
  println value.nummer + ";" + value.wkt_lv03 + ";" + value.wkt_lv95
}
