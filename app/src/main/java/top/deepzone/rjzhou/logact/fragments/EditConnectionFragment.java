package top.deepzone.rjzhou.logact.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import top.deepzone.rjzhou.logact.Connection;
import top.deepzone.rjzhou.logact.MainActivity;
import top.deepzone.rjzhou.logact.R;
import top.deepzone.rjzhou.logact.model.ConnectionModel;

public class EditConnectionFragment extends Fragment {

    private EditText httpHostname;
    private EditText clientId;
    private EditText serverHostname;
    private Switch cleanSession;
    private EditText username;
    private EditText password;
    private EditText timeout;
    private EditText keepAlive;
    private EditText lwtTopic;
    private EditText lwtMessage;
    private Spinner lwtQos;
    private Switch lwtRetain;

    public EditConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_connection, container, false);
        httpHostname = rootView.findViewById(R.id.http_host);
        clientId = rootView.findViewById(R.id.client_id);
        serverHostname = rootView.findViewById(R.id.mqtt_host);
        cleanSession = rootView.findViewById(R.id.clean_session_switch);
        username = rootView.findViewById(R.id.username);
        password = rootView.findViewById(R.id.password);
        timeout = rootView.findViewById(R.id.timeout);
        keepAlive = rootView.findViewById(R.id.keepalive);
        lwtTopic = rootView.findViewById(R.id.lwt_topic);
        lwtMessage = rootView.findViewById(R.id.lwt_message);
        lwtQos = rootView.findViewById(R.id.lwt_qos_spinner);
        lwtRetain = rootView.findViewById(R.id.retain_switch);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.qos_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lwtQos.setAdapter(adapter);

        Connection connection = Connection.getInstance(getActivity());
        if (connection != null) {
            System.out.println("Editing an existing connection");
            ConnectionModel.importConnection(connection);
            populateFromConnectionModel();
        } else {
            populateFromConnectionModel();

        }

        setFormItemListeners();
        // Inflate the layout for this fragment
        return rootView;
    }

    private void setFormItemListeners(){
        httpHostname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ConnectionModel.setHttpUri(s.toString());
            }
        });

       clientId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ConnectionModel.setClientId(s.toString());
            }
        });

        serverHostname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ConnectionModel.setMqttUri(s.toString());
            }
        });

        cleanSession.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ConnectionModel.setCleanSession(isChecked);
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().equals("")) {
                    ConnectionModel.setUsername(s.toString());
                } else {
                    ConnectionModel.setUsername("");
                }

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().equals("")) {
                    ConnectionModel.setPassword(s.toString());
                } else {
                    ConnectionModel.setPassword("");
                }
            }
        });
        timeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    ConnectionModel.setTimeout(Integer.parseInt(s.toString()));
                }
            }
        });
        keepAlive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    ConnectionModel.setKeepAlive(Integer.parseInt(s.toString()));
                }
            }
        });
        lwtTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ConnectionModel.setLwtTopic(s.toString());
            }
        });
        lwtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ConnectionModel.setLwtMessage(s.toString());
            }
        });
        lwtQos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ConnectionModel.setLwtQos(Integer.parseInt(getResources().getStringArray(R.array.qos_options)[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lwtRetain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ConnectionModel.setLwtRetain(isChecked);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void populateFromConnectionModel() {
        httpHostname.setText(ConnectionModel.getHttpUri());
        clientId.setText(ConnectionModel.getClientId());
        serverHostname.setText(ConnectionModel.getMqttUri());
        cleanSession.setChecked(ConnectionModel.isCleanSession());
        username.setText(ConnectionModel.getUsername());
        password.setText(ConnectionModel.getPassword());
        timeout.setText(Integer.toString(ConnectionModel.getTimeout()));
        keepAlive.setText(Integer.toString(ConnectionModel.getKeepAlive()));
        lwtTopic.setText(ConnectionModel.getLwtTopic());
        lwtMessage.setText(ConnectionModel.getLwtMessage());
        lwtQos.setSelection(ConnectionModel.getLwtQos());
        lwtRetain.setChecked(ConnectionModel.isLwtRetain());
    }

    private void saveConnection(){
        ((MainActivity) getActivity()).updateAndConnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_edit_connection, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_connection) {
            saveConnection();
        }

        return super.onOptionsItemSelected(item);
    }

}