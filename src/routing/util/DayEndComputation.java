package routing.util;

import core.DTNHost;
import core.SimScenario;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class DayEndComputation implements Callable<Boolean>, Runnable {

    private Double simTime;
    private Double previousUpdateTime;
    private Double dayEndTime;

    public DayEndComputation(Double simTime, Double previousUpdateTime, Double dayEndTime) {
        this.simTime = simTime;
        this.previousUpdateTime = previousUpdateTime;
        this.dayEndTime = dayEndTime;
    }

    @Override
    public void run() {
        try {
            call();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean call() throws Exception {

        Double timer = 0.0;
        for (DTNHost host : SimScenario.getInstance().getHosts()) {
            timer = 0.0;
            List<TimeSeriesModel> timeSeriesModelList = new ArrayList<>();
            while (timer < dayEndTime) {
                for (ContactTimeAndDurationModel contactTimeAndDurationModel : host.getContactTimeAndDelays()) {
                    List<TimeSeriesModel> timeSeriesModels = host
                            .findByTimeAndHost(timer % dayEndTime, contactTimeAndDurationModel.getTo());
                    if (timeSeriesModels != null && timeSeriesModels.size() > 0) {
                        Optional<TimeSeriesModel> oldModel = timeSeriesModels.stream()
                                .reduce((first, second) -> first.getDelay() < second.getDelay() ? first : second);
                        if (oldModel.isPresent()) {
                            oldModel.get().setLocation(host.getLocation());
                            oldModel.get().setDestination(host.getDestination());
                            oldModel.get().setDelay((oldModel.get().getDelay()
                                    + ((contactTimeAndDurationModel.getStartTime() % dayEndTime) - timer)) / 2);
                            oldModel.get().setDistance(host.getLocation().distance(host.getDestination()));
                            timeSeriesModelList.add(oldModel.get());
                        }
                    } else {
                        timeSeriesModelList.add(new TimeSeriesModel(timer, contactTimeAndDurationModel.getTo(),
                                ((contactTimeAndDurationModel.getStartTime() % dayEndTime) - timer),
                                host.getLocation(), host.getDestination(), host.getLocation().distance(host.getDestination())));
                    }
                }
                timer++;
            }
            host.setTimeSeries(timeSeriesModelList);
        }
        return true;
    }
}
