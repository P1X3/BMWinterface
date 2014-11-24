package com.osovskiy.bmwired;

import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.osovskiy.bmwired.fragment.plugins.PluginsFragment;
import com.osovskiy.bmwired.fragment.preferences.PreferencesFragment;
import com.osovskiy.bmwired.fragment.setup.SetupFragment;

public class MainActivity extends ActionBarActivity
{
  private static final String TAG = MainActivity.class.getSimpleName();

  private String[] fragmentNames = new String[3];
  private Fragment[] fragments = new Fragment[3];
  private DrawerLayout drawerLayout;
  private ListView listView;

  public MainActivity()
  {
    fragmentNames[0] = "Plugins";
    fragments[0] = new PluginsFragment();
    fragmentNames[1] = "Preferences";
    fragments[1] = new PreferencesFragment();
    fragmentNames[2] = "Setup";
    fragments[2] = new SetupFragment();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    Intent intent = new Intent(this, BMWiService.class);
    startService(intent);

    String action = getIntent().getAction();
    if ( action != null && action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) )
    {
      Log.d(TAG, "USB_DEVICE_ATTACHED intent received");
      Intent service = new Intent(getApplicationContext(), BMWiService.class);
      service.setAction(BMWiService.EVENT_USB_DEVICE_ATTACHED);
      startService(service);
      finish();
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    listView = (ListView) findViewById(R.id.drawer_list);
    listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, fragmentNames));
    listView.setOnItemClickListener(new DrawerItemClickListener());

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    if (findViewById(R.id.fragment_container) != null)
    {
      getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SetupFragment()).commit();
    }
  }

  private class DrawerItemClickListener implements ListView.OnItemClickListener
  {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
      listView.setItemChecked(position, true);

      getSupportActionBar().setTitle(fragmentNames[position]);

      getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragments[position]).commit();
      drawerLayout.closeDrawer(listView);
    }
  }
}
