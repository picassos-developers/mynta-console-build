package app.mynta.console.android.activities.helpCentre;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import app.mynta.console.android.R;
import app.mynta.console.android.adapter.ArticlesAdapter;
import app.mynta.console.android.adapter.SectionsAdapter;
import app.mynta.console.android.constants.API;
import app.mynta.console.android.models.Articles;
import app.mynta.console.android.models.Sections;
import app.mynta.console.android.utils.Helper;
import app.mynta.console.android.utils.RequestDialog;

public class HelpActivity extends AppCompatActivity {

    // request dialog
    RequestDialog requestDialog;

    // sections
    private final List<Sections> sectionsList = new ArrayList<>();
    private SectionsAdapter sectionsAdapter;

    // articles
    private final List<Articles> articlesList = new ArrayList<>();
    private ArticlesAdapter articlesAdapter;

    private EditText searchBar;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Helper.darkMode(this);

        setContentView(R.layout.activity_help);

        // initialize request dialog
        requestDialog = new RequestDialog(this);

        // finish activity
        findViewById(R.id.go_back).setOnClickListener(v -> finish());

        // Initialize sections recyclerview
        RecyclerView sectionsRecyclerview = findViewById(R.id.recycler_sections);

        // sections adapter
        sectionsAdapter = new SectionsAdapter(sectionsList, section -> {
            Intent intent = new Intent(this, ArticlesActivity.class);
            intent.putExtra("section_id", section.getSection_id());
            intent.putExtra("section_title", section.getSection_title());
            intent.putExtra("section_description", section.getSection_description());
            startActivity(intent);
        });

        sectionsRecyclerview.setAdapter(sectionsAdapter);
        sectionsRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        // request sections
        requestSections();

        // Initialize articles recyclerview
        RecyclerView articlesRecyclerview = findViewById(R.id.recycler_articles);

        // articles adapter
        articlesAdapter = new ArticlesAdapter(articlesList, article -> {
            Intent intent = new Intent(this, ViewArticleActivity.class);
            intent.putExtra("article_id", article.getArticle_id());
            startActivity(intent);
        });

        articlesRecyclerview.setAdapter(articlesAdapter);
        articlesRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        // search bar
        searchBar = findViewById(R.id.search_bar);
        searchBar.setImeActionLabel(getString(R.string.search), KeyEvent.KEYCODE_ENTER);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                if (!TextUtils.isEmpty(searchBar.getText().toString())) {
                    sectionsList.clear();
                    sectionsAdapter.notifyDataSetChanged();
                    requestArticles(searchBar.getText().toString());
                } else {
                    articlesList.clear();
                    articlesAdapter.notifyDataSetChanged();
                    requestSections();
                }
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            }
            return false;
        });

        // Voice Search
        findViewById(R.id.voice_search).setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_help));
            try {
                startActivityForResult.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Refresh Layout
        SwipeRefreshLayout refresh = findViewById(R.id.refresh_layout);
        refresh.setOnRefreshListener(() -> {
            if (refresh.isRefreshing()) {
                refresh.setRefreshing(false);
            }

            articlesList.clear();
            articlesAdapter.notifyDataSetChanged();
            requestSections();
        });
    }

    /**
     * request help centre sections
     */
    @SuppressLint("NotifyDataSetChanged")
    private void requestSections() {
        sectionsList.clear();
        sectionsAdapter.notifyDataSetChanged();

        findViewById(R.id.internet_connection).setVisibility(View.GONE);
        requestDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, API.API_URL + API.REQUEST_HC_SECTIONS,
                response -> {

                    try {
                        JSONObject obj = new JSONObject(response);

                        JSONArray array = obj.getJSONArray("sections");

                        // Check if data are empty
                        if (array.length() == 0) {
                            findViewById(R.id.no_items).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.no_items).setVisibility(View.GONE);
                        }

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            Sections sections = new Sections(object.getInt("section_id"), object.getString("title"), object.getString("description"));
                            sectionsList.add(sections);
                            sectionsAdapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    requestDialog.dismiss();
                }, error -> {
            requestDialog.dismiss();
            findViewById(R.id.internet_connection).setVisibility(View.VISIBLE);
            findViewById(R.id.try_again).setOnClickListener(v -> requestSections());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("request", "sections");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * request search articles
     * @param query for term (query)
     */
    @SuppressLint("NotifyDataSetChanged")
    private void requestArticles(String query) {
        articlesList.clear();
        articlesAdapter.notifyDataSetChanged();

        findViewById(R.id.internet_connection).setVisibility(View.GONE);
        requestDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, API.API_URL + API.REQUEST_SEARCH_ARTICLES,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        JSONArray array = obj.getJSONArray("articles");

                        // check if articles are empty
                        if (array.length() == 0) {
                            findViewById(R.id.no_items).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.no_items).setVisibility(View.GONE);
                        }

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            Articles articles = new Articles(object.getInt("article_id"), object.getString("title"));
                            articlesList.add(articles);
                            articlesAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    requestDialog.dismiss();
                }, error -> {
            requestDialog.dismiss();
            findViewById(R.id.internet_connection).setVisibility(View.VISIBLE);
            findViewById(R.id.try_again).setOnClickListener(v -> requestArticles(searchBar.getText().toString()));
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("query", query);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @SuppressLint("NotifyDataSetChanged")
    ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result != null && result.getResultCode() == RESULT_OK) {
            ArrayList<String> callback = Objects.requireNonNull(result.getData()).getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (callback != null) {
                searchBar.setText(callback.get(0));

                sectionsList.clear();
                sectionsAdapter.notifyDataSetChanged();
                requestArticles(searchBar.getText().toString());
            }
        }
    });
}