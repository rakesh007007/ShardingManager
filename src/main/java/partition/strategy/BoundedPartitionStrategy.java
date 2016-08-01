package partition.strategy;

public abstract class BoundedPartitionStrategy<T> implements
        PartitionStrategy<T> {

    private int lowerBound;
    private int upperBound;

    public BoundedPartitionStrategy(int lowerBound, int upperBound) {
        super();
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public int getPartitionId(T partitionKey) {
        int difference = upperBound - lowerBound;
        int bucket = Math.abs(getPartitionHash(partitionKey)) % difference;
        return bucket + lowerBound;
    }

    protected abstract int getPartitionHash(T partitionKey);
}
