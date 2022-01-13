package app.insightfuleye.client.activities.uploadImageActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.R;
import app.insightfuleye.client.activities.additionalDocumentsActivity.AdditionalDocumentAdapter;
import app.insightfuleye.client.activities.additionalDocumentsActivity.AdditionalDocumentViewHolder;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.models.DocumentObject;
import app.insightfuleye.client.utilities.StringUtils;
import app.insightfuleye.client.utilities.exception.DAOException;

public class uploadImageAdapter extends RecyclerView.Adapter<uploadImageViewHolder> {
    int screen_height;
    int screen_width;

    private List<DocumentObject> documentList = new ArrayList<>();
    private Context context;
    private String filePath;
    ImagesDAO imagesDAO = new ImagesDAO();
    private static final String TAG = uploadImageAdapter.class.getSimpleName();

    public uploadImageAdapter(Context context, List<DocumentObject> documentList, String filePath) {
        this.documentList = documentList;
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screen_height = displayMetrics.heightPixels;
        screen_width = displayMetrics.widthPixels;
        this.filePath = filePath;

    }

    @Override
    public uploadImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_additional_doc, null);
        uploadImageViewHolder rcv = new uploadImageViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final uploadImageViewHolder holder, final int position) {

//        holder.getDocumentNameTextView().setText(documentList.get(position).getDocumentName());
        holder.getDocumentNameTextView().setText
                ("Document - " + (position + 1));

        final File image = new File(documentList.get(position).getDocumentPhoto());

        Glide.with(context)
                .load(image)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .thumbnail(0.1f)
                .into(holder.getDocumentPhotoImageView());

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayImage(image);
            }
        });

        holder.getDeleteDocumentImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image.exists()) image.delete();
                documentList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, documentList.size());
                String imageName = holder.getDocumentNameTextView().getText().toString();

                try {
                    imagesDAO.deleteImageFromDatabase(StringUtils.getFileNameWithoutExtensionString(imageName));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

    public void add(DocumentObject doc) {
        boolean bool = documentList.add(doc);
        if (bool) Log.d(TAG, "add: Item added to list");
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.documentList.size();
    }


    public void displayImage(final File file) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);


        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);
        dialog.setView(dialogLayout);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT==Build.VERSION_CODES.M || Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog.supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        } else {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                ImageView imageView = dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                if (imageView != null) {
                    Glide.with(context)
                            .load(file)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .listener(new RequestListener<File, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, File file, Target<GlideDrawable> target, boolean b) {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable glideDrawable, File file, Target<GlideDrawable> target, boolean b, boolean b1) {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }
                            })
                            .override(screen_width, screen_height)
                            .into(imageView);
                }
            }
        });

        dialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }
}
