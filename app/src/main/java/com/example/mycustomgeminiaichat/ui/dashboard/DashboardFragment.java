package com.example.mycustomgeminiaichat.ui.dashboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mycustomgeminiaichat.api.GeminiApiService;
import com.example.mycustomgeminiaichat.SystemPrompts;
import com.example.mycustomgeminiaichat.databinding.FragmentDashboardBinding;

import io.noties.markwon.Markwon;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private GeminiApiService apiService;
    private EditText inputMessage;
    private TextView responseText;
    private Button sendButton;
    private ProgressBar progressBar;
    private Spinner modelSpinner;
    private ImageButton copyButton;
    private Markwon markwon;
    private String lastResponse = "";

    // Available Gemini models
    private String[] geminiModels = {
            "gemini-2.5-flash",        // Fast, stable, cost-effective
            "gemini-2.5-pro",          // Most capable, stable
            "gemini-2.0-flash-001",    // Older but very stable
            "gemini-flash-latest",     // Auto-updates to latest stable
            "gemini-pro-latest"        // Auto-updates to latest Pro
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize API service
        apiService = new GeminiApiService();

        // Initialize Markwon for markdown rendering (without syntax highlighting)
        markwon = Markwon.create(requireContext());

        // Initialize UI components
        initializeViews();
        setupModelSpinner();
        setupSendButton();
        setupCopyButton();

        // Keep the original text view binding for compatibility
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private void initializeViews() {
        inputMessage = binding.inputMessage;
        responseText = binding.responseText;
        sendButton = binding.sendButton;
        progressBar = binding.progressBar;
        modelSpinner = binding.modelSpinner;
        copyButton = binding.copyButton;
    }

    private void setupModelSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            geminiModels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedModel = geminiModels[modelSpinner.getSelectedItemPosition()];

            // Combine system prompt with user message
            String fullPrompt = SystemPrompts.SYSTEM_PROMPT + "\n\nUser: " + message;

            sendMessage(selectedModel, fullPrompt);
        });
    }

    private void setupCopyButton() {
        copyButton.setOnClickListener(v -> {
            if (!lastResponse.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("AI Response", lastResponse);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Response copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String model, String message) {
        // Show loading state
        setLoadingState(true);
        markwon.setMarkdown(responseText, "Generating response...");
        copyButton.setVisibility(View.GONE);

        apiService.generateContent(model, message, new GeminiApiService.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setLoadingState(false);
                        lastResponse = response;

                        // Render markdown content
                        markwon.setMarkdown(responseText, response);

                        // Show copy button
                        copyButton.setVisibility(View.VISIBLE);

                        inputMessage.setText(""); // Clear input after sending
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        setLoadingState(false);
                        String errorMessage = "Error: " + error;
                        lastResponse = errorMessage;
                        markwon.setMarkdown(responseText, errorMessage);
                        copyButton.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        sendButton.setEnabled(!isLoading);
        inputMessage.setEnabled(!isLoading);
        modelSpinner.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}