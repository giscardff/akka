package org.aioba.akka.learn;

import akka.actor.AbstractActor;

public class SupervisedActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        System.out.println("Supervised actor started");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("Supervised actor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals("fail",
                f -> {
                    System.out.println("supervised actor fails now");
                    throw new Exception("I failed");
                }
            ).build();
    }
}
