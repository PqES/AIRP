package airptool.jung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import airptool.core.DataStructure;
import airptool.util.AirpUtil;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class RecGraph extends javax.swing.JFrame {
	
	Graph<String, String> g;
	VisualizationViewer<String, String> vv;
	AbstractModalGraphMouse graphMouse;
	ArrayList<PositionXY> positions = new ArrayList<PositionXY>();
	Polygon arrowHead = new Polygon();
	AffineTransform tx = new AffineTransform();
    
    public RecGraph(Map<String, HashMap<String, Collection<? extends Object>>> classDependencies) {
    	g = getGraph(classDependencies);
        vv = 
            new VisualizationViewer<String, String>(
                new FRLayout<String, String>(g));

        graphMouse = 
            new DefaultModalGraphMouse<String,String>();
        vv.setGraphMouse(graphMouse);
        
    	init(classDependencies);
    }
    
    private void init(Map<String, HashMap<String, Collection<? extends Object>>> classDependencies) {

        for (Map.Entry<String, HashMap<String, Collection<? extends Object>>> entry : classDependencies.entrySet()) {
        	List<String> verticesInBox = new ArrayList<String>();
        	for (Map.Entry<String, Collection<? extends Object>> entry2 : entry.getValue().entrySet()) {
        		verticesInBox.add(entry.getKey()+":"+entry2.getKey());
			}
        	addVertexGroupPainter(vv, verticesInBox, entry.getKey());
        	verticesInBox.clear();
        }

        getContentPane().add(vv);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
    
    private <V> void addVertexGroupPainter(
            final VisualizationViewer<V, ?> vv, final Iterable<V> verticesInBox, final String pkg)
        {
            vv.addPreRenderPaintable(new VisualizationViewer.Paintable()
            {
                @Override
                public boolean useTransform()
                {
                    return true;
                }

                @Override
                public void paint(Graphics gr)
                {
                    Graphics2D g = (Graphics2D)gr;

                    Layout<V, ?> layout = vv.getGraphLayout();
                    AffineTransform transform = 
                        vv.getRenderContext().
                           getMultiLayerTransformer().
                           getTransformer(Layer.LAYOUT).
                           getTransform();
                    Rectangle2D boundingBox = 
                        computeBoundingBox(verticesInBox, layout, transform, pkg);

                    double d = 20;
                    Shape rect = new RoundRectangle2D.Double(
                        boundingBox.getMinX()-d, 
                        boundingBox.getMinY()-d,
                        boundingBox.getWidth()+d+d,
                        boundingBox.getHeight()+d+d, d, d);
                    g.setColor(new Color(255,200,200));
                    g.fill(rect);
                    g.setColor(Color.BLACK);
                    g.drawString(pkg,70,20);
                    g.draw(rect);

                }
            });
        }


        private <V> Rectangle2D computeBoundingBox(
            Iterable<V> vertices, Layout<V, ?> layout, AffineTransform at, String pkg)
        {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            for (V vertex : vertices)
            {
                Point2D location = layout.transform(vertex);
                at.transform(location, location);
                minX = Math.min(minX, location.getX());
                minY = Math.min(minY, location.getY());
                maxX = Math.max(maxX, location.getX());
                maxY = Math.max(maxY, location.getY());
            }
            positions.add(new PositionXY(pkg, minX, minY, maxY-minY));
            return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
        }


        public Graph<String, String> getGraph(Map<String, HashMap<String, Collection<? extends Object>>> classDependencies) 
        {
            Graph<String, String> g = new DirectedSparseGraph<String, String>();
            for (Map.Entry<String, HashMap<String, Collection<? extends Object>>> entry : classDependencies.entrySet()) {
				//pkgCells[i] = graph.insertVertex(parent, null, entry.getKey(), pkgX, pkgY, 80, 30);
				
				for (Map.Entry<String, Collection<? extends Object>> entry2 : entry.getValue().entrySet()) {
	        		g.addVertex(entry.getKey()+":"+entry2.getKey());
				}
				
            }
           return g;
        }
        
        public void addEdge(String e, String v1, String v2){
        	g.addEdge(e, v1, v2);
        	vv.setGraphLayout(
                    new FRLayout<String, String>(g));

            graphMouse = 
                new DefaultModalGraphMouse<String,String>();
            vv.setGraphMouse(graphMouse);
        }
        
        public void drawEdge(String e, final String v1, final String v2){
        	arrowHead.addPoint( 0,5);
        	arrowHead.addPoint( -5, -5);
        	arrowHead.addPoint( 5,-5);
        	
        	final Renderer.Vertex<String, String> originalVertexRenderer = 
        	        vv.getRenderer().getVertexRenderer();
        	    vv.getRenderer().setVertexRenderer(new Renderer.Vertex<String, String>()
        	    {
        	        @Override
        	        public void paintVertex(RenderContext<String, String> rc,
        	            Layout<String, String> layout, String vertex)
        	        {
        	        	if (vertex.equals(v1)){
	        	        	for(PositionXY posxy : positions){
		        	        	String pkg = posxy.getPkg();
	        	            	if (pkg.equals(v2)){
		        	                Point2D p = layout.transform(vertex);
		        	                Line2D.Double pseudoEdge = new Line2D.Double(
		        	                    p.getX(), p.getY(), p.getX() + 100, p.getY() + 100); 
		        	                rc.getGraphicsContext().draw(pseudoEdge);
		        	                drawArrowHead(rc.getGraphicsContext(), pseudoEdge);        
		        	            }
		        	            originalVertexRenderer.paintVertex(rc, layout, vertex);
	        	            }
        	        	}
        	        }
        	    });
        	
        	vv.setGraphLayout(
                    new FRLayout<String, String>(g));

            graphMouse = 
                new DefaultModalGraphMouse<String,String>();
            vv.setGraphMouse(graphMouse);
            
            Transformer<String, Paint> edgePaint = new Transformer<String, Paint>() {
                public Paint transform(String s) {
                    if(s.startsWith("fake")){
                    	return new Color(255, 255, 255, 0);
                    }
                    else{
                    	return Color.BLACK;
                    }
                }
            };
            
            vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
            g.addEdge("fake:"+e, v1, v2);
        }
        
        private void drawArrowHead(GraphicsDecorator gd, Line2D.Double line) {  
            tx.setToIdentity();
            double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
            tx.translate(line.x2, line.y2);
            tx.rotate((angle-Math.PI/2d));  

            Graphics2D g = (Graphics2D) gd.create();
            g.setTransform(tx);   
            g.fill(arrowHead);
            g.dispose();
        }
}
