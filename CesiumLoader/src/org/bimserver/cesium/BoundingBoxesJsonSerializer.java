package org.bimserver.cesium;

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
		
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayNode result = objectMapper.createArrayNode();
		for (long spaceOid : spaces.keySet()) {
			HashMapVirtualObject space = spaces.get(spaceOid);
			Object geometryInfoLink = space.get("geometry");
			if (geometryInfoLink != null) {
				long geometryInfoOid = (long) geometryInfoLink;
				HashMapVirtualObject geometryInfo = geometryInfos.get(geometryInfoOid);
				if (geometryInfo != null) {
					ObjectNode objectNode = objectMapper.createObjectNode();
					objectNode.put("guid", (String)space.get("GlobalId"));
					objectNode.put("name", (String)space.get("Name"));
					objectNode.put("oid", space.getOid());

					HashMapWrappedVirtualObject minBounds = (HashMapWrappedVirtualObject) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("minBounds"));
					HashMapWrappedVirtualObject maxBounds = (HashMapWrappedVirtualObject) geometryInfo.eGet(geometryInfo.eClass().getEStructuralFeature("maxBounds"));
					Double minX = (Double) minBounds.eGet("x");
					Double minY = (Double) minBounds.eGet("y");
					Double minZ = (Double) minBounds.eGet("z");
					Double maxX = (Double) maxBounds.eGet("x");
					Double maxY = (Double) maxBounds.eGet("y");
					Double maxZ = (Double) maxBounds.eGet("z");
					
					ObjectNode min = objectMapper.createObjectNode();
					min.put("x", minX);
					min.put("y", minY);
					min.put("z", minZ);
					
					ObjectNode max = objectMapper.createObjectNode();
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
			objectMapper.writeValue(new PrintWriter(outputStream), result);
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