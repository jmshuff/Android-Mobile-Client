package app.intelehealth.client.activities.activePatientsActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.intelehealth.client.R;
import app.intelehealth.client.models.ActivePatientModel;
import app.intelehealth.client.utilities.DateAndTimeUtils;

import app.intelehealth.client.activities.patientDetailActivity.PatientDetailActivity;

/**
 * Created by Dexter Barretto on 5/20/17.
 * Github : @dbarretto
 */

public class ActivePatientAdapter extends RecyclerView.Adapter<ActivePatientAdapter.BaseViewHolder> {

    List<ActivePatientModel> activePatientModels;
    Context context;
    ArrayList<String> listPatientUUID;
    int VIEW_TYPE_ITEM = 1;
    int VIEW_TYPE_LOADING = 2;


    public ActivePatientAdapter(List<ActivePatientModel> activePatientModels, Context context, ArrayList<String> _listPatientUUID) {
        this.activePatientModels = activePatientModels;
        this.context = context;
        this.listPatientUUID = _listPatientUUID;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = null;
        if (viewType == VIEW_TYPE_ITEM) {
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_active_patient, parent, false);
            return new ActivePatientViewHolder(root);
        } else {
            root = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_active_patient_progress, parent, false);
            return new ProgressViewHolder(root);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {

        if( holder instanceof ActivePatientViewHolder)
        {
            final ActivePatientModel activePatientModel = activePatientModels.get(position);
            String header;
            if (activePatientModel.getOpenmrs_id() != null) {
                header = String.format("%s %s, %s", activePatientModel.getFirst_name(),
                        activePatientModel.getLast_name(), activePatientModel.getOpenmrs_id());

//            holder.getTv_not_uploaded().setVisibility(View.GONE);
            } else {
                header = String.format("%s %s", activePatientModel.getFirst_name(),
                        activePatientModel.getLast_name());

//            holder.getTv_not_uploaded().setVisibility(View.VISIBLE);
//            holder.getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
//            holder.getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
            }

            if (activePatientModel.getSync().equalsIgnoreCase("0")){
                ((ActivePatientViewHolder) holder).getTv_not_uploaded().setVisibility(View.VISIBLE);
                ((ActivePatientViewHolder) holder).getTv_not_uploaded().setText(context.getResources().getString(R.string.visit_not_uploaded));
                ((ActivePatientViewHolder) holder).getTv_not_uploaded().setBackgroundColor(context.getResources().getColor(R.color.lite_red));
            } else {
                ((ActivePatientViewHolder) holder).getTv_not_uploaded().setVisibility(View.GONE);
            }


//        int age = DateAndTimeUtils.getAge(activePatientModel.getDate_of_birth());

            //get date of birth and convert it into years and months
            String age = DateAndTimeUtils.getAgeInYearMonth(activePatientModel.getDate_of_birth(), context);
            String dob = DateAndTimeUtils.SimpleDatetoLongDate(activePatientModel.getDate_of_birth());
            String body = context.getString(R.string.identification_screen_prompt_age) + "" + age;


            ((ActivePatientViewHolder) holder).getHeadTextView().setText(header);
            ((ActivePatientViewHolder) holder).getBodyTextView().setText(body);
            if (activePatientModel.getEnddate() == null) {
                ((ActivePatientViewHolder) holder).getIndicatorTextView().setText(R.string.active);
                ((ActivePatientViewHolder) holder).getIndicatorTextView().setBackgroundColor(Color.GREEN);
            } else {
                ((ActivePatientViewHolder) holder).getIndicatorTextView().setText(R.string.closed);
                ((ActivePatientViewHolder) holder).getIndicatorTextView().setBackgroundColor(Color.RED);
            }
            ((ActivePatientViewHolder) holder).getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String patientStatus = "returning";
                    Intent intent = new Intent(context, PatientDetailActivity.class);
                    intent.putExtra("patientUuid", activePatientModel.getPatientuuid());
                    intent.putExtra("status", patientStatus);
                    intent.putExtra("tag", "");

                    if (((ActivePatientViewHolder) holder).ivPriscription.getTag().equals("1")) {
                        intent.putExtra("hasPrescription", "true");
                    } else {
                        intent.putExtra("hasPrescription", "false");
                    }
                    context.startActivity(intent);
                }
            });

            for (int i = 0; i < listPatientUUID.size(); i++) {
                if (activePatientModels.get(position).getPatientuuid().equalsIgnoreCase(listPatientUUID.get(i))) {
                    ((ActivePatientViewHolder) holder).ivPriscription.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_prescription_green));
                    ((ActivePatientViewHolder) holder).ivPriscription.setTag("1");
                }
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(activePatientModels.get(position) != null)
            return VIEW_TYPE_ITEM;
        else
            return VIEW_TYPE_LOADING;
    }

    @Override
    public int getItemCount() {
        return activePatientModels.size();
    }

    public void addNullData() {
        activePatientModels.add(null);
        notifyItemInserted(activePatientModels.size()-1);
    }

    public void removeNull() {
        activePatientModels.remove(activePatientModels.size() - 1);
        notifyItemRemoved(activePatientModels.size());
    }

    public void addData(List<ActivePatientModel> activePatientModelList) {
        activePatientModels.addAll(activePatientModelList);
        notifyDataSetChanged();
    }

    public class ActivePatientViewHolder extends BaseViewHolder {
        private TextView headTextView;
        private TextView bodyTextView;
        private TextView indicatorTextView;
        private View rootView;
        private ImageView ivPriscription;
        private TextView tv_not_uploaded;

        public ActivePatientViewHolder(View itemView) {
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

    class ProgressViewHolder extends BaseViewHolder { public ProgressViewHolder (View itemView)
        {
            super(itemView);
        }
    }
    class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

}
