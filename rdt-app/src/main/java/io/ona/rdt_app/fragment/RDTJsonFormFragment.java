package io.ona.rdt_app.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.util.JsonFormUtils;

import io.ona.rdt_app.R;
import io.ona.rdt_app.activity.RDTJsonFormActivity;
import io.ona.rdt_app.interactor.RDTJsonFormInteractor;
import io.ona.rdt_app.presenter.RDTJsonFormFragmentPresenter;
import io.ona.rdt_app.util.Constants;
import io.ona.rdt_app.util.RDTJsonFormUtils;

import static io.ona.rdt_app.util.Constants.Form.RDT_TEST_FORM;

/**
 * Created by Vincent Karuri on 12/06/2019
 */
public class RDTJsonFormFragment extends JsonFormFragment {

    private static int currentStep;

    public static JsonFormFragment getFormFragment(String stepName) {
        String stepNum = stepName.substring(4);
        currentStep = Integer.valueOf(stepNum);
        RDTJsonFormFragment jsonFormFragment = new RDTJsonFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString("stepName", stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    protected void initializeBottomNavigation(final JSONObject step, View rootView) {
        super.initializeBottomNavigation(step, rootView);
        // Handle initialization of the countdown timer bottom navigation
        String currStep = "step" + currentStep;
        boolean buttonEnabled = true;
        if ("step12".equals(currStep)) {
            buttonEnabled = false;
        }
        setNextButtonState(rootView, buttonEnabled);

        rootView.findViewById(com.vijay.jsonwizard.R.id.previous_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(false);
            }
        });
        rootView.findViewById(com.vijay.jsonwizard.R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object isSubmit = v.getTag(R.id.submit);
                String currStep = "step" + currentStep;
                if ("step8".equals(currStep)) {
                    String rdtType = ((RDTJsonFormActivity) getActivity()).getRdtType();
                    if (Constants.CARESTART_RDT.equals(rdtType)) {
                        JsonFormFragment nextFragment = RDTJsonFormFragment.getFormFragment("step14");
                        transactThis(nextFragment);
                    } else {
                        next();
                    }
                } else if (isSubmit != null && Boolean.valueOf(isSubmit.toString())) {
                    save(false);
                    // Check if we need to continue to RDT
                    checkIfContinuingToRdt();
                } else {
                    next();
                }
            }
        });
    }

    public void checkIfContinuingToRdt() {
        if ("step1".equals("step" + currentStep)) {
            JSONArray formFields = JsonFormUtils.fields(getJsonApi().getmJSONObject());
            for (int i = 0; i < formFields.length(); i++) {
                if (formFields.optJSONObject(i).optString(JsonFormConstants.KEY).equals("conditional_save")) {
                    boolean continueToRdt = Integer.parseInt(formFields.optJSONObject(i).optString(JsonFormConstants.VALUE)) == 1;
                    if (continueToRdt) {
                        try {
                            new RDTJsonFormUtils().launchForm(getActivity(), RDT_TEST_FORM);
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void setNextButtonState(View rootView, boolean buttonEnabled) {
        Button button = rootView.findViewById(com.vijay.jsonwizard.R.id.next_button);
        button.setEnabled(buttonEnabled);
        int bgColor;
        if (!buttonEnabled) {
            bgColor = Color.parseColor("#D1D1D1");
        } else {
            bgColor = Color.parseColor("#0192D4");
        }
        Drawable background = button.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(bgColor);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(bgColor);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(bgColor);
        }
    }

    @Override
    public boolean save(boolean skipValidation) {
        return super.save(skipValidation) && presenter.isFormValid();
    }

    @Override
    protected JsonFormFragmentPresenter createPresenter() {
        presenter = new RDTJsonFormFragmentPresenter(this, new RDTJsonFormInteractor());
        return presenter;
    }
}
