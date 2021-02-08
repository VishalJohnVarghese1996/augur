package routing.util;

public class ContactTimeAndDurationModel {

    private Double startTime;
    private Double endTime;
    private Double duration;
    private int host1;
    private int host2;

    public ContactTimeAndDurationModel(int host1, int host2, Double startTime, Double endTime, Double duration) {
        this.startTime = startTime;
        this.duration = duration;
        this.host1 = host1;
        this.host2 = host2;
//        this.msg = msg;
        this.endTime = endTime;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public int getFrom() {
        return host1;
    }

    public void setFrom(int from) {
        this.host1 = from;
    }

    public int getTo() {
        return host2;
    }

    public void setTo(int to) {
        this.host2 = to;
    }

//    @Override
//    public boolean equals(Object o) {
//        TimeSeriesModel model = (TimeSeriesModel)o;
//        return (this.msg.equals(model.msg) && this.to.equals(model.to) && this.from.equals(model.from));
//    }
}
