package org.bimserver.cesium;

import java.util.Collections;
import java.util.Set;

import org.bimserver.emf.Schema;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.serializers.StreamingSerializer;
import org.bimserver.plugins.serializers.StreamingSerializerPlugin;
import org.bimserver.shared.exceptions.PluginException;

public class BoundingBoxesJsonSerializerPlugin implements StreamingSerializerPlugin {

	@Override
	public void init(PluginContext pluginContext) throws PluginException {
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return null;
	}

	@Override
	public StreamingSerializer createSerializer(PluginConfiguration plugin) {
		return new BoundingBoxesJsonSerializer();
	}

	@Override
	public Set<Schema> getSupportedSchemas() {
		return Collections.singleton(Schema.IFC2X3TC1);
	}

	@Override
	public String getOutputFormat(Schema schema) {
		return null;
	}
}