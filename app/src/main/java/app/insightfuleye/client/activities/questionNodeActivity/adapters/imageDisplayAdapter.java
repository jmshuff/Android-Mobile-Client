package app.insightfuleye.client.activities.questionNodeActivity.adapters;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import app.insightfuleye.client.R;

public class imageDisplayAdapter extends RecyclerView.Adapter<imageDisplayAdapter.MyViewHolder> {

    private ArrayList<String> imagesList;
    Context context;
    public imageDisplayAdapter(ArrayList<String> imagesList){
        this.imagesList=imagesList;
    } ;


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
        String imagePath=imagesList.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        holder.imageView.setImageBitmap(bitmap);

        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file= new File(imagesList.get(position));
                if (file.exists()) file.delete();
                imagesList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, imagesList.size());
            }
        });

        holder.downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage(imagesList.get(position));
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
}
