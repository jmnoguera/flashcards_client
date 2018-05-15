package com.joselestnh.flashcards_client;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_COLLECTION = 2;
    private List<Collection> collectionList;

    private static AppDatabase db;
    //definir adapter y parte grafica
    ListView listView;
    CollectionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class,
                "FlashCards-DB").build();

        reloadCollections();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                //redefine class invoked, creation or getting from the db?
                // the next activity is missing
                Intent intent = new Intent(MainActivity.this, null);
                startActivityForResult(intent, RESULT_COLLECTION);
            }
        });


        //update listView
        listView = this.findViewById(R.id.collectionsPool);
        adapter = new CollectionListAdapter(MainActivity.this, collectionList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                //check if there is any flashcard
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<Integer> result = executorService.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return MainActivity.getDb().flashcardDao().checkFlashcardsExistenceFor(
                                collectionList.get(position).getName());
                    }
                });

                int exist;

                try{
                    exist = result.get();
                }catch (Exception e){
                    return;
                }
                executorService.shutdown();

                if(exist == 1){
                    openFlashcardsResolution(position);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "This collection does not contain any flashcard",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFlashcardsResolution(int position) {
        Intent intent = new Intent(this, FlashcardResolutionActivity.class);
        intent.putExtra(Flashcard.KEY_FC_COLLECTION, collectionList.get(position).getName());
        startActivity(intent);
    }

    @Override
    public void onStart(){
        super.onStart();
        reloadCollections();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadCollections(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Collection>> result = executorService.submit(new Callable<List<Collection>>() {
            @Override
            public List<Collection> call() throws Exception {
                collectionList = db.collectionDao().getAll();
                return collectionList;
            }
        });
        try {
            collectionList = result.get();
            if(adapter != null){
                adapter.updateData(collectionList);
            }

        } catch (Exception e){
            collectionList = new ArrayList<>();
        }
        executorService.shutdown();

    }


    //its not the definitive implementation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==  RESULT_COLLECTION && resultCode == RESULT_OK){

            Bundle bundle = data.getExtras();
            String collectionName = bundle.getString(Collection.KEY_COLLECTION_NAME);
            String collectionDescription = bundle.getString(Collection.KEY_COLLECTION_DESCRIPTION);
            byte[] collectionImage = bundle.getByteArray(Collection.KEY_COLLECTION_IMAGE);

            final Collection collection = new Collection(collectionName,collectionDescription,collectionImage);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    db.collectionDao().insertCollection(collection);
                }
            }).start();

        }
    }

    public static AppDatabase getDb() {
        return db;
    }
}
