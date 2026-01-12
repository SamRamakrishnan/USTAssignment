package com.testproject.ustassignment;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<HomeActivity.Device> deviceList;

    public DeviceAdapter(List<HomeActivity.Device> deviceList) {
        this.deviceList = deviceList;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        HomeActivity.Device device = deviceList.get(position);
        holder.nameText.setText(device.getName());
        holder.ipText.setText(device.getIp());
        holder.statusText.setText(device.getStatus());
        holder.statusText.setTextColor(device.getStatus().equals("Online") ? Color.GREEN : Color.RED);

        // Add click listener to navigate to detail screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra("device_name", device.getName());
            intent.putExtra("device_ip", device.getIp());
            intent.putExtra("device_status", device.getStatus());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, ipText, statusText;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.device_name);
            ipText = itemView.findViewById(R.id.device_ip);
            statusText = itemView.findViewById(R.id.device_status);
        }
    }
}