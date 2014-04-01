/*
 * AnBox, and an Android Blackbox application for the have-not-so-much-money's
 * Copyright (C) 2010 Yoonsoo Kim, Heekuk Lee, Heejin Sohn
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ivehicle.AnBox;

import java.util.ArrayList;
import java.util.List;
import android.util.FloatMath;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MyPositionOverlay extends Overlay {
	Location location = new Location(LocationManager.GPS_PROVIDER);
	Location prevLocation = new Location(LocationManager.GPS_PROVIDER);
	List<Location> locArray = new ArrayList<Location>();
	public int cnt = 0;
	int i = 0, j = 0;
	// boolean flgSetLoc = false;
	public float orientation = 0; // rad
	public boolean isSetOrientation = false;
	public float dt;
	public float dist;
	public boolean isVisible = true;
	float rCos=1;
	float rSin=0;

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location olocation) {
		if (cnt > 0)
			prevLocation = location;
		else
			this.prevLocation = olocation;

		location = olocation;
		location.setLatitude(olocation.getLatitude());
		location.setLongitude(olocation.getLongitude());
		dt = (location.getTime() - prevLocation.getTime());
		dist = location.distanceTo(prevLocation);
		float t = (dist / dt) * 3600f; // m/s * 1000
		olocation.setSpeed(t);
		if (t < Config.MAX_VEL_FOR_ORIENTATION_SENSOR)
			isSetOrientation = true;
		else
			isSetOrientation = false;
		locArray.add(location);
		cnt++;

	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		//float[] arrowPoint = new float[8];
		float[] arrowPoint = new float[10];
		float diffx;
		float diffy;
		float r;
		Double latitude;
		Double longitude;
		GeoPoint geoPoint;
		Point point = new Point();
		Point prevpoint = new Point();
		Path arrow = new Path();
		Paint arrowPaint = new Paint();
		Point pathpoint = new Point();
		float[] pointArray = null;
		Paint pathPaint = new Paint();
		Projection projection = mapView.getProjection();

		if (shadow == false && isVisible) {
			latitude = location.getLatitude() * 1E6;
			longitude = location.getLongitude() * 1E6;
			geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());
			projection.toPixels(geoPoint, point);

			latitude = prevLocation.getLatitude() * 1E6;
			longitude = prevLocation.getLongitude() * 1E6;
			geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());
			projection.toPixels(geoPoint, prevpoint);

			if (isSetOrientation) {
				rCos = -FloatMath.sin(location.getBearing());
				rSin = FloatMath.cos(location.getBearing());
			} else {
				diffx = (point.x - prevpoint.x);
				diffy = (point.y - prevpoint.y);
				if (diffx!= 0 && diffy !=0) {
				r = FloatMath.sqrt(diffx * diffx + diffy * diffy);
				rCos = -diffy / r;
				rSin = diffx / r;
			}
			}
//			arrowPoint[0] = -rSin * (-15);
//			arrowPoint[1] = rCos * (-15);
//			arrowPoint[2] = rCos * (-20) - rSin * (20);
//			arrowPoint[3] = rSin * (-20) + rCos * (20);
//			arrowPoint[4] = -rSin * (15);
//			arrowPoint[5] = rCos * (15);
//			arrowPoint[6] = rCos * (30) - rSin * (30);
//			arrowPoint[7] = rSin * (30) + rCos * (30);
			
			arrowPoint[0] = -rSin * (-30);
			arrowPoint[1] = rCos * (-30);
			arrowPoint[2] = rCos * (-18) - rSin * (37);
			arrowPoint[3] = rSin * (-18) + rCos * (37);
			arrowPoint[4] = rCos * (-7) - rSin * (15);
			arrowPoint[5] = rSin * (-7) + rCos * (15);
			arrowPoint[6] = rCos * (7) - rSin * (15);
			arrowPoint[7] = rSin * (7) + rCos * (15);
			arrowPoint[8] = rCos * (18) - rSin * (37);
			arrowPoint[9] = rSin * (18) + rCos * (37);
			//arrowPaint.setAntiAlias(true);
			arrowPaint.setColor(Color.BLUE);
			arrowPaint.setStyle(Paint.Style.FILL);
			arrowPaint.setAlpha(170);

			if (locArray.size() > 1) {
				pointArray = new float[(locArray.size() - 1) * 4];
				for (i = 0; i < locArray.size() - 1; i++) {
					latitude = locArray.get(i).getLatitude() * 1E6;
					longitude = locArray.get(i).getLongitude() * 1e6;
					geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());
					projection.toPixels(geoPoint, pathpoint);
					pointArray[4 * i] = pathpoint.x;
					pointArray[4 * i + 1] = pathpoint.y;
					latitude = locArray.get(i + 1).getLatitude() * 1E6;
					longitude = locArray.get(i + 1).getLongitude() * 1e6;
					geoPoint = new GeoPoint(latitude.intValue(), longitude.intValue());
					projection.toPixels(geoPoint, pathpoint);
					pointArray[4 * i + 2] = pathpoint.x;
					pointArray[4 * i + 3] = pathpoint.y;
				}
			}
			arrowPoint[0] += point.x;
			arrowPoint[2] += point.x;
			arrowPoint[4] += point.x;
			arrowPoint[6] += point.x;
			arrowPoint[1] += point.y;
			arrowPoint[3] += point.y;
			arrowPoint[5] += point.y;
			arrowPoint[7] += point.y;
			arrowPoint[8] += point.x;
			arrowPoint[9] += point.y;
			arrow.moveTo(arrowPoint[0], arrowPoint[1]);
			arrow.lineTo(arrowPoint[2], arrowPoint[3]);
//			arrow.lineTo(arrowPoint[4], arrowPoint[5]);
//			arrow.lineTo(arrowPoint[6], arrowPoint[7]);
			arrow.cubicTo(arrowPoint[4], arrowPoint[5], arrowPoint[6], arrowPoint[7], arrowPoint[8], arrowPoint[9]);
			arrow.close();

			// pathPaint.setARGB(255, 255, 0, 0);
			pathPaint.setStyle(Paint.Style.STROKE);
			pathPaint.setStrokeWidth(5);
			pathPaint.setColor(Color.RED);
			pathPaint.setAlpha(100);
			//pathPaint.setAntiAlias(true);

			i = point.x;
			j = point.y;
			if (locArray.size() > 1) {
				canvas.drawLines(pointArray, pathPaint);
			}
			canvas.drawPath(arrow, arrowPaint);
			// flgSetLoc = false;
		}
		super.draw(canvas, mapView, shadow);

	}

	@Override
	public boolean onTap(GeoPoint point, MapView mapView) {
		return false;
	}
}
