package com.example.med;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.med.models.Visit;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.ViewHolder> {

    private List<Visit> visits;
    private OnVisitDeleteListener deleteListener;

    public interface OnVisitDeleteListener {
        void onDelete(Visit visit);
    }

    public VisitAdapter(List<Visit> visits, OnVisitDeleteListener deleteListener) {
        this.visits = visits;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Visit visit = visits.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        holder.doctorName.setText("🩺 " + visit.doctorName);
        holder.dateTime.setText(sdf.format(visit.dateTime));

        if (visit.notes != null && !visit.notes.isEmpty()) {
            holder.notes.setText("📝 " + visit.notes);
            holder.notes.setVisibility(View.VISIBLE);
        } else {
            holder.notes.setVisibility(View.GONE);
        }

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(visit));
    }

    @Override
    public int getItemCount() {
        return visits.size();
    }

    public void updateData(List<Visit> newVisits) {
        this.visits = newVisits;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName, dateTime, notes;
        Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            dateTime = itemView.findViewById(R.id.visitDateTime);
            notes = itemView.findViewById(R.id.visitNotes);
            deleteButton = itemView.findViewById(R.id.deleteVisitButton);
        }
    }
}