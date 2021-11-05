package ru.runa.wfe.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.util.Pair;

public class SecuredObjectUtil {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final Key JWT_SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    /**
     * All objects must be of the same SecuredObjectType.
     * If "objects" is null or empty, then returned "type" is null and returned "ids" is empty list.
     * <p>
     * TODO Similar logic exists in ReflectionRowBuilder.EnvImpl.isAllowed(), but with SecuredObjectExtractor call on each list item.
     *      After migrating to Spring & Hibernate versions that work with Java8, make this method overload which takes some iterable producer.
     */
    public static <T extends SecuredObject> Pair<SecuredObjectType, ArrayList<Long>> splitObjectsToTypeAndIds(List<T> objects) {
        int n = objects == null ? 0 : objects.size();
        val ids = new ArrayList<Long>(n);
        if (n == 0) {
            return new Pair<>(null, ids);
        }

        val type = objects.get(0).getSecuredObjectType();
        for (val o : objects) {
            if (o.getSecuredObjectType() != type) {
                throw new InternalApplicationException("Found objects of different types: (" + type + "," + objects.get(0).getId() + ") and ("
                        + o.getSecuredObjectType() + "," + o.getId() + ")");
            }
            ids.add(o.getId());
        }
        return new Pair<>(type, ids);
    }
}
