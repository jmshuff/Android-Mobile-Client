package app.insightfuleye.client.activities.followUpActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.patientDetailActivity.PatientDetailActivity;
import app.insightfuleye.client.models.FollowUpPatientModel;
import app.insightfuleye.client.utilities.DateAndTimeUtils;

public class followUpPatientAdapter extends RecyclerView.Adapter<followUpPatientAdapter.FollowUpPatientViewHolder> {
        List<FollowUpPatientModel> followUpPatientModelList;
        Context context;
        LayoutInflater layoutInflater;
        ArrayList<String> listPatientUUID;

    public followUpPatientAdapter(List<FollowUpPatientModel> followUpPatientList, Context context, ArrayList<String> listPatientUUID) {
        this.followUpPatientModelList = followUpPatientList;
        this.context = context;
        this.listPatientUUID = listPatientUUID;
    }



    @NonNull
    @Override
    public followUpPatientAdapter.FollowUpPatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.list_item_today_patient, parent, false);
        followUpPatientAdapter.FollowUpPatientViewHolder viewHolder = new followUpPatientAdapter.FollowUpPatientViewHolder(row);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(followUpPatientAdapter.FollowUpPatientViewHolder holder, int position) {
    final FollowUpPatientModel followUpPatientModel = followUpPatientModelList.get(position);
            String header;
            if (followUpPatientModel.getRightEyeDiagnosis() != "" && followUpPatientModel.getLeftEyeDiagnosis()!="") {
                header = String.format("%s %s, R: %s L: %s", followUpPatientModel.getFirst_name(),
                        followUpPatientModel.getLast_name(), followUpPatientModel.getRightEyeDiagnosis(), followUpPatientModel.getLeftEyeDiagnosis());
            }
            else if (followUpPatientModel.getRightEyeDiagnosis() != "") {
            header = String.format("%s %s, R: %s", followUpPatientModel.getFirst_name(),
            followUpPatientModel.getLast_name(), followUpPatientModel.getRightEyeDiagnosis());
            }
            else if (followUpPatientModel.getLeftEyeDiagnosis() != "") {
                header = String.format("%s %s, L: %s", followUpPatientModel.getFirst_name(),
                        followUpPatientModel.getLast_name(), followUpPatientModel.getLeftEyeDiagnosis());
            }
            else {
            header = String.format("%s %s", followUpPatientModel.getFirst_name(),
            followUpPatientModel.getLast_name());
            }
    //        int age = DateAndTimeUtils.getAge(followUpPatientModel.getDate_of_birth());
            String age = DateAndTimeUtils.getAgeInYearMonth(followUpPatientModel.getDate_of_birth(), context);
            String dob = DateAndTimeUtils.SimpleDatetoLongDate(followUpPatientModel.getDate_of_birth());
            String body = context.getString(R.string.identification_screen_prompt_age) + "" + age;

            if (followUpPatientModel.getSync().equalsIgnoreCase("0")){
            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
            } else {
            holder.getTv_not_uploaded().setVisibility(View.GONE);
            }

            holder.getHeadTextView().setText(header);
            holder.getBodyTextView().setText(body);
            if (followUpPatientModel.getEnddate() == null) {
            holder.getIndicatorTextView().setText(R.string.active);
            holder.getIndicatorTextView().setBackgroundColor(Color.GREEN);
            } else {
            holder.getIndicatorTextView().setText(R.string.closed);
            holder.getIndicatorTextView().setBackgroundColor(Color.RED);
            }
            holder.getRootView().setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
            String patientStatus = "returning";
            Intent intent = new Intent(context, PatientDetailActivity.class);
            intent.putExtra("patientUuid", followUpPatientModel.getPatientuuid());
            intent.putExtra("status", patientStatus);
            intent.putExtra("tag", "");

            if (holder.ivPriscription.getTag().equals("1")) {
            intent.putExtra("hasPrescription", "true");
            } else {
            intent.putExtra("hasPrescription", "false");
            }

            context.startActivity(intent);
            }
            });

            for (int i = 0; i < listPatientUUID.size(); i++) {
            if (followUpPatientModelList.get(position).getPatientuuid().equalsIgnoreCase(listPatientUUID.get(i))) {
            holder.ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
            holder.ivPriscription.setTag("1");
            }
            }


            }

    @Override
    public int getItemViewType(int position) {
            return position;
            }

    @Override
    public int getItemCount() {
            return followUpPatientModelList.size();
            }

    class FollowUpPatientViewHolder extends RecyclerView.ViewHolder {

        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;

        public FollowUpPatientViewHolder(View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.list_item_head_text_view);
            bodyTextView = itemView.findViewById(R.id.list_item_body_text_view);
            indicatorTextView = itemView.findViewById(R.id.list_item_indicator_text_view);
            ivPriscription = itemView.findViewById(R.id.iv_prescription);
            tv_not_uploaded = (TextView) itemView.findViewById(R.id.tv_not_uploaded);
            rootView = itemView;
        }

        public TextView getHeadTextView() {
            return headTextView;
        }

        public void setHeadTextView(TextView headTextView) {
            this.headTextView = headTextView;
        }

        public TextView getBodyTextView() {
            return bodyTextView;
        }

        public void setBodyTextView(TextView bodyTextView) {
            this.bodyTextView = bodyTextView;
        }

        public TextView getIndicatorTextView() {
            return indicatorTextView;
        }

        public void setIndicatorTextView(TextView indicatorTextView) {
            this.indicatorTextView = indicatorTextView;
        }

        public View getRootView() {
            return rootView;
        }

        public TextView getTv_not_uploaded() {
            return tv_not_uploaded;
        }

        public void setTv_not_uploaded(TextView tv_not_uploaded) {
            this.tv_not_uploaded = tv_not_uploaded;
        }
    }


}
