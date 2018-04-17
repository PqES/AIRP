package airptool.zest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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

import airptool.handlers.DadosView;
import airptool.handlers.SimilarityReportHandler;

public class ZestGraph {

	public ZestGraph() {
		// Create the shell
		int nedges=0, i=0, j=0, k=0;
		Shell shell = new Shell(Display.getCurrent());
		shell.setText("Recommendation Graph");
		shell.setLayout(new FillLayout());
		shell.setSize(800, 800);

		Graph g = new Graph(shell, SWT.NONE);
		g.setPreferredSize(3100, 1000);
		ZoomManager zoomManager = new ZoomManager(
			    g.getRootLayer(), 
			    g.getViewport() );
			zoomManager.setZoomAsText("80%");
		
		Map<String, GraphContainer> packages = new HashMap<String, GraphContainer>();
		Map<String, GraphNode> classes = new HashMap<String, GraphNode>();
		Map<String,Map<String, GraphConnection>> edges = new HashMap<String,Map<String, GraphConnection>>();
		
		for (DadosView dv : SimilarityReportHandler.recTabMC) {
			if(!packages.containsKey(dv.getPkg_ori())) {
				packages.put(dv.getPkg_ori(), new GraphContainer(g, SWT.NONE, dv.getPkg_ori()));
				packages.get(dv.getPkg_ori()).open(false);
			}
			if(!packages.containsKey(dv.getPkg_des())) {
				packages.put(dv.getPkg_des(), new GraphContainer(g, SWT.NONE, dv.getPkg_des()));
				packages.get(dv.getPkg_des()).close(false);
			}
			
			if(!classes.containsKey(dv.getClss_ori())) {
				classes.put(dv.getClss_ori(), new GraphNode(packages.get(dv.getPkg_ori()), ZestStyles.NODES_FISHEYE,dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)));
			}
			
			if(!edges.containsKey(dv.getClss_ori())) {
				edges.put(dv.getClss_ori(), new HashMap<String,GraphConnection>());
			}
			
			if(!edges.get(dv.getClss_ori()).containsKey(dv.getPkg_des())) {
					edges.get(dv.getClss_ori()).put(dv.getPkg_des(), new GraphConnection(g, ZestStyles.CONNECTIONS_DIRECTED,classes.get(dv.getClss_ori()),packages.get(dv.getPkg_des())));
					edges.get(dv.getClss_ori()).get(dv.getPkg_des()).setText("Move Class (+"+dv.getFx()+")");
					edges.get(dv.getClss_ori()).get(dv.getPkg_des()).setVisible(true);
			}
			else {
				String edgesTemp = edges.get(dv.getClss_ori()).get(dv.getPkg_des()).getText();
				edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText(edgesTemp+"\nMove Class (+"+dv.getFx()+")");
			}
			
		}
		
		for (DadosView dv : SimilarityReportHandler.recTabMM) {
			if(!packages.containsKey(dv.getPkg_ori())) {
				packages.put(dv.getPkg_ori(), new GraphContainer(g, SWT.NONE, dv.getPkg_ori()));
				packages.get(dv.getPkg_ori()).open(false);
			}
			if(!packages.containsKey(dv.getPkg_des())) {
				packages.put(dv.getPkg_des(), new GraphContainer(g, SWT.NONE, dv.getPkg_des()));
				packages.get(dv.getPkg_des()).open(false);
			}
			else {
				packages.get(dv.getPkg_des()).open(false);
			}
			
			if(!classes.containsKey(dv.getClss_ori())) {
				classes.put(dv.getClss_ori(), new GraphNode(packages.get(dv.getPkg_ori()), ZestStyles.NODES_FISHEYE,dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)));
			}
			
			if(!classes.containsKey(dv.getClss_des())) {
				classes.put(dv.getClss_des(), new GraphNode(packages.get(dv.getPkg_des()), ZestStyles.NODES_FISHEYE,dv.getClss_des().substring(dv.getClss_des().lastIndexOf(".")+1)));
			}
			
			if(!edges.containsKey(dv.getClss_ori())) {
				edges.put(dv.getClss_ori(), new HashMap<String,GraphConnection>());
			}
			
			if(!edges.get(dv.getClss_ori()).containsKey(dv.getClss_des())) {
					edges.get(dv.getClss_ori()).put(dv.getClss_des(), new GraphConnection(g, ZestStyles.CONNECTIONS_DIRECTED,classes.get(dv.getClss_ori()),classes.get(dv.getClss_des())));
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText("Move "+dv.getMet_ori()+" ("+dv.getFx()+")");
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setVisible(true);
			}
			else {
				String edgesTemp = edges.get(dv.getClss_ori()).get(dv.getClss_des()).getText();
				edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText(edgesTemp+"\nMove "+dv.getMet_ori()+" (+"+dv.getFx()+")");
			}
			
		}
		
		for (DadosView dv : SimilarityReportHandler.recTabEM) {
			if(!packages.containsKey(dv.getPkg_ori())) {
				packages.put(dv.getPkg_ori(), new GraphContainer(g, SWT.NONE, dv.getPkg_ori()));
				packages.get(dv.getPkg_ori()).open(false);
			}
			
			if(!classes.containsKey(dv.getClss_ori())) {
				classes.put(dv.getClss_ori(), new GraphNode(packages.get(dv.getPkg_ori()), ZestStyles.NODES_FISHEYE,dv.getClss_ori().substring(dv.getClss_ori().lastIndexOf(".")+1)));
			}
			
			if(!edges.containsKey(dv.getClss_ori())) {
				edges.put(dv.getClss_ori(), new HashMap<String,GraphConnection>());
			}
			
			if(!edges.get(dv.getClss_ori()).containsKey(dv.getClss_des())) {
					edges.get(dv.getClss_ori()).put(dv.getClss_des(), new GraphConnection(g, ZestStyles.CONNECTIONS_DIRECTED,classes.get(dv.getClss_ori()),classes.get(dv.getClss_ori())));
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText("Extract block "+dv.getBlo_ori()+" from "+dv.getMet_ori()+" (+"+dv.getFx()+")");
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setCurveDepth(20);
					edges.get(dv.getClss_ori()).get(dv.getClss_des()).setVisible(true);
			}
			else {
				String edgesTemp = edges.get(dv.getClss_ori()).get(dv.getClss_des()).getText();
				edges.get(dv.getClss_ori()).get(dv.getClss_des()).setText(edgesTemp+"\nExtract block "+dv.getBlo_ori()+" from "+dv.getMet_ori()+" (+"+dv.getFx()+")");
			}
			
		}
		
		for(Map.Entry<String, GraphContainer> entry1 : packages.entrySet()) {
			entry1.getValue().setLayoutAlgorithm(
					new GridLayoutAlgorithm(LayoutStyles.ENFORCE_BOUNDS),
					true);
		}

		g.setLayoutAlgorithm(
				new GridLayoutAlgorithm(LayoutStyles.ENFORCE_BOUNDS),
				true);

		shell.open();
		
		
	}
}
