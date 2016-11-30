package io.xjhub.gitview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RepoListAdapter extends ArrayAdapter<Api.Repo> {

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView description;
    }

    public RepoListAdapter(Context context, List<Api.Repo> repoList) {
        super(context, R.layout.fragment_repo_row, repoList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Api.Repo repo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.fragment_repo_row, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.description = (TextView) convertView.findViewById(R.id.tvDescription);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.name.setText(repo.name);
        viewHolder.description.setText(repo.description);
        // Return the completed view to render on screen
        return convertView;
    }

}
