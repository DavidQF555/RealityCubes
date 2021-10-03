package io.github.davidqf555.minecraft.realitycubes.common.world.properties.shapes;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ShapesHelper {

    public static final GenerationShape DEFAULT;
    private static final Map<String, GenerationShape> SHAPES = new HashMap<>();

    static {
        DEFAULT = new FlatShape();
        addShape(DEFAULT);
        addShape(new PeakShape());
    }

    public static void addShape(GenerationShape shape) {
        SHAPES.put(shape.getName(), shape);
    }

    public static GenerationShape getOrDefaultShape(String name) {
        return SHAPES.getOrDefault(name, DEFAULT);
    }

    @Nullable
    public static GenerationShape getShape(String name) {
        return SHAPES.get(name);
    }

    public static Set<String> getShapes() {
        return SHAPES.keySet();
    }

}
