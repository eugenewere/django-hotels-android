package com.muflone.android.django_hotels.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.muflone.android.django_hotels.PDFCreator;
import com.muflone.android.django_hotels.R;
import com.muflone.android.django_hotels.Settings;
import com.muflone.android.django_hotels.Singleton;
import com.muflone.android.django_hotels.commands.CommandConstants;
import com.muflone.android.django_hotels.tasks.TaskReportInterface;
import com.muflone.android.django_hotels.tasks.TaskReportTimestampsListByDate;

import java.io.File;
import java.util.Objects;

public class ReportsFragment extends Fragment {
    private View rootLayout;
    private TextView buttonReportTimestamps;
    private TextView buttonReportDailyActivities;
    private TextView buttonReportMonthActivities;
    private WebView webReport;
    private final Singleton singleton = Singleton.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Execute START REPORTS BEGIN commands
        this.singleton.commandFactory.executeCommands(
                this.getActivity(),
                this.getContext(),
                CommandConstants.CONTEXT_START_REPORTS_BEGIN);
        // Initialize UI
        this.loadUI(inflater, Objects.requireNonNull(container));
        // Prepares output callback functions for TaskReports
        View.OnClickListener clickListener = view -> {
            String reportText = null;
            TaskReportInterface reportCallback = new TaskReportInterface() {
                @Override
                public void showHTML(String data) {
                    // Load report HTML data in WebView
                    ReportsFragment.this.webReport.loadData(data, "text/html", "utf-8");
                }

                @Override
                public void createPDF(String data) {
                    // Create PDF report from data
                    // Prepares reports output directory
                    Settings settings = ReportsFragment.this.singleton.settings;
                    File destinationDirectory = new File(
                            ReportsFragment.this.singleton.settings.context.getCacheDir() +
                                    File.separator +
                                    "reports");
                    // Create missing destination directory
                    if (! destinationDirectory.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        destinationDirectory.mkdir();
                    }
                    String destinationPath = destinationDirectory + File.separator + this.getClass().getSimpleName() + ".pdf";
                    try {
                        PDFCreator pdfCreator = new PDFCreator();
                        pdfCreator.pageSize = PageSize.A4;
                        pdfCreator.title = settings.context.getString(R.string.report_timestamps);
                        pdfCreator.subject = settings.context.getString(R.string.report_timestamps);
                        pdfCreator.creator = settings.getApplicationNameVersion();
                        pdfCreator.author = settings.context.getString(R.string.author_name);
                        pdfCreator.keywords = settings.getString(CommandConstants.SETTING_REPORTS_TIMESTAMPS_KEYWORDS, "");
                        if (! pdfCreator.htmlToPDF(data, destinationPath)) {
                            Log.w(this.getClass().getSimpleName(), "Unable to create PDF document");
                        }
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
            };
            if (view == this.buttonReportTimestamps) {
                TaskReportTimestampsListByDate task = new TaskReportTimestampsListByDate(reportCallback);
                task.execute();
            } else if (view == this.buttonReportDailyActivities) {
                reportText = "<html><body><h1>Daily Activities</h1></body></html>";
            } else if (view == this.buttonReportMonthActivities) {
                reportText = "<html><body><h1>Monthly Activities</h1></body></html>";
            }
            // Check if valid report and show it
            if (reportText != null) {
                ReportsFragment.this.webReport.loadData(
                        reportText, "text/html", "utf-8");
            }
        };
        this.buttonReportTimestamps.setOnClickListener(clickListener);
        this.buttonReportDailyActivities.setOnClickListener(clickListener);
        this.buttonReportMonthActivities.setOnClickListener(clickListener);
        // Set WebView zoom enabled
        this.webReport.getSettings().setBuiltInZoomControls(
                this.singleton.settings.getBoolean(CommandConstants.SETTING_REPORTS_ZOOM_ENABLE, false));
        // Set WebView zoom control
        this.webReport.getSettings().setDisplayZoomControls(
                this.singleton.settings.getBoolean(CommandConstants.SETTING_REPORTS_ZOOM_CONTROLS, false));
        // Set WebView default zoom level
        this.webReport.getSettings().setTextZoom(
                this.singleton.settings.getInteger(CommandConstants.SETTING_REPORTS_ZOOM_DEFAULT, 100));
        // Execute START REPORTS END commands
        Singleton.getInstance().commandFactory.executeCommands(
                this.getActivity(),
                this.getContext(),
                CommandConstants.CONTEXT_START_REPORTS_END);
        return this.rootLayout;
    }

    private void loadUI(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container) {
        // Inflate the layout for this fragment
        this.rootLayout = inflater.inflate(R.layout.reports_fragment, container, false);
        this.buttonReportTimestamps = this.rootLayout.findViewById(R.id.buttonReportTimestamps);
        this.buttonReportDailyActivities = this.rootLayout.findViewById(R.id.buttonReportDailyActivities);
        this.buttonReportMonthActivities = this.rootLayout.findViewById(R.id.buttonReportMonthActivities);
        this.webReport = this.rootLayout.findViewById(R.id.webReport);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
