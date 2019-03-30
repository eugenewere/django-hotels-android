package com.muflone.android.django_hotels.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.muflone.android.django_hotels.Utility;
import com.muflone.android.django_hotels.api.Api;
import com.muflone.android.django_hotels.api.ApiData;
import com.muflone.android.django_hotels.database.AppDatabase;
import com.muflone.android.django_hotels.database.dao.BrandDao;
import com.muflone.android.django_hotels.database.dao.BuildingDao;
import com.muflone.android.django_hotels.database.dao.CompanyDao;
import com.muflone.android.django_hotels.database.dao.ContractBuildingsDao;
import com.muflone.android.django_hotels.database.dao.ContractDao;
import com.muflone.android.django_hotels.database.dao.ContractTypeDao;
import com.muflone.android.django_hotels.database.dao.CountryDao;
import com.muflone.android.django_hotels.database.dao.EmployeeDao;
import com.muflone.android.django_hotels.database.dao.JobTypeDao;
import com.muflone.android.django_hotels.database.dao.LocationDao;
import com.muflone.android.django_hotels.database.dao.RegionDao;
import com.muflone.android.django_hotels.database.dao.RoomDao;
import com.muflone.android.django_hotels.database.dao.ServiceDao;
import com.muflone.android.django_hotels.database.dao.StructureDao;
import com.muflone.android.django_hotels.database.dao.TimestampDao;
import com.muflone.android.django_hotels.database.dao.TimestampDirectionDao;
import com.muflone.android.django_hotels.database.models.Building;
import com.muflone.android.django_hotels.database.models.Contract;
import com.muflone.android.django_hotels.database.models.ContractBuildings;
import com.muflone.android.django_hotels.database.models.Room;
import com.muflone.android.django_hotels.database.models.Service;
import com.muflone.android.django_hotels.database.models.Structure;
import com.muflone.android.django_hotels.database.models.Timestamp;
import com.muflone.android.django_hotels.database.models.TimestampDirection;

import java.util.List;

public class AsyncTaskDownload extends AsyncTask<Void, Void, AsyncTaskResult<ApiData>> {
    private final Api api;
    private final AsyncTaskListener callback;

    public AsyncTaskDownload(Api api, AsyncTaskListener callback) {
        this.api = api;
        this.callback = callback;
    }

    @Override
    protected AsyncTaskResult doInBackground(Void... params) {
        // Do the background job
        boolean transmissionErrors = false;
        AppDatabase database = AppDatabase.getAppDatabase(this.api.context);

        // Check if the system date/time matches with the remote date/time
        ApiData data = this.api.checkDates();
        if (data.exception == null) {
            // Transmit any incomplete timestamp (UPLOAD)
            List<Timestamp> timestampsList = database.timestampDao().listByUntrasmitted();
            for (Timestamp timestamp : timestampsList) {
                data = this.api.putTimestamp(timestamp);
                if (data.exception == null) {
                    // Update transmission date
                    timestamp.transmission = Utility.getCurrentDateTime(this.api.settings.getTimeZone());
                    database.timestampDao().update(timestamp);
                } else {
                    // There were some errors during the timestamps transmissions
                    transmissionErrors = true;
                }
            }
            if (! transmissionErrors) {
                // Get new data from the server (DOWNLOAD)
                data = this.api.getData();
                if (data.exception == null) {
                    // Success, save data in database
                    this.saveToDatabase(data, this.api.context);
                }
            }
        }
        return new AsyncTaskResult(data, this.callback, data.exception);
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<ApiData> results) {
        super.onPostExecute(results);
        // Check if callback listener was requested
        if (this.callback != null & results != null) {
            if (results.exception == null) {
                // Return flow to the caller
                this.callback.onSuccess(results);
            } else {
                // Failure with exception
                this.callback.onFailure(results.exception);
            }
        }
    }

    private void saveToDatabase(ApiData data, Context context) {
        AppDatabase database = AppDatabase.getAppDatabase(context);
        BrandDao brandDao = database.brandDao();
        BuildingDao buildingDao = database.buildingDao();
        CompanyDao companyDao = database.companyDao();
        ContractDao contractDao = database.contractDao();
        ContractBuildingsDao contractBuildingsDao = database.contractBuildingsDao();
        ContractTypeDao contractTypeDao = database.contractTypeDao();
        JobTypeDao jobTypeDao = database.jobTypeDao();
        CountryDao countryDao = database.countryDao();
        EmployeeDao employeeDao = database.employeeDao();
        LocationDao locationDao = database.locationDao();
        RegionDao regionDao = database.regionDao();
        RoomDao roomDao = database.roomDao();
        ServiceDao serviceDao = database.serviceDao();
        StructureDao structureDao = database.structureDao();
        TimestampDirectionDao timestampDirectionDao = database.timestampDirectionDao();

        // Delete previous data
        roomDao.truncate();
        contractBuildingsDao.truncate();
        buildingDao.truncate();
        contractDao.truncate();
        contractTypeDao.truncate();
        jobTypeDao.truncate();
        structureDao.truncate();
        locationDao.truncate();
        regionDao.truncate();
        countryDao.truncate();
        companyDao.truncate();
        brandDao.truncate();
        serviceDao.truncate();
        timestampDirectionDao.truncate();

        // Save data from structures
        for (Structure structure : data.structuresMap.values()) {
            brandDao.insert(structure.brand);
            companyDao.insert(structure.company);
            countryDao.insert(structure.location.country);
            regionDao.insert(structure.location.region);
            locationDao.insert(structure.location);
            structureDao.insert(structure);
            // Save buildings
            for (Building building : structure.buildings) {
                countryDao.insert(building.location.country);
                regionDao.insert(building.location.region);
                locationDao.insert(building.location);
                buildingDao.insert(building);
                // Save rooms
                for (Room room : building.rooms) {
                    roomDao.insert(room);
                }
            }
        }
        // Save data from contracts
        for (Contract contract : data.contractsMap.values()) {
            employeeDao.insert(contract.employee);
            companyDao.insert(contract.company);
            contractTypeDao.insert(contract.contractType);
            jobTypeDao.insert(contract.jobType);
            contractDao.insert(contract);
            // Save ContractBuildings
            for (long building_id : contract.buildings) {
                contractBuildingsDao.insert(new ContractBuildings(contract.id, building_id));
            }
        }
        // Save data for services
        for (Service service : data.serviceMap.values()) {
            serviceDao.insert(service);
        }
        // Save data for timestamp directions
        for (TimestampDirection timestampDirection : data.timestampDirectionsMap.values()) {
            timestampDirectionDao.insert(timestampDirection);
        }
    }
}
