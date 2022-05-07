package app.mynta.console.android.sheets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.mynta.console.android.R;
import app.mynta.console.android.constants.API;
import app.mynta.console.android.sharedPreferences.ConsolePreferences;
import app.mynta.console.android.utils.RequestDialog;
import app.mynta.console.android.utils.Toasto;

public class FirebaseSettingsBottomSheetModal extends BottomSheetDialogFragment {
    View view;
    RequestDialog requestDialog;
    private ConsolePreferences consolePreferences;

    private EditText serverKey;

    public FirebaseSettingsBottomSheetModal() {

    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.firebase_settings_bottom_sheet_modal, container, false);

        consolePreferences = new ConsolePreferences(requireContext());

        // initialize request dialog
        requestDialog = new RequestDialog(requireContext());

        // firebase fields
        serverKey = view.findViewById(R.id.firebase_server_key);
        serverKey.addTextChangedListener(onValueChange);

        // save settings
        view.findViewById(R.id.save_settings).setOnClickListener(v -> {
            // validate firebase settings
            if (!TextUtils.isEmpty(serverKey.getText().toString())) {
                requestSaveSettings(serverKey.getText().toString());
            } else {
                Toasto.show_toast(requireContext(), getString(R.string.firebase_server_key_empty), 1, 2);
            }
        });

        // request settings
        requestSettings();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * request firebase settings
     */
    private void requestSettings() {
        view.findViewById(R.id.save_settings).setEnabled(false);
        StringRequest request = new StringRequest(Request.Method.POST, API.API_URL + API.REQUEST_FIREBASE_SETTINGS,
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONObject root = object.getJSONObject("settings");

                        serverKey.setText(root.getString("server_key"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    view.findViewById(R.id.save_settings).setEnabled(true);
                }, error -> {
            view.findViewById(R.id.save_settings).setEnabled(true);
            Toasto.show_toast(requireContext(), getString(R.string.settings_saved), 0, 0);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("secret_api_key", consolePreferences.loadSecretAPIKey());
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    /**
     * request save firebase settings
     * @param server_key for server key
     */
    private void requestSaveSettings(String server_key) {
        if (consolePreferences.loadSecretAPIKey().equals("demo")) {
            Toasto.show_toast(requireContext(), getString(R.string.demo_project), 1, 0);
        } else {
            requestDialog.show();

            StringRequest request = new StringRequest(Request.Method.POST, API.API_URL + API.REQUEST_SAVE_FIREBASE_SETTINGS,
                    response -> {
                        if (response.equals("200")) {
                            Toasto.show_toast(requireContext(), getString(R.string.settings_saved), 1, 0);
                            dismiss();
                        } else {
                            Toasto.show_toast(requireContext(), getString(R.string.unknown_issue), 1, 1);
                        }
                        requestDialog.dismiss();
                    }, error -> {
                requestDialog.dismiss();
                Toasto.show_toast(requireActivity().getApplicationContext(), getString(R.string.unknown_issue), 1, 1);
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("secret_api_key", consolePreferences.loadSecretAPIKey());
                    params.put("server_key", server_key);
                    return params;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        }
    }

    /**
     * on fields value change, update
     * button activity
     */
    private final TextWatcher onValueChange = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @SuppressLint("SetTextI18n")
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            view.findViewById(R.id.save_settings).setEnabled(!TextUtils.isEmpty(serverKey.getText().toString()));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}
