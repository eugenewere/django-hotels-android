package com.muflone.android.django_hotels.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "buildings",
        foreignKeys = {
            @ForeignKey(entity = Structure.class,
                        parentColumns = "id",
                        childColumns = "structure_id",
                        onDelete = ForeignKey.RESTRICT),
            @ForeignKey(entity = Location.class,
                        parentColumns = "id",
                        childColumns = "location_id",
                        onDelete = ForeignKey.RESTRICT)
        })
public class Building {
    @PrimaryKey
    public final long id;

    @ColumnInfo(name = "name")
    public final String name;

    @ColumnInfo(name = "structure_id", index = true)
    public final long structureId;

    @Ignore
    public Location location = null;

    @ColumnInfo(name = "location_id", index = true)
    public final long locationId;

    @ColumnInfo(name = "extras")
    public final boolean extras;

    @Ignore
    public List<Room> rooms = null;

    @Ignore
    public List<Employee> employees = null;

    public Building(long id, String name, long structureId, long locationId, boolean extras) {
        this.id = id;
        this.name = name;
        this.structureId = structureId;
        this.locationId = locationId;
        this.extras = extras;
    }

    @Ignore
    public Building(long id, String name, Structure structure, Location location, boolean extras, List<Room> rooms) {
        this(id, name, structure.id, location.id, extras);
        this.location = location;
        this.rooms = rooms;
    }

    @Ignore
    public Building(JSONObject jsonObject, Structure structure, boolean extras) throws JSONException {
        this(jsonObject.getJSONObject("building").getLong("id"),
                jsonObject.getJSONObject("building").getString("name"),
                structure,
                new Location(jsonObject.getJSONObject("location")),
                extras,
                new ArrayList<>());
        // Loop over every room
        JSONArray jsonRooms = jsonObject.getJSONArray("rooms");
        for (int i = 0; i < jsonRooms.length(); i++) {
            Room room = new Room(jsonRooms.getJSONObject(i),
                                 jsonObject.getJSONObject("building").getLong("id"));
            this.rooms.add(room);
        }
    }
}
