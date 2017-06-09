package org.aioba.akka.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class DeviceManagerTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup(){
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown(){
        TestKit.shutdownActorSystem(system, Duration.create("3600 seconds") , true);
        system = null;
    }

    @Test
    public void testRegisterDeviceGroup(){

        TestKit probe = new TestKit(system);

        ActorRef deviceManager = system.actorOf(DeviceManager.props());
        deviceManager.tell(new DeviceManager.RequestTrackDevice("group1", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

    }

    @Test
    public void testActiveGroups(){

        TestKit probe = new TestKit(system);

        ActorRef deviceManager = system.actorOf(DeviceManager.props());
        deviceManager.tell(new DeviceManager.RequestTrackDevice("group1", "device11"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        deviceManager.tell(new DeviceManager.RequestTrackDevice("group1", "device12"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        deviceManager.tell(new DeviceManager.RequestTrackDevice("group2", "device21"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        deviceManager.tell(new DeviceManager.RequestTrackDevice("group3", "device31"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        deviceManager.tell(new DeviceManager.RequestTrackDevice("group3", "device32"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        deviceManager.tell(new DeviceManager.RequestTrackDevice("group3", "device33"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        deviceManager.tell(new DeviceManager.RequestDeviceGroupList(1L), probe.testActor());
        DeviceManager.ReplyDeviceGroupList r = probe.expectMsgClass(DeviceManager.ReplyDeviceGroupList.class);

        assertEquals(1L, r.requestId);
        assertEquals(Stream.of("group1", "group2", "group3").collect(Collectors.toSet()), r.ids);

    }

}
