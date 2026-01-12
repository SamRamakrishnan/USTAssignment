package com.testproject.ustassignment;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private List<Device> deviceList = new ArrayList<>();
    private DeviceAdapter adapter;
    private RecyclerView recyclerView;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.devices_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(deviceList);
        recyclerView.setAdapter(adapter);

        db = AppDatabase.getDatabase(this);
        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        initializeDiscoveryListener();

        // Load devices from DB and mark as offline initially
        loadDevicesFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDiscovery();
    }

    private void loadDevicesFromDb() {
        new AsyncTask<Void, Void, List<DeviceEntity>>() {
            @Override
            protected List<DeviceEntity> doInBackground(Void... voids) {
                List<DeviceEntity> entities = db.deviceDao().getAllDevices();
                // Mark all as offline on load
                for (DeviceEntity entity : entities) {
                    entity.status = "Offline";
                    db.deviceDao().update(entity);
                }
                return entities;
            }

            @Override
            protected void onPostExecute(List<DeviceEntity> entities) {
                deviceList.clear();
                for (DeviceEntity entity : entities) {
                    deviceList.add(new Device(entity.name, entity.ip, entity.status));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service found: " + service.getServiceName());
                nsdManager.resolveService(service, new NsdManager.ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(TAG, "Resolve failed: " + errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        InetAddress host = serviceInfo.getHost();
                        if (host != null) {
                            String ip = host.getHostAddress();
                            String name = serviceInfo.getServiceName();
                            Device device = new Device(name, ip, "Online");
                            if (!deviceList.contains(device)) {
                                deviceList.add(device);
                                // Save to DB
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        db.deviceDao().insert(new DeviceEntity(name, ip, "Online"));
                                        return null;
                                    }
                                }.execute();
                            } else {
                                // Update status to online
                                for (Device d : deviceList) {
                                    if (d.getIp().equals(ip)) {
                                        d.setStatus("Online");
                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... voids) {
                                                db.deviceDao().updateStatus(ip, "Online");
                                                return null;
                                            }
                                        }.execute();
                                        break;
                                    }
                                }
                            }
                            runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }
                    }
                });
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.d(TAG, "Service lost: " + service.getServiceName());
                // Optionally update status to offline
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "Discovery stopped");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: " + errorCode);
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Discovery failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Stop discovery failed: " + errorCode);
            }
        };
    }

    private void startDiscovery() {
        nsdManager.discoverServices("_services._dns-sd._udp", NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
    }

    public static class Device {
        private String name;
        private String ip;
        private String status;

        public Device(String name, String ip, String status) {
            this.name = name;
            this.ip = ip;
            this.status = status;
        }

        public String getName() { return name; }
        public String getIp() { return ip; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Device) {
                Device d = (Device) obj;
                return name.equals(d.name) && ip.equals(d.ip);
            }
            return false;
        }
    }
}