package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.data.LngLat;

public class WeightedLngLat implements Comparable<WeightedLngLat> {

    /**
     * The coordinate of defined by an instance of LngLat class.
     */
    private LngLat lngLat;

    /**
     * The associated priority weighting to be used in a priority queue.
     */
    private double priority;
    public WeightedLngLat(LngLat lngLatin, double distanceIn){
        this.lngLat = lngLatin;
        this.priority = distanceIn;
    }

    @Override
    public int compareTo(WeightedLngLat lngLat) {
        return Double.compare(this.priority, lngLat.priority);
    }

    public LngLat getLngLat(){
        return this.lngLat;
    }
}
