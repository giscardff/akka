package org.aioba.akka.learn;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class SupervisingActor extends AbstractActor {

    ActorRef child = getContext().actorOf(Props.create(SupervisedActor.class), "supervised-actor");

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals("failChild",
                f -> {
                    child.tell("fail", getSelf());
                }
            ).build();
    }
}
