package app.insightfuleye.client.activities.todayPatientActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import app.insightfuleye.client.R;
import app.insightfuleye.client.models.volunteerTotalModel;

public class recyclerVolunteerAdapter extends RecyclerView.Adapter<recyclerVolunteerAdapter.MyViewHolder>{
    private ArrayList<volunteerTotalModel> volunteerList;
    public recyclerVolunteerAdapter(ArrayList<volunteerTotalModel> volunteerList){
        this.volunteerList=volunteerList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView volunteerText;
        private TextView totalText;

        public MyViewHolder(final View view){
            super(view);
            volunteerText=view.findViewById(R.id.volunteer);
            totalText=view.findViewById(R.id.total);
        }
    }

    @NonNull
    @Override
    public recyclerVolunteerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_totals, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerVolunteerAdapter.MyViewHolder holder, int position) {
        String name=volunteerList.get(position).getVolunteerName();
        int total=volunteerList.get(position).getVolunteerTotal();
        holder.volunteerText.setText(name);
        holder.totalText.setText(String.valueOf(total));
    }

    @Override
    public int getItemCount() {
        return volunteerList.size();
    }
}
