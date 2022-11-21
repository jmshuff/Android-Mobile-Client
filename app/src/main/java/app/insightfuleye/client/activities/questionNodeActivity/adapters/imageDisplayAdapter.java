package app.insightfuleye.client.activities.questionNodeActivity.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import app.insightfuleye.client.R;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.ImagesDAO;
import app.insightfuleye.client.models.imageDisplay;
import app.insightfuleye.client.utilities.exception.DAOException;

public class imageDisplayAdapter extends RecyclerView.Adapter<imageDisplayAdapter.MyViewHolder> {

    private ArrayList<imageDisplay> imagesList;
    Context context;

    public imageDisplayAdapter(ArrayList<imageDisplay> imagesList) {
        this.imagesList = imagesList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private RelativeLayout deleteImage;
        private RelativeLayout downloadImage;
        public MyViewHolder(final View view){
            super(view);
            imageView=view.findViewById(R.id.iv_image_display);
            deleteImage=view.findViewById(R.id.delete_image_button);
            downloadImage=view.findViewById(R.id.download_image_button);
        }
    }
    @NonNull
    @Override
    public imageDisplayAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_image_card, parent,false);
        context=parent.getContext();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull imageDisplayAdapter.MyViewHolder holder, int position) {
        String imagePath=imagesList.get(position).getImagePath();
        File file=new File(imagePath);
        if (file.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            holder.imageView.setImageBitmap(bitmap);
            Log.d("imageListLength", String.valueOf(imagesList.size()));
        }


        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
                //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
                alertDialogBuilder.setMessage(R.string.delete_confirmation);
                alertDialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file= new File(imagesList.get(position).getImagePath());
                        if (file.exists()) file.delete();
                        ImagesDAO imagesDAO=new ImagesDAO();
                        String[] imageName=imagesList.get(position).getImagePath().split("/");
                        try {
                            imagesDAO.removeAzureSynced(imageName[imageName.length-1]);
                        } catch (DAOException e) {
                            e.printStackTrace();
                        }
                        imagesList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imagesList.size());
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.generic_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.show();
                //alertDialog.show();
                IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);
            }
        });

        holder.downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(imagesList.get(position).getImagePath());
                if (file.exists()){
                    downloadImage(imagesList.get(position).getImagePath());
                }
                else{
                    Toast.makeText(context, "No image available", Toast.LENGTH_LONG);
                }
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewImage(imagesList.get(position).getImagePath());
            }
        });



    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    private void downloadImage(String path){
        //ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        //ActivityCompat.requestPermissions(, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG).show();
        String DIR_NAME="AROMA Photos";
        File direct =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + "/" + DIR_NAME + "/");

        Log.d("Download Image: ", path);

        Bitmap bitmap = BitmapFactory.decodeFile(path);

        FileOutputStream outputStream = null;



        direct.mkdirs();
        File file = Environment.getExternalStorageDirectory();
        String filename = String.format("%d.png",System.currentTimeMillis());
        File outFile = new File(direct,filename);

        try{
            outputStream = new FileOutputStream(outFile);
        }catch (Exception e){
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        try{
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(outFile);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private void previewImage(String path){

    }
}
