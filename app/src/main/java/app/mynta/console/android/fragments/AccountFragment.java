package app.mynta.console.android.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import app.mynta.console.android.R;
import app.mynta.console.android.activities.projects.ProjectsActivity;
import app.mynta.console.android.activities.about.MyTicketsActivity;
import app.mynta.console.android.activities.about.PurchasesActivity;
import app.mynta.console.android.models.viewModel.SharedViewModel;
import app.mynta.console.android.sharedPreferences.ConsolePreferences;
import app.mynta.console.android.sheets.AppSettingsBottomSheetModal;
import app.mynta.console.android.utils.RequestDialog;

public class AccountFragment extends Fragment {
    SharedViewModel sharedViewModel;

    Bundle bundle;
    View view;

    RequestDialog requestDialog;
    private ConsolePreferences consolePreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        // initialize bundle & shared preferences
        bundle = new Bundle();
        consolePreferences = new ConsolePreferences(requireContext());

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getRequestCode().observe(requireActivity(), item -> {

        });

        // account settings
        view.findViewById(R.id.account_settings).setOnClickListener(v -> {
            AppSettingsBottomSheetModal appSettingsBottomSheetModal = new AppSettingsBottomSheetModal();
            appSettingsBottomSheetModal.show(getChildFragmentManager(), "TAG");
        });

        // tab layout
        TabLayout tab_layout = view.findViewById(R.id.profile_tab_layout);

        // add tabs
        tab_layout.addTab(tab_layout.newTab().setText("Test"));
        tab_layout.addTab(tab_layout.newTab().setText("Test 2"));

        // set tab gravity
        tab_layout.setTabGravity(TabLayout.GRAVITY_FILL);



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize request dialog
        requestDialog = new RequestDialog(requireContext());


        // Refresh Layout
        SwipeRefreshLayout refresh = view.findViewById(R.id.refresh_layout);
        refresh.setOnRefreshListener(() -> {
            if (refresh.isRefreshing()) {
                refresh.setRefreshing(false);
            }
        });
    }

    /**
     * change app language
     */
    private void chooseLanguage() {
        Dialog designsDialog = new Dialog(requireContext());

        designsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        designsDialog.setContentView(R.layout.dialog_language);

        // enable dialog cancel
        designsDialog.setCancelable(true);
        designsDialog.setOnCancelListener(dialog -> designsDialog.dismiss());

        // close dialog
        designsDialog.findViewById(R.id.dialog_close).setOnClickListener(v -> designsDialog.dismiss());

        // english language
        designsDialog.findViewById(R.id.language_english).setOnClickListener(v -> {

        });

        // arabic language
        designsDialog.findViewById(R.id.language_arabic).setOnClickListener(v -> {

        });

        if (designsDialog.getWindow() != null) {
            designsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            designsDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        designsDialog.show();
    }
}
