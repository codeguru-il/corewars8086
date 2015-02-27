package il.co.codeguru.corewars8086.war;

import org.apache.commons.math3.random.RandomDataGenerator;

public class RandomIterator implements CompetitionIterator {
	private final RandomDataGenerator rnd;
	private final int groupSize, numItems;
	private boolean hasNext;

    public RandomIterator(int numItems, int groupSize) {
        assert numItems >= groupSize;
        this.numItems = numItems;
		this.groupSize = groupSize;
		hasNext = true;

		rnd = new RandomDataGenerator();
    }	

    /**
     * Returns the next group in the sequence
     */
    public int[] next()
    {
    	hasNext = false;
		return rnd.nextPermutation(numItems, groupSize);
    }

	public boolean hasNext() {
        return hasNext;
    }

	public long getNumberOfItems(int itemsLessThan) {
		return 1;
	}

	public void reset() {
		hasNext = true;
	}
}