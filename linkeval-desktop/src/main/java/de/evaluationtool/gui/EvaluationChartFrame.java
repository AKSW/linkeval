package de.evaluationtool.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.shef.wit.simmetrics.metrichandlers.MetricHandler;

/**
 * A demo showing the addition and removal of multiple datasets / renderers.
 */
@SuppressWarnings("serial") public class EvaluationChartFrame extends JFrame implements ActionListener {
	
    /** The plot. */
    private XYPlot plot;
    
    final EvaluationFrame frame;
    
    /** The index of the last dataset added. */
    private int datasetIndex = 0;
    
    private final XYDataset[] datasets;
    private final boolean[] toggles; 
    static final XYDataset EMPTY_DATASET = new XYSeriesCollection(new XYSeries(""));

    final JButton[] toggleButtons;
    final JComboBox<String> metricBox;
    final JButton changeMetricButton;
    /**
     * Constructs a new demonstration application.
     *
     * @param title  the frame title.
     */
    public EvaluationChartFrame(final EvaluationFrame frame, final String title, XYDataset precisionDataset, XYDataset recallDataset,XYDataset fScoreDataset)
    {    	
        super(title);
        this.frame = frame;
        MetricHandler.createMetric("");
        datasets = new XYDataset[] {precisionDataset,recallDataset,fScoreDataset};
        String[] descriptions = {"precision","recall","f0.5-score"};
        toggles = new boolean[datasets.length];
        Arrays.fill(toggles,true);
       // final TimeSeriesCollection dataset1 = createRandomDataset("Series 1");
        final JFreeChart chart = ChartFactory.createXYLineChart("", "Confidence Cutoff", "", datasets[0], PlotOrientation.VERTICAL,true, true, false);
        chart.setBackgroundPaint(Color.white);
        
        this.plot = chart.getXYPlot();
        this.plot.setBackgroundPaint(Color.lightGray);
        this.plot.setDomainGridlinePaint(Color.white);
        this.plot.setRangeGridlinePaint(Color.white);
        
//        XYItemRenderer renderer = plot.getRenderer();
//        renderer.setSeriesPaint(0, Color.green);
//        renderer.setSeriesPaint(1, Color.red);
//        renderer.setSeriesPaint(2, Color.blue);
         
        final ValueAxis axis = this.plot.getDomainAxis();
//        axis.setLowerMargin(0.05);
//        axis.setUpperMargin(0.05);
//        axis.setLowerBound(-0.01);
//        axis.setUpperBound(1.1);
        
        axis.setAutoRange(true);
//
        final ValueAxis axisY = this.plot.getRangeAxis();
        axisY.setLowerBound(-0.01);
        axisY.setUpperBound(1.01);
        //axisY.setAutoRange(true);
        
        final JPanel content = new JPanel(new BorderLayout());

        final ChartPanel chartPanel = new ChartPanel(chart);
        content.add(chartPanel);

        final JPanel buttonPanel = new JPanel(new FlowLayout());

        String[] metricNames = MetricHandler.GetMetricsAvailable().toArray(new String[0]);
        metricBox = new JComboBox<String>(metricNames);
        buttonPanel.add(metricBox);
        metricBox.addActionListener(this);
        
        changeMetricButton = new JButton("Apply metric");
        buttonPanel.add(changeMetricButton);
        changeMetricButton.addActionListener(this);
        
    	toggleButtons = new JButton[datasets.length];
        for(int i = 0;i<datasets.length;i++)
        {        
        	JButton button = new JButton(descriptions[i]);
        	toggleButtons[i] = button;
        	buttonPanel.add(button);
        	button.addActionListener(this);

        	if(i!=0) {addDataset(datasets[i]);}
        	this.plot.setRenderer(i, new StandardXYItemRenderer());
        }
//        final JButton button1 = new JButton("Add Dataset");
//        button1.setActionCommand("ADD_DATASET");
//        button1.addActionListener(this);
//        
//        final JButton button2 = new JButton("Remove Dataset");
//        button2.setActionCommand("REMOVE_DATASET");
//        button2.addActionListener(this);

//        buttonPanel.add(button1);
//        buttonPanel.add(button2);
        
        content.add(buttonPanel, BorderLayout.SOUTH);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 600));
        
        setContentPane(content);     
    }

//    /**
//     * Creates a random dataset.
//     * 
//     * @param name  the series name.
//     * 
//     * @return The dataset.
//     */
//    private TimeSeriesCollection createRandomDataset(final String name) {
//        final TimeSeries series = new TimeSeries(name);
//        double value = 100.0;
//        RegularTimePeriod t = new Day();
//        for (int i = 0; i < 50; i++) {
//            series.add(t, value);
//            t = t.next();
//            value = value * (1.0 + Math.random() / 100);
//        }
//        return new TimeSeriesCollection(series);
//    }
    
    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    public void addDataset(XYDataset dataset)
    {
    	this.datasetIndex++;
    	 this.plot.setDataset(this.datasetIndex, dataset);
    }
    
    public void actionPerformed(final ActionEvent e)
    {
        for(int i = 0;i<datasets.length;i++)
        {
        	if(e.getSource()==this.changeMetricButton)
        	{
        		frame.applyMetric(metricBox.getSelectedItem().toString());
        		this.setVisible(false);
        		frame.evaluate();
        	}
        	if(e.getSource()==toggleButtons[i])
        	{	
        		toggles[i]^=true;
        		this.plot.setDataset(i, toggles[i]?datasets[i]:EMPTY_DATASET);
        	}
        }
//        if (e.getActionCommand().equals("ADD_DATASET")) {
//            if (this.datasetIndex < 20) {
//                this.datasetIndex++;
//                this.plot.setDataset(
//                    this.datasetIndex, createRandomDataset("S" + this.datasetIndex)
//                );
//                this.plot.setRenderer(this.datasetIndex, new StandardXYItemRenderer());
//            }
//        }
//        else if (e.getActionCommand().equals("REMOVE_DATASET")) {
//            if (this.datasetIndex >= 1) {
//                this.plot.setDataset(this.datasetIndex, null);
//                this.plot.setRenderer(this.datasetIndex, null);
//                this.datasetIndex--;
//            }
//        }
//        
    }

//    /**
//     * Starting point for the demonstration application.
//     *
//     * @param args  ignored.
//     */
//    public static void main(final String[] args) {
//
//        final EvaluationChartFrame demo = new EvaluationChartFrame("Multiple Dataset Demo 1");
//        demo.pack();
//        RefineryUtilities.centerFrameOnScreen(demo);
//        demo.setVisible(true);
//
//    }

}