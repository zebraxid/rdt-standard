package io.ona.rdt.presenter;

import android.widget.LinearLayout;

import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.ona.rdt.application.RDTApplication;
import io.ona.rdt.contract.RDTJsonFormFragmentContract;
import io.ona.rdt.fragment.RDTJsonFormFragment;
import io.ona.rdt.interactor.RDTJsonFormInteractor;
import io.ona.rdt.util.Constants;
import io.ona.rdt.util.StepStateConfig;
import timber.log.Timber;

import static io.ona.rdt.util.Constants.Step.BLOT_PAPER_TASK_PAGE;
import static io.ona.rdt.util.Constants.Step.MANUAL_ENTRY_EXPIRATION_PAGE;
import static io.ona.rdt.util.Constants.Step.RDT_EXPIRED_PAGE;
import static io.ona.rdt.util.Constants.Step.TAKE_IMAGE_OF_RDT_PAGE;
import static io.ona.rdt.util.Utils.isExpired;
import static io.ona.rdt.widget.RDTExpirationDateReaderFactory.conditionallyMoveToNextStep;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.getJSONArray;
import static org.smartregister.util.JsonFormUtils.value;

/**
 * Created by Vincent Karuri on 19/06/2019
 */
public class RDTJsonFormFragmentPresenter extends JsonFormFragmentPresenter implements RDTJsonFormFragmentContract.Presenter {

    private RDTJsonFormInteractor interactor;
    private RDTJsonFormFragmentContract.View rdtFormFragment;

    public RDTJsonFormFragmentPresenter(RDTJsonFormFragmentContract.View rdtFormFragment, JsonFormInteractor jsonFormInteractor) {
        super((JsonFormFragment) rdtFormFragment, jsonFormInteractor);
        this.interactor = (RDTJsonFormInteractor) jsonFormInteractor;
        this.rdtFormFragment = rdtFormFragment;
    }

    @Override
    public boolean onNextClick(LinearLayout mainView) {
        checkAndStopCountdownAlarm();
        this.validateAndWriteValues();
        boolean validateOnSubmit = this.validateOnSubmit();
        if (validateOnSubmit || this.isFormValid()) {
            return this.moveToNextStep();
        } else {
            this.getView().showSnackBar(this.getView().getContext().getResources().getString(com.vijay.jsonwizard.R.string.json_form_on_next_error_msg));
            return false;
        }
    }

    private boolean moveToNextStep() {
        if (hasNextStep()) {
            JsonFormFragment next = RDTJsonFormFragment.getFormFragment(getNextStep());
            this.getView().hideKeyBoard();
            this.getView().transactThis(next);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasNextStep() {
        return !getNextStep().isEmpty();
    }

    private String getNextStep() {
        return mStepDetails.optString("next");
    }

    public void moveToNextStep(String stepName) {
        JsonFormFragment next = RDTJsonFormFragment.getFormFragment(stepName);
        this.getView().hideKeyBoard();
        this.getView().transactThis(next);
    }

    @Override
    public void setUpToolBar() {
        super.setUpToolBar();
        getView().updateVisibilityOfNextAndSave(false, false);
    }

    @Override
    public void saveForm() throws JSONException {
        interactor.saveForm(new JSONObject(getView().getCurrentJsonState()));
    }

    @Override
    public void performNextButtonAction(String currentStep, Object isSubmit) {
        try {
            StepStateConfig stepStateConfig = RDTApplication.getInstance().getStepStateConfiguration();
            if (isCurrentStep(stepStateConfig, BLOT_PAPER_TASK_PAGE, currentStep)) {
                String rdtType = rdtFormFragment.getRDTType();
                if (Constants.RDTType.CARESTART_RDT.equals(rdtType)) {
                    JsonFormFragment nextFragment = RDTJsonFormFragment.getFormFragment(
                            stepStateConfig.getStepStateObj().optString(TAKE_IMAGE_OF_RDT_PAGE));

                    rdtFormFragment.transactFragment(nextFragment);
                } else {
                    rdtFormFragment.moveToNextStep();
                }
            } else if (isCurrentStep(stepStateConfig, RDT_EXPIRED_PAGE, currentStep)) {
                saveForm();
                rdtFormFragment.moveToNextStep();
            } else if (isCurrentStep(stepStateConfig, MANUAL_ENTRY_EXPIRATION_PAGE, currentStep)) {
                JsonFormFragment formFragment = (JsonFormFragment) rdtFormFragment;
                String dateStr =  value(getJSONArray(formFragment.getStep(stepStateConfig.getStepStateObj()
                        .optString(MANUAL_ENTRY_EXPIRATION_PAGE)), FIELDS), "", "");
                if (StringUtils.isNotBlank(dateStr)) {
                    Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
                    conditionallyMoveToNextStep(formFragment, stepStateConfig, isExpired(date));
                }
            } else if (isSubmit != null && Boolean.valueOf(isSubmit.toString())) {
                rdtFormFragment.saveForm();
            } else {
                rdtFormFragment.moveToNextStep();
            }
        } catch (JSONException e) {
            Timber.e(e);
            return;
        } catch (ParseException e) {
            Timber.e(e);
        }
    }

    private boolean isCurrentStep(StepStateConfig stepStateConfig, String key, String currentStep) {
        return stepStateConfig.getStepStateObj().optString(key).equals(currentStep);
    }
}
