package partition.manager;

import configuration.pojo.DatabaseShard;
import configuration.pojo.Partition;
import org.apache.commons.lang3.Validate;

import java.util.*;

/**
 * Provides a partition .
 *
 * @author anantharam.v
 */
public class HorizontalPartitionManager {

    private NavigableMap<Integer, Partition> partitionMap = new TreeMap<Integer, Partition>();

    private Map<String, DatabaseShard> databaseShards = new HashMap<>();

    public HorizontalPartitionManager(Collection<DatabaseShard> dataabaseShards) {
        for (DatabaseShard shard : dataabaseShards) {
            databaseShards.put(shard.getShardId(), shard);
        }
    }

    public void addPartition(Partition partition) {

        Validate.notNull(partition, "configuration.pojo.Partition is empty");
        Validate.notBlank(partition.getShardId(), "ShardId is empty");
        Validate.isTrue(databaseShards.containsKey(partition.getShardId()),
                "ShardId %s does not have a DB mapping", partition.getShardId());

        Map.Entry<Integer, Partition> lowerEntry = partitionMap
                .lowerEntry(partition.getPartitionStartIndex());
        Map.Entry<Integer, Partition> higherEntry = partitionMap
                .lowerEntry(partition.getPartitionEndIndex());

        if ((lowerEntry == null && higherEntry == null)
                || lowerEntry.getKey().equals(higherEntry.getKey())) {
            // We do not have any overlapping sequences.
            partitionMap.put(partition.getPartitionStartIndex(), partition);
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "Conflict by inserting partion: %s. Two partition indexes are overlapping.",
                            partition.getPartitionId()));
        }
    }

    public DatabaseShard getDatabaseShard(int partitionIndex) {
        Map.Entry<Integer, Partition> partition = partitionMap
                .floorEntry(partitionIndex);
        if (partition == null) {
            throw new IllegalArgumentException(
                    "No shard found for virtual partition index:"
                            + partitionIndex); // Should I throw a different
            // exception?
        }
        return databaseShards.get(partition.getValue().getShardId());
    }

}
