package re.neotamia.config.registry;

import re.neotamia.config.Serializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerializerRegistry {
    private final List<Serializer> serializers = new ArrayList<>();

    public void register(Serializer serializer) {
        serializers.add(serializer);
    }

    public Serializer getSerializerFromExtension(String extension) {
        for (Serializer serializer : serializers) {
            if (serializer.getSupportedExtensions().contains(extension.toLowerCase()))
                return serializer;
        }
        throw new IllegalArgumentException("No serializer found for extension: " + extension);
    }

    public Serializer getByExtension(Path path) {
        return this.getSerializerFromExtension(path.toString().substring(path.toString().lastIndexOf('.') + 1));
    }

    public List<Serializer> getSerializers() {
        return serializers;
    }
}
