package app.insightfuleye.client.activities.questionNodeActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.physcialExamActivity.PhysicalExamActivity;
import app.insightfuleye.client.activities.questionNodeActivity.adapters.AssociatedSysAdapter;
import app.insightfuleye.client.activities.questionNodeActivity.adapters.imageDisplayAdapter;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.knowledgeEngine.Node;
import app.insightfuleye.client.knowledgeEngine.PhysicalExam;
import app.insightfuleye.client.models.imageDisplay;

/**
 * Created by Sagar Shimpi
 * Github - TheSeasApps
 */
public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ChipsAdapterViewHolder> implements AssociatedSysAdapter.FabVisibility {

    LayoutInflater layoutInflater;
    Context context;
    Node currentNode;
    int pos;
    RecyclerView recyclerView;
    FabClickListener _mListener;
    String _mCallingClass;
    boolean isAssociateSym;
    boolean showPopUp;
    ArrayList<imageDisplay> imageList;
    imageDisplayAdapter imageDisplayAdapter;


    public void updateNode(Node currentNode) {
        this.currentNode = currentNode;
        notifyDataSetChanged();
    }

    boolean isChildNeedRefresh = false;

    public void refreshChildAdapter() {
        this.isChildNeedRefresh = true;
    }

    @Override
    public void setVisibility(boolean data) {
        showPopUp=data;
    }

    public interface FabClickListener {
        void fabClickedAtEnd();

        void onChildListClickEvent(int groupPos, int childPos, int physExamPos, String type);


    }


    public QuestionsAdapter(Context _context, Node node, RecyclerView _rvQuestions, String callingClass,
                            FabClickListener _mListener, boolean isAssociateSym, ArrayList<imageDisplay> imageList) {
        this.context = _context;
        this.currentNode = node;
        this.recyclerView = _rvQuestions;
        this._mCallingClass = callingClass;
        this._mListener = _mListener;
        this.isAssociateSym = isAssociateSym;

    }

    PhysicalExam physicalExam;

    public QuestionsAdapter(Context _context, PhysicalExam node, RecyclerView _rvQuestions, String callingClass,
                            FabClickListener _mListener, boolean isAssociateSym, ArrayList<imageDisplay> imageList) {
        this.context = _context;
        this.physicalExam = node;
        this.recyclerView = _rvQuestions;
        this._mCallingClass = callingClass;
        this._mListener = _mListener;
        this.isAssociateSym = isAssociateSym;
        this.imageList= imageList;

    }

    @Override
    public QuestionsAdapter.ChipsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.quesionnode_list_item, parent, false);
        return new ChipsAdapterViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ChipsAdapterViewHolder holder, int position) {
        Node _mNode;
        if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
            _mNode = physicalExam.getExamNode(position).getOption(0);
            final String parent_name = physicalExam.getExamParentNodeName(position);
            String nodeText = parent_name + " : " + _mNode.findDisplay();
            Log.d("NodeText",nodeText);
            holder.physical_exam_text_view.setText(nodeText);
            holder.physical_exam_text_view.setVisibility(View.GONE);
            //Log.d("isAidAvailab;e", String.valueOf(_mNode.isAidAvailable()));
            //Log.d("isBilateral", String.valueOf(_mNode.isBilateral()));
            if (_mNode.isAidAvailable()) {
                String type = _mNode.getJobAidType();
                if (type.equals("video")) {
                    holder.physical_exam_image_view.setVisibility(View.GONE);
                } else if (type.equals("image")) {
                    holder.physical_exam_image_view.setVisibility(View.VISIBLE);
                    String drawableName = "physicalExamAssets/" + _mNode.getJobAidFile() + ".jpg";
                    try {
                        // get input stream
                        InputStream ims = context.getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        holder.physical_exam_image_view.setImageDrawable(d);
                        holder.physical_exam_image_view.setMinimumHeight(300);
                        holder.physical_exam_image_view.setMinimumWidth(300);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        holder.physical_exam_image_view.setVisibility(View.GONE);
                    }
                } else {
                    holder.physical_exam_image_view.setVisibility(View.GONE);
                }
            } else {
                holder.physical_exam_image_view.setVisibility(View.GONE);
            }
            holder.tvQuestion.setText(_mNode.findDisplay());

            //JS display images taken by camera
            holder.rvImages.setVisibility(View.GONE);
            for(int i=0; i<physicalExam.getExamNode(position).getOption(0).size(); i++){
                if (physicalExam.getExamNode(position).getOption(0).getOption(i).getInputType().equals("camera")){
                    Log.d("inputtype", "camera");
                    holder.rvImages.setVisibility(View.VISIBLE);
                    break;
                }
            }

            ArrayList<imageDisplay> imageDisplayList = new ArrayList<>();
            Log.d("Pos", String.valueOf(position));
            for (imageDisplay imageInfo : imageList){
                Log.d("makeimagelist", imageInfo.getImagePath() + " pos: " + imageInfo.getPosition());

                if (imageInfo.getPosition()==position){
                    imageDisplayList.add(imageInfo);
                }
            }
            imageDisplayAdapter=new imageDisplayAdapter(imageDisplayList);
            holder.rvImages.setAdapter(imageDisplayAdapter);

        } else {
            _mNode = currentNode;
            if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
                holder.tvQuestion.setText(_mNode.getOptionsList().get(0).findDisplay());
            } else {
                holder.tvQuestion.setText(_mNode.getOptionsList().get(position).findDisplay());
            }
            holder.physical_exam_image_view.setVisibility(View.GONE);
            holder.physical_exam_text_view.setVisibility(View.GONE);

            //JS display images taken by camera
            holder.rvImages.setVisibility(View.GONE);
            for(int i=0; i<_mNode.getOption(0).size(); i++){
                if (_mNode.getOption(0).getOption(i).getInputType().equals("camera")){
                    Log.d("inputtype", "camera");
                    holder.rvImages.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }

        if (position == getItemCount() - 1) {
            holder.fab.setVisibility(View.VISIBLE);
            
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.llBody.getLayoutParams();
            layoutParams.weight = (float) 0.88;
            holder.llBody.setLayoutParams(layoutParams);

            //RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) holder.fabLayout.getLayoutParams();
            //layoutParams1.weight = (float) 0.12;
            

        } else {
            holder.fab.setVisibility(View.INVISIBLE);
        }




        holder.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showPopUp){
                    Toast.makeText(context,"Select all the answers",Toast.LENGTH_LONG).show();

                }else {
                    _mListener.fabClickedAtEnd();
                }

            }
        });

        if (isChildNeedRefresh) {
            holder.rvChips.getAdapter().notifyDataSetChanged();

        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
            return physicalExam.getTotalNumberOfExams();
        } else {
            if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
               /* List<Node> nodeList = currentNode.getOptionsList().get(0).getOptionsList();
                if (nodeList.size() > 8) {
                    List<List<Node>> spiltList = Lists.partition(currentNode.getOptionsList().get(0).getOptionsList(), 8);*/
                return 1;
               /* } else {
                    return currentNode.getOptionsList().size();
                }*/
            } else {
                return currentNode.getOptionsList().size();
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        pos = position;
        return position;
    }

    public class ChipsAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuestion, physical_exam_text_view;
        ImageView ivAyu, physical_exam_image_view;
        RecyclerView rvChips;
        RecyclerView rvImages;
        FloatingActionButton fab;
        ComplaintNodeListAdapter chipsAdapter;
        AssociatedSysAdapter associatedSysAdapter;
        LinearLayout llBody;
        RelativeLayout fabLayout;



        public ChipsAdapterViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_complaintQuestion);
            rvChips = itemView.findViewById(R.id.rv_chips);
            rvImages=itemView.findViewById(R.id.rv_image_display);
            fab = itemView.findViewById(R.id.fab);
            physical_exam_text_view = itemView.findViewById(R.id.physical_exam_text_view);
            physical_exam_image_view = itemView.findViewById(R.id.physical_exam_image_view);
            llBody=itemView.findViewById(R.id.LL_body);
            fabLayout=itemView.findViewById(R.id.fab_layout);


            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);

            rvChips.setLayoutManager(linearLayoutManager);
            rvChips.setHasFixedSize(true);
            //rvChips.setItemAnimator(new DefaultItemAnimator());
            rvChips.setNestedScrollingEnabled(true);

            rvImages.setLayoutManager(linearLayoutManager1);

            Node groupNode;
            List<Node> chipList = new ArrayList<>();
            if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                groupNode = physicalExam.getExamNode(pos).getOption(0);
                for (int i = 0; i < groupNode.getOptionsList().size(); i++) {
                    chipList.add(groupNode.getOptionsList().get(i));
                }
            } else {
                groupNode = currentNode;
                if (isAssociateSym && currentNode.getOptionsList().size() == 1) {
                    chipList = currentNode.getOptionsList().get(0).getOptionsList();
                }else{
                    Node node = currentNode.getOptionsList().get(pos);
                    for (int i = 0; i < node.getOptionsList().size(); i++) {
                        chipList.add(node.getOptionsList().get(i));
                    }
                }
            }


            int groupPos = (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName()) || (isAssociateSym && currentNode.getOptionsList().size() == 1)) ? 0 : pos;

            if(groupNode.getOption(groupPos).getText().equalsIgnoreCase("Associated symptoms") || groupNode.getOption(groupPos).getText().equalsIgnoreCase("जुड़े लक्षण")) {
                associatedSysAdapter=new AssociatedSysAdapter(context, chipList, groupNode, groupPos, _mListener, _mCallingClass, pos);
                rvChips.setAdapter(associatedSysAdapter);

            }   else {
                chipsAdapter = new ComplaintNodeListAdapter(context, chipList, groupNode, groupPos, _mListener, _mCallingClass, pos);
                rvChips.setAdapter(chipsAdapter);
            }


        }
    }


    class ComplaintNodeListAdapter extends RecyclerView.Adapter<ComplaintNodeListAdapter.ItemViewHolder> {
        private static final String TAG = "CNodeListAdapter";

        private Context mContext;
        private int layoutResourceID;
        private ImmutableList<Node> mNodes;
        private List<Node> mNodesFilter;
        private Node mGroupNode;
        private int mGroupPos;
        private QuestionsAdapter.FabClickListener _mListener;
        String _mCallingClass;
        private int physExamNodePos;

        public ComplaintNodeListAdapter(Context context, List<Node> nodes, Node groupNode, int groupPos,
                                        QuestionsAdapter.FabClickListener listener, String callingClass, int nodePos) {
            this.mContext = context;
            this.mNodesFilter = nodes;
            this.mNodes = ImmutableList.copyOf(mNodesFilter);
            mGroupNode = groupNode;
            mGroupPos = groupPos;
            this._mListener = listener;
            this._mCallingClass = callingClass;
            this.physExamNodePos = nodePos;
        }


        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View row;
            //row = inflater.inflate(R.layout.layout_chip, parent, false);
            //Log.d("ChooseLayout", String.valueOf(physicalExam.getExamNode(mGroupPos).getOption(0).isBilateral()));
            //Log.d("mGroupNode", mGroupNode.getOption(mGroupPos).toString());
            if (mGroupNode.isBilateral() || mGroupNode.getOption(mGroupPos).isBilateral()){
                Log.d("Layout", "Bilateral");
                row=inflater.inflate(R.layout.layout_chip_bilateral,parent, false);

            }else{
                row = inflater.inflate(R.layout.layout_chip, parent, false);
            }


            return new ItemViewHolder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull ComplaintNodeListAdapter.ItemViewHolder itemViewHolder, int position) {
            final Node thisNode = mNodesFilter.get(position);
            itemViewHolder.mChipText.setText(thisNode.findDisplay());

            Node groupNode = mGroupNode.getOption(mGroupPos);
            //Log.d("isBilateral", String.valueOf(groupNode.isBilateral()));

            if (thisNode.getInputType().equals("camera")){
                itemViewHolder.mChipImage.setImageResource(R.drawable.ic_camera_black);

                int padding_start_dp = 36;  // 6 dps
                int padding_end_dp=15;
                int padding_top_dp=10;
                int padding_bottom_dp=10;
                final float scale = context.getResources().getDisplayMetrics().density;
                int padding_start_px = (int) (padding_start_dp * scale + 0.5f);
                int padding_end_px= (int) (padding_end_dp * scale + 0.5f);
                int padding_top_px = (int) (padding_top_dp * scale + 0.5f);
                int padding_bot_px = (int) (padding_bottom_dp * scale + 0.5f);
                itemViewHolder.mChipText.setPaddingRelative(padding_start_px, padding_top_px, padding_end_px, padding_bot_px);
            }


            //Change color of the node if it is selected
            if(mGroupNode.isBilateral() || mGroupNode.getOption(mGroupPos).isBilateral()){
                //Log.d("Colorchange", "working");
                if (thisNode.isRightSelected()){
                    itemViewHolder.mChipRight.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    itemViewHolder.mChipRight.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
                }
                else{
                    itemViewHolder.mChipRight.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    itemViewHolder.mChipRight.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                }
                if(thisNode.isLeftSelected()){
                    itemViewHolder.mChipLeft.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    itemViewHolder.mChipLeft.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
                }
                else{
                    itemViewHolder.mChipLeft.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    itemViewHolder.mChipLeft.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                }
            }
            else {

                if ((groupNode.getText().equalsIgnoreCase("Associated symptoms") && thisNode.isNoSelected()) || (groupNode.getText().equalsIgnoreCase("जुड़े लक्षण") && thisNode.isNoSelected()) || thisNode.isSelected()) {
                    itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));
                } else {
                    itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                    //itemViewHolder.mChip.setChipBackgroundColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.transparent))));
                    //itemViewHolderiewHolder.mChip.setTextColor((ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.primary_text))));
                }
            }
            itemViewHolder.mChipText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (groupNode.getText() != null) {
                        //null checking to avoid weird crashes.
                        String type="both";
                        if (groupNode.getText().equalsIgnoreCase("Associated symptoms") || groupNode.getText().equalsIgnoreCase("जुड़े लक्षण")) {
                            MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(context);
                            confirmDialog.setTitle(R.string.have_symptom);
                            confirmDialog.setCancelable(false);
                            LayoutInflater layoutInflater = LayoutInflater.from(context);
                            View convertView = layoutInflater.inflate(R.layout.list_expandable_item_radio, null);
                            confirmDialog.setView(convertView);
                            RadioButton radio_yes = convertView.findViewById(R.id.radio_yes);
                            RadioButton radio_no = convertView.findViewById(R.id.radio_no);
                            confirmDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = confirmDialog.create();
                            radio_yes.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    thisNode.setNoSelected(false);
                                    List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                                    int indexOfCheckedNode = childNode.indexOf(thisNode);
                                    _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos, type);
                                    notifyDataSetChanged();
                                    if (alertDialog != null) {
                                        alertDialog.dismiss();
                                    }

                                }
                            });

                            radio_no.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    thisNode.setNoSelected(true);
                                    thisNode.setUnselected();
                                    notifyDataSetChanged();
                                    if (alertDialog != null) {
                                        alertDialog.dismiss();
                                    }
                                }
                            });

                            switch (_mCallingClass) {

                                case "ComplaintNodeActivity":
                                    if (thisNode.isSelected()) {
                                        radio_yes.setChecked(true);
                                    } else {
                                        radio_no.setChecked(true);
                                    }
                                    break;
                                default:
                                    if (thisNode.isSelected()) {
                                        radio_yes.setChecked(true);
                                    } else {
                                        if (thisNode.isNoSelected()) {
                                            radio_no.setChecked(true);
                                        } else {
                                            radio_no.setChecked(false);
                                        }
                                    }
                                    break;
                            }

                            alertDialog.show();
                            IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);

                        } else {
                                //thisNode.toggleSelected();
                                int indexOfCheckedNode;
                                if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                                    indexOfCheckedNode = position;
                                } else {
                                    List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                                    indexOfCheckedNode = childNode.indexOf(thisNode);
                                }
                                _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos, type);
                                notifyDataSetChanged();
                        }
                    }
                    else {
                        Toast.makeText(mContext, R.string.some_issue_mindmaps, Toast.LENGTH_SHORT).show();
                    }

                }
            });
            if (mGroupNode.isBilateral() || mGroupNode.getOption(mGroupPos).isBilateral()) {
                itemViewHolder.mChipRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int indexOfCheckedNode;
                        String type = "right";
                        if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                            indexOfCheckedNode = position;
                        } else {
                            List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                            indexOfCheckedNode = childNode.indexOf(thisNode);
                        }
                        _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos, type);
                        notifyDataSetChanged();
                    }
                });

                itemViewHolder.mChipLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int indexOfCheckedNode;
                        String type = "left";
                        if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                            indexOfCheckedNode = position;
                        } else {
                            List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                            indexOfCheckedNode = childNode.indexOf(thisNode);
                        }
                        _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos, type);
                        notifyDataSetChanged();
                    }
                });
            }


        /*   itemViewHolder.mChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //thisNode.toggleSelected();
                    if ((groupNode.getText().equalsIgnoreCase("Associated symptoms") && thisNode.isNoSelected())) {
                        thisNode.setNoSelected(false);

                        if(!thisNode.isSelected()) {
                            thisNode.setSelected(true);
                            itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                            itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_blue));

                        }else {
                            itemViewHolder.mChipText.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                            thisNode.setSelected(false);
                            itemViewHolder.mChipText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.rounded_rectangle_orange));
                        }


                      //  thisNode.toggleSelected();
                    }
                    int indexOfCheckedNode;
                    if (_mCallingClass.equalsIgnoreCase(PhysicalExamActivity.class.getSimpleName())) {
                        indexOfCheckedNode = position;
                    } else {
                        List<Node> childNode = mGroupNode.getOptionsList().get(mGroupPos).getOptionsList();
                        indexOfCheckedNode = childNode.indexOf(thisNode);
                    }
                    _mListener.onChildListClickEvent(mGroupPos, indexOfCheckedNode, physExamNodePos);
                    notifyDataSetChanged();
                }
            });
        */
        }

        @Override
        public int getItemCount() {
            return (mNodesFilter != null ? mNodesFilter.size() : 0);
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView mChipText;
            RelativeLayout mChip;
            TextView mChipLeft;
            TextView mChipRight;
            ImageView mChipImage;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                //Log.d("mGroupNode", mGroupNode.toString());
                //Log.d("mGroupNodeOption", mGroupNode.getOption(mGroupPos).toString());
                if (mGroupNode.isBilateral() || mGroupNode.getOption(mGroupPos).isBilateral()){
                    mChip = itemView.findViewById(R.id.complaint_chip);
                    mChipText = itemView.findViewById(R.id.tvChipText);
                    mChipLeft= itemView.findViewById(R.id.tvChipLeft);
                    mChipRight=itemView.findViewById(R.id.tvChipRight);
                    mChipLeft.setText("Left Eye");
                    mChipRight.setText("Right Eye");
                }
                else {
                    mChip = itemView.findViewById(R.id.complaint_chip);
                    mChipText = itemView.findViewById(R.id.tvChipText);
                    mChipImage=itemView.findViewById(R.id.ivChip);
                }
            }
        }


        public ImmutableList<Node> getmNodes() {
            return mNodes;
        }
    }


    public static <T> List<List<T>> partitionList(List<T> list, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Invalid  size to partition: " + chunkSize);
        }
        List<List<T>> chunkList = new ArrayList<>(list.size() / chunkSize);
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunkList.add(list.subList(i, i + chunkSize >= list.size() ? list.size() - 1 : i + chunkSize));
        }
        return chunkList;
    }


}



