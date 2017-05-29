package org.aioba.akka.learn;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class PrintMyActorRefActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals("printit",
                p -> {
                    ActorRef secondRef = getContext().actorOf(Props.empty(), "second-actor");
                    System.out.println("Second: " + secondRef);
                }
            ).build();
    }

}