package top.deepzone.rjzhou.logact;

import top.deepzone.rjzhou.logact.model.ReceivedMessage;

public interface IReceivedMessageListener {

    void onMessageReceived(ReceivedMessage message);
}