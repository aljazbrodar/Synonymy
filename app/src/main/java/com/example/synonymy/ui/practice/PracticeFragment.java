package com.example.synonymy.ui.practice;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.synonymy.R;
import com.example.synonymy.databinding.FragmentPracticeBinding;
import com.example.synonymy.ui.slideshow.SlideshowFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PracticeFragment extends Fragment   {
    public String[] wordArray = {
            "abundant", "accurate", "amazing", "angry",
            "attractive", "beautiful", "brave", "careful", "clever",
            "comfortable", "confident", "confused", "cozy", "crazy",
            "curious", "dangerous", "delicious", "delightful", "difficult",
            "eager", "exhausted", "famous", "fearless",
            "fierce", "friendly", "generous", "gigantic", "glorious",
            "graceful", "happy", "honest", "hungry", "impressive",
            "innocent", "intelligent", "joyful", "kind", "lively",
            "lonely", "lucky", "magnificent", "mysterious", "nervous",
            "perfect", "polite", "powerful", "precious", "proud",
            "quick", "quiet", "remarkable", "responsible", "scared",
            "silly", "smart", "strong", "successful", "talented",
            "thirsty", "thoughtful", "tiny", "tricky", "unique",
            "valuable", "victorious", "warm", "wonderful",
            "worried", "witty", "yummy", "zealous", "zesty",
            "zippy", "zany", "jovial", "plentiful", "cunning",
            "hearty", "fearful", "weary", "humble", "grumpy",
            "gloomy", "bashful", "fidgety", "daring",
            "bountiful", "fierce", "gracious", "sincere", "tender"
    };

    public String[] possibleAnswers = new String[4];
    public String[][] answerRecord = new String[5][4];//word. sol, ans, boolean
    public int total_score = 0;//1-5
    public  String solution = "";
    public  String curSol = "";
    public String ranWord ="";
    public String prevWord ="";
    public static final String WEBSTER_API_KEY = "02a4f10f-a15f-43a6-8b85-54ebf3651cf4";
    public int round = 1;
    String token = System.getenv("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");
    public FragmentPracticeBinding binding;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PracticeViewModel practiceViewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
        String word = "umpire";

        DictionaryApiCall apiCall = new DictionaryApiCall(word, WEBSTER_API_KEY);
        apiCall.execute();
// Inflate the layout for this fragment
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TableLayout res_table = root.findViewById(R.id.res_table);
        ImageView vocab_img = root.findViewById(R.id.vocab_img);
        RadioButton o1 = root.findViewById(R.id.option1);
        o1.setChecked(true);
        Button start_button = root.findViewById(R.id.start_quiz);
        TextView guides = root.findViewById(R.id.guide);
        TextView the_word = root.findViewById(R.id.the_word);
        Button next_button = root.findViewById(R.id.next_btn);
        Button restart = root.findViewById(R.id.restart);
        RadioGroup rg = root.findViewById(R.id.questions_group);
        TextView rnds = root.findViewById(R.id.rounds);
        TextView score_view = root.findViewById(R.id.score_view);
        Button button = root.findViewById(R.id.next_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(round<5){
                    // Define behavior to execute when button is clicked
                    int selectedId = rg.getCheckedRadioButtonId();
                    if (selectedId != -1) {
                        RadioButton selectedRadioButton = root.findViewById(selectedId);
                        String sel = (String) selectedRadioButton.getText();
                        if(sel.equals(curSol)){
                            total_score++;
                            score_view.setText("Score: "+total_score);
                            answerRecord[round-1][3]="true";
                        }else{
                            answerRecord[round-1][3]="false";
                        }

                        answerRecord[round-1][2]=sel;
                        round++;
                        rnds.setText(round+"/5");
                        score_view.setText("Score: "+total_score);

                        //set new word... also add check with previous words, loop the array
                        ranWord = getRandWord();
                        the_word.setText("Word : "+ranWord);
                        answerRecord[round-1][0] = ranWord;
                        DictionaryApiCall apiCall = new DictionaryApiCall(ranWord, WEBSTER_API_KEY);
                        apiCall.execute();
                    }
                }else{
                    int selectedId = rg.getCheckedRadioButtonId();
                    if (selectedId != -1) {
                        RadioButton selectedRadioButton = root.findViewById(selectedId);
                        String sel = (String) selectedRadioButton.getText();
                        if(sel.equals(curSol)){
                            total_score++;
                            answerRecord[round-1][3]="true";
                        }else{
                            answerRecord[round-1][3]="false";
                        }
                        answerRecord[round-1][2]=sel;
                    }
                    rg.setVisibility(View.INVISIBLE);
                    rnds.setVisibility(View.INVISIBLE);
                    score_view.setVisibility(View.INVISIBLE);
                    next_button.setVisibility(View.INVISIBLE);
                    guides.setVisibility(View.INVISIBLE);
                    restart.setVisibility(View.VISIBLE);
                    the_word.setVisibility(View.INVISIBLE);
                    //display results
                    TextView final_score = root.findViewById(R.id.final_score);
                    res_table.setVisibility(View.VISIBLE);
                    final_score.setText("Final Score: "+total_score+"/5");

                    final_score.setVisibility(View.VISIBLE);
                    TextView r1 = root.findViewById(R.id.res1);
                    TextView r2= root.findViewById(R.id.res2);
                    TextView r3 = root.findViewById(R.id.res3);
                    TextView r4 = root.findViewById(R.id.res4);
                    TextView r5 = root.findViewById(R.id.res5);
                    r1.setText("1. Word: "+answerRecord[0][0]+", Solution: "+answerRecord[0][1]);
                    if(answerRecord[0][3].equals("true")){
                        r1.setTextColor(Color.GREEN);
                    }else{
                        r1.setTextColor(Color.RED);
                    }
                    r2.setText("2. Word: "+answerRecord[1][0]+", Solution: "+answerRecord[1][1]);
                    if(answerRecord[1][3].equals("true")){
                        r2.setTextColor(Color.GREEN);
                    }else{
                        r2.setTextColor(Color.RED);
                    }
                    r3.setText("3. Word: "+answerRecord[2][0]+", Solution: "+answerRecord[2][1]);
                    if(answerRecord[2][3].equals("true")){
                        r3.setTextColor(Color.GREEN);
                    }else{
                        r3.setTextColor(Color.RED);
                    }
                    r4.setText("4. Word: "+answerRecord[3][0]+", Solution: "+answerRecord[3][1]);
                    if(answerRecord[3][3].equals("true")){
                        r4.setTextColor(Color.GREEN);
                    }else{
                        r4.setTextColor(Color.RED);
                    }
                    r5.setText("5. Word: "+answerRecord[4][0]+", Solution: "+answerRecord[4][1]);
                    if(answerRecord[4][3].equals("true")){
                        r5.setTextColor(Color.GREEN);
                    }else{
                        r5.setTextColor(Color.RED);
                    }

                }

            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    total_score = 0;
                    round = 1;
                    rnds.setText(round+"/5");
                    score_view.setText("Score: "+total_score);
                    restart.setVisibility(View.INVISIBLE);
                    TextView final_score = root.findViewById(R.id.final_score);
                    final_score.setVisibility(View.INVISIBLE);
                    guides.setVisibility(View.VISIBLE);
                    start_button.setVisibility(View.VISIBLE);
                    vocab_img.setVisibility(View.VISIBLE);
                    the_word.setVisibility(View.INVISIBLE);
                    res_table.setVisibility(View.INVISIBLE);

                    the_word.setText("Word: ");
                }
            });

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PracticeFragment.QuizTask quizTask = new PracticeFragment.QuizTask("ran_word");
                //quizTask.execute();
                TextView guides = root.findViewById(R.id.guide);
                ranWord = getRandWord();
                the_word.setText("Word : "+ranWord);
                answerRecord[round-1][0] = ranWord;
                DictionaryApiCall apiCall = new DictionaryApiCall(ranWord, WEBSTER_API_KEY);
                apiCall.execute();
                // Define behavior to execute when button is clicked
                rg.setVisibility(View.VISIBLE);
                rnds.setVisibility(View.VISIBLE);
                guides.setVisibility(View.INVISIBLE);
                the_word.setVisibility(View.VISIBLE);
                score_view.setVisibility(View.VISIBLE);
                next_button.setVisibility(View.VISIBLE);
                start_button.setVisibility(View.INVISIBLE);
                vocab_img.setVisibility(View.INVISIBLE);

            }
        });
        return root;
    }
    private String getRandWord(){
        Random random = new Random();
        int randomIndex = random.nextInt(wordArray.length);
        return  wordArray[randomIndex];
    }
    private boolean checkPrevAns(String current){
        return  true;
    }
    public class DictionaryApiCall extends AsyncTask<Void, Void, String> {
        private static final String TAG = "DictionaryApiCall";
        private static final String BASE_URL = "https://www.dictionaryapi.com/api/v3/references/thesaurus/json/";
        private String word;
        private String apiKey;

        public DictionaryApiCall(String word, String apiKey) {
            this.word = word;
            this.apiKey = apiKey;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String apiUrl = BASE_URL + word + "?key=" + apiKey;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                InputStream inputStream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occurred during API call: " + e.getMessage());
            } finally {
                // Close connections
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing reader: " + e.getMessage());
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            View root = binding.getRoot();
            RadioGroup radioGroup = root.findViewById(R.id.questions_group);
            RadioButton o1 = root.findViewById(R.id.option1);
            RadioButton o2 = root.findViewById(R.id.option2);
            RadioButton o3 = root.findViewById(R.id.option3);
            RadioButton o4 = root.findViewById(R.id.option4);

            // Process the API response here
            try {
                JSONArray responseArray = new JSONArray(result);
                if (responseArray.length() > 0) {
                    JSONObject responseObject = responseArray.getJSONObject(0);
                    JSONObject metaObject = responseObject.getJSONObject("meta");
                    JSONArray synsArray = metaObject.getJSONArray("syns");
                    JSONArray antsArray = metaObject.getJSONArray("ants");

                    // Convert the JSONArray to a regular array
                    String[] syns = new String[synsArray.length()];
                    for (int i = 0; i < synsArray.length(); i++) {
                        JSONArray innerArray = synsArray.getJSONArray(i);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < innerArray.length(); j++) {
                            if (j > 0) {
                                stringBuilder.append(", ");
                            }
                            stringBuilder.append(innerArray.getString(j));
                        }
                        syns[i] = stringBuilder.toString();
                    }
                    String[] ants = new String[antsArray.length()];
                    for (int i = 0; i < antsArray.length(); i++) {
                        JSONArray innerArray = antsArray.getJSONArray(i);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < innerArray.length(); j++) {
                            if (j > 0) {
                                stringBuilder.append(", ");
                            }
                            stringBuilder.append(innerArray.getString(j));
                        }
                        ants[i] = stringBuilder.toString();
                    }

                    // Find the first element in syns that is not equal to the word
                    for (String syn : syns) {
                        if (!syn.equalsIgnoreCase(word)) {
                            curSol = syn.split(",")[0];
                            possibleAnswers[0]=curSol;
                            answerRecord[round-1][1] = curSol;
                            break;
                        }
                    }

                    int answerIndex = 1;
                    for (String ant : ants) {
                        if (answerIndex < 4) {
                            // Split the comma-separated values into separate words
                            String[] words = ant.split(", ");
                            for (String word : words) {
                                if (answerIndex < 4) {
                                    possibleAnswers[answerIndex] = word;
                                    answerIndex++;
                                } else {
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }

                    // Fill the remaining slots with values from the reel list if available
                    JSONArray reelArray = metaObject.optJSONArray("reel");
                    if (reelArray != null) {
                        for (int i = 0; i < reelArray.length(); i++) {
                            String reelValue = reelArray.optString(i);
                            if (answerIndex < 4) {
                                possibleAnswers[answerIndex] = reelValue;
                                answerIndex++;
                            } else {
                                break;
                            }
                        }
                    }
                    possibleAnswers = shuffleArray(possibleAnswers);
                    // Set the options
                    o1.setText(possibleAnswers[0]);
                    o2.setText(possibleAnswers[1]);
                    o3.setText(possibleAnswers[2]);
                    o4.setText(possibleAnswers[3]);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing API response: " + e.getMessage());
            }
        }


    }



    public class QuizTask extends AsyncTask<String, Void, String> {
        private String flag;

        public QuizTask(String flag) {
            this.flag = flag;
        }

        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                // Define the URL for the HTTP POST request
                String url = "https://us-central1-ai-project-386614.cloudfunctions.net/getQuiz";

                // Create an OkHttpClient instance
                OkHttpClient client = new OkHttpClient();

                // Build the HTTP POST request
                RequestBody body = RequestBody.create(null, "");
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                // Send the HTTP POST request and get the response
                Response response = client.newCall(request).execute();

                // Check if the response was successful
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Return the response body as a string
                return response.body().string();
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }


        protected void onPostExecute(String result) {
            View root = binding.getRoot();
            TextView guides = root.findViewById(R.id.guide);
            TextView the_word = root.findViewById(R.id.the_word);
            RadioGroup radioGroup = root.findViewById(R.id.questions_group);
            RadioButton o1 = root.findViewById(R.id.option1);
            RadioButton o2 = root.findViewById(R.id.option2);
            RadioButton o3 = root.findViewById(R.id.option3);
            RadioButton o4 = root.findViewById(R.id.option4);
            if (result != null) {
                Log.d("QuizTask", "Result: " + result);
                String[] resArr = result.split("\n");
                String guide = resArr[0];
                //set
                if (flag.equals("ran_word")) {
                    //the_word.setText("Word : "+guide.toLowerCase());
                    //answerRecord[round-1][0] = ranWord;
                    //DictionaryApiCall apiCall = new DictionaryApiCall(ranWord, WEBSTER_API_KEY);
                    //apiCall.execute();
                }else{
                    ranWord = guide.toLowerCase();
                    possibleAnswers[Integer.parseInt(flag)] = "ranWord";
                }
            }
        }
    }



    public static String[] shuffleArray(String[] array) {
        List<String> list = Arrays.asList(array);
        Collections.shuffle(list);
        return list.toArray(new String[list.size()]);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}