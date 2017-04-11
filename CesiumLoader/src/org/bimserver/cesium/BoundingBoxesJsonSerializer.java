package org.bimserver.cesium;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.bimserver.BimserverDatabaseException;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.models.store.IfcHeader;
import org.bimserver.plugins.PluginManagerInterface;
import org.bimserver.plugins.serializers.ObjectProvider;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;
import org.bimserver.plugins.serializers.SerializerInputstream;
import org.bimserver.plugins.serializers.StreamingReader;
import org.bimserver.plugins.serializers.StreamingSerializer;
import org.bimserver.shared.HashMapVirtualObject;
import org.bimserver.shared.HashMapWrappedVirtualObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BoundingBoxesJsonSerializer implements StreamingSerializer, StreamingReader {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private ObjectProvider objectProvider;
	private PackageMetaData packageMetaData;

	@Override
	public void init(ObjectProvider objectProvider, ProjectInfo projectInfo, IfcHeader ifcHeader, PluginManagerInterface pluginManager, PackageMetaData packageMetaData) throws SerializerException {
		this.objectProvider = objectProvider;
		this.packageMetaData = packageMetaData;
	}

	@Override
	public void writeToOutputStream(OutputStream outputStream) throws SerializerException, BimserverDatabaseException {
		Map<Long, HashMapVirtualObject> spaces = new HashMap<>();
		Map<Long, HashMapVirtualObject> geometryInfos = new HashMap<>();
		HashMapVirtualObject next = objectProvider.next();
		while (next != null) {
			if (packageMetaData.getEClass("IfcSpace").isSuperTypeOf(next.eClass())) {
				spaces.put(next.getOid(), next);
			} else if (packageMetaData.getEClassIncludingDependencies("GeometryInfo").isSuperTypeOf(next.eClass())) {
				geometryInfos.put(next.getOid(), next);
			}
			next = objectProvider.next();
		}
		
		ArrayNode result = OBJECT_MAPPER.createArrayNode();
		double lowestX = Double.MAX_VALUE;
		double lowestY = Double.MAX_VALUE;
		double lowestZ = Double.MAX_VALUE;

		double highestX = -Double.MAX_VALUE;
		double highestY = -Double.MAX_VALUE;
		double highestZ = -Double.MAX_VALUE;
		
		for (long spaceOid : spaces.keySet()) {
			HashMapVirtualObject space = spaces.get(spaceOid);
			Object geometryInfoLink = space.get("geometry");
			if (geometryInfoLink != null) {
				long geometryInfoOid = (long) geometryInfoLink;
				HashMapVirtualObject geometryInfo = geometryInfos.get(geometryInfoOid);
				if (geometryInfo != null) {
					HashMapWrappedVirtualObject minBounds = (HashMapWrappedVirtualObject) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("minBounds"));
					HashMapWrappedVirtualObject maxBounds = (HashMapWrappedVirtualObject) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("maxBounds"));
					Double minX = (Double) minBounds.eGet("x");
					Double minY = (Double) minBounds.eGet("y");
					Double minZ = (Double) minBounds.eGet("z");
					Double maxX = (Double) maxBounds.eGet("x");
					Double maxY = (Double) maxBounds.eGet("y");
					Double maxZ = (Double) maxBounds.eGet("z");
					
					if (minX < lowestX) {
						lowestX = minX;
					}
					if (minY < lowestY) {
						lowestY = minY;
					}
					if (minZ < lowestZ) {
						lowestZ = minZ;
					}
					if (maxX > highestX) {
						highestX = maxX;
					}
					if (maxY > highestY) {
						highestY = maxY;
					}
					if (maxZ > highestZ) {
						highestZ = maxZ;
					}
				}
			}
		}
		
		double xChange = (highestX - lowestX) / 2.0 + lowestX;
		double yChange = (highestY - lowestY) / 2.0 + lowestY;
		double zChange = (highestZ - lowestZ) / 2.0 + lowestZ;
		
		for (long spaceOid : spaces.keySet()) {
			HashMapVirtualObject space = spaces.get(spaceOid);
			Object geometryInfoLink = space.get("geometry");
			if (geometryInfoLink != null) {
				long geometryInfoOid = (long) geometryInfoLink;
				HashMapVirtualObject geometryInfo = geometryInfos.get(geometryInfoOid);
				if (geometryInfo != null) {
//					byte[] transformation = (byte[]) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("transformation"));
//					ByteBuffer bb = ByteBuffer.wrap(transformation);
//					bb.order(ByteOrder.LITTLE_ENDIAN);
//					DoubleBuffer buffer = bb.asDoubleBuffer();
//					double[] matrix = new double[16];
//					for (int i=0; i<16; i++) {
//						matrix[i] = buffer.get();
//					}
					
					ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
					objectNode.put("guid", (String)space.get("GlobalId"));
					objectNode.put("name", (String)space.get("Name"));
					objectNode.put("oid", space.getOid());

					HashMapWrappedVirtualObject minBounds = (HashMapWrappedVirtualObject) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("minBounds"));
					HashMapWrappedVirtualObject maxBounds = (HashMapWrappedVirtualObject) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("maxBounds"));
					Double minX = (Double) minBounds.eGet("x") - xChange;
					Double minY = (Double) minBounds.eGet("y") - yChange;
					Double minZ = (Double) minBounds.eGet("z") - zChange;
					Double maxX = (Double) maxBounds.eGet("x") - xChange;
					Double maxY = (Double) maxBounds.eGet("y") - yChange;
					Double maxZ = (Double) maxBounds.eGet("z") - zChange;

					ObjectNode min = OBJECT_MAPPER.createObjectNode();
					min.put("x", minX);
					min.put("y", minY);
					min.put("z", minZ);

					ObjectNode max = OBJECT_MAPPER.createObjectNode();
					max.put("x", maxX);
					max.put("y", maxY);
					max.put("z", maxZ);
					
					objectNode.set("min", min);
					objectNode.set("max", max);
					result.add(objectNode);
				}				
			}
		}
		try {
			OBJECT_MAPPER.writeValue(new PrintWriter(outputStream), result);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public InputStream getInputStream() {
		return new SerializerInputstream(this);
	}

	@Override
	public boolean write(OutputStream out) throws SerializerException, BimserverDatabaseException {
		return false;
	}
}