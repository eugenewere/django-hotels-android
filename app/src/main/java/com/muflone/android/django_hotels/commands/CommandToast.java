/*
 *     Project: Django Hotels Android
 * Description: The Android client companion app for Django Hotels
 *     Website: http://www.muflone.com/django-hotels-android/
 *      Author: Fabio Castelli (Muflone) <muflone@muflone.com>
 *   Copyright: 2018-2020 Fabio Castelli
 *     License: GPL-3+
 * Source code: https://github.com/muflone/django-hotels-android
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.muflone.android.django_hotels.commands;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.muflone.android.django_hotels.database.models.Command;

import org.json.JSONException;

public class CommandToast extends CommandBase {
    /**
     * This Command shows a Toast notification
     *
     * The command must have the following arguments:
     * message: the message to show
     * duration: the duration to use (0 for LENGTH_SHORT, 1 for LENGTH_LONG)
     */
    public CommandToast(Activity activity, Context context, Command command) {
        super(activity, context, command);
    }

    @Override
    public void execute() {
        super.execute();
        try {
            Toast.makeText(this.context,
                    this.command.command.getString("message"),
                    this.command.command.getInt("duration")
            ).show();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }
}
