package com.joselestnh.flashcards_client;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FlashcardResolutionActivity extends AppCompatActivity {

    private String collectionName;
    private List<Flashcard> flashcardList;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_resolution_activity);


        Intent intent = getIntent();
        collectionName = intent.getStringExtra(Flashcard.KEY_FC_COLLECTION);
        currentPosition = 0;
        reloadFlashcards();
//        updateFlashcardView();

        //button functions
        findViewById(R.id.checkButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.solution);
                String solution = input.getText().toString();

                if(solution.equals(flashcardList.get(currentPosition).getWordB())){
                    correctAnswer();
                    updateFlashcardView();
                }else{
                    Toast.makeText(getApplicationContext(),"Wrong Answer", Toast.LENGTH_SHORT).show();
                }

            }
        });

        findViewById(R.id.previousButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPosition>0){
                    currentPosition--;
                    updateFlashcardView();
                }
            }
        });

        findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPosition<flashcardList.size()-1){
                    currentPosition++;
                    updateFlashcardView();
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        reloadFlashcards();
    }

    public void updateFlashcardView(){

        if(flashcardList.isEmpty()) return;

        //use current position
        ImageView background = findViewById(R.id.flashcardImage);
        TextView hintWord = findViewById(R.id.hintWord);
        TextView answer = findViewById(R.id.answer);
        EditText input = findViewById(R.id.solution);

        //gray out unavailable buttons
        Button previousButton = findViewById(R.id.previousButton);
        Button checkButton = findViewById(R.id.checkButton);
        Button nextButton = findViewById(R.id.nextButton);

        if(currentPosition==0){
            previousButton.setVisibility(View.INVISIBLE);
//            previousButton.setAlpha(0.5f);
//            previousButton.setClickable(false);
        }else{
            previousButton.setVisibility(View.VISIBLE);
//            previousButton.setAlpha(1f);
//            previousButton.setClickable(true);
        }

        if(currentPosition==flashcardList.size()-1){
            nextButton.setVisibility(View.INVISIBLE);
//            nextButton.setAlpha(0.5f);
//            nextButton.setClickable(false);
        }else{
            nextButton.setVisibility(View.VISIBLE);
//            nextButton.setAlpha(1f);
//            nextButton.setClickable(true);
        }

        //re-print background colour and show answer
        if(flashcardList.get(currentPosition).getDone()==1){
            getWindow().getDecorView().setBackgroundResource(R.color.lightGreen);
            answer.setText(getResources().getString(R.string.answer,flashcardList.get(currentPosition).getWordB()));
            answer.setVisibility(View.VISIBLE);
            input.setVisibility(View.INVISIBLE);
        }else{
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            answer.setVisibility(View.INVISIBLE);
            input.setVisibility(View.VISIBLE);
        }


        int flashcardType = flashcardList.get(currentPosition).getType();
        switch(flashcardType){
            case Flashcard.TRANSLATE:
                background.setBackgroundResource(R.color.veryLightGray);
                background.setImageBitmap(null);
                hintWord.setText(this.flashcardList.get(currentPosition).getWordA());
                hintWord.setVisibility(View.VISIBLE);
                break;

            case Flashcard.RELATE:
                byte[] imageBytes = this.flashcardList.get(currentPosition).getImage();
                background.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length));
                hintWord.setVisibility(View.INVISIBLE);
                break;

        }



    }

    private void reloadFlashcards(){
        //buscar en la db
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<Flashcard>> result = executorService.submit(new Callable<List<Flashcard>>() {
            @Override
            public List<Flashcard> call() throws Exception {
                flashcardList = MainActivity.getDb().flashcardDao().getAllByCollection(collectionName);
                return flashcardList;
            }
        });
        try {
            flashcardList = result.get();
            updateFlashcardView();
        } catch (Exception e){
            flashcardList = new ArrayList<>();
        }
        executorService.shutdown();
    }

    private void correctAnswer(){
        //modify db and background
//        getWindow().getDecorView().setBackgroundResource(R.color.lightGreen);
//        TextView answer = findViewById(R.id.answer);
//        answer.setText(getResources().getString(R.string.answer,flashcardList.get(currentPosition).getWordB()));
//        answer.setVisibility(View.VISIBLE);


        //modify db entry
        flashcardList.get(currentPosition).setDone(1);

        final Flashcard changedFlashcard = flashcardList.get(currentPosition);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.getDb().flashcardDao().updateFlashcards(changedFlashcard);

            }
        }).start();




    }
}
