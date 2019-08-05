package io.ona.rdt_app.presenter;

import android.app.Activity;

import org.mockito.Mockito;

import io.ona.rdt_app.contract.PatientRegisterActivityContract;

/**
 * Created by Vincent Karuri on 02/08/2019
 */
public class PatientRegisterActivityStub implements PatientRegisterActivityContract.View {
    PatientRegisterFragmentPresenter fragmentPresenter;

    @Override
    public void openDrawerLayout() {
        // do nothing
    }

    @Override
    public void closeDrawerLayout() {
        // do nothing
    }

    @Override
    public PatientRegisterFragmentPresenter getRegisterFragmentPresenter() {
        if (fragmentPresenter == null) {
            fragmentPresenter = Mockito.mock(PatientRegisterFragmentPresenter.class);
        }
        return fragmentPresenter;
    }

    @Override
    public void onFormSaved() {
        // do nothing
    }
}
