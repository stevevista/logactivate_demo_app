package top.deepzone.rjzhou.logact;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import top.deepzone.rjzhou.logact.model.ConnectionModel;
import top.deepzone.rjzhou.logact.model.ReceivedMessage;
import top.deepzone.rjzhou.logact.model.Subscription;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Represents a {@link MqttAndroidClient} and the actions it has performed
 *
 */
public class Connection {
    private static final String TAG = "Connection";

    public static final String ConnectionStatusProperty = "connectionStatus";

    /** The clientId of the client associated with this <code>Connection</code> object **/
    private String clientId;

    /** The host that the {@link MqttAndroidClient} represented by this <code>Connection</code> is represented by **/
    private String uri;

    /** {@link ConnectionStatus } of the {@link MqttAndroidClient} represented by this <code>Connection</code> object. Default value is {@link ConnectionStatus#NONE} **/
    private ConnectionStatus status = ConnectionStatus.NONE;

    /** The {@link MqttAndroidClient} instance this class represents **/
    private MqttAndroidClient client;

    /** Collection of {@link java.beans.PropertyChangeListener} **/
    private final ArrayList<PropertyChangeListener> listeners = new ArrayList<>();

    /** The {@link Context} of the application this object is part of**/
    private Context context;

    /** The {@link MqttConnectOptions} that were used to connect this client **/
    private MqttConnectOptions mqttConnectOptions;

    /**
     * Connections status for  a connection
     */
    public enum ConnectionStatus {

        /** Client is Connecting **/
        CONNECTING,
        /** Client is Connected **/
        CONNECTED,
        /** Client is Disconnecting **/
        DISCONNECTING,
        /** Client is Disconnected **/
        DISCONNECTED,
        /** Client has encountered an Error **/
        ERROR,
        /** Status is unknown **/
        NONE
    }

    /** The list of this connection's subscriptions **/
    private final Map<String, Subscription> subscriptions = new HashMap<>();

    private final ArrayList<ReceivedMessage> messageHistory =  new ArrayList<>();

    private final ArrayList<IReceivedMessageListener> receivedMessageListeners = new ArrayList<>();

    private static Connection instance = null;

    private Connection(Context context){
        this.context = context;
        updateConnection(ConnectionModel.getClientId(), ConnectionModel.getMqttUri());

        addConnectionOptionsByModel();
        addNewSubscription(new Subscription("/presence", 0, true));
    }

    public synchronized static Connection getInstance(Context context){
        if(instance ==  null){
            instance = new Connection(context);
        }
        return instance;
    }


    public void updateConnection(String clientId, String uri){
        this.clientId = clientId;
        this.uri = uri;
        final MqttAndroidClient newClient = new MqttAndroidClient(context, uri, clientId);
        newClient.setCallback(new MqttCallbackHandler(context));

        // disconnect old one
        if (isConnected()) {
            try {
                client.disconnect(null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
                        Connection.this.client = newClient;
                        Connection.this.connect();
                    }

                    @Override
                    public void onFailure(IMqttToken token, Throwable exception) {
                        changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
                        Connection.this.client = newClient;
                        Connection.this.connect();
                    }
                });
            } catch( MqttException ex){
                Log.e(TAG, "Exception occurred during disconnect: " + ex.getMessage());
            }
        } else {
            this.client = newClient;
        }
    }

    public boolean isConnected() {
        return status == ConnectionStatus.CONNECTED;
    }

    /**
     * Register a {@link PropertyChangeListener} to this object
     * @param listener the listener to register
     */
    public void registerChangeListener(PropertyChangeListener listener)
    {
        listeners.add(listener);
    }

    public void unregisterChangeListener(PropertyChangeListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Changes the connection status of the client
     * @param connectionStatus The connection status of this connection
     */
    public void changeConnectionStatus(ConnectionStatus connectionStatus) {
        status = connectionStatus;
        notifyListeners((new PropertyChangeEvent(this, ConnectionStatusProperty, null, null)));
    }

    /**
     * A string representing the state of the client this connection
     * object represents
     *
     *
     * @return A string representing the state of the client
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(clientId);
        sb.append("\n ");

        switch (status) {

            case CONNECTED :
                sb.append(context.getString(R.string.connection_connected_to));
                break;
            case DISCONNECTED :
                sb.append(context.getString(R.string.connection_disconnected_from));
                break;
            case NONE :
                sb.append(context.getString(R.string.connection_unknown_status));
                break;
            case CONNECTING :
                sb.append(context.getString(R.string.connection_connecting_to));
                break;
            case DISCONNECTING :
                sb.append(context.getString(R.string.connection_disconnecting_from));
                break;
            case ERROR :
                sb.append(context.getString(R.string.connection_error_connecting_to));
        }
        sb.append(" ");
        sb.append(uri);

        return sb.toString();
    }

    /**
     * Get the client Id for the client this object represents
     * @return the client id for the client this object represents
     */
    public String getId() {
        return clientId;
    }

    /**
     * Get the host name of the server that this connection object is associated with
     * @return the host name of the server this connection object is associated with
     */
    public String getUri() {

        return uri;
    }

    /**
     * Add the connectOptions used to connect the client to the server
     * @param connectOptions the connectOptions used to connect to the server
     */
    public void addConnectionOptions(MqttConnectOptions connectOptions) {
        mqttConnectOptions = connectOptions;
    }

    public void addConnectionOptionsByModel() {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(ConnectionModel.isCleanSession());
        connOpts.setConnectionTimeout(ConnectionModel.getTimeout());
        connOpts.setKeepAliveInterval(ConnectionModel.getKeepAlive());
        if(!ConnectionModel.getUsername().equals("")){
            connOpts.setUserName(ConnectionModel.getUsername());
        }

        if(!ConnectionModel.getPassword().equals("")){
            connOpts.setPassword(ConnectionModel.getPassword().toCharArray());
        }
        if(!ConnectionModel.getLwtTopic().equals("") && !ConnectionModel.getLwtMessage().equals("")){
            connOpts.setWill(ConnectionModel.getLwtTopic(), ConnectionModel.getLwtMessage().getBytes(), ConnectionModel.getLwtQos(), ConnectionModel.isLwtRetain());
        }

        addConnectionOptions(connOpts);
    }
    /**
     * Get the connectOptions used to connect this client to the server
     * @return The connectOptions used to connect the client to the server
     */
    public MqttConnectOptions getConnectionOptions()
    {
        return mqttConnectOptions;
    }

    /**
     * Notify {@link PropertyChangeListener} objects that the object has been updated
     * @param propertyChangeEvent - The property Change event
     */
    private void notifyListeners(PropertyChangeEvent propertyChangeEvent)
    {
        for (PropertyChangeListener listener : listeners)
        {
            listener.propertyChange(propertyChangeEvent);
        }
    }

    public ArrayList<Subscription> getSubscriptions(){
        ArrayList<Subscription> subs = new ArrayList<>(subscriptions.values());
        return subs;
    }

    public void addReceivedMessageListener(IReceivedMessageListener listener){
        receivedMessageListeners.add(listener);
    }

    public void removeReceivedMessageListener(IReceivedMessageListener listener){
        receivedMessageListeners.remove(listener);
    }

    public void messageArrived(String topic, MqttMessage message){
        ReceivedMessage msg = new ReceivedMessage(topic, message);
        messageHistory.add(0, msg);
        if(subscriptions.containsKey(topic)){
            subscriptions.get(topic).setLastMessage(new String(message.getPayload()));
            if(subscriptions.get(topic).isEnableNotifications()){
                //create intent to start activity
                Intent intent = new Intent();
                intent.setClass(context, MainActivity.class);

                //format string args
                Object[] notifyArgs = new String[3];
                notifyArgs[0] = new String(message.getPayload());
                notifyArgs[1] = topic;

                //notify the user
                Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

            }
        }

        for(IReceivedMessageListener listener : receivedMessageListeners){
            listener.onMessageReceived(msg);
        }
    }

    public ArrayList<ReceivedMessage> getMessages(){
        return messageHistory;
    }

    public void disconnect(){
        if (!isConnected()) {
            return;
        }

        changeConnectionStatus(ConnectionStatus.DISCONNECTING);
        try {
            client.disconnect();
        } catch( MqttException ex){
            Log.e(TAG, "Exception occurred during disconnect: " + ex.getMessage());
        }
    }

    public void connect() {
        if (isConnected()) {
            return;
        }

        changeConnectionStatus(ConnectionStatus.CONNECTING);
        try {
            client.connect(getConnectionOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    changeConnectionStatus(ConnectionStatus.CONNECTED);
                    Log.i(TAG, "Connected.");
                    for (Subscription sub : subscriptions.values()) {
                        Log.i(TAG, "Auto-subscribing to: " + sub.getTopic() + "@ QoS: " + sub.getQos());
                        subscribe(sub.getTopic(), sub.getQos());
                    }
                }

                @Override
                public void onFailure(IMqttToken token, Throwable exception) {
                    changeConnectionStatus(Connection.ConnectionStatus.ERROR);
                    System.out.println("Client failed to connect");
                }
            });
        }
        catch (MqttException e) {
            Log.e(TAG, "MqttException occurred", e);
        }
    }

    public void publish(String topic, String message, int qos, boolean retain){
        if (!isConnected()) {
            return;
        }

        try {
            final String[] actionArgs = new String[2];
            actionArgs[0] = message;
            actionArgs[1] = topic;
            client.publish(topic, message.getBytes(), qos, retain, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    String actionTaken = context.getString(R.string.toast_pub_success,
                            (Object[]) actionArgs);
                    Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
                    System.out.print("Published");
                }

                @Override
                public void onFailure(IMqttToken token, Throwable exception) {
                    String action = context.getString(R.string.toast_pub_failed,
                            (Object[]) actionArgs);
                    Notify.toast(context, action, Toast.LENGTH_SHORT);
                    System.out.print("Publish failed");
                }
            });
        } catch( MqttException ex){
            Log.e(TAG, "Exception occurred during publish: " + ex.getMessage());
        }
    }

    public void subscribe(String topic, int qos) {
        if (!isConnected()) {
            return;
        }

        final String[] actionArgs = new String[1];
        actionArgs[0] = topic;

        try {
            client.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    String actionTaken = context.getString(R.string.toast_sub_success,
                            (Object[]) actionArgs);
                    Notify.toast(context, actionTaken, Toast.LENGTH_SHORT);
                    System.out.print(actionTaken);
                }

                @Override
                public void onFailure(IMqttToken token, Throwable exception) {
                    String action = context.getString(R.string.toast_sub_failed,
                            (Object[]) actionArgs);
                    Notify.toast(context, action, Toast.LENGTH_SHORT);
                    System.out.print(action);
                }
            });
        } catch (MqttException ex) {
            Log.e(TAG, "Exception occurred during subscribe: " + ex.getMessage());
        }
    }

    public void addNewSubscription(Subscription subscription) {
        if(!subscriptions.containsKey(subscription.getTopic())){
            subscribe(subscription.getTopic(), subscription.getQos());
            subscriptions.put(subscription.getTopic(), subscription);
        }
    }

    public void unsubscribe (String topic) {
        if(subscriptions.containsKey(topic)){
            try {
                client.unsubscribe(topic);
            } catch (MqttException e) {
                Log.e(TAG, "Exception occurred during unsubscribe: " + e.getMessage());
            }
            subscriptions.remove(topic);
        }
    }
}
