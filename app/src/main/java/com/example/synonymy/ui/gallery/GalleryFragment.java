package com.example.synonymy.ui.gallery;
import com.example.synonymy.R;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import com.example.synonymy.databinding.FragmentGalleryBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private TableLayout tableLayout;
private FragmentGalleryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get a reference to the TableLayout in the layout
        tableLayout = root.findViewById(R.id.table_history);
        // Read the words from the file
        ArrayList<String> words = readWordsFromFile();
        for (String word : words) {
            TableRow row = new TableRow(getContext());

            // Create a TextView for the word
            TextView wordView = new TextView(getContext());
            wordView.setText(word);
            wordView.setGravity(Gravity.CENTER);

            TableRow.LayoutParams textLayoutParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
            textLayoutParams.setMargins(50, 0, 50, 0); // Set left and right margin
            wordView.setLayoutParams(textLayoutParams);



            row.setGravity(Gravity.CENTER);
            row.addView(wordView);
            Button button = new Button(getContext());
            button.setText("Copy");
            TableRow.LayoutParams buttonLayoutParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
            buttonLayoutParams.setMargins(50, 0, 50, 0); // Set left and right margin
            button.setLayoutParams(buttonLayoutParams);
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
            row.addView(button);
            // Add the row to the table
            tableLayout.addView(row);
        }

        Button clrAllButton = root.findViewById(R.id.clr_his);


        clrAllButton.setBackgroundColor(Color.RED);
        clrAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Delete the existing file
                    File file = new File(getContext().getFilesDir(), ".history.txt");
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
        });
        return root;
    }



    private ArrayList<String> readWordsFromFile() {
        ArrayList<String> words = new ArrayList<>();
        try {
            File file = new File(getContext().getFilesDir(), ".history.txt");
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
    private void updateTable() {
        // Get a reference to the table layout
        TableLayout table = getView().findViewById(R.id.table_history);

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
@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}