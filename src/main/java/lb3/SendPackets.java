package lb3;

import MQTT.MqttToolkit;
import ReadCom.SerialPortToolKit;

import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SendPackets {
    public static void main(String[] args) throws Exception {
        AtomicReference<Double> value = new AtomicReference<>((double) 0);
        DecimalFormat df = new DecimalFormat("#.#");
        String broker = "tcp://25.48.240.153:1883";
        String topic = "mqtt/test";
        String username = "user";
        String password = "user";
        String clientID = "PIDor";
        String topicWill = "mqtt/will";
        SerialPortToolKit serialPort = new SerialPortToolKit();
        SerialPortToolKit serialPort1= new SerialPortToolKit();
        int qos = 0;
        MqttToolkit mqtt = new MqttToolkit();
        ScheduledExecutorService sendToTopicThread = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService readPortThread = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService sendToTX = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService anotherThread = Executors.newSingleThreadScheduledExecutor();
        mqtt.createBroker(broker, topic, username, password, clientID, qos);
        //mqtt.setWill(topicWill, "i died");
        mqtt.connectToBroker();
        mqtt.setupAndSubscribeOnTopic();
        readPortThread.scheduleAtFixedRate(()-> {
            try {
                value.getAndSet((double) (serialPort.readPort()));
               System.out.println(value.get());
            } catch (Exception e) {
                //System.out.println("Can't read port");
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

        sendToTopicThread.scheduleAtFixedRate(()->
                mqtt.sendMessageToTopic(String.valueOf(Math.round((3.3/1024)*value.get()* 10.0) / 10.0)),
                10000, 50, TimeUnit.MILLISECONDS);
        sendToTX.scheduleAtFixedRate(()->{
        try {
            serialPort1.sendBytes(mqtt.getMessageFromTopic());
        } catch (Exception e) {
        }
        }, 10000, 50, TimeUnit.MILLISECONDS);
    }

}
