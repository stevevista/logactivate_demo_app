package top.deepzone.rjzhou.logact.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import top.deepzone.rjzhou.logact.Connection;
import top.deepzone.rjzhou.logact.IReceivedMessageListener;
import top.deepzone.rjzhou.logact.R;
import top.deepzone.rjzhou.logact.model.ReceivedMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private MessageListItemAdapter messageListAdapter;
    private ArrayList<ReceivedMessage> messages;

    private IReceivedMessageListener recvMsgListener = new IReceivedMessageListener() {
        @Override
        public void onMessageReceived(ReceivedMessage message) {
            System.out.println("GOT A MESSAGE in history " + new String(message.getMessage().getPayload()));
            System.out.println("M: " + messages.size());
            messageListAdapter.notifyDataSetChanged();
        }
    };

    public HistoryFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Connection connection = Connection.getInstance(getActivity());
        setHasOptionsMenu(true);
        messages = connection.getMessages();
        connection.addReceivedMessageListener(recvMsgListener);
    }

    @Override
    public void onDestroy() {
        Connection.getInstance(getActivity()).removeReceivedMessageListener(recvMsgListener);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_connection_history, container, false);

        messageListAdapter = new MessageListItemAdapter(getActivity(), messages);
        ListView messageHistoryListView = rootView.findViewById(R.id.history_list_view);
        messageHistoryListView.setAdapter(messageListAdapter);

        Button clearButton = rootView.findViewById(R.id.history_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.clear();
                messageListAdapter.notifyDataSetChanged();
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    private class MessageListItemAdapter extends ArrayAdapter<ReceivedMessage> {

        private final Context context;
        private final ArrayList<ReceivedMessage> messages;

        public MessageListItemAdapter(Context context, ArrayList<ReceivedMessage> messages){
            super(context, R.layout.message_list_item, messages);
            this.context = context;
            this.messages = messages;

        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.message_list_item, parent, false);
            TextView topicTextView = rowView.findViewById(R.id.message_topic_text);
            TextView messageTextView = rowView.findViewById(R.id.message_text);
            TextView dateTextView = rowView.findViewById(R.id.message_date_text);
            messageTextView.setText(new String(messages.get(position).getMessage().getPayload()));
            topicTextView.setText(context.getString(R.string.topic_fmt, messages.get(position).getTopic()));
            DateFormat dateTimeFormatter = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            String shortDateStamp = dateTimeFormatter.format(messages.get(position).getTimestamp());
            dateTextView.setText(context.getString(R.string.message_time_fmt, shortDateStamp));
            return rowView;
        }
    }

}

