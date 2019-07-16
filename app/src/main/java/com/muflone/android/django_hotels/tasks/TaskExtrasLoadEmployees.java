package com.muflone.android.django_hotels.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.muflone.android.django_hotels.ExtraStatus;
import com.muflone.android.django_hotels.Singleton;
import com.muflone.android.django_hotels.database.models.Employee;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskExtrasLoadEmployees extends AsyncTask<Void, Void, Void> {
    private final Singleton singleton = Singleton.getInstance();
    private final List<String> employeesListInternal = new ArrayList<>();
    private final List<String> employeesList;
    private final WeakReference<ListView> employeesView;
    private final HashMap<Long, List<ExtraStatus>> extrasStatusMap;

    @SuppressWarnings("WeakerAccess")
    public TaskExtrasLoadEmployees(@NonNull List<String> employeesList,
                                   @NonNull ListView employeesView,
                                   @NonNull HashMap<Long, List<ExtraStatus>> extrasStatusMap) {
        this.employeesList = employeesList;
        this.employeesView = new WeakReference<>(employeesView);
        this.extrasStatusMap = extrasStatusMap;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Load employees for the selected structure
        this.extrasStatusMap.clear();
        this.employeesListInternal.clear();
        for (Employee employee : this.singleton.selectedStructure.employees) {
            this.employeesListInternal.add(String.format("%s %s", employee.firstName, employee.lastName));
            List<ExtraStatus> extraStatusList = new ArrayList<>();
            for (int i = 1; i <= employee.id; i++) {
                ExtraStatus extraStatus = new ExtraStatus(this.singleton.settings.context, 0, i, null,
                        String.format("%s %s %d", employee.firstName, employee.lastName, i), null);
                extraStatusList.add(extraStatus);
            }
            this.extrasStatusMap.put(employee.id, extraStatusList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // Update data in the list
        this.employeesList.clear();
        this.employeesList.addAll(this.employeesListInternal);
        ((ArrayAdapter) this.employeesView.get().getAdapter()).notifyDataSetChanged();
        // Select the first employee for the selected tab
        if (this.employeesList.size() > 0) {
            this.employeesView.get().performItemClick(
                    this.employeesView.get().getAdapter().getView(0, null, null),
                    0,
                    this.employeesView.get().getAdapter().getItemId(0)
            );
        }
    }
}
