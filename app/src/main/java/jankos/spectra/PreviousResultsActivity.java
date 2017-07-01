package jankos.spectra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PreviousResultsActivity extends AppCompatActivity {

    private Config config;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_results);

        config = Config.GetInstance();
    }
}
