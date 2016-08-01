package configuration;

import configuration.pojo.DatabaseShard;
import configuration.pojo.Partition;
import configuration.pojo.PartitionBounds;

import java.util.ArrayList;
import java.util.List;

public class ShardedDatasourceConfiguration {

    private List<DatabaseShard> databaseShards = new ArrayList<DatabaseShard>();

    private List<Partition> partitions = new ArrayList<>();

    private PartitionBounds partitionBounds = new PartitionBounds();

    public ShardedDatasourceConfiguration() {
    }

    public List<DatabaseShard> getDatabaseShards() {
        return databaseShards;
    }

    public void setDatabaseShards(List<DatabaseShard> databaseShards) {
        this.databaseShards = databaseShards;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public PartitionBounds getPartitionBounds() {
        return partitionBounds;
    }

    public void setPartitionBounds(PartitionBounds partitionBounds) {
        this.partitionBounds = partitionBounds;
    }

}
