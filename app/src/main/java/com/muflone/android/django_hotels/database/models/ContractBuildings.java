package com.muflone.android.django_hotels.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

@Entity(tableName = "contract_buildings",
        primaryKeys = {"contract_id", "building_id"},
        foreignKeys = {
                @ForeignKey(entity = Contract.class,
                        parentColumns = "id",
                        childColumns = "contract_id",
                        onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = Building.class,
                        parentColumns = "id",
                        childColumns = "building_id",
                        onDelete = ForeignKey.RESTRICT)
        })
public class ContractBuildings {
    @ColumnInfo(name = "contract_id", index = true)
    public final long contractId;

    @ColumnInfo(name = "building_id", index = true)
    public final long buildingId;

    public ContractBuildings(final long contractId, final long buildingId) {
        this.contractId = contractId;
        this.buildingId = buildingId;
    }
}
