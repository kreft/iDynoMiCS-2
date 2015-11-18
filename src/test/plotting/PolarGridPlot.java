package test.plotting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.PolarChartPanel;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import grid.CylindricalGrid;

public class PolarGridPlot extends ApplicationFrame {
	private static final long serialVersionUID = 1L;
	CylindricalGrid grid;
	JFreeChart chart;
    
    public PolarGridPlot(final String title, CylindricalGrid grid) {
        super(title);
        this.grid=grid;
        
    	chart = createChart(createDataset(new int[]{0,0,0}));
    	ChartPanel chartPanel = new PolarChartPanel(chart);
    	chartPanel.setPreferredSize(new Dimension(1920, 1080));
    	chartPanel.setEnforceFileExtensions(false);
    	setContentPane(chartPanel);
    }
    
    private XYDataset[] createDataset(int[] wanted) {    
        final XYSeriesCollection[] d = new XYSeriesCollection[3];
        final XYSeries[] s = new XYSeries[3];
        for (int i=0; i<d.length;++i) d[i]=new XYSeriesCollection();
        for (int i=0; i<s.length;++i) s[i]=new XYSeries(i);
        
        int[] current, nbh;
        for ( current = grid.resetIterator(); grid.isIteratorValid();
				  current = grid.iteratorNext())
		{
        	if (linearAlgebra.Vector.areSame(wanted, current)){
        		s[1].add(grid.getVoxelCentre(current)[1]*180/Math.PI, grid.getVoxelCentre(current)[0]*180/Math.PI);
        		for ( nbh = grid.resetNbhIterator(); 
    					grid.isNbhIteratorValid(); nbh = grid.nbhIteratorNext() )
    			{
        			s[2].add(grid.getVoxelCentre(nbh)[1]*180/Math.PI, grid.getVoxelCentre(nbh)[0]*180/Math.PI);
    			}
        	}else s[0].add(grid.getVoxelCentre(current)[1]*180/Math.PI, grid.getVoxelCentre(current)[0]*180/Math.PI);
		}
        
        d[0].addSeries(s[0]);
        d[1].addSeries(s[1]);
        d[2].addSeries(s[2]);

        return d;
    }
    
    private JFreeChart createChart(final XYDataset[] dataset) {
        final JFreeChart chart = ChartFactory.createPolarChart(
            "Polar Grid Iterator", null, true, true, false
        ); 
        final PolarPlot p = (PolarPlot) chart.getPlot();
        PolarItemRenderer[] r = new  PolarItemRenderer[3];
        r[0] = new PolarItemRenderer(Color.white);
        r[1] = new PolarItemRenderer(Color.blue);
        r[2] = new PolarItemRenderer(Color.red);
        p.setDataset(2,dataset[0]);
        p.setRenderer(2,r[0]);
        p.setRenderer(0,r[1]);
        p.setRenderer(1,r[2]);
        return chart;
    } 
    
    public void start(){
    	PolarPlot plot = (PolarPlot) chart.getPlot();
    	int[] current;
    	long t;
        for ( current = grid.resetIterator(); grid.isIteratorValid();
        		current = grid.iteratorNext())
        {
        	t=System.currentTimeMillis();
        	int[] state = linearAlgebra.Vector.copy(current);
        	XYDataset[] dataset = createDataset(state);
        	plot.setDataset(0,dataset[1]);
        	plot.setDataset(1,dataset[2]);
        	grid.setCurrent(state);
        	try {
        		Thread.sleep(Math.max(0, 500-System.currentTimeMillis()+t));
        	} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}

        }
    }
    
    private class PolarItemRenderer extends DefaultPolarItemRenderer {
		private static final long serialVersionUID = 1L;
		Color c;
    	
    	PolarItemRenderer(Color c){
    		this.c=c;
    	}

        @Override
        public void drawSeries(java.awt.Graphics2D g2, java.awt.geom.Rectangle2D dataArea, PlotRenderingInfo info, PolarPlot plot, XYDataset dataset, int seriesIndex) {

        	g2.setColor(c);
            int n = dataset.getItemCount(seriesIndex);
            for (int i = 0; i < n; i++) {

                double theta = dataset.getXValue(seriesIndex, i);
                double radius = dataset.getYValue(seriesIndex, i);
                Point p = plot.translateValueThetaRadiusToJava2D(theta, radius,dataArea);
                Ellipse2D el = new Ellipse2D.Double(p.x, p.y, 10, 10);
                g2.fill(el);
                g2.draw(el);
            }
        }
    }
}