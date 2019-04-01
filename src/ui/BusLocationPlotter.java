package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.R;
import ca.ubc.cs.cpsc210.translink.model.Bus;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ca.ubc.cs.cpsc210.translink.util.Geometry.gpFromLatLon;

// A plotter for bus locations
public class BusLocationPlotter extends MapViewOverlay {
    /**
     * overlay used to display bus locations
     */
    private ItemizedIconOverlay<OverlayItem> busLocationsOverlay;

    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusLocationPlotter(Context context, MapView mapView) {
        super(context, mapView);
        busLocationsOverlay = createBusLocnOverlay();
    }

    public ItemizedIconOverlay<OverlayItem> getBusLocationsOverlay() {
        return busLocationsOverlay;
    }

    /**
     * Plot buses serving selected stop
     */
    public void plotBuses() {
        updateVisibleArea();
        StopManager stopManager = StopManager.getInstance();
        Stop selected = stopManager.getSelected();
        //busLocationsOverlay.removeAllItems();
        if (selected != null) {
            busLocationsOverlay = createBusLocnOverlay();
            List<Bus> buses = selected.getBuses();
            Iterator busIter = buses.iterator();
            plotBus(busIter);
        }

    }

    /**
     * Plot buses
     */
    private void plotBus(Iterator busIter) {
        while (busIter.hasNext()) {
            Bus bus = (Bus) busIter.next();
            LatLon busLatLon = bus.getLatLon();
            GeoPoint busGeoPoint = gpFromLatLon(busLatLon);
            OverlayItem overlayItem =
                    new OverlayItem(bus.getDestination(), bus.getRoute().getNumber(), busGeoPoint);
            busLocationsOverlay.addItem(overlayItem);
            //createBusLocnOverlay();
        }
    }

    /**
     * Create the overlay for bus markers.
     */
    private ItemizedIconOverlay<OverlayItem> createBusLocnOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);

        return new ItemizedIconOverlay<OverlayItem>(
                new ArrayList<OverlayItem>(),
                context.getResources().getDrawable(R.drawable.bus),
                null, rp);
    }
}
