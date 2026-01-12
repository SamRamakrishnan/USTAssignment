package com.testproject.ustassignment;



import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.devices_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(deviceList);
        recyclerView.setAdapter(adapter);

        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        initializeDiscoveryListener();
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

    private void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service found: " + service.getServiceName());
                // Resolve the service to get IP
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
                            Device device = new Device(name, ip);
                            if (!deviceList.contains(device)) {
                                deviceList.add(device);
                                runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }
                        }
                    }
                });
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.d(TAG, "Service lost: " + service.getServiceName());
                // Optionally remove from list
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

    // Inner class for device data
    public static class Device {
        private String name;
        private String ip;

        public Device(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public String getName() { return name; }
        public String getIp() { return ip; }

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