package routing;

import core.*;
import routing.util.TimeSeriesModel;
import util.Tuple;

import java.util.*;

public class AugurRouter extends ActiveRouter {

    public AugurRouter(Settings s) {
        super(s);
    }

    protected AugurRouter(ActiveRouter r) {
        super(r);
    }

    @Override
    public void update() {
        super.update();
        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }
        //exchange the deliverable messages to the other hosts
        if (exchangeDeliverableMessages() != null) {
            return;
        }
        //try to relay other msgs in the buffer
        tryOtherMessages();
        return;
    }

    protected Connection exchangeDeliverableMessages() {
        List<Connection> connections = getConnections();

        if (connections.size() == 0) {
            return null;
        }
        //sort by descending order of created time
        this.setMessages(sortByCreatedTime(new ArrayList(getMessageCollection())));
        @SuppressWarnings(value = "unchecked")
        Tuple<Message, Connection> t = tryMessagesForConnected(getMessagesForConnected());

        if (t != null) {
            return t.getValue(); // started transfer
        }

        // didn't start transfer to any node -> ask messages from connected
        for (Connection con : connections) {
            if (con.getOtherNode(getHost()).requestDeliverableMessages(con)) {
                return con;
            }
        }

        return null;
    }


    private Set addMsgToBeRelayed(Message msg, Connection con, DTNHost other) {
        Set<Tuple<Message, Connection>> msgs = new HashSet<>();
        Double directionOfThisHost = null;
        Double directionOfOtherHost = null;
        try {
            directionOfThisHost = this.getHost().getLocation().distance(msg.getTo().getLocation())
                    - this.getHost().getDestination().distance(msg.getTo().getLocation());
        } catch (NullPointerException ex) {

        }
        try {
            directionOfOtherHost = other.getLocation().distance(msg.getTo().getLocation())
                    - other.getDestination().distance(msg.getTo().getLocation());
        } catch (NullPointerException ex) {
        }
        if (directionOfOtherHost != null && directionOfThisHost != null) {
            if (directionOfOtherHost > 0) {
                if (directionOfThisHost < 0) {
                    msgs.add(new Tuple<Message, Connection>(msg, con));
                } else {
                    if (other.getDestination().distance(msg.getTo().getLocation())
                            < this.getHost().getDestination().distance(msg.getTo().getLocation())) {
                        msgs.add(new Tuple<Message, Connection>(msg, con));
                    }
                }
            }

        }
        return msgs;
    }

    private TimeSeriesModel getThisHostTimeSeries(Message msg) {
        TimeSeriesModel thisHostTimeSeriesModel = null;
        for (TimeSeriesModel model : this.getHost().getTimeSeries()) {
            if (model.getOtherHost() == msg.getTo().getAddress()) {
                if (model.getTimeInstant().doubleValue() >= (SimClock.getTime() % 3600)
                        && model.getTimeInstant().doubleValue() < (SimClock.getTime() % 3600 + 1)) {
                    Double directionOfModel = null;
                    try {
                        directionOfModel = model.getLocation().distance(msg.getTo().getLocation())
                                - model.getDestination().distance(msg.getTo().getLocation());
                    } catch (NullPointerException ex) {
                    }
                    if (thisHostTimeSeriesModel != null) {
                        if (directionOfModel != null && directionOfModel > 0) {
                            Double distanceToRecOld = thisHostTimeSeriesModel.getLocation().distance(msg.getTo().getLocation());
                            Double distanceToRecModel = model.getLocation().distance(msg.getTo().getLocation());
                            if (distanceToRecModel < distanceToRecOld) {
                                thisHostTimeSeriesModel = model;
                            }
                        }
                    } else {
                        if (directionOfModel != null && directionOfModel > 0) {
                            thisHostTimeSeriesModel = model;
                        }
                    }
                }
            }
        }
        return thisHostTimeSeriesModel;
    }

    private TimeSeriesModel getOtherHostTimeSeries(Message msg, DTNHost other) {
        TimeSeriesModel otherHostTimeSeriesModel = null;
        for (TimeSeriesModel model : other.getTimeSeries()) {
            if (model.getOtherHost() == msg.getTo().getAddress()) {
                if (model.getTimeInstant().doubleValue() >= (SimClock.getTime() % 3600)
                        && model.getTimeInstant().doubleValue() < (SimClock.getTime() % 3600 + 1)) {
                    Double directionOfModel = null;
                    try {
                        directionOfModel = model.getLocation().distance(msg.getTo().getLocation())
                                - model.getDestination().distance(msg.getTo().getLocation());
                    } catch (NullPointerException ex) {
                    }

                    if (otherHostTimeSeriesModel != null) {
                        if (directionOfModel != null && directionOfModel > 0) {
                            Double distanceToRecOld = otherHostTimeSeriesModel.getLocation().distance(msg.getTo().getLocation());
                            Double distanceToRecModel = model.getLocation().distance(msg.getTo().getLocation());
                            if (distanceToRecModel < distanceToRecOld) {
                                otherHostTimeSeriesModel = model;
                            }
                        }
                    } else {
                        if (directionOfModel != null && directionOfModel > 0) {
                            otherHostTimeSeriesModel = model;
                        }
                    }
                }
            }
        }
        return otherHostTimeSeriesModel;
    }

    private Tuple<Message, Connection> tryOtherMessages() {

        //messages to be relayed or delivered to next node in the corresponding connection
        Set<Tuple<Message, Connection>> msgs = new HashSet<>();
        int flag = 0;
        //check if there are existing connections
        if (getConnections().size() > 0) {
            //sort msgs in descending order of creation time
            this.setMessages(sortByCreatedTime(new ArrayList(getMessageCollection())));
            //iterate for each and every connection
            for (Connection con : getConnections()) {
                //get the other host in the connection
                DTNHost other = con.getOtherNode(getHost());
                //get the other router in the connection to which the other host is mapped
                AugurRouter othRouter = (AugurRouter) other.getRouter();
                //if other router router is transferring then skip it and go to next connection
                if (othRouter.isTransferring()) {
                    continue; // skip hosts that are transferring
                }

                //iterate through the message collection
                for (Message msg : getMessageCollection()) {

                    //if the other node has the msg then skip it
                    if (othRouter.hasMessage(msg.getId())) {
                        continue; // skip messages that the other one has
                    }
                    //if other host in the destination of the message then deliver it
                    if (other.equals(msg.getTo())) {
                        msgs.add(new Tuple<Message, Connection>(msg, con));
                    } else {
                        flag = 0;

                        //sending the msg based on the direction of the two nodes w.r.t msg destination
                        Set msgToBeRelayed = addMsgToBeRelayed(msg, con, other);
                        if (msgToBeRelayed.size() > 0) {
                            msgs.addAll(msgToBeRelayed);
                            continue;
                        }


                        //finding the best possible time series for this host based on current time and location
                        TimeSeriesModel thisHostTimeSeriesModel = getThisHostTimeSeries(msg);

                        //finding the best possible time series for other host based on current time and location
                        TimeSeriesModel otherHostTimeSeriesModel = getOtherHostTimeSeries(msg, other);

                        if (otherHostTimeSeriesModel != null) {

                            //variables to check if the hosts are moving towards msg destination or not
                            Double directionOfThisHost2 = null;
                            Double directionOfOtherHost2 = null;

                            if (thisHostTimeSeriesModel != null) {
                                try {
                                    directionOfThisHost2 = thisHostTimeSeriesModel.getLocation().distance(msg.getTo().getLocation())
                                            - thisHostTimeSeriesModel.getDestination().distance(msg.getTo().getLocation());
                                } catch (NullPointerException ex) {

                                }
                                try {
                                    directionOfOtherHost2 = otherHostTimeSeriesModel.getLocation().distance(msg.getTo().getLocation())
                                            - otherHostTimeSeriesModel.getDestination().distance(msg.getTo().getLocation());
                                } catch (NullPointerException ex) {
                                }

                                if (directionOfOtherHost2 != null && directionOfThisHost2 != null) {
                                    if (directionOfOtherHost2 > 0) {

                                        //check if other host is moving closer to msg destination or the other host delay is less compared to this host delay
                                        if (directionOfThisHost2 < 0
                                                || (otherHostTimeSeriesModel.getDestination().distance(msg.getTo().getLocation())
                                                < thisHostTimeSeriesModel.getDestination().distance(msg.getTo().getLocation()))
                                                || (otherHostTimeSeriesModel.getDelay() < thisHostTimeSeriesModel.getDelay())) {

                                            //check if the msg size is greater than the other host buffer size or other routerhas this message
                                            if ((long) msg.getSize() > othRouter.getBufferSize() || othRouter.hasMessage(msg.getId())) {
                                                continue;
                                            }

                                            //sort the other router messages in descending order of creation time
                                            othRouter.setMessages(sortByCreatedTime(new ArrayList(othRouter.getMessageCollection())));

                                            //free the buffer space to accomodate the message
                                            while (othRouter.getFreeBufferSize() < msg.getSize()) {
                                                Message oldMsg = othRouter.getMessageCollection().stream().reduce((first, second) -> second)
                                                        .orElse(null);
                                                if (oldMsg != null && oldMsg.getCreationTime() < msg.getCreationTime()) {
                                                    othRouter.getMessageCollection().remove(oldMsg);
                                                } else {
                                                    flag = 1;
                                                    break;
                                                }
                                            }

                                            //if the other node's buffer has space
                                            if (flag == 0) {
                                                //add message to be relayed
                                                msgs.add(new Tuple<Message, Connection>(msg, con));
                                                continue;
                                            }
                                        }
                                    }
                                }
                            } else {
                                //TODO remove this else if we have global data
                                msgs.add(new Tuple<Message, Connection>(msg, con));
                            }
                        }
                    }
                }
            }
        }
        //if there is no messages to be relayed or delivered
        if (msgs.size() == 0) {
            return null;
        }
        //sort msgs based on the creation time
        Collections.sort(new ArrayList<>(msgs), new TupleComparator());
        //try sending the messages to connections
        return tryMessagesForConnected(new ArrayList<>(msgs));    // try to send messages
    }

    //comparator for sorting messages in descending order of creation time
    private class TupleComparator implements Comparator
            <Tuple<Message, Connection>> {

        public int compare(Tuple<Message, Connection> tuple1,
                           Tuple<Message, Connection> tuple2) {

            double p1 = tuple1.getKey().getCreationTime().doubleValue();
            // -"- tuple2...
            double p2 = tuple2.getKey().getCreationTime().doubleValue();


            if (p1 - p2 == 0) {
                return 1;
            } else if (p1 - p2 < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }


    //sorting the message buffer in descending order of creation time
    protected List sortByCreatedTime(List list) {
        Collections.sort(list,
                new Comparator() {
                    /** Compares two tuples by their messages' receiving time */
                    public int compare(Object o1, Object o2) {
                        double diff;
                        Message m1, m2;

                        if (o1 instanceof Tuple) {
                            m1 = ((Tuple<Message, Connection>) o1).getKey();
                            m2 = ((Tuple<Message, Connection>) o2).getKey();
                        } else if (o1 instanceof Message) {
                            m1 = (Message) o1;
                            m2 = (Message) o2;
                        } else {
                            throw new SimError("Invalid type of objects in " +
                                    "the list");
                        }

                        diff = m1.getCreationTime() - m2.getCreationTime();
                        if (diff == 0) {
                            return 0;
                        }
                        return (diff < 0 ? 1 : -1);
                    }
                });
        return list;
    }

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);
    }

    @Override
    public AugurRouter replicate() {
        return new AugurRouter(this);
    }

    //set the message buffer with these messages
    public void setMessages(List<Message> messages) {
        this.messages.clear();
        LinkedHashMap<String, Message> newMessages = new LinkedHashMap<>();
        for (Message message : messages) {
            newMessages.put(message.getId(), message);
        }
        this.messages = newMessages;
    }

}
