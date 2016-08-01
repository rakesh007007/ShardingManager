package configuration.pojo;

import org.apache.commons.lang3.Validate;

/**
 * Defines a partition. A partition contains a startIndex and an endIndex. Two
 * partitions cannot have overlapping indexes. Multiple partitions can point to
 * a {@code shard}. We can assume that a partition is a {@code virtual shard}
 * and a {@code shard} is a physical DB.
 * 
 * @author anantharam.v
 *
 */
public class Partition {

	private String partitionId;

	private String shardId;

	private int partitionStartIndex;

	private int partitionEndIndex;

	public String getPartitionId() {
		return partitionId;
	}

	public int getPartitionStartIndex() {
		return partitionStartIndex;
	}

	public int getPartitionEndIndex() {
		return partitionEndIndex;
	}

	public String getShardId() {
		return shardId;
	}

	public void setPartitionId(String partitionId) {
		this.partitionId = partitionId;
	}

	public void setPartitionStartIndex(int partitionStartIndex) {
		this.partitionStartIndex = partitionStartIndex;
	}

	public void setPartitionEndIndex(int partitionEndIndex) {
		this.partitionEndIndex = partitionEndIndex;
	}

	public void setShardId(String shardId) {
		this.shardId = shardId;
	}

	/**
	 * Simple builder class to create a partition.
	 * 
	 * @author anantharam.v
	 *
	 */
	public class PartitionBuilder {
		private final Partition partition;

		public PartitionBuilder() {
			partition = new Partition();
		}

		public PartitionBuilder withPartitionId(String partitionId) {
			partition.partitionId = partitionId;
			return this;
		}

		public PartitionBuilder withPartitionStartIndex(int partitionStartIndex) {
			partition.partitionStartIndex = partitionStartIndex;
			return this;
		}

		public PartitionBuilder withPartitionEndIndex(int partitionEndIndex) {
			partition.partitionEndIndex = partitionEndIndex;
			return this;
		}

		public PartitionBuilder withShardId(String shardId) {
			partition.shardId = shardId;
			return this;
		}

		public Partition build() {
			Validate.notBlank(partition.partitionId);
			Validate.notBlank(partition.shardId);
			return partition;
		}
	}
}
