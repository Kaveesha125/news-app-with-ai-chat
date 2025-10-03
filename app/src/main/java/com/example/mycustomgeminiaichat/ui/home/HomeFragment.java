package com.example.mycustomgeminiaichat.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycustomgeminiaichat.adapter.NewsAdapter;
import com.example.mycustomgeminiaichat.api.NewsApiClient;
import com.example.mycustomgeminiaichat.api.NewsApiService;
import com.example.mycustomgeminiaichat.databinding.FragmentHomeBinding;
import com.example.mycustomgeminiaichat.model.Article;
import com.example.mycustomgeminiaichat.model.NewsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NewsAdapter newsAdapter;
    private List<Article> articleList;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private TextView textHome;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize views
        searchView = binding.searchView;
        recyclerView = binding.recyclerView;
        textHome = binding.textHome;

        // Setup RecyclerView
        articleList = new ArrayList<>();
        newsAdapter = new NewsAdapter(getContext(), articleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(newsAdapter);

        // Setup SearchView
        setupSearchView();

        return root;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    searchNews(query.trim());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchNews(String query) {
        // Hide the default text and show loading state
        textHome.setVisibility(View.GONE);

        NewsApiService apiService = NewsApiClient.getApiService();
        Call<NewsResponse> call = apiService.searchNews(query, NewsApiService.API_KEY);

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NewsResponse newsResponse = response.body();
                    List<Article> articles = newsResponse.getArticles();

                    if (articles != null && !articles.isEmpty()) {
                        articleList.clear();
                        articleList.addAll(articles);
                        newsAdapter.updateArticles(articleList);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        showNoResultsMessage();
                    }
                } else {
                    showErrorMessage("Failed to fetch news");
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                showErrorMessage("Network error: " + t.getMessage());
            }
        });
    }

    private void showNoResultsMessage() {
        textHome.setText("No news found for your search");
        textHome.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        textHome.setText("Search for news above");
        textHome.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}