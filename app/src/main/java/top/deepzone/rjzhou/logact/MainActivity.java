package top.deepzone.rjzhou.logact;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import top.deepzone.rjzhou.logact.fragments.ConnectionFragment;
import top.deepzone.rjzhou.logact.fragments.EditConnectionFragment;
import top.deepzone.rjzhou.logact.fragments.LogFragment;
import top.deepzone.rjzhou.logact.model.ConnectionModel;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FragmentDrawer drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        displayFragment(new ConnectionFragment(), "Connection");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onAddConnectionSelected() {
        displayFragment(new EditConnectionFragment(), "Configure");
    }

    @Override
    public void onConnectionSelected() {
        displayFragment(new ConnectionFragment(), "Connection");
    }

    @Override
    public void onLogActSelected() {
        displayFragment(new LogFragment(), "LogAct");
    }

    private void displayFragment(Fragment fragment, String title){
        if (fragment != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // Set Toolbar Title
            getSupportActionBar().setTitle(title);
        }
    }

    public void updateAndConnect(){
        Connection connection = Connection.getInstance(this);

        connection.addConnectionOptionsByModel();
        connection.updateConnection(ConnectionModel.getClientId(), ConnectionModel.getMqttUri());

        displayFragment(new ConnectionFragment(), "Connection");
    }
}
