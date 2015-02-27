package il.co.codeguru.corewars8086.war;

import java.util.Iterator;

public interface CompetitionIterator extends Iterator<int[]>
{
	public long getNumberOfItems(int itemsLessThan);
	public void reset();
}