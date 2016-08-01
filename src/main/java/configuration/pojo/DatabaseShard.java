package configuration.pojo;

import io.dropwizard.db.DataSourceFactory;

/**
 * A physical database {@code shard} defined along with data source etc.
 *
 * @author anantharam.v
 */
public class DatabaseShard {

    private String shardId;

    private DataSourceFactory database;

    public DatabaseShard() {
    }

    public String getShardId() {
        return shardId;
    }

    public void setShardId(String shardId) {
        this.shardId = shardId;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public void setDatabase(DataSourceFactory database) {
        this.database = database;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((shardId == null) ? 0 : shardId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return obj.equals(shardId);
    }

}
