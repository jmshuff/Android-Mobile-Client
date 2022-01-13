package app.insightfuleye.client.activities.uploadImageActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import app.insightfuleye.client.R;

public class uploadImageViewHolder extends RecyclerView.ViewHolder {
    private TextView documentNameTextView;
    private ImageView documentPhotoImageView;
    private ImageView deleteDocumentImageView;
    private View rootView;

    public uploadImageViewHolder(View itemView) {
        super(itemView);
        rootView = itemView;
        documentNameTextView = itemView.findViewById(R.id.document_name_TextView);
        documentPhotoImageView = itemView.findViewById(R.id.document_photo_ImageView);
        deleteDocumentImageView = itemView.findViewById(R.id.document_delete_button_ImageView);
    }

    public TextView getDocumentNameTextView() {
        return documentNameTextView;
    }

    public ImageView getDocumentPhotoImageView() {
        return documentPhotoImageView;
    }

    public ImageView getDeleteDocumentImageView() {
        return deleteDocumentImageView;
    }

    public View getRootView() {
        return rootView;
    }

}
