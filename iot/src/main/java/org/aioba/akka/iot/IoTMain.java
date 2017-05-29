package org.aioba.akka.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

public class IoTMain {

    public static void main(String[] args) throws IOException {

        ActorSystem system = ActorSystem.create("iot-system");

        try{
            ActorRef supervisor = system.actorOf(IoTSupervisor.props(), "iot-supervisor");
            System.out.println("Press <ENTER> to exist the system");
            System.in.read();
        } finally {
            system.terminate();
        }


    }

}
