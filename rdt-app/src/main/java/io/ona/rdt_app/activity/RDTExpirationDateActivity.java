package io.ona.rdt_app.activity;

import android.content.Intent;

import edu.washington.cs.ubicomplab.rdt_reader.ExpirationDateActivity;

import static io.ona.rdt_app.util.Constants.EXPIRATION_DATE_RESULT;

/**
 * Created by Vincent Karuri on 21/06/2019
 */
public class RDTExpirationDateActivity extends ExpirationDateActivity {
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onResult(boolean isValid) {
        super.onResult(isValid);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXPIRATION_DATE_RESULT, isValid);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}