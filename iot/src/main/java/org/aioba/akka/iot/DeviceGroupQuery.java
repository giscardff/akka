package org.aioba.akka.iot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DeviceGroupQuery extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    final Map<ActorRef, String> actorToDeviceId;
    final long requestId;
    final ActorRef requester;
    Cancellable queryTimeoutTimer;

    public static Props props(Map<ActorRef, String> actorToDeviceId, long requestId, ActorRef requester, FiniteDuration timeout){
        return Props.create(DeviceGroupQuery.class, actorToDeviceId, requestId, requester, timeout);
    }

    public DeviceGroupQuery(Map<ActorRef, String> actorToDeviceId, long requestId, ActorRef requester, FiniteDuration timeout){
        this.actorToDeviceId = actorToDeviceId;
        this.requestId = requestId;
        this.requester = requester;
        this.queryTimeoutTimer = getContext().getSystem().scheduler().scheduleOnce(timeout, getSelf(), new CollectionTimeout(), getContext().dispatcher(), getSelf());
    }

    //----------------------------------------------------------------------------------------
    // MESSAGES
    //----------------------------------------------------------------------------------------
    //--------- TIMEOUT
    public static final class CollectionTimeout {}

    //----------------------------------------------------------------------------------------
    // LIFE CYCLE
    //----------------------------------------------------------------------------------------
    @Override
    public void preStart() throws Exception {
        for(ActorRef deviceActor : actorToDeviceId.keySet()){
            getContext().watch(deviceActor);
            deviceActor.tell(new Device.ReadTemperature(0L), getSelf());
        }
    }

    @Override
    public void postStop() throws Exception {
        queryTimeoutTimer.cancel();
    }

    //----------------------------------------------------------------------------------------
    // HANDLING MESSAGES
    //----------------------------------------------------------------------------------------
    @Override
    public Receive createReceive() {
        return waitingForReplies(new HashMap<>(), actorToDeviceId.keySet());
    }

    public Receive waitingForReplies(Map<String, DeviceGroup.TemperatureReading> repliesSoFar, Set<ActorRef> stillWaiting){
        return receiveBuilder()
            .build();
    }



}
