package org.aioba.akka.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Optional;

public class Device extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    final String groupId;
    final String deviceId;

    Optional<Double> lastTemperatureReading = Optional.empty();

    public Device(String groupId, String deviceId){
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    public static Props props(String groupId, String deviceId){
        return Props.create(Device.class, groupId, deviceId);
    }

    //----------------------------------------------------------------------------------------
    // MESSAGES
    //----------------------------------------------------------------------------------------
    //--------- READ TEMPERATURE
    public static final class ReadTemperature {
        long requestId;
        public ReadTemperature(long requestId){
            this.requestId = requestId;
        }
    }

    public static final class RespondTemperature {
        long requestId;
        final Optional<Double> value;
        public RespondTemperature(long requestId, Optional<Double> value){
            this.requestId = requestId;
            this.value = value;
        }
    }

    //---------- RECORD TEMPERATURE
    public static final class RecordTemperature {
        final long requestId;
        final double value;

        public RecordTemperature(long requestId, double value){
            this.requestId = requestId;
            this.value = value;
        }
    }

    public static final class TemperatureRecorded {
        final long requestId;

        public TemperatureRecorded(long requestId){
            this.requestId = requestId;
        }
    }

    //----------------------------------------------------------------------------------------
    // LIFE CYCLE
    //----------------------------------------------------------------------------------------
    @Override
    public void preStart() throws Exception {
        log.info("Device {}-{} started", groupId, deviceId);
    }

    @Override
    public void postStop() throws Exception {
        log.info("Device {}-{} stopped", groupId, deviceId);
    }

    //----------------------------------------------------------------------------------------
    // HANDLING MESSAGES
    //----------------------------------------------------------------------------------------
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DeviceManager.RequestTrackDevice.class,
                r -> {
                    if (this.groupId.equals(r.groupId) && this.deviceId.equals(r.deviceId)){
                        getSender().tell(new DeviceManager.DeviceRegistered(), getSelf());
                    }
                }
            )
            .match(RecordTemperature.class,
                r -> {
                    log.info("Recorded temperature reading {} with {}", r.value, r.requestId);
                    lastTemperatureReading = Optional.of(r.value);
                    getSender().tell(new TemperatureRecorded(r.requestId), getSelf());
                }
            )
            .match(ReadTemperature.class,
                r -> {
                    getSender().tell(new RespondTemperature(r.requestId, lastTemperatureReading), getSelf());
                }
            ).build();
    }
}
