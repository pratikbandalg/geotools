/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

public abstract class JDBCEmptyGeometryOnlineTest extends JDBCTestSupport {

    @Override
    protected abstract JDBCEmptyGeometryTestSetup createTestSetup();

    @Test
    public void testEmptyPoint() throws Exception {
        testInsertEmptyGeometry("POINT");
    }

    @Test
    public void testEmptyLine() throws Exception {
        testInsertEmptyGeometry("LINESTRING");
    }

    @Test
    public void testEmptyPolygon() throws Exception {
        testInsertEmptyGeometry("POLYGON");
    }

    @Test
    public void testEmptyMultiPoint() throws Exception {
        testInsertEmptyGeometry("MULTIPOINT");
    }

    @Test
    public void testEmptyMultiLine() throws Exception {
        testInsertEmptyGeometry("MULTILINESTRING");
    }

    @Test
    public void testEmptyMultiPolygon() throws Exception {
        testInsertEmptyGeometry("MULTIPOLYGON");
    }

    private void testInsertEmptyGeometry(String type) throws Exception {
        WKTReader reader = new WKTReader();
        Geometry emptyGeometry = reader.read(type.toUpperCase() + " EMPTY");

        try (Transaction tx = new DefaultTransaction();
                FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                        dataStore.getFeatureWriterAppend(tname("empty"), tx)) {
            SimpleFeature feature = writer.next();
            feature.setAttribute(aname("id"), Integer.valueOf(100));
            feature.setAttribute(aname("geom_" + type.toLowerCase()), emptyGeometry);
            feature.setAttribute(aname("name"), new String("empty " + type));
            writer.write();
            writer.close();
            tx.commit();
        }

        SimpleFeatureCollection fc = dataStore.getFeatureSource(tname("empty")).getFeatures();
        assertEquals(1, fc.size());
        try (SimpleFeatureIterator fi = fc.features()) {
            SimpleFeature nf = fi.next();
            Geometry geometry = (Geometry) nf.getDefaultGeometry();
            // either null or empty, we don't really care
            assertTrue(geometry == null || geometry.isEmpty());
        }
    }
}
