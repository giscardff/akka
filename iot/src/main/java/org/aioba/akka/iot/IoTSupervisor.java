package org.aioba.akka.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class IoTSupervisor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(){
        return Props.create(IoTSupervisor.class);
    }

    @Override
    public void preStart() throws Exception {
        log.info("IoT Application Started");
    }

    @Override
    public void postStop() throws Exception {
        log.info("IoT Application Stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
