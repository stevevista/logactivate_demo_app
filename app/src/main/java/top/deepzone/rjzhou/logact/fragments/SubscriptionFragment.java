package top.deepzone.rjzhou.logact.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import top.deepzone.rjzhou.logact.Connection;
import top.deepzone.rjzhou.logact.R;
import top.deepzone.rjzhou.logact.model.Subscription;

import java.util.ArrayList;

public class SubscriptionFragment extends Fragment {

    private ArrayList<Subscription> subscriptions;

    public SubscriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscriptions = Connection.getInstance(getActivity()).getSubscriptions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        ListView subscriptionListView = rootView.findViewById(R.id.subscription_list_view);
        SubscriptionListItemAdapter adapter = new SubscriptionListItemAdapter(this.getActivity(), subscriptions);

        subscriptionListView.setAdapter(adapter);
        return rootView;
    }

    private class SubscriptionListItemAdapter extends ArrayAdapter<Subscription> {

        private final Context context;
        private final ArrayList<Subscription> topics;

        public SubscriptionListItemAdapter(Context context, ArrayList<Subscription> topics){
            super(context, R.layout.subscription_list_item, topics);
            this.context = context;
            this.topics = topics;

        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.subscription_list_item, parent, false);
            TextView topicTextView = (TextView) rowView.findViewById(R.id.message_text);
            TextView  qosTextView = (TextView) rowView.findViewById(R.id.qos_label);
            topicTextView.setText(topics.get(position).getTopic());
            String qosString = context.getString(R.string.qos_text, topics.get(position).getQos());
            qosTextView.setText(qosString);
            TextView notifyTextView = (TextView) rowView.findViewById(R.id.show_notifications_label);
            String notifyString = context.getString(R.string.notify_text, (topics.get(position).isEnableNotifications() ? context.getString(R.string.enabled) : context.getString(R.string.disabled)));
            notifyTextView.setText(notifyString);

            return rowView;
        }
    }
}