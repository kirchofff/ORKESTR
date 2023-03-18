package MQTT;

import lombok.SneakyThrows;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MqttToolkit implements MqttCallback{
    private MqttConnectOptions options = new MqttConnectOptions();
    private MqttClient publisher;
    private String topic;
    private String username;
    private String password;
    private int qos;
    private byte[] messageArrived;
    private ScheduledExecutorService reconnectThread = Executors.newSingleThreadScheduledExecutor();
    @SneakyThrows
    public void createBroker(String broker, String topic, String username, String password, String clientID, int qos){
        this.topic = topic;
        this.username = username;
        this.password =password;
        this.qos = qos;
        this.publisher = new MqttClient(broker, clientID, new MemoryPersistence());
    }
    public void setWill (String willTopic,String willMessage){
        // Set will message
        options.setWill(willTopic, willMessage.getBytes(), 0, true);
    }
    @SneakyThrows
    public void connectToBroker(){
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        options.setCleanSession(true);
        publisher.setCallback(this);
        publisher.connect(options);
        System.out.println(publisher.getClientId() + " connected to broker");
    }
    @SneakyThrows
    public boolean setupAndSubscribeOnTopic(){
        if (publisher.isConnected()){
            publisher.subscribe(topic, qos);
        } else {return false;}
        return true;
    }
    @SneakyThrows
    public void disconnect(){
        publisher.disconnectForcibly();
    }
    @SneakyThrows
    public boolean sendMessageToTopic(String content){
        if (publisher.isConnected()){
            // Create message
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            // Send message
            publisher.publish(topic, message);
            //System.out.println("Message published" + " into the topic: " + topic + " with content: " + content);
            return true;
        } else {
            System.out.println("Not connected to broker");
            return false;
        }
    }
    public byte[] getMessageFromTopic(){
        return messageArrived;
    }

    @Override
    @SneakyThrows
    public void connectionLost(Throwable throwable) {
        // This method is called when the connection to the server is lost
        System.out.println("Connection lost because: " + throwable.getMessage());
        reconnect();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        // This method is called when a message arrives from the server
        System.out.println("Topic: " + topic + ", received message " +
                " with content: " + new String(mqttMessage.getPayload()));
        messageArrived = mqttMessage.getPayload();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // Called when delivery for a message has been completed, and all acknowledgments have been received
        //System.out.println("Delivery complete " + iMqttDeliveryToken.isComplete());
    }
    public void reconnect(){
        try {
            publisher.connect(options);
            publisher.subscribe(topic, qos);
            if (publisher.isConnected()){
                System.out.println("Successfully reconnected");
                reconnectThread.shutdown();
            }
        } catch (MqttException e) {
            System.out.println("Failed connection");
            reconnectThread.schedule(this::reconnect, 1000, TimeUnit.MILLISECONDS);
        }
    }
}
