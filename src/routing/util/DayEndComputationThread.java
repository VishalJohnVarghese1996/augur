//package routing.util;
//
//import core.DTNHost;
//import core.SimScenario;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class DayEndComputationThread extends Thread {
//
//
//    private boolean keepRunning = false;
//
//    private boolean threadEnded = false;
//
//    public void startThread() throws IOException {
//        start();
//    }
//
//    public void stopThread() {
//        if (keepRunning == false) {
//            return;
//        }
//        threadEnded = false;
//        keepRunning = false;
//        while (!threadEnded) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//
//            }
//        }
//    }
//
//    public void run() {
//        keepRunning = true;
//        try {
//            while (keepRunning) {
//                updateTimeSeries();
//            }
//        } catch (Exception ex) {
//            threadEnded = true;
//        }
//        keepRunning = false;
//        threadEnded = true;
//    }
//
//    public synchronized void updateTimeSeries() {
//        Double timer = 0.0;
////        while (timer < 86400) {
//        for (DTNHost host : SimScenario.getInstance().getHosts()) {
//            timer = 0.0;
//            List<TimeSeriesModel> timeSeriesModelList = new ArrayList<>();
//            while (timer < 3600) {
//                for (ContactTimeAndDurationModel contactTimeAndDurationModel : host.getContactTimeAndDelays()) {
//                    if (contactTimeAndDurationModel.getDuration() >= 2 && contactTimeAndDurationModel.getDuration() <= 4) {
//                        List<TimeSeriesModel> timeSeriesModels = host.findByTimeAndHost(timer % 3600, contactTimeAndDurationModel.getTo());
//                        if (timeSeriesModels != null && timeSeriesModels.size() > 0) {
//                            TimeSeriesModel oldModel = timeSeriesModels.get(0);
//                            oldModel.setDelay((oldModel.getDelay() + ((contactTimeAndDurationModel.getStartTime() % 3600) - timer)) / 2);
//                            timeSeriesModelList.add(oldModel);
//                        } else {
//                            timeSeriesModelList.add(new TimeSeriesModel(timer, contactTimeAndDurationModel.getTo(),
//                                    ((contactTimeAndDurationModel.getStartTime() % 3600) - timer)));
//                        }
//                    }
//                }
//                timer++;
//            }
//            host.setTimeSeries(timeSeriesModelList);
//        }
////        }
//    }
//}
//
