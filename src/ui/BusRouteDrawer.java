package ca.ubc.cs.cpsc210.translink.ui;

import android.content.Context;
import ca.ubc.cs.cpsc210.translink.BusesAreUs;
import ca.ubc.cs.cpsc210.translink.model.Route;
import ca.ubc.cs.cpsc210.translink.model.RoutePattern;
import ca.ubc.cs.cpsc210.translink.model.Stop;
import ca.ubc.cs.cpsc210.translink.model.StopManager;
import ca.ubc.cs.cpsc210.translink.util.LatLon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static ca.ubc.cs.cpsc210.translink.util.Geometry.gpFromLatLon;
import static ca.ubc.cs.cpsc210.translink.util.Geometry.rectangleIntersectsLine;

// A bus route drawer
public class BusRouteDrawer extends MapViewOverlay {
    /**
     * overlay used to display bus route legend text on a layer above the map
     */
    private BusRouteLegendOverlay busRouteLegendOverlay;
    /**
     * overlays used to plot bus routes
     */
    private List<Polyline> busRouteOverlays;

    /**
     * Constructor
     *
     * @param context the application context
     * @param mapView the map view
     */
    public BusRouteDrawer(Context context, MapView mapView) {
        super(context, mapView);
        busRouteLegendOverlay = createBusRouteLegendOverlay();
        busRouteOverlays = new ArrayList<>();
    }

    /**
     * Plot each visible segment of each route pattern of each route going through the selected stop.
     */
    public void plotRoutes(int zoomLevel) {
        updateVisibleArea();
        busRouteOverlays.clear();
        busRouteLegendOverlay.clear();
        Stop stop = StopManager.getInstance().getSelected();
        List<GeoPoint> geoPoints = new ArrayList<>();
        if (stop != null) {
            float lineWidth = getLineWidth(zoomLevel);
            Iterator routeIter = stop.getRoutes().iterator();
            while (routeIter.hasNext()) {
                Route route = (Route) routeIter.next();
                busRouteLegendOverlay.add(route.getNumber());
                Iterator routePatternIter = route.getPatterns().iterator();
                routePatternIter(geoPoints, lineWidth, route, routePatternIter);
            }
        }
    }

    /**
     * Plot visible segment of each route pattern of route going through the selected stop.
     *
     * @param geoPoints the list of GeoPoint
     * @param lineWidth the width of line used to plot bus route based on zoom level
     * @param route current route
     * @param routePatternIter the iterator of routePattern
     */
    private void routePatternIter(List<GeoPoint> geoPoints, float lineWidth, Route route, Iterator routePatternIter) {
        while (routePatternIter.hasNext()) {
            RoutePattern routePattern = (RoutePattern) routePatternIter.next();
            for (int i = 0; i < routePattern.getPath().size() - 1; i++) {
                LatLon first = routePattern.getPath().get(i);
                LatLon second = routePattern.getPath().get(i + 1);
                drawLines(geoPoints, lineWidth, route, first, second);
            }
        }
    }

    /**
     * Plot segment if it is visible
     *
     * @param geoPoints the list of GeoPoint
     * @param lineWidth the width of line used to plot bus route based on zoom level
     * @param route current route
     * @param first the start point
     * @param second the end point
     */
    private void drawLines(List<GeoPoint> geoPoints, float lineWidth, Route route, LatLon first, LatLon second) {
        if (rectangleIntersectsLine(northWest, southEast, first, second)) {
            GeoPoint firstGeoPoint = gpFromLatLon(first);
            GeoPoint secondGeoPoint = gpFromLatLon(second);

            //if (!geoPoints.contains(firstGeoPoint)) {
            geoPoints.add(firstGeoPoint);
            //}
            //if (!geoPoints.contains(secondGeoPoint)) {
            geoPoints.add(secondGeoPoint);
            //}
            Polyline polyline = new Polyline(context);
            polyline.setColor(busRouteLegendOverlay.getColor(route.getNumber()));
            polyline.setWidth(lineWidth);
            polyline.setPoints(geoPoints);
            busRouteOverlays.add(polyline);//add&draw lines
            geoPoints.clear();
        }
    }

    public List<Polyline> getBusRouteOverlays() {
        return Collections.unmodifiableList(busRouteOverlays);
    }

    public BusRouteLegendOverlay getBusRouteLegendOverlay() {
        return busRouteLegendOverlay;
    }


    /**
     * Create text overlay to display bus route colours
     */
    private BusRouteLegendOverlay createBusRouteLegendOverlay() {
        ResourceProxy rp = new DefaultResourceProxyImpl(context);
        return new BusRouteLegendOverlay(rp, BusesAreUs.dpiFactor());
    }

    /**
     * Get width of line used to plot bus route based on zoom level
     *
     * @param zoomLevel the zoom level of the map
     * @return width of line used to plot bus route
     */
    private float getLineWidth(int zoomLevel) {
        if (zoomLevel > 14) {
            return 7.0f * BusesAreUs.dpiFactor();
        } else if (zoomLevel > 10) {
            return 5.0f * BusesAreUs.dpiFactor();
        } else {
            return 2.0f * BusesAreUs.dpiFactor();
        }
    }
}
