package com.joselestnh.flashcards_client;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CollectionListAdapter extends BaseAdapter {

    public final static int CELL_HEIGHT = 150;

    private List<Collection> collectionList;

    private Context context;
    private LayoutInflater inflater;

    public CollectionListAdapter(Context context, List<Collection> collections){
        this.context = context;
        this.collectionList = collections;
    }

    @Override
    public int getCount() {
        return collectionList.size();
    }

    @Override
    public Object getItem(int position) {
        return collectionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listView = convertView;

        if(listView == null){
            this.inflater = (LayoutInflater) this.context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            listView = this.inflater.inflate(R.layout.collection_layout, null);
        }

        //load data, need to be done at the graphic step
        ImageView image = listView.findViewById(R.id.collectionImage);
        TextView name = listView.findViewById(R.id.collectionName);

        byte[] imageBytes = this.collectionList.get(position).getImage();
        image.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length));
        name.setText(this.collectionList.get(position).getName());

        //update progress
        final int positionUsed = position;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Integer>> result = executorService.submit(new Callable<List<Integer>>() {
            @Override
            public List<Integer> call() throws Exception {
                return MainActivity.getDb().flashcardDao().getProgressByCollection(
                        collectionList.get(positionUsed).getName());

            }
        });
        List<Integer> doneList;

        ProgressBar progressBar = listView.findViewById(R.id.progressBar);
        try{
            doneList = result.get();
            if(!doneList.isEmpty()) {
                float progress = Collections.frequency(doneList, 1) / (float) doneList.size();
                progressBar.setProgress((int) (progress * progressBar.getMax()));
            }
        }catch(Exception e){
            doneList = new ArrayList<>();
            progressBar.setProgress(0);
        }

        executorService.shutdown();



        return listView;
    }

    public void updateData(List<Collection> collectionList){
        this.collectionList = collectionList;
        notifyDataSetChanged();
    }
}


