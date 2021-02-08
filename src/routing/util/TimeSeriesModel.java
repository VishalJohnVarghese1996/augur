package routing.util;

import core.Coord;

import java.util.Objects;

public class TimeSeriesModel {
    private Double timeInstant;
    private int otherHost;
    private Double delay;
    private Coord location;
    private Coord destination;
    private Double distance;

    public TimeSeriesModel(Double timeInstant, int otherHost, Double delay, Coord location, Coord destination, Double distance) {
        this.timeInstant = timeInstant;
        this.otherHost = otherHost;
        this.delay = delay;
        this.location = location;
        this.destination = destination;
        this.distance = distance;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getTimeInstant() {
        return timeInstant;
    }

    public void setTimeInstant(Double timeInstant) {
        this.timeInstant = timeInstant;
    }

    public int getOtherHost() {
        return otherHost;
    }

    public void setOtherHost(int otherHost) {
        this.otherHost = otherHost;
    }

    public Double getDelay() {
        return delay;
    }

    public void setDelay(Double delay) {
        this.delay = delay;
    }

    public Coord getLocation() {
        return location;
    }

    public void setLocation(Coord location) {
        this.location = location;
    }

    public Coord getDestination() {
        return destination;
    }

    public void setDestination(Coord destination) {
        this.destination = destination;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeInstant, otherHost);
    }

    @Override
    public boolean equals(Object o) {
        return this.getTimeInstant() == ((TimeSeriesModel) o).getTimeInstant() && this.getOtherHost() == ((TimeSeriesModel) o).getOtherHost();
    }
}
