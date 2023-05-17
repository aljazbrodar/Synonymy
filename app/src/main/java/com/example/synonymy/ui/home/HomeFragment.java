package com.example.synonymy.ui.home;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;


import com.example.synonymy.R;
import com.example.synonymy.databinding.FragmentHomeBinding;
import com.example.synonymy.ui.slideshow.SlideshowFragment;

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

public class HomeFragment extends Fragment  {
    public  String[] tab = new String[2];
    private Button mButton;
    private Button defButton;
    private Button copy1;
    private Button copy2;
    private Button copy3;
    private Button copy4;
    private Button copy5;

    private Button save1;
    private Button save2;
    private Button save3;
    private Button save4;
    private Button save5;
    private static final String CLOUD_FUNCTION_URL = "https://us-central1-ai-project-386614.cloudfunctions.net/getSynonyms";
    String token = System.getenv("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");
    public interface DefinitionTaskCallback {
        void onTaskComplete(String[] tab);
    }
    public class DefinitionTask extends AsyncTask<String, Void, String> {
        private HomeFragment.DefinitionTaskCallback callback;
        public DefinitionTask(HomeFragment.DefinitionTaskCallback callback) {
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
    private FragmentHomeBinding binding;
    public class SynonymsTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                String word = params[0];
                MediaType mediaType = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                String url = "https://us-central1-ai-project-386614.cloudfunctions.net/getSynonyms";
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
            Log.d("SynonymsTask", "Result: " + result);
            String[] arr = result.split(",");
            // Handle the result here
            if (result != null) {
                for (int i = 0; i < arr.length && i < 5; i++) {
                    String twId = "textView" + (i + 1);
                    int resId = getResources().getIdentifier(twId, "id", getContext().getPackageName());
                    //**
                    View root = binding.getRoot();
                    TextView tw = root.findViewById(resId);
                    if (tw != null) {
                        tw.setText(arr[i].toLowerCase());
                    } else {
                        Log.e("SynonymsTask", "TextView with ID " + twId + " not found!");
                    }
                }
            } else {
                // Handle the exception here
            }
        }
    }

    private void showInfoModal(String word) {
        HomeFragment.DefinitionTaskCallback callback = new HomeFragment.DefinitionTaskCallback() {
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

        HomeFragment.DefinitionTask definitionTask = new HomeFragment.DefinitionTask(callback);
        definitionTask.execute(word);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        defButton = root.findViewById(R.id.get_info);
        defButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Handle info button click
                TextView twGtext = root.findViewById(R.id.editTextText);
                showInfoModal(twGtext.getText().toString());
            }
        });
        mButton = root.findViewById(R.id.button15);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define behavior to execute when button is clicked
                TextView myTextView = root.findViewById(R.id.editTextText);
                Log.d("SynonymsTask", myTextView.getText().toString());
                String userInput = myTextView.getText().toString();

                HomeFragment.SynonymsTask synonymsTask = new HomeFragment.SynonymsTask();
                synonymsTask.execute(userInput);
                try{
                    File file = new File(getContext().getFilesDir(), ".history.txt");
                    PrintWriter pw = new PrintWriter(new FileWriter(file, true));
                    pw.println(userInput);
                    pw.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        //copy buttons on click functionality code:
        copy1 = root.findViewById(R.id.button1);
        copy1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define behavior to execute when button is clicked
                TextView oneTextView = root.findViewById(R.id.textView1);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", oneTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        copy2 = root.findViewById(R.id.button2);
        copy2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define behavior to execute when button is clicked
                TextView oneTextView = root.findViewById(R.id.textView2);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", oneTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        copy3 = root.findViewById(R.id.button3);
        copy3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define behavior to execute when button is clicked
                TextView oneTextView = root.findViewById(R.id.textView3);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", oneTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        copy4 = root.findViewById(R.id.button4);
        copy4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define behavior to execute when button is clicked
                TextView oneTextView = root.findViewById(R.id.textView4);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", oneTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        copy5 = root.findViewById(R.id.button5);
        copy5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define behavior to execute when button is clicked
                TextView oneTextView = root.findViewById(R.id.textView5);
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", oneTextView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        File file = new File(getContext().getFilesDir(), ".vocab.txt");
        save1 = root.findViewById(R.id.button6);
        save1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintWriter pw = null;
                //BufferedReader br = null;
                try {
                    TextView tw1 = root.findViewById(R.id.textView1);

                    pw = new PrintWriter(new FileWriter(file, true));
                    pw.println(tw1.getText().toString());
                    pw.close();
                    /*br = new BufferedReader(new FileReader(file));
                    while (br.ready()){
                        Log.d("ReadTest", br.readLine());
                    }
                    br.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the PrintWriter when done
                    if (pw != null) {
                        pw.close();
                    }
                }

            }
        });
        save2 = root.findViewById(R.id.button7);
        save2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintWriter pw = null;
                //BufferedReader br = null;
                try {
                    TextView tw1 = root.findViewById(R.id.textView2);

                    pw = new PrintWriter(new FileWriter(file, true));
                    pw.println(tw1.getText().toString());
                    pw.close();
                    /*br = new BufferedReader(new FileReader(file));
                    while (br.ready()){
                        Log.d("ReadTest", br.readLine());
                    }
                    br.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the PrintWriter when done
                    if (pw != null) {
                        pw.close();
                    }
                }

            }
        });
        save3 = root.findViewById(R.id.button8);
        save3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintWriter pw = null;
                //BufferedReader br = null;
                try {
                    TextView tw1 = root.findViewById(R.id.textView3);

                    pw = new PrintWriter(new FileWriter(file, true));
                    pw.println(tw1.getText().toString());
                    pw.close();
                    /*br = new BufferedReader(new FileReader(file));
                    while (br.ready()){
                        Log.d("ReadTest", br.readLine());
                    }
                    br.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the PrintWriter when done
                    if (pw != null) {
                        pw.close();
                    }
                }

            }
        });
        save4 = root.findViewById(R.id.button9);
        save4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintWriter pw = null;
                //BufferedReader br = null;
                try {
                    TextView tw1 = root.findViewById(R.id.textView4);

                    pw = new PrintWriter(new FileWriter(file, true));
                    pw.println(tw1.getText().toString());
                    pw.close();
                    /*br = new BufferedReader(new FileReader(file));
                    while (br.ready()){
                        Log.d("ReadTest", br.readLine());
                    }
                    br.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the PrintWriter when done
                    if (pw != null) {
                        pw.close();
                    }
                }

            }
        });
        save5 = root.findViewById(R.id.button10);
        save5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintWriter pw = null;
                //BufferedReader br = null;
                try {
                    TextView tw1 = root.findViewById(R.id.textView5);

                    pw = new PrintWriter(new FileWriter(file, true));
                    pw.println(tw1.getText().toString());
                    pw.close();
                    /*br = new BufferedReader(new FileReader(file));
                    while (br.ready()){
                        Log.d("ReadTest", br.readLine());
                    }
                    br.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // Close the PrintWriter when done
                    if (pw != null) {
                        pw.close();
                    }
                }

            }
        });

        return root;
    }


@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}