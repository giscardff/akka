package org.aioba.akka.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.JavaTestKit;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DeviceGroupTest {

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
    public void testRegisterDeviceActor() {

        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = probe.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = probe.lastSender();
        assertNotEquals(deviceActor1, deviceActor2);

        deviceActor1.tell(new Device.RecordTemperature(0L, 1.0), probe.testActor());
        assertEquals(0L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
        deviceActor1.tell(new Device.RecordTemperature(1L, 2.0), probe.testActor());
        assertEquals(1L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);

    }

    @Test
    public void testIgnoreRequestsFromWrongGroupId(){
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device1"), probe.testActor());
        probe.expectNoMsg();

    }

    @Test
    public void testReturnSameActorForSameDeviceId(){

        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = probe.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = probe.lastSender();

        assertEquals(deviceActor1, deviceActor2);

    }

    @Test
    public void testListActiveDevices(){

        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceGroup.RequestDeviceList(0L), probe.testActor());
        DeviceGroup.ReplyDeviceList reply = probe.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);

    }

    @Test
    public void testListActiveDevicesAfterOneShutdown(){
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef toShutDown = probe.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);

        groupActor.tell(new DeviceGroup.RequestDeviceList(0L), probe.testActor());
        DeviceGroup.ReplyDeviceList reply = probe.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);

        probe.watch(toShutDown);
        toShutDown.tell(PoisonPill.getInstance(), ActorRef.noSender());
        probe.expectTerminated(toShutDown, Duration.create("3600 seconds"));

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        groupActor.tell(new DeviceGroup.RequestDeviceList(1L), probe.testActor());
        DeviceGroup.ReplyDeviceList r = probe.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(1L, r.requestId);
        assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.ids);

    }

    @Test public void testCollectTemperaturesFromAllActiveDevices(){

        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = probe.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device2"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = probe.lastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device3"), probe.testActor());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor3 = probe.lastSender();

        // Check that the devices actors are working
        deviceActor1.tell(new Device.RecordTemperature(0L, 1.0), probe.testActor());
        assertEquals(0L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
        deviceActor2.tell(new Device.RecordTemperature(1L, 2.0), probe.testActor());
        assertEquals(1L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);
        // No temperature for device 3

        groupActor.tell(new DeviceGroup.RequestAllTemperatures(0L), probe.testActor());
        DeviceGroup.RespondAllTemperatures response = probe.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertEquals(0L, response.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));
        expectedTemperatures.put("device3", new DeviceGroup.TemperatureNotAvailable());

        assertTrue(assertTemperatures(expectedTemperatures, response.temperatures));

    }

    private boolean assertTemperatures(Map<String, DeviceGroup.TemperatureReading> t1, Map<String, DeviceGroup.TemperatureReading> t2){

        if(t1.size() != t2.size())return false;

        for(String deviceId : t1.keySet()) {

            if (t2.containsKey(deviceId) == false)return false;

            DeviceGroup.TemperatureReading tr1 = t1.get(deviceId);
            DeviceGroup.TemperatureReading tr2 = t2.get(deviceId);

            if(tr1.getClass() != tr2.getClass())return false;

            if(tr1 instanceof DeviceGroup.Temperature && tr2 instanceof DeviceGroup.Temperature){
                if(((DeviceGroup.Temperature)tr1).value != ((DeviceGroup.Temperature)tr2).value)return false;
            }

        }

        return true;

    }

}
