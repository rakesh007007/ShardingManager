package shardProvider;

import com.google.inject.Injector;
import org.apache.commons.lang3.Validate;

import java.util.*;

/**
 * @author anantharam.v Provides a sharding context to execute actions within a
 *         shard.
 */
public class ShardingContextProvider {

    private Map<String, Injector> shardedContexts = new HashMap<>();

    /**
     * Adds an {@link Injector} per shard defined in the app. The
     * {@link Injector} provides a context of objects that are associated with a
     * shard. It is recommended to instantiate the {@link Injector} during
     * startup and the mapping between an <code>context</code> and
     * <code>shardId</code> are defined on startup. More than one
     * <code>context</code> cannot be associated to a shardId.
     *
     * @param shardId
     * @param context
     * @throws IllegalArgumentException If <code>shardId</code> or <code>context</code> is null.
     *                                  Or an existing {@link Injector} for the
     *                                  <code>shardId</code>
     */
    public synchronized void addContext(String shardId, Injector context)
            throws IllegalArgumentException {

        Validate.notBlank(shardId, "ShardId cannot be empty.");
        Validate.notNull(context, "Injector/Context cannot be null");
        if (shardedContexts.containsKey(shardId)) {
            throw new IllegalArgumentException(String.format(
                    "ShardId: %s is already associated with an injector",
                    shardId));
        }
        shardedContexts.put(shardId, context);
    }

    public Injector getContext(String shardId) {
        return shardedContexts.get(shardId);
    }

    public List<Injector> getAllContexts() {
        return Collections.unmodifiableList(new ArrayList<>(shardedContexts
                .values()));
    }
}
