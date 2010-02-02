///////////////////////////////////////////////////////////////////////////////
//FILE:           SurveyorRoiManager.java
//PROJECT:        Micro-Manager
//SUBSYSTEM:      Micro-Manager Plugin
//-----------------------------------------------------------------------------
//
//AUTHOR:         Arthur Edelstein, arthuredelstein@gmail.com, September 2009
//
//COPYRIGHT:      University of California, San Francisco, 2009
//                
//LICENSE:        This file is distributed under the LGPL license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
///////////////////////////////////////////////////////////////////////////////

package org.micromanager.surveyor;


import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;

import org.micromanager.api.ScriptInterface;
import org.micromanager.navigation.PositionList;
import org.micromanager.navigation.MultiStagePosition;
//import org.micromanager.surveyor.Surveyor.CoordinateMap;
import org.micromanager.utils.MMScriptException;

import ij.gui.Roi;
import ij.gui.ShapeRoi;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import org.micromanager.utils.JavaUtils;
import org.micromanager.utils.ReportingUtils;




public class RoiManager extends ij.plugin.frame.RoiManager{
	/**
	 * 
	 */
    private static RoiManager instance;

	private static final long serialVersionUID = 8610461471406234227L;
	
	private int overlap_;

	private CMMCore mmc;

	//private CoordinateMap mosaicCoords_;
	private Coordinates surveyorCoords_;
	private ScriptInterface app_;
	private Hub hub_;
    private Controller controller_;
	private Dimension tileDimensions_;

	public RoiManager(Hub hub) {
        super();
        instance = this;

		hub_ = hub;
        this.setVisible(false);

        app_ = hub.getApp();

        updateMappings();

		//mosaicCoords_ = surveyorCoords_.clone();
		//mosaicCoords_.setMag(1.0);

		overlap_ = 8;
		

		mmc = app_.getMMCore();
        


        /*
        try {
            JavaUtils.invokeRestrictedMethod(this, super.getClass(), "showAll");
            JavaUtils.invokeRestrictedMethod(this, super.getClass(), "updateShowAll");
        } catch (Exception e) {
            ReportingUtils.logError(e);
        }
         * */

	}

    public ArrayList<Point> getFrameCentersInRoi(ArrayList<Point> acqTraj, Roi acqRoiOnMap, Dimension frameDimensions) {
        ArrayList<Point> minAcqTraj = new ArrayList<Point>();
        for (Point pt : acqTraj) {
            Roi tileRoi = new Roi(pt.x-frameDimensions.width/2, pt.y-frameDimensions.height/2, frameDimensions.width, frameDimensions.height);
            Rectangle br = (new ShapeRoi(acqRoiOnMap)).and(new ShapeRoi(tileRoi)).getBoundingRect();
            if (br.width > 0 || br.height > 0) {
                minAcqTraj.add(pt);
            }
        }
        return minAcqTraj;
    }

    public ArrayList<Point> getFrameCentersOnRoiBoundingRect(Rectangle acqRect, Dimension frameDimensions) {
        int smallWidth = frameDimensions.width-overlap_;
        int smallHeight = frameDimensions.height-overlap_;

        int nx = (int) ((acqRect.width)/(float)(smallWidth) + 0.999);
        int ny = (int) ((acqRect.height)/(float)(smallHeight) + 0.999);
        
        if (nx <= 0) {
            nx = 1;
        }
        if (ny <= 0) {
            ny = 1;
        }

        int mosaicWidth = nx*smallWidth + overlap_;
        int mosaicHeight = nx*smallHeight + overlap_;

        int extraWidth = mosaicWidth - acqRect.width;
        int extraHeight = mosaicHeight - acqRect.height;

        int mosaicLeft = acqRect.x - extraWidth/2;
        int mosaicTop = acqRect.y - extraHeight/2;

        double i;
        double j;
        ArrayList<Point> acqTraj = new ArrayList<Point>();
        for (i=0.5; i<nx; ++i) {
            for (j=0.5; j<ny; ++j) {
                int tileX = (int) (i*smallWidth + mosaicLeft);
                int tileY = (int) (j*smallHeight + mosaicTop);
                acqTraj.add(new Point(tileX, tileY));
            }
        }
        System.out.println("ul frame center: "+acqTraj.get(0));
        return acqTraj;
    }

    public void updateMappings() {
        controller_ = hub_.getController();
        tileDimensions_ = hub_.getTileDimensions();
        surveyorCoords_ = hub_.getCoordinates();
    }

    public static RoiManager getInstance() {
         return instance;
    }

    public ArrayList<Point> generateRoiTrajectory(Roi acqRoiOffScreen) {
        Rectangle frameRect = surveyorCoords_.offScreenToRoiRect(new Point(0,0));
        Dimension frameDimensions = new Dimension(frameRect.width, frameRect.height);
		Rectangle acqRect = acqRoiOffScreen.getBoundingRect();
        System.out.println("acqRect: "+acqRect);

        ArrayList<Point> latticeInRectangle = getFrameCentersOnRoiBoundingRect(acqRect, frameDimensions);

		if (acqRoiOffScreen.getType() == Roi.POINT)
			return latticeInRectangle;

        ArrayList<Point> minAcqTraj = getFrameCentersInRoi(latticeInRectangle, acqRoiOffScreen, frameDimensions);

		return minAcqTraj;
	}

	
	
	void addRoiTrajectoryToPositionList(ArrayList<Point> acqTraj, int roiNumber, PositionList posList) {
		String xystage = mmc.getXYStageDevice();
		String zstage = mmc.getFocusDevice();

		int tileCount = 0;
		for (Point acqPt:acqTraj) {
			tileCount++;
            Point mapPt = surveyorCoords_.offScreenClickToMap(acqPt);
			Point2D.Double stagePos = controller_.mapToStage(mapPt);
            //System.out.println("acqPt: "+acqPt + "computed stagePos: "+stagePos);
			MultiStagePosition msp = new MultiStagePosition(xystage, stagePos.x, stagePos.y, zstage, 0);
			msp.setLabel("roi"+roiNumber+"."+tileCount);
			posList.addPosition(msp);
		}
		try {
			app_.setPositionList(posList);
		} catch (MMScriptException e) {
			ReportingUtils.logError(e);
		}
	}

	/*
	void scanRoiTrajectory(ArrayList<Point> acqTraj) {
		for (Point acqPt:acqTraj) {
			Point2D.Double stagePos = coords_.offScreenToStage(acqPt);
			surveyor_.stageGo(stagePos);
			String xystage = mmc.getXYStageDevice();
			while(mmc.deviceBusy(xystage));
			updateMap(true);
		}
	}
*/
	PositionList convertRoiManagerToPositionList() {

		
		int roiCount = 0;
		PositionList posList = new PositionList();
		Roi[] acqRois = this.getRoisAsArray();

		for (Roi acqRoi:acqRois) {
			roiCount++;
            //Roi mapRoi = surveyorCoords_.roiOffScreenToMap(acqRoi);
            //if (mapRoi != null) {
                //System.out.println("mapRoi: "+mapRoi.getBoundingRect());
                ArrayList<Point> traj = generateRoiTrajectory(acqRoi);
                System.out.println("traj :" + traj);
                addRoiTrajectoryToPositionList(traj, roiCount, posList);
            //}
			//scanRoiTrajectory(traj);
		}
		displayPositionList(posList);
		return posList;
	}




	public void displayPositionList(PositionList posList) {
		System.out.println("Position list: ");
		for (int i=0;i<posList.getNumberOfPositions();++i) {
			MultiStagePosition pos = posList.getPosition(i);
			System.out.println("    ["+pos.getX() +","+ pos.getY()+"]");
		}
	}

    public class ResizableShapeRoi extends ShapeRoi {

        ResizableShapeRoi(Roi roi) {
            super(roi);
        }

        public void scaleToMap() {
            int factor = 1 << -surveyorCoords_.zoomLevel_;
            Shape originalShape = getShape();
            AffineTransform at = new AffineTransform();
            at.scale(factor, factor);
            setShape(at.createTransformedShape(originalShape));
        }

        public Shape getShape() {
            try {
                System.out.println(super.getClass());
                return (Shape) JavaUtils.invokeRestrictedMethod(this, this.getClass().getSuperclass(), "getShape");
            } catch (Exception ex) {
                ReportingUtils.logError(ex);
                return null;
            }  
        }

        public void setShape(Shape shape) {
            try {
                JavaUtils.invokeRestrictedMethod(this, this.getClass().getSuperclass(), "setShape", shape, Shape.class);
            } catch (Exception ex) {
                ReportingUtils.logError(ex);
            }
        }

    }

}

