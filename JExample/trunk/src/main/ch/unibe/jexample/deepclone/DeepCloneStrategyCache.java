package ch.unibe.jexample.deepclone;

import static ch.unibe.jexample.deepclone.DeepCloneStrategy.IMMUTABLE;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;



public class DeepCloneStrategyCache {

    private static final String[] IMMUTABLES = {
        "sun.font",
        "java.lang.Boolean",
        "java.lang.Character",
        "java.lang.Void",
        "java.lang.String",
        "java.lang.Number",
        "java.lang.Class",
        "java.lang.ClassLoader",
        "java.lang.Throwable",
        "java.lang.Thread",
    };

    private static DeepCloneStrategyCache DEFAULT = null;

    public Map<Class<?>,DeepCloneStrategy> cache; 

    private boolean isImmutable(Class<?> type) {
        if (type.isEnum()) return true;
        if (type.isAnnotation()) return true;
        if (type.isPrimitive()) return true;
        if (Object.class == type) return true;
        for (Class<?> curr = type; curr != null; curr = curr.getSuperclass()) {
            String fqn = curr.getName();
            for (String each: IMMUTABLES) if (fqn.startsWith(each)) return true;
        }
        return false;
    }


    public static DeepCloneStrategyCache getDefault() {
        return DEFAULT == null ? DEFAULT = new DeepCloneStrategyCache() : DEFAULT;
    }

    public DeepCloneStrategyCache() {
        this.cache = new IdentityHashMap<Class<?>,DeepCloneStrategy>();
    }

    public DeepCloneStrategy lookup(Object object) {
        return lookup(object.getClass());
    }

    public DeepCloneStrategy lookup(Class<?> type) {
        if (type.isPrimitive()) return IMMUTABLE;
        DeepCloneStrategy result = cache.get(type);
        if (result != null) return result;
        cache.put(type, result = makeStrategy(type));
        return result;
    }

    private DeepCloneStrategy makeStrategy(Class<?> type) {
        if (isImmutable(type)) return IMMUTABLE;
        if (type.isArray()) return new ArrayCloning(type);
        if (HashMap.class.isAssignableFrom(type)) return new HashMapCloning(); 
        if (Reference.class.isAssignableFrom(type)) return new UnsafeWithoutTransientCloning(type);
        if (noFieldsOrFinalFieldsOnly(type)) return IMMUTABLE;
        return new UnsafeCloning(type);
    }

    private boolean noFieldsOrFinalFieldsOnly(Class<?> type) {
        for (Class<?> curr = type; curr != null; curr = curr.getSuperclass()) {
            for (Field f: curr.getDeclaredFields()) {
                if (!Modifier.isFinal(f.getModifiers())) return false;
                if (!isImmutable(f.getType())) return false;
            }
        }
        return true;
    }


}
