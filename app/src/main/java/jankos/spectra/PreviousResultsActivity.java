package jankos.spectra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PreviousResultsActivity extends AppCompatActivity {

    Config config = Config.GetInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_results);
    }
}
