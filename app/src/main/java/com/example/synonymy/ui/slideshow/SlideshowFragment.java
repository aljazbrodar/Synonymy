package com.example.synonymy.ui.slideshow;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.synonymy.R;
import com.example.synonymy.databinding.FragmentSlideshowBinding;
import com.example.synonymy.ui.home.HomeFragment;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SlideshowFragment extends Fragment {
    private TableLayout tableLayout;
    public  String[] tab = new String[2];
    private FragmentSlideshowBinding binding;
    private static final String CLOUD_FUNCTION_URL = "https://us-central1-ai-project-386614.cloudfunctions.net/getSynonyms";
    String token = System.getenv("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");
    public interface DefinitionTaskCallback {
        void onTaskComplete(String[] tab);
    }
    public class DefinitionTask extends AsyncTask<String, Void, String> {
        private DefinitionTaskCallback callback;
        public DefinitionTask(DefinitionTaskCallback callback) {
            this.callback = callback;
        }

        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                String word = params[0];
                MediaType mediaType = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                String url = "https://us-central1-ai-project-386614.cloudfunctions.net/getDefinition";
                String json = "{\"word\":\"" + word + "\"}";
                RequestBody body = RequestBody.create(json, mediaType);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "bearer " + token)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d("DefinitionTask", "Result: " + result);
                // Find the index of the newline character that separates the definition and example
                String[] parts = result.split("\n");


                    // Extract the definition and example as separate strings
                    String definition = parts[0];
                    String example = parts[1];

                    // Trim any leading or trailing whitespace from the definition and example
                    definition = definition.trim();
                    example = example.trim();
                    // Return the two in an array so I can then set them as text in the modal...

                    tab[0] = definition;
                    tab[1] = example;

            }
            callback.onTaskComplete(tab);
        }
    }
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {SlideshowViewModel slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get a reference to the TableLayout in the layout
        tableLayout = root.findViewById(R.id.table_layout);
        // Read the words from the file
        ArrayList<String> words = readWordsFromFile();
        //clear all button
/*        Button clrAllButton = root.findViewById(R.id.clear_all);
        clrAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Delete the existing file
                    File file = new File(getContext().getFilesDir(), ".vocab.txt");
                    if (file.exists()) {
                        file.delete();
                    }

                    // Create a new empty file in its place
                    file.createNewFile();

                    updateTable();
                    // Notify the user that the file has been cleared
                    Toast.makeText(getContext(), "Cleared successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/
        // Add a row to the table for each word
        for (String word : words) {
            TableRow row = new TableRow(getContext());

            // Create a TextView for the word
            TextView wordView = new TextView(getContext());
            wordView.setText(word);
            wordView.setGravity(Gravity.CENTER);
            row.addView(wordView);

            // Create three buttons for each row
            for (int i = 0; i < 3; i++) {
                Button button = new Button(getContext());
                switch(i) {
                    case 0:
                        button.setText("Copy");

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Get the text of the TextView in the same row as the button
                                TableRow row = (TableRow) v.getParent();
                                TextView wordView = (TextView) row.getChildAt(0);
                                String text = wordView.getText().toString();

                                // Copy the text to the clipboard
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("text", text);
                                clipboard.setPrimaryClip(clip);
                            }
                        });
                        break;
                    case 1:
                        button.setText("Info");
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Handle info button click
                                showInfoModal(word);
                            }
                        });
                        break;
                    case 2:
                        button.setText("Delete");
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TableRow row = (TableRow) v.getParent();
                                TextView wordView = (TextView) row.getChildAt(0);
                                String text = wordView.getText().toString();

                                try{
                                    File file = new File(getContext().getFilesDir(), ".vocab.txt");
                                    BufferedReader br = new BufferedReader(new FileReader(file));
                                    ArrayList<String> words = new ArrayList<>();
                                    String line;
                                    while ((line=br.readLine())!=null){
                                        String trimmed = line.trim();
                                        if(!trimmed.equals(text.trim())){
                                            words.add(line);
                                        }
                                    }
                                    br.close();

                                    PrintWriter pw = new PrintWriter(new FileWriter(file));
                                    for(String w : words){
                                        pw.println(w);
                                    }
                                    pw.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                // Remove the row from the table and update the list of words
                                tableLayout.removeView(row);
                            }
                        });
                        break;
                }
                row.addView(button);
            }

            // Add the row to the table
            row.setGravity(Gravity.CENTER);
            tableLayout.addView(row);
        }

        return root;
    }
    private void updateTable() {
        // Get a reference to the table layout
        TableLayout table = getView().findViewById(R.id.table_layout);

        // Clear the current table
        table.removeAllViews();

        // Get the list of words from the file
        ArrayList<String> words = readWordsFromFile();

        // Loop through the list of words and add each one to the table
        for (String word : words) {
            // Create a new table row
            TableRow row = new TableRow(getContext());

            // Create a new text view for the word and add it to the row
            TextView wordView = new TextView(getContext());
            wordView.setText(word);
            row.addView(wordView);

            // Add the row to the table
            table.addView(row);
        }
    }

    private void showInfoModal(String word) {
        SlideshowFragment.DefinitionTaskCallback callback = new SlideshowFragment.DefinitionTaskCallback() {
            @Override
            public void onTaskComplete(String[] tab) {
                // Create a dialog with a custom layout
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.modalinfo);

                // Set the text of the dialog to the word
                TextView wordView = dialog.findViewById(R.id.modalTextView);
                TextView egView = dialog.findViewById(R.id.exampleTextView);
                wordView.setText(tab[0]);
                egView.setText(tab[1]);

                // Set a click listener on the close button to dismiss the dialog
                Button closeButton = dialog.findViewById(R.id.close_button);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }
        };

        SlideshowFragment.DefinitionTask definitionTask = new SlideshowFragment.DefinitionTask(callback);
        definitionTask.execute(word);
    }

    private ArrayList<String> readWordsFromFile() {
        ArrayList<String> words = new ArrayList<>();
        try {
            File file = new File(getContext().getFilesDir(), ".vocab.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                // Add the word to the list
                words.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}