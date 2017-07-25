package org.droidplanner.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
import com.o3dr.services.android.lib.drone.property.Gps;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.BuildConfig;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.core.api.Net;
import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.dialogs.SupportEditInputDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenMissionDialog;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.account.editor.tool.EditorToolsFragment;
import org.droidplanner.android.fragments.account.editor.tool.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.account.editor.tool.EditorToolsImpl;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.net.model.NetError;
import org.droidplanner.android.net.model.Path;
import org.droidplanner.android.net.model.RestrictedArea;
import org.droidplanner.android.net.model.ServerPoint;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.file.IO.MissionReader;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.List;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
        EditorToolsFragment.EditorToolListener, MissionDetailFragment.OnMissionDetailListener,
        OnEditorInteraction, MissionSelection.OnSelectionUpdateListener, OnClickListener,
        OnLongClickListener, SupportEditInputDialog.Listener, NetSubscriber {

    private static final double DEFAULT_SPEED = 5; //meters per second.

    /**
     * Used to retrieve the item detail window when the activity is destroyed,
     * and recreated.
     */
    private static final String ITEM_DETAIL_TAG = "Item Detail Window";

    private static final String EXTRA_OPENED_MISSION_FILENAME = "extra_opened_mission_filename";

    private static final IntentFilter eventFilter = new IntentFilter();
    private static final String MISSION_FILENAME_DIALOG_TAG = "Mission filename";

    private boolean needRebuildPath = true;
    private boolean needRestrictedZone = true;
    private boolean isPathRebuilt = false;

    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(AttributeEvent.PARAMETERS_REFRESH_COMPLETED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    updateMissionLength();

                    if (needRebuildPath) {
                        updateMissionPoints();
                    }
                    break;
                case AttributeEvent.GPS_POSITION:
                    final Drone drone = dpApp.getDrone();
                    if (drone.isConnected() && needRebuildPath) {
                        updateMissionPoints();
                    }
                    if (drone.isConnected() && needRestrictedZone) {
                        Gps gps = drone.getAttribute(AttributeType.GPS);
                        DroidPlannerApp.getApp(EditorActivity.this).getNet().
                                getRestrictedArea(new LatLng(gps.getPosition().getLatitude(),
                                        gps.getPosition().getLongitude()));
                    }
                    break;

                case AttributeEvent.MISSION_RECEIVED:
                    final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
                    if (planningMapFragment != null) {
                        planningMapFragment.zoomToFit();
                    }
                    break;
            }
        }
    };

    /**
     * Used to provide access and interact with the
     * {@link org.droidplanner.android.proxy.mission.MissionProxy} object on the Android
     * layer.
     */
    private MissionProxy missionProxy;

    /*
     * View widgets.
     */
    private GestureMapFragment gestureMapFragment;
    private EditorToolsFragment editorToolsFragment;
    private MissionDetailFragment itemDetailFragment;
    private FragmentManager fragmentManager;

    private TextView infoView;

    /**
     * If the mission was loaded from a file, the filename is stored here.
     */
    private String openedMissionFilename;

    private FloatingActionButton itemDetailToggle;
    private EditorListFragment editorListFragment;

    private LatLng mDroneLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        gestureMapFragment = ((GestureMapFragment) fragmentManager.findFragmentById(R.id.editor_map_fragment));
        if (gestureMapFragment == null) {
            gestureMapFragment = new GestureMapFragment();
            fragmentManager.beginTransaction().add(R.id.editor_map_fragment, gestureMapFragment).commit();
        }

        editorListFragment = (EditorListFragment) fragmentManager.findFragmentById(R.id.mission_list_fragment);

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        final FloatingActionButton zoomToFit = (FloatingActionButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);

        final FloatingActionButton mGoToMyLocation = (FloatingActionButton) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        mGoToMyLocation.setOnLongClickListener(this);

        final FloatingActionButton mGoToDroneLocation = (FloatingActionButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        mGoToDroneLocation.setOnLongClickListener(this);

        itemDetailToggle = (FloatingActionButton) findViewById(R.id.toggle_action_drawer);
        itemDetailToggle.setOnClickListener(this);

        if (savedInstanceState != null) {
            openedMissionFilename = savedInstanceState.getString(EXTRA_OPENED_MISSION_FILENAME);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager.findFragmentByTag(ITEM_DETAIL_TAG);

        gestureMapFragment.setOnPathFinishedListener(this);
        openActionDrawer();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEV) {

//                    mDroneLocation = new LatLng(37.8068, -122.410741); // Pier 39 SF
                    mDroneLocation = new DroidPlannerPrefs(EditorActivity.this).getDroneLocation();// Pier 39 SF
                    gestureMapFragment.getMapFragment().goToDroneLocation(mDroneLocation);
                }
            }
        }, 500);

    }

    @Override
    protected void onStart() {
        super.onStart();
        DroidPlannerApp.getApp(this).getNet().subscribe(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        DroidPlannerApp.getApp(this).getNet().unsubscribe(this);
    }

    @Override
    protected float getActionDrawerTopMargin() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
    }

    /**
     * Account for the various ui elements and update the map padding so that it
     * remains 'visible'.
     */
    private void updateLocationButtonsMargin(boolean isOpened) {
        final View actionDrawer = getActionDrawer();
        if (actionDrawer == null)
            return;

        itemDetailToggle.setActivated(isOpened);
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        missionProxy = dpApp.getMissionProxy();
        if (missionProxy != null) {
            missionProxy.selection.addSelectionUpdateListener(this);
            itemDetailToggle.setVisibility(missionProxy.selection.getSelected().isEmpty() ? View.GONE : View.VISIBLE);
        }

        updateMissionLength();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();

        if (missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);

        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onClick(View v) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (v.getId()) {
            case R.id.toggle_action_drawer:
                if (missionProxy == null)
                    return;

                if (itemDetailFragment == null) {
                    List<MissionItemProxy> selected = missionProxy.selection.getSelected();
                    showItemDetail(selectMissionDetailType(selected));
                } else {
                    removeItemDetail();
                }
                break;

            case R.id.zoom_to_fit_button:
                if (planningMapFragment != null) {
                    planningMapFragment.zoomToFit();
                }
                break;

            case R.id.drone_location_button:
                planningMapFragment.goToDroneLocation();
                break;
            case R.id.my_location_button:
                planningMapFragment.goToMyLocation();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (view.getId()) {
            case R.id.drone_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            case R.id.my_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        editorToolsFragment.setToolAndUpdateView(getTool());
        setupTool();
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_OPENED_MISSION_FILENAME, openedMissionFilename);
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_mission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_mission:
                openMissionFile();
                return true;

            case R.id.menu_save_mission:
                saveMissionFile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openMissionFile() {
        OpenFileDialog missionDialog = new OpenMissionDialog() {
            @Override
            public void waypointFileLoaded(MissionReader reader) {
                openedMissionFilename = getSelectedFilename();

                if (missionProxy != null) {
                    missionProxy.readMissionFromFile(reader);
                    gestureMapFragment.getMapFragment().zoomToFit();
                }
            }
        };
        missionDialog.openDialog(this);
    }

    @Override
    public void onOk(String dialogTag, CharSequence input) {
        final Context context = getApplicationContext();

        switch (dialogTag) {
            case MISSION_FILENAME_DIALOG_TAG:
                if (missionProxy.writeMissionToFile(input.toString())) {
                    Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT)
                            .show();

                    final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                            .setCategory(GAUtils.Category.MISSION_PLANNING)
                            .setAction("Mission saved to file")
                            .setLabel("Mission items count");
                    GAUtils.sendEvent(eventBuilder);

                    break;
                }

                Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onCancel(String dialogTag) {
    }

    private void saveMissionFile() {
        final String defaultFilename = TextUtils.isEmpty(openedMissionFilename)
                ? FileStream.getWaypointFilename("waypoints")
                : openedMissionFilename;

        final SupportEditInputDialog dialog = SupportEditInputDialog.newInstance(MISSION_FILENAME_DIALOG_TAG,
                getString(R.string.label_enter_filename), defaultFilename, true);

        dialog.show(getSupportFragmentManager(), MISSION_FILENAME_DIALOG_TAG);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gestureMapFragment.getMapFragment().saveCameraPosition();
    }

    private void updateMissionLength() {
        if (missionProxy != null) {

            double missionLength = missionProxy.getMissionLength();
            LengthUnit convertedMissionLength = unitSystem.getLengthUnitProvider().boxBaseValueToTarget(missionLength);
            double speedParameter = dpApp.getDrone().getSpeedParameter() / 100; //cm/s to m/s conversion.
            if (speedParameter == 0)
                speedParameter = DEFAULT_SPEED;

            int time = (int) (missionLength / speedParameter);

            String infoString = getString(R.string.editor_info_window_distance, convertedMissionLength.toString())
                    + ", " + getString(R.string.editor_info_window_flight_time, time / 60, time % 60);

            infoView.setText(infoString);

            // Remove detail window if item is removed
            if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                removeItemDetail();
            }
        }
    }

    private void updateMissionPoints() {
        List<ServerPoint> points = new ArrayList<>();

        if (missionProxy != null && !missionProxy.getCurrentMission().getMissionItems().isEmpty()) {

            ServerPoint firstPoint = new ServerPoint();
            firstPoint.setCn("TAKEOFF");
            firstPoint.setLatitude(mDroneLocation.latitude);
            firstPoint.setLongitude(mDroneLocation.longitude);
            firstPoint.setAltitude(0);
            points.add(firstPoint);

            List<MissionItem> items = missionProxy.getCurrentMission().getMissionItems();
            for (int i = 0; i < items.size(); i++) {
                ServerPoint serverPoint = ServerPoint.toServerModel(items.get(i));
                Log.e("CN", serverPoint.getCn());
                points.add(serverPoint);
            }

//            mDroneLocation = new LatLng(50.003137, 36.272996);
            final Drone drone = dpApp.getDrone();
            if (BuildConfig.DEV) {
                recalculateRouteDebug(drone, points);
            } else {
                recalculateRouteProd(drone, points);
            }

        }
    }

    private void recalculateRouteDebug(Drone drone, List<ServerPoint> points) {
        gestureMapFragment.getMapFragment().goToDroneLocation();

//        DroidPlannerApp.getApp(this).getNet().calculateRoute(points, mDroneLocation);
//        DroidPlannerApp.getApp(this).getNet().calculateRoute(points, "1573342362099253248");
        DroidPlannerApp.getApp(this).getNet().calculateRoute(points,
                new DroidPlannerPrefs(EditorActivity.this).getDroneID());
        needRebuildPath = false;

        Toast.makeText(this, "Calculating route in debug mode...", Toast.LENGTH_LONG).show();

    }

    private void recalculateRouteProd(Drone drone, List<ServerPoint> points) {
        if (!drone.isConnected()) {
            Toast.makeText(this, "In order to calculate route please connect to drone!", Toast.LENGTH_LONG).show();
            return;
        }

        final Gps droneGps = drone.getAttribute(AttributeType.GPS);
        if (droneGps != null && droneGps.isValid()) {
            mDroneLocation = new LatLng(droneGps.getPosition().getLatitude(),
                    droneGps.getPosition().getLongitude());
        }
        if (mDroneLocation != null) {
//            DroidPlannerApp.getApp(this).getNet().calculateRoute(points, mDroneLocation);
//            DroidPlannerApp.getApp(this).getNet().calculateRoute(points, "1573342362099253248");
            DroidPlannerApp.getApp(this).getNet().calculateRoute(points,
                    new DroidPlannerPrefs(EditorActivity.this).getDroneID());
            needRebuildPath = false;
        } else {
            Toast.makeText(this, "In order to calculate route please connect to drone!", Toast.LENGTH_LONG).show();
        }

        missionProxy.getCurrentMission().getMissionItems();
    }

    @Override
    public void onMapClick(LatLong point) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onMapClick(point);
    }

    public EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    public EditorToolsImpl getToolImpl() {
        return editorToolsFragment.getToolImpl();
    }

    @Override
    public void editorToolChanged(EditorTools tools) {
        setupTool();
    }

    @Override
    public void enableGestureDetection(boolean enable) {
        if (gestureMapFragment == null)
            return;

        if (enable)
            gestureMapFragment.enableGestureDetection();
        else
            gestureMapFragment.disableGestureDetection();
    }

    @Override
    public void skipMarkerClickEvents(boolean skip) {
        if (gestureMapFragment == null)
            return;

        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.skipMarkerClickEvents(skip);
    }

    private void setupTool() {
        final EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.setup();
        editorListFragment.enableDeleteMode(toolImpl.getEditorTools() == EditorTools.TRASH);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateLocationButtonsMargin(itemDetailFragment != null);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        editorToolsFragment = (EditorToolsFragment) fragmentManager.findFragmentById(toolbarId);
        if (editorToolsFragment == null) {
            editorToolsFragment = new EditorToolsFragment();
            fragmentManager.beginTransaction().add(toolbarId, editorToolsFragment).commit();
        }
    }

    private void showItemDetail(MissionDetailFragment itemDetail) {
        if (itemDetailFragment == null) {
            addItemDetail(itemDetail);
        } else {
            switchItemDetail(itemDetail);
        }

        editorToolsFragment.setToolAndUpdateView(EditorTools.NONE);
    }

    private void addItemDetail(MissionDetailFragment itemDetail) {
        itemDetailFragment = itemDetail;
        if (itemDetailFragment == null)
            return;

        fragmentManager.beginTransaction()
                .replace(getActionDrawerId(), itemDetailFragment, ITEM_DETAIL_TAG)
                .commit();
        updateLocationButtonsMargin(true);
    }

    public void switchItemDetail(MissionDetailFragment itemDetail) {
        removeItemDetail();
        addItemDetail(itemDetail);
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null) {
            fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            itemDetailFragment = null;

            updateLocationButtonsMargin(false);
        }
    }

    @Override
    public void onPathFinished(List<LatLong> path) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<LatLong> points = planningMapFragment.projectPathIntoMap(path);
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onPathFinished(points);

    }

    @Override
    public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        if (missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            List<MissionItemProxy>>> oldNewItemsList) {
        missionProxy.replaceAll(oldNewItemsList);
    }

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies) {
        if (proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for (MissionItemProxy proxy : proxies) {
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if (referenceType == null) {
                referenceType = proxyType;
            } else if (referenceType != proxyType
                    || MissionDetailFragment.typeWithNoMultiEditSupport.contains(referenceType)) {
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        if (missionProxy == null) return;

        if (isPathRebuilt) {
            EditorToolsImpl toolImpl = getToolImpl();
            toolImpl.onListItemClick(item);

            if (zoomToFit) {
                zoomToFitSelected();
            }
        }
    }

    @Override
    public void zoomToFitSelected() {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<MissionItemProxy> selected = missionProxy.selection.getSelected();
        if (selected.isEmpty()) {
            planningMapFragment.zoomToFit();
        } else {
            planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
        }
    }

    @Override
    public void onListVisibilityChanged() {
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onSelectionUpdate(selected);

        final boolean isEmpty = selected.isEmpty();

        if (isEmpty) {
            itemDetailToggle.setVisibility(View.GONE);
            removeItemDetail();
        } else {
            itemDetailToggle.setVisibility(View.VISIBLE);
            if (getTool() == EditorTools.SELECTOR)
                removeItemDetail();
            else {
                showItemDetail(selectMissionDetailType(selected));
            }
        }

        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.postUpdate();
    }

    @Override
    public void onNetRequestSuccess(@Net.NetEvent int eventId, Object netObject) {
        switch (eventId) {
            case Net.CALCULATE_ROUTE:
                Path path = (Path) netObject;
                Toast.makeText(this, String.valueOf(path.getPassedPoints().size()), Toast.LENGTH_LONG).show();

                List<LatLong> points = new ArrayList<>();

                missionProxy.getCurrentMission().clear();
                for (int i = 0; i < path.getPassedPoints().size(); i++) {

                    BaseSpatialItem item = new Waypoint();
                    item.setCoordinate(new LatLongAlt(path.getPassedPoints().get(i).getLatitude(),
                            path.getPassedPoints().get(i).getLongitude(), path.getPassedPoints().get(i).getAltitude()));
                    missionProxy.getCurrentMission().addMissionItem(0, item);
                }

                missionProxy.clear();
                for (int i = 0; i < path.getPassedPoints().size(); i++) {
                    missionProxy.addWaypoint(new LatLong(path.getPassedPoints().get(i).getLatitude(),
                            path.getPassedPoints().get(i).getLongitude()));
                }
                needRebuildPath = false;
                isPathRebuilt = true;
                break;
            case Net.RESTRICTED_AREA:
                needRestrictedZone = false;
                RestrictedArea area = (RestrictedArea) netObject;
                Toast.makeText(this, "Restricted area - success!", Toast.LENGTH_LONG).show();
                gestureMapFragment.getMapFragment().addRestrictedAreas(area);
                break;
        }
    }

    @Override
    public void onNetRequestError(@Net.NetEvent int eventId, NetError netError) {
        switch (eventId) {
            case Net.CALCULATE_ROUTE:
                needRebuildPath = true;
                break;
            case Net.RESTRICTED_AREA:
                Toast.makeText(this, "Restricted area - error !", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
