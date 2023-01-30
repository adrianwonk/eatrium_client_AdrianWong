package edu.cis.ibcs_app.Models;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.cis.ibcs_app.R;

public class Admin_viewHolder extends RecyclerView.ViewHolder {
    public TextView TVName;
    public TextView TVId;
    public TextView TVYearLevel;
    public Button bAccept;
    public Button bDeny;
    public Button overlay;

    public Admin_viewHolder(@NonNull View itemView) {
        super(itemView);
        overlay = (Button) itemView.findViewById(R.id.request_completedOverlay);
        TVName = (TextView) itemView.findViewById(R.id.request_name);
        TVId = (TextView) itemView.findViewById(R.id.request_id);
        TVYearLevel = (TextView) itemView.findViewById(R.id.request_yearLevel);
        bAccept = (Button) itemView.findViewById(R.id.request_accept);
        bDeny = (Button) itemView.findViewById(R.id.request_deny);
    }
}
