package com.muflone.android.django_hotels.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.muflone.android.django_hotels.R;

import java.util.Calendar;
import java.util.Locale;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutFragment extends Fragment {
    private Context context;

    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return new AboutPage(this.context)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription(String.format(Locale.ROOT,
                        "%s %s\n%s",
                        this.getString(R.string.app_name),
                        this.getString(R.string.app_version),
                        this.getString(R.string.app_description)))
                .addPlayStore(this.getString(R.string.about_app_playstore))
                .addGitHub(this.getString(R.string.about_app_github))
                // Contacts section
                .addGroup(this.getString(R.string.about_contacts))
                .addEmail(this.getString(R.string.author_email))
                .addWebsite(this.getString(R.string.author_web))
                .addTwitter(this.getString(R.string.author_twitter))
                // License section
                .addGroup(this.getString(R.string.about_license))
                .addItem(this.getCopyRightsElement())
                .addWebsite(this.getString(R.string.about_license_url),
                        this.getString(R.string.about_app_license))
                // Credits section
                .addGroup(this.getString(R.string.about_credits))
                .addGitHub(this.getString(R.string.about_zxing_url),
                        this.getString(R.string.about_zxing_title))
                .addGitHub(this.getString(R.string.about_about_url),
                        this.getString(R.string.about_about_title))
                .addGitHub(this.getString(R.string.about_guava_url),
                        this.getString(R.string.about_guava_title))
                .addGitHub(this.getString(R.string.about_numberprogressbar_url),
                        this.getString(R.string.about_numberprogressbar_title))
                .create();
    }

    private Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(this.getString(R.string.about_app_copyright),
                this.getString(R.string.author_name),
                Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.ic_copyright);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.START);
        copyRightsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, copyrights, Toast.LENGTH_SHORT).show();
            }
        });
        return copyRightsElement;
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }
}
