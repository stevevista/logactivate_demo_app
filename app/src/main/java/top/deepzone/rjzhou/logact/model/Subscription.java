package top.deepzone.rjzhou.logact.model;


public class Subscription {

    private String topic;
    private int qos;
    private String lastMessage;
    private boolean enableNotifications;

    public Subscription(String topic, int qos, boolean enableNotifications){
        this.topic = topic;
        this.qos = qos;
        this.enableNotifications = enableNotifications;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "topic='" + topic + '\'' +
                ", qos=" + qos +
                ", enableNotifications='" + enableNotifications + '\''+
                '}';
    }
}
