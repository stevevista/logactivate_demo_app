package top.deepzone.rjzhou.logact.model;


import top.deepzone.rjzhou.logact.Connection;

public class ConnectionModel {

    private static String clientId = "LogActivate";
    private static String httpUri = "http://10.0.2.2:3001";
    private static String mqttUri = "tcp://10.0.2.2:1883";
    private static boolean cleanSession = true;
    private static String username = "user";
    private static String password = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InVzZXIiLCJpYXQiOjE1NDE2MTU0OTAsImV4cCI6MTU3MzE3MzA5MH0.KgpsWKVlAq1Y2F3WYRZRN6I77tmzaraGRHeWnapdsuI";

    private static int timeout = 80;
    private static int keepAlive = 200;
    private static String lwtTopic = "";
    private static String lwtMessage = "";
    private static int lwtQos = 0;
    private static boolean lwtRetain =  false;

    /** Initialise the ConnectionModel with an existing connection **/
    public static void importConnection(Connection connection){
        clientId = connection.getId();
        mqttUri = connection.getUri();
        cleanSession = connection.getConnectionOptions().isCleanSession();

        if(connection.getConnectionOptions().getUserName() == null){
            username = "";
        }else {
            username = connection.getConnectionOptions().getUserName();
        }
        if(connection.getConnectionOptions().getPassword() != null) {
            password = new String(connection.getConnectionOptions().getPassword());
        } else {
            password = "";
        }

        timeout = connection.getConnectionOptions().getConnectionTimeout();
        keepAlive = connection.getConnectionOptions().getKeepAliveInterval();

        if(connection.getConnectionOptions().getWillDestination() == null){
            lwtTopic = "";
        } else {
            lwtTopic = connection.getConnectionOptions().getWillDestination();
        }
        if(connection.getConnectionOptions().getWillMessage() != null) {
            lwtMessage = new String(connection.getConnectionOptions().getWillMessage().getPayload());
            lwtQos = connection.getConnectionOptions().getWillMessage().getQos();
            lwtRetain = connection.getConnectionOptions().getWillMessage().isRetained();
        } else {
            lwtMessage = "";
            lwtQos = 0;
            lwtRetain = false;
        }

    }

    public static String getHttpUri() {
        return httpUri;
    }

    public static void setHttpUri(String hostname) {
        httpUri = hostname;
    }

    public static String getClientId() {
        return clientId;
    }

    public static void setClientId(String id) {
        clientId = id;
    }

    public static String getMqttUri() {
        return mqttUri;
    }

    public static  void setMqttUri(String hostName) {
        mqttUri = hostName;
    }

    public static boolean isCleanSession() {
        return cleanSession;
    }

    public static void setCleanSession(boolean clean) {
        cleanSession = clean;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String name) {
        username = name;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String pass) {
        password = pass;
    }

    public static int getTimeout() {
        return timeout;
    }

    public static void setTimeout(int time) {
        timeout = time;
    }

    public static int getKeepAlive() {
        return keepAlive;
    }

    public static void setKeepAlive(int keep) {
        keepAlive = keep;
    }

    public static String getLwtTopic() {
        return lwtTopic;
    }

    public static void setLwtTopic(String topic) {
        lwtTopic = topic;
    }

    public static String getLwtMessage() {
        return lwtMessage;
    }

    public static void setLwtMessage(String message) {
        lwtMessage = message;
    }

    public static int getLwtQos() {
        return lwtQos;
    }

    public static void setLwtQos(int qos) {
        lwtQos = qos;
    }

    public static boolean isLwtRetain() {
        return lwtRetain;
    }

    public static void setLwtRetain(boolean retain) {
        lwtRetain = retain;
    }
}
