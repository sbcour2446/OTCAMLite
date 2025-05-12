package gov.mil.otc._3dvis.datamodel.timed;

import gov.mil.otc._3dvis.time.TimeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic class to store and maintain a timer ordered list of data.
 *
 * @param <Z> The data type.
 */
public class TimedDataSet<Z extends TimedData> {

    protected final List<Z> indexedList = new ArrayList<>();
    protected int currentIndex = -1;
    private final boolean allowClear;

    public TimedDataSet() {
        this.allowClear = false;
    }

    public TimedDataSet(boolean allowClear) {
        this.allowClear = allowClear;
    }

    /**
     * Updates the index to the list with respect to the provided timestamp.  The index will be updated to point
     * to the last item less than or equal to the provided timestamp.
     *
     * @param time The new time.
     * @return True if the index position changed, false otherwise.
     */
    public boolean updateTime(long time) {
        synchronized (indexedList) {
            int index = currentIndex;

            if (indexedList.isEmpty() || indexedList.get(0).getTimestamp() > time) {
                currentIndex = -1;
                return currentIndex != index;
            }

            int lastIndex = indexedList.size() - 1;
            if (currentIndex < 0) {
                currentIndex = 0;
            }

            while (currentIndex < lastIndex && indexedList.get(currentIndex + 1).getTimestamp() <= time) {
                currentIndex++;
            }

            while (currentIndex > 0 && indexedList.get(currentIndex).getTimestamp() > time) {
                currentIndex--;
            }

            return currentIndex != index;
        }
    }

    public Z getFirst() {
        synchronized (indexedList) {
            return indexedList.isEmpty() ? null : indexedList.get(0);
        }
    }

    public Z getCurrent() {
        synchronized (indexedList) {
            return currentIndex < 0 || indexedList.isEmpty() ? null : indexedList.get(currentIndex);
        }
    }

    public Z getLast() {
        synchronized (indexedList) {
            return indexedList.isEmpty() ? null : indexedList.get(indexedList.size() - 1);
        }
    }

    public Z getLastBefore(long timestamp) {
        synchronized (indexedList) {
            int index = currentIndex;

            if (indexedList.isEmpty() || indexedList.get(0).getTimestamp() > timestamp) {
                return null;
            }

            int lastIndex = indexedList.size() - 1;
            if (index < 0) {
                index = 0;
            }

            while (index < lastIndex && indexedList.get(index + 1).getTimestamp() <= timestamp) {
                index++;
            }

            while (index > 0 && indexedList.get(index).getTimestamp() > timestamp) {
                index--;
            }

            return indexedList.get(index);
        }
    }

    public Z getNextAfter(long timestamp) {
        synchronized (indexedList) {
            int index = currentIndex;

            if (indexedList.isEmpty() || indexedList.get(indexedList.size() - 1).getTimestamp() < timestamp) {
                return null;
            }

            int lastIndex = indexedList.size() - 1;
            if (index < 0) {
                index = 0;
            }

            while (index < lastIndex && indexedList.get(index).getTimestamp() <= timestamp) {
                index++;
            }

            while (index > 0 && indexedList.get(index - 1).getTimestamp() > timestamp) {
                index--;
            }

            return indexedList.get(index);
        }
    }

    /**
     * Add data item to the set.
     *
     * @param z The data item.
     */
    public void add(Z z) {
        // Search list to place new item, assuming most data is added to the end of the list.
        synchronized (indexedList) {
            int index = indexedList.size() - 1;
            if (indexedList.isEmpty() || indexedList.get(index).getTimestamp() < z.getTimestamp()) {
                indexedList.add(z);
            } else if (indexedList.get(index).getTimestamp() != z.getTimestamp()) {
                while (index > 0 && indexedList.get(index).getTimestamp() > z.getTimestamp()) {
                    index--;
                }
                if (indexedList.get(index).getTimestamp() != z.getTimestamp()) {
                    if (indexedList.get(index).getTimestamp() < z.getTimestamp()) {
                        indexedList.add(index + 1, z);
                    } else {
                        indexedList.add(index, z);
                    }
                }
            }
        }
    }

    public void addAll(List<Z> zList) {
        synchronized (indexedList) {
            if (indexedList.isEmpty()) {
                indexedList.addAll(zList);
                return;
            }
            int index = 0;
            for (Z z : zList) {
                if (indexedList.get(index).getTimestamp() < z.getTimestamp()) {
                    indexedList.add(z);
                    index++;
                } else if (indexedList.get(index).getTimestamp() != z.getTimestamp()) {
                    while (index > 0 && indexedList.get(index).getTimestamp() > z.getTimestamp()) {
                        index--;
                    }
                    if (indexedList.get(index).getTimestamp() != z.getTimestamp()) {
                        if (indexedList.get(index).getTimestamp() < z.getTimestamp()) {
                            indexedList.add(index + 1, z);
                            index++;
                        } else {
                            indexedList.add(index, z);
                            index++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the reverse history from the current time to the cutoff time.
     *
     * @param cutoff The cutoff time.
     * @return The reverse ordered list.
     */
    public List<Z> getHistory(long cutoff) {
        List<Z> history = new ArrayList<>();
        int index = currentIndex;
        long currentTime = TimeManager.getTime();
        while (index >= 0 && currentTime - indexedList.get(index).getTimestamp() < cutoff) {
            history.add(indexedList.get(index--));
        }
        return history;
    }

    /**
     * Get all items in this data set.
     *
     * @return The ordered list of items in data set.
     */
    public List<Z> getAll() {
        return new ArrayList<>(indexedList);
    }

    /**
     * Clears all items if allowed.  Must set allow clear on instantiation.
     */
    public void clear() {
        if (allowClear) {
            synchronized (indexedList) {
                indexedList.clear();
                currentIndex = -1;
            }
        }
    }

    public void copy(TimedDataSet<Z> timedDataSet) {
        for (Z z : timedDataSet.indexedList) {
            add(z);
        }
    }
}
