package org.aioba.akka.iot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DeviceManager extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    final Map<String, ActorRef> groupIdToActor = new HashMap<>();
    final Map<ActorRef, String> actorToGroupId = new HashMap<>();

    public static Props props(){
        return Props.create(DeviceManager.class);
    }

    public DeviceManager(){}

    //----------------------------------------------------------------------------------------
    // MESSAGES
    //----------------------------------------------------------------------------------------
    //--------- REQUEST TO TRACK A DEVICE
    public static final class RequestTrackDevice {
        final String groupId;
        final String deviceId;
        public RequestTrackDevice(String groupId, String deviceId){
            this.groupId = groupId;
            this.deviceId = deviceId;
        }
    }

    //--------- DEVICE REGISTERED
    public static final class DeviceRegistered {
        public DeviceRegistered(){}
    }

    //--------- REQUEST DEVICE GROUP LIST
    public static final class RequestDeviceGroupList {
        final long requestId;
        public RequestDeviceGroupList(long requestId){
            this.requestId = requestId;
        }
    }

    public static final class ReplyDeviceGroupList {
        final long requestId;
        final Set<String> ids;
        public ReplyDeviceGroupList(long requestId, Set<String> ids){
            this.requestId = requestId;
            this.ids = ids;
        }
    }

    //----------------------------------------------------------------------------------------
    // LIFE CYCLE
    //----------------------------------------------------------------------------------------
    @Override
    public void preStart() throws Exception {
        log.info("DeviceManager started");

    }

    @Override
    public void postStop() throws Exception {
        log.info("DeviceManager stopped");
    }


    //----------------------------------------------------------------------------------------
    // HANDLING MESSAGES
    //----------------------------------------------------------------------------------------
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(RequestTrackDevice.class, this::onTrackDevice)
            .match(RequestDeviceGroupList.class, this::onDeviceGroupList)
            .match(Terminated.class, this::onTerminated)
            .build();
    }

    private void onTrackDevice(RequestTrackDevice trackMsg){
        String groupId = trackMsg.groupId;
        ActorRef ref = groupIdToActor.get(groupId);
        if(ref != null){
            ref.forward(trackMsg, getContext());
        } else {
            log.info("Creating device group actor for {}", groupId);
            ActorRef groupActor = getContext().actorOf(DeviceGroup.props(groupId), "group-" + groupId);
            getContext().watch(groupActor);
            groupActor.forward(trackMsg, getContext());
            groupIdToActor.put(groupId, groupActor);
            actorToGroupId.put(groupActor, groupId);
        }
    }

    private void onDeviceGroupList(RequestDeviceGroupList dgl){
        getSender().tell(new ReplyDeviceGroupList(dgl.requestId, this.groupIdToActor.keySet()), getSelf());
    }

    private void onTerminated(Terminated t){
        ActorRef groupActor = t.getActor();
        String groupId = actorToGroupId.get(groupActor);
        log.info("Device group actor for {} has been terminated", groupId);
        actorToGroupId.remove(groupActor);
        groupIdToActor.remove(groupId);
    }
}
