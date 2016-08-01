package partition.strategy;

/**
 * @param <T>
 * @author anantharam.v
 */
public interface PartitionStrategy<T> {

    public int getPartitionId(T partitionKey);

}
