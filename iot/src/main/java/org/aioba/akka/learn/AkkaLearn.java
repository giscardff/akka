package org.aioba.akka.learn;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class AkkaLearn {

    public static void main(String[] args){

//        {
//            final ActorSystem system = ActorSystem.create("learn");
//            ActorRef firstRef = system.actorOf(Props.create(PrintMyActorRefActor.class), "first-actor");
//            System.out.println("First: " + firstRef);
//            firstRef.tell("printit", ActorRef.noSender());
//        }
//
//        {
//            final ActorSystem system = ActorSystem.create("learn");
//            ActorRef first = system.actorOf(Props.create(StartStopActor1.class), "first");
//            first.tell("stop", ActorRef.noSender());
//        }

        {
            final ActorSystem system = ActorSystem.create("learn");
            ActorRef supervisingActor = system.actorOf(Props.create(SupervisingActor.class), "supervising-actor");
            supervisingActor.tell("failChild", ActorRef.noSender());
        }



    }

}
