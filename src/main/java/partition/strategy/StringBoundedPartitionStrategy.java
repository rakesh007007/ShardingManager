package partition.strategy;

public class StringBoundedPartitionStrategy extends
        BoundedPartitionStrategy<String> {

    public StringBoundedPartitionStrategy(int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
    }

    @Override
    protected int getPartitionHash(String partitionKey) {
        return partitionKey.hashCode();
    }
}
