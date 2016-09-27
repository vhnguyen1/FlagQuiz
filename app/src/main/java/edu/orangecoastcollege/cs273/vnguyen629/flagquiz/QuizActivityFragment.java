package edu.orangecoastcollege.cs273.vnguyen629.flagquiz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;

/**
 * QuizActivityFragment contains the Flag Quiz logic (correct/incorrect/statistics).
 * It also handles the delay of guessing correctly, so user can see "Correct!" message
 * before the next flag is displayed.
 */
public class QuizActivityFragment extends Fragment {
    /*
    public QuizActivityFragment() {
    }
    */

    // String used when logging error messages
    private static final String TAG = "FlagQuiz Activity";

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList; // flag file names
    private List<String> quizCountriesList; // countries in current quiz
    private Set<String> regionSet; // world regions in current quiz
    private String correctAnswer; // correct country for the current flag
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct guesses
    private int guessRows; // number of rows displaying guess Buttons
    private SecureRandom random; //used to randomize the quiz
    private Handler handler; // used to delay loading next flag

    private TextView questionNumberTextView; // shows current question #
    private ImageView flagImageView; // displays a flag
    private LinearLayout[] guessLinearLayout; // rows of answer Buttons
    private TextView answerTextView; // displays correct answer

    /**
     * Configures the QuizActivityFragment when its View is created
     * @param inflater The layout inflater
     * @param container The view group contain in which the fragment resides
     * @param savedInstanceState Any saves state to restore in this fragment
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.fragment_quiz, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // get reference to GUI components
        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayout = new LinearLayout[4];
        guessLinearLayout[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayout[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayout[2] =
                (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayout[3] =
                (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // configure listeners for the guess Buttons
        for (LinearLayout row : guessLinearLayout) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildCount(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        // set questionNumberTextView's text
        questionNumberTextView.setText(
                getString(R.string.question, 1, FLAGS_IN_QUIZ);
        return view;
        //return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    /**
     * updateGuessRows is called from QuizActivity when the app is launched and each time
     * the user changes the number of guess buttons to display with each flag.
     * @param SharedPreferences The shared preferences from preferences.xml
     */
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // get the number of guess buttons that should be displayed
        String choices =
                sharedPreferences.getString(QuizActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // hide all guess button LinearLayouts
        for (LinearLayout layout : guessLinearLayout)
            layout.setVisibility(View.GONE);

        // display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayout[row].setVisibility(View.VISIBLE);
    }

    /**
     * Updates the world regions for the quiz based on valyes in SharedPReferences.
     * @param sharedPreferences The shared preferences defined in preferences.xml
     */
    public void updateRegions(SharedPreferences sharedPreferences) {
        regionSet =
                sharedPreferences.getStringSet(QuizActivity.REGIONS, null);
    }

    /**
     * Configure and start up a new quiz based on the settings.
     */
    public void resetQuiz() {
        // use AssetManager to get image file names for eneabled regions
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear(); // empty list of image file names

        try {
            // loop through each region
            for (String region : regionSet) {
                // get a list of all flag image files in this region
                String[] paths = assets.list(region);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // reset the number of correct answers made
        totalGuesses = 0; // reset the total number of guesses the user made
        quizCountriesList.clear(); // clear prior list of quiz countries

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        // add FLAGS_IN_QUIZ random file names to the quizCountriesList
        while (flagCounter <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            // get the random file name
            String fileName = fileNameList.get(randomIndex);

            // if the region is enabled and it hasn't already been chosen
            if (!quizCountriesList.contains(fileName)) {
                quizCountriesList.add(fileName); // add the file to the list
                flagCounter++;
            }
        }

        loadNextFlag(); // start the quiz by loading the first flag
    }

    /**
     * After user guesses a flag correctly, load the next flag.
     */
    private void loadNextFlag() {
        // get file name of the next flag and remove it from the list
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // update the correct answer
        answerTextView.setText(""); // clear answerTextView

        // display current question number
        questionNumberTextView.setText(getString(
                R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));

        // extract the region from the next image's name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // get an InputStream to the asset representing the next flag
        // and try to use the inputStream
        try (InputStream stream =
                     assets.open(region + "/" + nextImage + ".png")) {
            // load the asset as a Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
        }
        catch (IOException exception) {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // add 2, 4, or 8 guess Buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++) {
            // place Buttons in currentTableRow
            for (int column = 0;
                    column < guessLinearLayout[row].getChildCount();
                    column++) {
                // get reference to Button to configure
                Button newGuessButton =
                        (Button) guessLinearLayout[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessButton's text
                String fileName = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }

        // randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayout[row]; // get the row
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    /**
     * Called when a guess button is clicked. This listener is used for all buttons
     * in the flag quiz.
     */
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            totalGuesses++; // increments the number of guesses the user has made

            if (guess.equals(answer)) { // if the guess is correct
                correctAnswers++; // increment the number of correct answers

                // display correct answer in green text
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer,
                                getContext().getTheme()));

                disableButtons(); // disable all guess Buttons

                // if the user has correctly identified FLAGS_IN_QUIZ flags
                if (correctAnswers == FLAGS_IN_QUIZ) {
                    // DialogFragment to display quiz state and start new quiz
                    DialogFragment quizResults =
                            new DialogFragment() {
                                // create an AlertDialog and return it
                                @Override
                                public Dialog onCreateDialog(Bundle bundle) {
                                    AlertDialog.Builder builder =
                                            new AlertDialog.Builder(getActivity());
                                    builder.setMessage(
                                            getString(R.string.results,
                                                    totalGuesses,
                                                    (1000 / (double) totalGuesses)));
                                    // "Reset Quiz" Button
                                    builder.setPositiveButton(R.string.reset_quiz,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                   int id) {
                                                    resetQuiz();
                                                }
                                            }
                                    );

                                    return builder.create(); // return the AlertDialog
                                }
                            };
                }
            }
        }
    };
}
