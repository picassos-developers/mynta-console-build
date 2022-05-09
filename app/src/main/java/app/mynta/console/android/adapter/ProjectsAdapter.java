package app.mynta.console.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import app.mynta.console.android.R;

import app.mynta.console.android.interfaces.OnProjectClickListener;
import app.mynta.console.android.models.Projects;
import app.mynta.console.android.sharedPreferences.ConsolePreferences;

import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final ConsolePreferences consolePreferences;
    private final List<Projects> projectsList;
    private final OnProjectClickListener listener;

    public ProjectsAdapter(Context context, ConsolePreferences consolePreferences, List<Projects> projectsList, OnProjectClickListener listener) {
        this.context = context;
        this.consolePreferences = consolePreferences;
        this.projectsList = projectsList;
        this.listener = listener;
    }

    class ProjectsHolder extends RecyclerView.ViewHolder {
        View projectContainer;
        TextView name, packagename;

        ProjectsHolder(@NonNull View itemView) {
            super(itemView);
            projectContainer = itemView.findViewById(R.id.project_container);
            name = itemView.findViewById(R.id.project_name);
            packagename = itemView.findViewById(R.id.project_packagename);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void setData(Projects data) {
            name.setText(data.getName());
            packagename.setText(data.getPackagename());
            if (data.getSak().equals(consolePreferences.loadSecretAPIKey())) {
                projectContainer.setBackground(context.getDrawable(R.drawable.item_background_selected));
            } else {
                projectContainer.setBackground(context.getDrawable(R.drawable.item_background));
            }
        }

        void bind(final Projects item, final OnProjectClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_listitem, parent, false);
        return new ProjectsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Projects projects = projectsList.get(position);
        ProjectsHolder projectsHolder = (ProjectsHolder) holder;
        projectsHolder.setData(projects);
        projectsHolder.bind(projectsList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return projectsList.size();
    }
}
