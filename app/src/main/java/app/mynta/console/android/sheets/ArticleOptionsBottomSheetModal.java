package app.mynta.console.android.sheets;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import app.mynta.console.android.R;
import app.mynta.console.android.utils.IntentHandler;
import app.mynta.console.android.utils.Toasto;

public class ArticleOptionsBottomSheetModal extends BottomSheetDialogFragment {

    View view;

    public ArticleOptionsBottomSheetModal() {

    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.article_options_bottom_sheet_modal, container, false);

        // article title
        TextView articleTitle = view.findViewById(R.id.article_title);
        articleTitle.setText(requireArguments().getString("article_title"));

        // article author
        TextView articleAuthor = view.findViewById(R.id.article_author);
        articleAuthor.setText(getString(R.string.posted_by) + " " + requireArguments().getString("article_author"));

        // facebook link
        view.findViewById(R.id.facebook_link).setOnClickListener(v -> IntentHandler.handleWeb(requireContext(), "https://facebook.com/PicassosTeam"));

        // instagram link
        view.findViewById(R.id.instagram_link).setOnClickListener(v -> IntentHandler.handleWeb(requireContext(), "https://instagram.com/PicassosTeam"));

        // youtube link
        view.findViewById(R.id.youtube_link).setOnClickListener(v -> IntentHandler.handleWeb(requireContext(), "https://youtube.com/channel/UC2Oos3JPCcWtRxCa5nCHZHw"));

        // copy link
        view.findViewById(R.id.copy_link).setOnClickListener(v -> {
            String articleUrl = "https://console.themintapp.com/hc/en-us/articles?ai=" + requireArguments().getInt("article_id") + "&s=" + requireArguments().getString("article_title");
            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText(requireArguments().getString("article_title"), articleUrl);
            clipboardManager.setPrimaryClip(data);
            Toasto.show_toast(requireContext(), getString(R.string.link_copied), 0, 0);
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
