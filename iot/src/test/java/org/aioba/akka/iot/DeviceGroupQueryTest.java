package org.aioba.akka.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviceGroupQueryTest {

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
    public void testReturnTemperatureValueForWorkingDevices(){

        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.testActor(), "device1");
        actorToDeviceId.put(device2.testActor(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 1L, requester.testActor(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(0L, device1.expectMsgClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMsgClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.testActor());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.testActor());

        DeviceGroup.RespondAllTemperatures response = requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertEquals(1L, response.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));
        assertTrue(assertTemperatures(expectedTemperatures, response.temperatures));

    }

    @Test
    public void testReturnTemperatureNotAvailableForDevicesWithNoReadings(){

        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.testActor(), "device1");
        actorToDeviceId.put(device2.testActor(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 1L, requester.testActor(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(0L, device1.expectMsgClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMsgClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(new Device.RespondTemperature(0L, Optional.empty()), device1.testActor());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.testActor());

        DeviceGroup.RespondAllTemperatures response = requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertEquals(1L, response.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.TemperatureNotAvailable());
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));
        assertTrue(assertTemperatures(expectedTemperatures, response.temperatures));

    }

    @Test
    public void testReturnDeviceNotAvailableIfDeviceStopsBeforeAnswering(){

        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.testActor(), "device1");
        actorToDeviceId.put(device2.testActor(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 1L, requester.testActor(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(0L, device1.expectMsgClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMsgClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.testActor());
        device2.testActor().tell(PoisonPill.getInstance(), ActorRef.noSender());

        DeviceGroup.RespondAllTemperatures response = requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertEquals(1L, response.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.DeviceNotAvailable());
        assertTrue(assertTemperatures(expectedTemperatures, response.temperatures));

    }

    @Test
    public void testReturnTemperatureReadingEventIfDeviceStopsAfterAnswering(){

        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.testActor(), "device1");
        actorToDeviceId.put(device2.testActor(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 1L, requester.testActor(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(0L, device1.expectMsgClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMsgClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.testActor());
        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(2.0)), device2.testActor());
        device2.testActor().tell(PoisonPill.getInstance(), ActorRef.noSender());

        DeviceGroup.RespondAllTemperatures response = requester.expectMsgClass(DeviceGroup.RespondAllTemperatures.class);
        assertEquals(1L, response.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.Temperature(2.0));
        assertTrue(assertTemperatures(expectedTemperatures, response.temperatures));

    }

    @Test
    public void testReturnDeviceTimedOutIfDeviceDoesNotAnswerInTime(){

        TestKit requester = new TestKit(system);
        TestKit device1 = new TestKit(system);
        TestKit device2 = new TestKit(system);

        Map<ActorRef, String> actorToDeviceId = new HashMap<>();
        actorToDeviceId.put(device1.testActor(), "device1");
        actorToDeviceId.put(device2.testActor(), "device2");

        ActorRef queryActor = system.actorOf(DeviceGroupQuery.props(actorToDeviceId, 1L, requester.testActor(), new FiniteDuration(3, TimeUnit.SECONDS)));

        assertEquals(0L, device1.expectMsgClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMsgClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(new Device.RespondTemperature(0L, Optional.of(1.0)), device1.testActor());

        DeviceGroup.RespondAllTemperatures response = requester.expectMsgClass(FiniteDuration.create(5, TimeUnit.SECONDS), DeviceGroup.RespondAllTemperatures.class);
        assertEquals(1L, response.requestId);

        Map<String, DeviceGroup.TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new DeviceGroup.Temperature(1.0));
        expectedTemperatures.put("device2", new DeviceGroup.DeviceTimedOut());
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
