package airptool.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.*;

import airptool.handlers.DadosView;
import airptool.handlers.SimilarityReportHandler;
//import javafx.scene.paint.Color;

import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.viewers.internal.ZoomManager;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphContainer;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;

import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class RecGraph extends ViewPart implements IZoomableWorkbenchPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "airptool.views.RecGraph";
    private int layout = 1;
    private GraphViewer graph;

	public void createPartControl(Composite parent) {
		int nedges=0, i=0, j=0, k=0;
		
		Graph g = new Graph(parent, SWT.NONE);
		g.setPreferredSize(3100, 1000);
		ZoomManager zoomManager = new ZoomManager(
			    g.getRootLayer(), 
			    g.getViewport() );
			zoomManager.setZoomAsText("90%");
			//graph.setControl(g);
		
		Map<String, GraphContainer> packages = new HashMap<String, GraphContainer>();
		Map<String, GraphNode> classes = new HashMap<String, GraphNode>();
		Map<String,Map<String, GraphConnection>> edges = new HashMap<String,Map<String, GraphConnection>>();
		
		for (DadosView dv : SimilarityReportHandler.recTabMC) {
			if(!packages.containsKey(dv.getPkg_ori()) && dv.getFx()>=0.01) {
				packages.put(dv.getPkg_ori(), new GraphContainer(g, SWT.NONE, dv.getPkg_ori()));
				packages.get(dv.getPkg_ori()).open(false);
				packages.get(dv.getPkg_ori()).setScale(110);
				packages.get(dv.getPkg_ori()).setBackgroundColor(new Color(null,204,204,204));
				packages.get(dv.getPkg_ori()).setForegroundColor(new Color(null,0,0,0));
			}
			if(!packages.containsKey(dv.getPkg_des()) && dv.getFx()>=0.01) {
				packages.put(dv.getPkg_des(), new GraphContainer(g, SWT.NONE, dv.getPkg_des()));
				packages.get(dv.getPkg_des()).close(false);
				packages.get(dv.getPkg_des()).setScale(110);
				packages.get(dv.getPkg_des()).setBackgroundColor(new Color(null,204,204,204));
				packages.get(dv.getPkg_des()).setForegroundColor(new Color(null,0,0,0));
			}
			
			if(!classes.containsKey(dv.getClss_ori()) && dv.getFx()>=0.01) {
				classes.put(dv.getClss_ori(), new GraphNode(packages.get(dv.getPkg_ori()), ZestStyles.NODES_FISHEYE,dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)));
				classes.get(dv.getClss_ori()).setBackgroundColor(new Color(null,229,229,229));
				classes.get(dv.getClss_ori()).setForegroundColor(new Color(null,0,0,0));
			}
			
			if(!edges.containsKey(dv.getClss_ori()) && dv.getFx()>=0.01) {
				edges.put(dv.getClss_ori(), new HashMap<String,GraphConnection>());
			}
			
			if(!edges.get(dv.getClss_ori()).containsKey(dv.getPkg_des()) && dv.getFx()>=0.01 ) {
					edges.get(dv.getClss_ori()).put(dv.getPkg_des(), new GraphConnection(g, ZestStyles.CONNECTIONS_DIRECTED,classes.get(dv.getClss_ori()),packages.get(dv.getPkg_des())));
					edges.get(dv.getClss_ori()).get(dv.getPkg_des()).setText("Move "+dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)+" [+"+fmt(dv.getFx())+"]");
					edges.get(dv.getClss_ori()).get(dv.getPkg_des()).setVisible(false);
			}

		}
		
		for (DadosView dv : SimilarityReportHandler.recTabMM) {
			if(!packages.containsKey(dv.getPkg_ori()) && dv.getFx()>=0.01) {
				packages.put(dv.getPkg_ori(), new GraphContainer(g, SWT.NONE, dv.getPkg_ori()));
				packages.get(dv.getPkg_ori()).open(false);
				packages.get(dv.getPkg_ori()).setScale(110);
				packages.get(dv.getPkg_ori()).setBackgroundColor(new Color(null,204,204,204));
				packages.get(dv.getPkg_ori()).setForegroundColor(new Color(null,0,0,0));
			}
			if(!packages.containsKey(dv.getPkg_des()) && dv.getFx()>=0.01) {
				packages.put(dv.getPkg_des(), new GraphContainer(g, SWT.NONE, dv.getPkg_des()));
				packages.get(dv.getPkg_des()).open(false);
				packages.get(dv.getPkg_des()).setScale(110);
				packages.get(dv.getPkg_des()).setBackgroundColor(new Color(null,204,204,204));
				packages.get(dv.getPkg_des()).setForegroundColor(new Color(null,0,0,0));
			}
			else if(packages.containsKey(dv.getPkg_des()) && dv.getFx()>=0.01){
				packages.get(dv.getPkg_des()).open(false);
				packages.get(dv.getPkg_des()).setScale(110);
				packages.get(dv.getPkg_des()).setBackgroundColor(new Color(null,204,204,204));
				packages.get(dv.getPkg_des()).setForegroundColor(new Color(null,0,0,0));
			}
			
			if(!classes.containsKey(dv.getClss_ori()) && dv.getFx()>=0.01) {
				classes.put(dv.getClss_ori(), new GraphNode(packages.get(dv.getPkg_ori()), ZestStyles.NODES_FISHEYE,dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)));
				classes.get(dv.getClss_ori()).setBackgroundColor(new Color(null,229,229,229));
				classes.get(dv.getClss_ori()).setForegroundColor(new Color(null,0,0,0));
			}
			
			if(!classes.containsKey(dv.getClss_des()) && dv.getFx()>=0.01) {
				classes.put(dv.getClss_des(), new GraphNode(packages.get(dv.getPkg_des()), ZestStyles.NODES_FISHEYE,dv.getClss_des().substring(dv.getClss_des().lastIndexOf(".")+1)));
				classes.get(dv.getClss_des()).setBackgroundColor(new Color(null,229,229,229));
				classes.get(dv.getClss_des()).setForegroundColor(new Color(null,0,0,0));
			}
			
			if(!edges.containsKey(dv.getClss_ori()) && dv.getFx()>=0.01) {
				edges.put(dv.getClss_ori(), new HashMap<String,GraphConnection>());
			}

			if(!edges.get(dv.getClss_ori()).containsKey(dv.getClss_des()) && dv.getFx()>=0.01) {
					edges.get(dv.getClss_ori()).put(dv.getClss_des(), new GraphConnection(g, ZestStyles.CONNECTIONS_DIRECTED,classes.get(dv.getClss_ori()),classes.get(dv.getClss_des())));
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText("Move "+dv.getMet_ori()+"() ["+fmt(dv.getFx())+"]");
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setVisible(false);
			}
			else if(dv.getFx()>=0.01){
				String edgesTemp = edges.get(dv.getClss_ori()).get(dv.getClss_des()).getText();
				edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText(edgesTemp+"\nMove "+dv.getMet_ori()+"() ["+fmt(dv.getFx())+"]");
				edges.get(dv.getClss_ori()).get(dv.getClss_des()).setVisible(false);
			}
			
		}
		
		for (DadosView dv : SimilarityReportHandler.recTabEM) {
			if(!packages.containsKey(dv.getPkg_ori()) && dv.getFx()>=0.01) {
				packages.put(dv.getPkg_ori(), new GraphContainer(g, SWT.NONE, dv.getPkg_ori()));
				packages.get(dv.getPkg_ori()).open(false);
				packages.get(dv.getPkg_ori()).setScale(110);
				packages.get(dv.getPkg_ori()).setBackgroundColor(new Color(null,204,204,204));
				packages.get(dv.getPkg_ori()).setForegroundColor(new Color(null,0,0,0));
			}
			
			if(!classes.containsKey(dv.getClss_ori()) && dv.getFx()>=0.01) {
				classes.put(dv.getClss_ori(), new GraphNode(packages.get(dv.getPkg_ori()), ZestStyles.NODES_FISHEYE,dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)));
				//classes.get(dv.getClss_ori()).setBackgroundColor(new Color(null,137, 226, 170));
				classes.get(dv.getClss_ori()).setBackgroundColor(new Color(null,229,229,229));
				classes.get(dv.getClss_ori()).setForegroundColor(new Color(null,0,0,0));
			}
			
				if(!edges.containsKey(dv.getClss_ori()) && dv.getFx()>=0.01) {
				edges.put(dv.getClss_ori(), new HashMap<String,GraphConnection>());
			}
			
			if(!edges.get(dv.getClss_ori()).containsKey(dv.getClss_des()) && dv.getFx()>=0.01) {
					edges.get(dv.getClss_ori()).put(dv.getClss_des(), new GraphConnection(g, ZestStyles.CONNECTIONS_DIRECTED,classes.get(dv.getClss_ori()),classes.get(dv.getClss_ori())));
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setCurveDepth(40);
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setVisible(true);
					
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setFont(new Font(null, "Arial", 15, SWT.NORMAL));
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setLineColor(new Color(null,1, 0, 72));
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setLineWidth(3);
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText("Extract block "+dv.getBlo_ori()+" from\n "+dv.getMet_ori().substring(0, dv.getMet_ori().indexOf("WithExtract"))+"() [+"+fmt(dv.getFx())+"]");
					
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setVisible(true);
					
					//classes.get(dv.getClss_ori()).setBackgroundColor(new Color(null,137, 226, 170));
					classes.get(dv.getClss_ori()).setBorderColor(new Color(null,1, 0, 72));
					classes.get(dv.getClss_ori()).setBorderWidth(2);
			}
			else {
				String edgesTemp = edges.get(dv.getClss_ori()).get(dv.getClss_des()).getText();
				edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText(edgesTemp+"\nExtract block "+dv.getBlo_ori()+" from "+dv.getMet_ori().substring(0, dv.getMet_ori().indexOf("WithExtract"))+"() [+"+fmt(dv.getFx())+"]");
			}
			
		}
		
		for(Map.Entry<String, GraphContainer> entry1 : packages.entrySet()) {
			entry1.getValue().setLayoutAlgorithm(
					new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING),
					true);
		}

		g.setLayoutAlgorithm(
				new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING),
				true);

	}
	
	public static String fmt(double d)
	{
		return new DecimalFormat("##.##").format(d);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	private void fillToolBar() {
        ZoomContributionViewItem toolbarZoomContributionViewItem = new ZoomContributionViewItem(
                this);
        IActionBars bars = getViewSite().getActionBars();
        bars.getMenuManager().add(toolbarZoomContributionViewItem);

    }

	@Override
    public AbstractZoomableViewer getZoomableViewer() {
        //return graph;
		return null;
    }
}
