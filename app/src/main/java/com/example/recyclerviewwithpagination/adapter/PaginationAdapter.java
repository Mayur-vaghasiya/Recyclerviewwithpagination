package com.example.recyclerviewwithpagination.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.recyclerviewwithpagination.R;
import com.example.recyclerviewwithpagination.model.DataModel;
import com.example.recyclerviewwithpagination.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private List<DataModel> movieResults;
    private List<DataModel> listOriginResult;
    private Context context;

    private boolean isLoadingAdded = false;

    public PaginationAdapter(Context context, List<DataModel> movieResults) {
        this.context = context;
        this.movieResults = movieResults;
        // this.movieResults = new ArrayList<>();
        this.listOriginResult = new ArrayList<DataModel>();
        this.listOriginResult.addAll(movieResults);
    }


    public List<DataModel> getMovies() {
        return movieResults;
    }

    public void setMovies(ArrayList<DataModel> movieResults) {
        this.movieResults = movieResults;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.item_list, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        DataModel result = movieResults.get(position); // Movie

        switch (getItemViewType(position)) {
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.firstName.setText(result.getFirstName());
                movieVH.lastName.setText(result.getLastName());
                movieVH.maileId.setText(result.getEmail());


                Glide
                        .with(context)
                        .load(result.getAvatar())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                // TODO: 08/11/16 handle failure
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // image ready, hide progress now
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;   // return false if you want Glide to handle everything else.
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                        .centerCrop()
                        .crossFade()
                        .into(movieVH.mPosterImg);

                break;

            case LOADING:
//                Do nothing
                break;
        }

    }

    @Override
    public int getItemCount() {
        return movieResults == null ? 0 : movieResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == movieResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */

    // Filter Class
    public void filters(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        movieResults.clear();
        if (charText.length() == 0) {
            movieResults.addAll(listOriginResult);
        } else {
            for (DataModel wp : listOriginResult) {
                if (wp.getFirstName().toLowerCase(Locale.getDefault()).contains(charText) ||
                        wp.getLastName().toLowerCase(Locale.getDefault()).contains(charText) ||
                        wp.getEmail().toLowerCase(Locale.getDefault()).contains(charText)) {
                    movieResults.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void add(DataModel r) {
        movieResults.add(r);
        notifyItemInserted(movieResults.size() - 1);
    }

    public void addAll(List<DataModel> moveResults) {
        for (DataModel result : moveResults) {
            add(result);
        }
    }

    public void remove(DataModel r) {
        int position = movieResults.indexOf(r);
        if (position > -1) {
            movieResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new DataModel());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = movieResults.size() - 1;
        DataModel result = getItem(position);

        if (result != null) {
            movieResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public DataModel getItem(int position) {
        return movieResults.get(position);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

        /**
         * Main list's content ViewHolder
         */
        protected class MovieVH extends RecyclerView.ViewHolder {
            private AppCompatTextView firstName, lastName, maileId;

            private ImageView mPosterImg;
            private ProgressBar mProgress;

            public MovieVH(View itemView) {
                super(itemView);

                firstName = (AppCompatTextView) itemView.findViewById(R.id.first_name);
                lastName = (AppCompatTextView) itemView.findViewById(R.id.last_name);
                maileId = (AppCompatTextView) itemView.findViewById(R.id.email_id);
                mPosterImg = (ImageView) itemView.findViewById(R.id.movie_poster);
                mProgress = (ProgressBar) itemView.findViewById(R.id.movie_progress);
            }
        }


        protected class LoadingVH extends RecyclerView.ViewHolder {

            public LoadingVH(View itemView) {
                super(itemView);
            }
        }


    }
