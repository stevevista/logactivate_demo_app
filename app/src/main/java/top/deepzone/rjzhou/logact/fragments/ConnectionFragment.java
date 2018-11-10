package top.deepzone.rjzhou.logact.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import top.deepzone.rjzhou.logact.Connection;
import top.deepzone.rjzhou.logact.R;

public class ConnectionFragment extends Fragment {
    private FragmentTabHost mTabHost;
    private Switch connectSwitch;

    private final ChangeListener changeListener = new ChangeListener();

    public ConnectionFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Connection.getInstance(getActivity()).registerChangeListener(changeListener);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        Connection.getInstance(getActivity()).unregisterChangeListener(changeListener);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);

        Bundle bundle = new Bundle();

        // Initialise the tab-host
        mTabHost = rootView.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
        // Add a tab to the tabHost
        mTabHost.addTab(mTabHost.newTabSpec("History").setIndicator("History"), HistoryFragment.class, bundle);
        mTabHost.addTab(mTabHost.newTabSpec("Publish").setIndicator("Publish"), PublishFragment.class, bundle);
        mTabHost.addTab(mTabHost.newTabSpec("Subscribe").setIndicator("Subscribe"), SubscriptionFragment.class, bundle);
        return rootView;

    }

    private void changeConnectedState(boolean state){
        if (connectSwitch.isChecked() != state) {
            mTabHost.getTabWidget().getChildTabViewAt(1).setEnabled(state);
            mTabHost.getTabWidget().getChildTabViewAt(2).setEnabled(state);
            connectSwitch.setChecked(state);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_connection, menu);

        connectSwitch = menu.findItem(R.id.connect_switch).getActionView().findViewById(R.id.switchForActionBar);

        connectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Connection.getInstance(getActivity()).connect();
                } else {
                    Connection.getInstance(getActivity()).disconnect();
                }
            }
        });

        changeConnectedState(Connection.getInstance(getActivity()).isConnected());
        super.onCreateOptionsMenu(menu, inflater);
    }

    private class ChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(Connection.ConnectionStatusProperty)) {
                return;
            }

            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConnectionFragment.this.changeConnectedState(Connection.getInstance(getActivity()).isConnected());
                }
            });
        }
    }
}
