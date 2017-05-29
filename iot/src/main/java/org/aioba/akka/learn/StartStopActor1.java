package org.aioba.akka.learn;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class StartStopActor1 extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        System.out.println("first started");
        getContext().actorOf(Props.create(StartStopActor2.class), "second");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("first stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals("stop",
                s -> {
                    getContext().stop(getSelf());
                }
            ).build();
    }
}
