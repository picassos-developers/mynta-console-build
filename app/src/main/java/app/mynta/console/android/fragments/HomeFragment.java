package app.mynta.console.android.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import app.mynta.console.android.R;

import app.mynta.console.android.activities.GiftActivity;
import app.mynta.console.android.sheets.ChooseNotificationProviderBottomSheetModal;
import app.mynta.console.android.activities.EditProjectActivity;
import app.mynta.console.android.activities.MainActivity;
import app.mynta.console.android.activities.MembersActivity;
import app.mynta.console.android.adapter.MembersAdapter;
import app.mynta.console.android.constants.API;
import app.mynta.console.android.libraries.showcaseview.GuideView;
import app.mynta.console.android.libraries.showcaseview.config.DismissType;
import app.mynta.console.android.libraries.showcaseview.config.Gravity;
import app.mynta.console.android.models.Members;
import app.mynta.console.android.sharedPreferences.ConsolePreferences;
import app.mynta.console.android.sheets.LaunchAppBottomSheetModal;
import app.mynta.console.android.utils.PromotionDialog;
import app.mynta.console.android.utils.RequestDialog;
import app.mynta.console.android.utils.Toasto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    View view;
    Bundle bundle;

    private RequestDialog requestDialog;
    private ConsolePreferences consolePreferences;

    // fonts
    private Typeface title, content;

    // members
    private final List<Members> membersList = new ArrayList<>();
    private MembersAdapter membersAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        // initialize request dialog
        requestDialog = new RequestDialog(requireContext());
        consolePreferences = new ConsolePreferences(requireActivity().getApplicationContext());

        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bundle = new Bundle();

        title = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/poppins_bold.ttf");
        content = Typeface.createFromAsset(requireActivity().getAssets(), "fonts/poppins_regular.ttf");

        // check promotion
        requestPromotion();

        // open navigation
        view.findViewById(R.id.open_navigation).setOnClickListener(v -> ((MainActivity) requireActivity()).openNavigation());

        requestDialog.show();

        // push notifications
        view.findViewById(R.id.push_notifications_container).setOnClickListener(v -> {
            ChooseNotificationProviderBottomSheetModal chooseNotificationProviderBottomSheetModal = new ChooseNotificationProviderBottomSheetModal();
            chooseNotificationProviderBottomSheetModal.show(getParentFragmentManager(), "TAG");
        });

        // Refresh Layout
        SwipeRefreshLayout refresh = view.findViewById(R.id.refresh_layout);
        refresh.setOnRefreshListener(() -> {
            if (refresh.isRefreshing()) {
                refresh.setRefreshing(false);
            }

            requestConfiguration();
        });

        // launch application
        view.findViewById(R.id.launch_app).setOnClickListener(v -> {
            if (consolePreferences.loadPackageName().equals("exception:error?package_name")) {
                startActivity(new Intent(requireContext(), EditProjectActivity.class));
            } else {
                try {
                    LaunchAppBottomSheetModal launchAppBottomSheetModal = new LaunchAppBottomSheetModal();
                    launchAppBottomSheetModal.show(getChildFragmentManager(), "TAG");
                } catch (NullPointerException e) {
                    Toasto.show_toast(requireContext(), requireContext().getString(R.string.invalid_package_name), 1, 1);
                    startActivity(new Intent(requireContext(), EditProjectActivity.class));
                }
            }
        });

        // Initialize members recyclerview
        RecyclerView membersRecyclerview = view.findViewById(R.id.recycler_members);

        // members adapter
        membersAdapter = new MembersAdapter(membersList, members -> startActivity(new Intent(requireContext(), MembersActivity.class)), true);
        membersRecyclerview.setAdapter(membersAdapter);
        membersRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));

        requestConfiguration();
    }

    /**
     * request configuration
     */
    @SuppressLint({"SetTexti18n", "NotifyDataSetChanged"})
    public void requestConfiguration() {
        view.findViewById(R.id.dashboard_container).setVisibility(View.VISIBLE);
        view.findViewById(R.id.internet_connection).setVisibility(View.GONE);
        requestDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, API.API_URL + API.REQUEST_CONFIGURATION,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject root = object.getJSONObject("app");

                        /* statistics start **/
                        TextView uniqueAppVisits = view.findViewById(R.id.unique_app_visits);
                        TextView totalMembers = view.findViewById(R.id.total_members);

                        uniqueAppVisits.setText(String.valueOf(root.getJSONObject("statistics").getInt("unique_app_visits")));
                        totalMembers.setText(String.valueOf(root.getJSONObject("statistics").getInt("total_members")));
                        /* statistics end **/

                        /* members container start **/
                        membersList.clear();
                        membersAdapter.notifyDataSetChanged();
                        JSONArray members = root.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            membersList.add(new Members(
                                    members.getJSONObject(i).getInt("member_id"), members.getJSONObject(i).getString("username"), members.getJSONObject(i).getString("email"), members.getJSONObject(i).getString("verified")));
                            membersAdapter.notifyDataSetChanged();
                        }
                        // check if members are empty
                        if (members.length() == 0) {
                            view.findViewById(R.id.members_container).setVisibility(View.GONE);
                        } else {
                            view.findViewById(R.id.members_container).setVisibility(View.VISIBLE);
                        }
                        // view all members
                        view.findViewById(R.id.view_all_members).setOnClickListener(v -> startActivity(new Intent(requireContext(), MembersActivity.class)));
                        /* members container end **/

                        // customization card
                        if (root.getInt("review") == 0) {
                            view.findViewById(R.id.customization_card).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.customization_card).setOnClickListener(v -> startActivity(new Intent(requireContext(), GiftActivity.class)));
                        } else {
                            view.findViewById(R.id.customization_card).setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    requestDialog.dismiss();
                }, error -> {
            requestDialog.dismiss();
            view.findViewById(R.id.dashboard_container).setVisibility(View.GONE);
            view.findViewById(R.id.internet_connection).setVisibility(View.VISIBLE);
            view.findViewById(R.id.try_again).setOnClickListener(v -> requestConfiguration());
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", consolePreferences.loadToken());
                params.put("secret_api_key", consolePreferences.loadSecretAPIKey());
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * request promotion
     */
    private void requestPromotion() {
        StringRequest request = new StringRequest(Request.Method.POST, API.API_URL + API.REQUEST_PROMOTION,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject root = object.getJSONObject("app");
                        JSONObject promotion = root.getJSONObject("promotion");

                        if (promotion.getInt("enabled") == 1) {
                            if (!promotion.getString("identifier").equals(consolePreferences.loadPromotionID())) {
                                PromotionDialog promotionDialog = new PromotionDialog(requireContext());
                                promotionDialog.show();
                                consolePreferences.setPromotionID(promotion.getString("identifier"));
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("PROMOTION", "Failed to load promotion request")) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("request", "promotion");
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /* guide start **/
    public void requestGuideSix() {
        new GuideView.Builder(requireContext())
                .setTitle(getString(R.string.guide_six_title))
                .setContentText(getString(R.string.guide_six_description))
                .setGravity(Gravity.auto)
                .setDismissType(DismissType.anywhere)
                .setTargetView(view.findViewById(R.id.launch_app))
                .setTitleTypeFace(title)
                .setContentTypeFace(content)
                .setContentTextSize(12)
                .setTitleTextSize(13)
                .setGuideListener(view -> requestGuideSeven())
                .build()
                .show();
    }

    private void requestGuideSeven() {
        new GuideView.Builder(requireContext())
                .setTitle(getString(R.string.guide_seven_title))
                .setContentText(getString(R.string.guide_seven_description))
                .setGravity(Gravity.auto)
                .setDismissType(DismissType.anywhere)
                .setTargetView(view.findViewById(R.id.launch_app))
                .setTitleTypeFace(title)
                .setContentTypeFace(content)
                .setContentTextSize(12)
                .setTitleTextSize(13)
                .setGuideListener(view -> requestGuideEight())
                .build()
                .show();
    }

    private void requestGuideEight() {
        new GuideView.Builder(requireContext())
                .setTitle(getString(R.string.guide_eight_title))
                .setContentText(getString(R.string.guide_eight_description))
                .setGravity(Gravity.auto)
                .setDismissType(DismissType.anywhere)
                .setTargetView(view.findViewById(R.id.open_navigation))
                .setTitleTypeFace(title)
                .setContentTypeFace(content)
                .setContentTextSize(12)
                .setTitleTextSize(13)
                .setGuideListener(view -> consolePreferences.setGuide(true))
                .build()
                .show();
    }
    /* guide end **/
}