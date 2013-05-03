package de.linkeval.web;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalSplitPanel;
/*import de.steinwedel.messagebox.ButtonId;
import de.steinwedel.messagebox.Icon;
import de.steinwedel.messagebox.MessageBox;
*/
/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class LinkEvalUi extends UI
{
	
	// Data structures declarations
	List<LinkCandidate> infoList;
	String userId="",userName="",task="";
	
	Panel displayedPanel;
    @Override
    protected void init(VaadinRequest request) 
    {
    	
    	displayedPanel=designLoginPanel();
       	setContent(displayedPanel);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private Panel designLoginPanel()
    {
    	
    	final Panel loginPanel =new Panel("Login");
    	HashMap<String, String> users=null;
   
    	loginPanel.setWidth("100%");
    	loginPanel.setHeight("100%");
      	// Create absolute layout specifying its properties
    	final AbsoluteLayout loginLayout= new AbsoluteLayout();
    	loginLayout.setSizeFull();
    	// Create components Objects and specify their properties
    	int startLeft=200,startTop=300;
    	try {
			users=getLoginInfo("jdbc:mysql://localhost:3306/","linkeval","root","mofo");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	Button btnLogin = new Button("Login"); 
       	final ComboBox cmbUser = new ComboBox("Select your USER ID");
        final ComboBox cmbTask = new ComboBox("Select your TASK");
        cmbUser.setNullSelectionAllowed(false);
        cmbTask.setNullSelectionAllowed(false);
        //Add data
        Iterator it = users.entrySet().iterator();
        while (it.hasNext()) 
        {
            Map.Entry pairs = (Map.Entry) it.next();
            cmbUser.addItem(pairs.getKey()); 
            cmbUser.setItemCaption(pairs.getKey(),pairs.getValue().toString());
            it.remove(); // avoids a ConcurrentModificationException
        }

        cmbUser.setValue(cmbUser.getItemIds().iterator().next());
        
        //Listeners

        btnLogin.addClickListener(new Button.ClickListener() 
        {
            @Override
			public void buttonClick(ClickEvent event) 
            {
            	userId = String.valueOf(cmbUser.getValue()); //which is his Id in tabel as this combo box shows names as captions and Ids as values
            	userName=cmbUser.getItemCaption(userId);
                task = String.valueOf(cmbTask.getValue());
                if(task != "" && userId != "" )
                {
	            	VerticalSplitPanel vsplit = new VerticalSplitPanel();
	            	displayedPanel=designMainPanel();
	            	vsplit.setSecondComponent(displayedPanel);
	            	vsplit.setFirstComponent(allLinksDetails());
	            	UI.getCurrent().setContent(vsplit);
                }
            }
        });
        cmbUser.addFocusListener(new FocusListener() {
			
			@Override
			public void focus(FocusEvent event) {
				// TODO Auto-generated method stub
				cmbUser.setValue(cmbUser.getItemIds().iterator().next());
			}
		});
        cmbUser.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) 
            {
                final String valueString = String.valueOf(event.getProperty().getValue());
                
                try {
     				List<String>  tasks= getTasksInfo("jdbc:mysql://localhost:3306/","linkeval","root","mofo",valueString);
     				
     				cmbTask.removeAllItems();
     				if(tasks != null)
     				{
	     				for (String task : tasks) 
	     				{
	     					cmbTask.addItem(task);
	     				}
	     				cmbTask.setValue(cmbTask.getItemIds().iterator().next());
     				}	
     			} catch (SQLException e) {
     				// TODO Auto-generated catch block
     				Notification.show(e.getMessage()) ;
     			}
            }
        });

        loginLayout.addComponent(cmbUser, "left: 800px; top: 200px;");
        loginLayout.addComponent(cmbTask, "left: 800px; top: 300px;");
        loginLayout.addComponent(btnLogin, "left: 900px; top: 400px;");

        loginPanel.setContent(loginLayout);
    	
       	return loginPanel;
    }
 ///////////////////////////////////////////////////////////////////////////////////////////   
   
    Label source,destination;
    Table tblSourceDestinationparam,tblSourcePropertiesParam,tblDestinationPropertiesParam; //pass tables between different panels
    long lStartTime =0;
	long lEndTime=0; 
	boolean newLink=true;
	Table tblSourceDestination = new Table("Source and Destination URIs");
    private Panel allLinksDetails()
    {
    	//positioning indices
    	//final int leftStart=10,topStart=10, space=100;
    	//create the panel that will hold all components
    	Panel linksDetails = new Panel("Links Details");
    	//set panel's properties   	
    	linksDetails.setWidth("100%");
    	linksDetails.setHeight("100%");
    	
      	// Create absolute layout specifying its properties
    	final AbsoluteLayout layout = new AbsoluteLayout();
    	//set layout's properties
    	layout.setSizeFull();
     	
    	// Create components Objects and specify their properties
       	Button btnLoad = new Button("Load task");
    	//final Table tblSourceDestination = new Table("Source and Destination URIs");
     	tblSourceDestination.setSelectable(true);     	
    	tblSourceDestination.setWidth("90%");    	
    	//fill the Source and Destination URIs table
    	SQLContainer container=connectToDB("root", "mofo",userName);
    	Notification.show("Welcome "+userName+" you loaded task Nr.: "+task);
    	Compare.Equal suburbFilter = new Compare.Equal("taskId",Integer.valueOf(task));
    	container.addContainerFilter(suburbFilter);
    	//Fill the main (tblSourceDestination) table with resources
    	tblSourceDestination.setContainerDataSource(container);
    	tblSourceDestinationparam=tblSourceDestination;
    	
    	// add component to the layout specifying its position on the layout
    	int tableWidth = (int) tblSourceDestination.getWidth();
    	layout.addComponent(btnLoad, "left:1500px; top: 10px;");
    	layout.addComponent(tblSourceDestination,"left: 10px; top: 20px;");
    	
    	linksDetails.setContent(layout);
    	// Add EventHandlers for some of the components
    	
    	
    	btnLoad.addClickListener(new Button.ClickListener() 
        {
            @Override
			public void buttonClick(ClickEvent event) 
            {
           		//lStartTime= System.currentTimeMillis();
        
            	try
            	{
	            	Object rowId =tblSourceDestination.getValue();
	                Property sourceProperty=tblSourceDestination.getContainerProperty(rowId,"sourceURI");
	                Property destinationProperty=tblSourceDestination.getContainerProperty(rowId,"destinationURI");
	                
	                source.setValue(sourceProperty.toString());
	                destination.setValue(destinationProperty.toString());
	                tblSourcePropertiesParam.removeAllItems();
	                tblDestinationPropertiesParam.removeAllItems();
	                
	                
	                Notification loadURI= new Notification("");
	                loadURI.show("Links' URIs are successfully loaded ");
            	}
            	catch(Exception e)
            	{
            		Notification error= new Notification("Error");
            		error.show("You did not select an item in the links table");
            	}

            }
        });
    	
    	tblSourceDestination.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
			public void itemClick(ItemClickEvent event) 
            {
            	/*lStartTime= System.currentTimeMillis();
                
            	try
            	{
	            	Object rowId =tblSourceDestination.getValue();
	                Property sourceProperty=tblSourceDestination.getContainerProperty(rowId,"sourceURI");
	                Property destinationProperty=tblSourceDestination.getContainerProperty(rowId,"destinationURI");
	                
	                source.setValue(sourceProperty.toString());
	                destination.setValue(destinationProperty.toString());
	                tblSourcePropertiesParam.removeAllItems();
	                tblDestinationPropertiesParam.removeAllItems();
	                
	                
	                Notification loadURI= new Notification("");
	                loadURI.show("Links' URIs are successfully loaded ");
            	}
            	catch(Exception e)
            	{
            		Notification error= new Notification("Error");
            		error.show("You did not select an item in the links table");
            	}*/
            }
        });
    	
    	
     	return linksDetails;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    private boolean checkIfCached(int taskid)
    {
    	return true;
    }
    private void cachingForTriples(Table table, String endpoint)
    {
    	Model model = ModelFactory.createDefaultModel();
    	List<String> resources=null;
    	FileWriter fstream=null;
		try {
			fstream = new FileWriter("/home/mofeed/TrialStart/zicozico.nt");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedWriter out = new BufferedWriter(fstream);
    	try
    	{
	    	for (Object id : table.getItemIds()) 
	    	{
	    		Item item = table.getItem(id);
	    		Property sourceURI = item.getItemProperty("sourceURI");//vaadin property not jena
	    		Resource resource = model.createResource();
	    		try
	        	{
	            	String sparqlQuery="select distinct * where { <"+sourceURI.getValue()+"> ?p  ?o .}";
	    	        Query query = QueryFactory.create(sparqlQuery);
	    			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
	    			com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
	    			com.hp.hpl.jena.query.QuerySolution binding=null;
	    		    while (results.hasNext()) 
	    		    {
	    		    	binding = results.next();
	    		    	String property=binding.getResource("?p").toString();
	    		    	String value;
	    		    	if(binding.get("?o").isResource())
	    		    		value=binding.getResource("?o").toString();
	    		    	else
	    		    		value=binding.getLiteral("?o").toString();
	    		    	com.hp.hpl.jena.rdf.model.Property pt = ResourceFactory.createProperty(property);
	    		    	//resource.addProperty(pt, value);
	    		    	model.add(resource, pt, value);
	    		    }
	    		    qexec.close() ;
	    		    model.write(out, null, "TURTLE");
	        	}
	        	catch (Exception e)
	    	 	  {
	    		        Notification.show(e.toString());
	    	 	  }
	    		//model.add(s, p, o);
	    		//Property destinationURI = item.getItemProperty("destinationURI");
   			   // out.write(sourceURI.getValue()+"\n");
			}
	    	/*out.flush();
	    	fstream.flush();*/
	    	out.close();
    	}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    private SQLContainer connectToDB(String userName, String passWord, String table)
    {
    	SimpleJDBCConnectionPool connectionPool;  
    	SQLContainer container=null;
        try
        {
        	connectionPool = new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/linkeval",userName, passWord, 2,7);
        	TableQuery tq = new TableQuery(table, connectionPool);
        	container = new SQLContainer(tq);
        }
        catch(Exception e)
        {
        	Notification.show(e.getMessage());
        }
        return container;
    }
    private SQLContainer connectToDB(String userName, String passWord)
    {//has problems
    	SimpleJDBCConnectionPool connectionPool;  
    	SQLContainer container=null;
        try
        {
        	connectionPool = new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/linkeval",userName, passWord, 2,7);
        	FreeformQuery query = new FreeformQuery("SELECT endpoint FROM Endpoints", connectionPool);
        	container = new SQLContainer(query);
        }
        catch(Exception e)
        {
        	Notification.show(e.getMessage());
        }
        return container;
    }
    private SQLContainer getSuggestedProperties(String userName, String passWord)
    {//has problems
    	SimpleJDBCConnectionPool connectionPool;  
    	SQLContainer container=null;
        try
        {
        	connectionPool = new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/linkeval",userName, passWord, 2,7);
        	FreeformQuery query = new FreeformQuery("SELECT property " +
        											"FROM Properties AS P,PropertiesSuggestions AS S" +
        											" WHERE P.Id = S.propertyId AND S.taskId = "+task, connectionPool);
        	container = new SQLContainer(query);
        }
        catch(Exception e)
        {
        	Notification.show(e.getMessage());
        }
        return container;
    }
    
    
    
    
    //////////////////////////////////////////////////////////////////////////////////////////
    private Panel designMainPanel()
    {
    	//positioning indices
    	final int leftStart=450,topStart=10, space=100;
    	//create the panel that will hold all components
    	
    	Panel pnlURIsProperties = new Panel("URI Display");
    	   	
    	pnlURIsProperties.setWidth("100%");
    	pnlURIsProperties.setHeight("100%");
    	
      	// Create absolute layout specifying its properties
    	final AbsoluteLayout layout = new AbsoluteLayout();
       	layout.setWidth("100%");
    	layout.setHeight("100%");
    	layout.setSizeFull();
     	
    	// Create components Objects and specify their properties
       	
    	Button btnCorrect = new Button("Correct");
    	Button btnIncorrect = new Button("Incorrect");
    	Button btnUnsure = new Button("Unsure");
    	Button btnGetProperties= new Button("Get properties");
    	
    	final NativeSelect cmbSourceEndpoint= new NativeSelect("Source Endpoint");
    	final NativeSelect cmbDestinationEndpoint= new NativeSelect("Destination Endpoint");
    	final ListSelect lstSuggestedProperties = new ListSelect("Lookup Properties");
    	// to load properties of loaded resources automatically
    	final CheckBox chkAutomaticPropertiesLoad = new CheckBox("Automatic Properties loading (next time)");
    	chkAutomaticPropertiesLoad.setValue(false);
    	
    	cmbSourceEndpoint.setNullSelectionAllowed(false);
    	cmbDestinationEndpoint.setNullSelectionAllowed(false);
    	
    	lstSuggestedProperties.setRows(4);
    	lstSuggestedProperties.setNullSelectionAllowed(false);

    	source= new Label("Source URI");
    	destination= new Label("Destination URI");
    	
    	final Table tblSourcePropertiesMapping = new Table("Source Properties");
    	final Table tblDestinationPropertiesMapping = new Table("Destination Properties");
    	tblSourcePropertiesParam=tblSourcePropertiesMapping;
    	tblDestinationPropertiesParam=tblDestinationPropertiesMapping;
    	
    	tblSourcePropertiesMapping.setWidth("50%");    	
    	tblDestinationPropertiesMapping.setWidth("100%");  
    	tblSourcePropertiesMapping.setSelectable(true);
    	tblDestinationPropertiesMapping.setSelectable(true);
    	/* Define the names and data types of columns.
    	 * The "default value" parameter is meaningless here. */
    	tblSourcePropertiesMapping.addContainerProperty("Property", String.class,  null);
    	tblSourcePropertiesMapping.addContainerProperty("Value",  String.class,  null);

    	tblDestinationPropertiesMapping.addContainerProperty("Property",  String.class,  null);
    	tblDestinationPropertiesMapping.addContainerProperty("Value",  String.class,  null);
    	tblDestinationPropertiesMapping.setMultiSelect(true);
    	///get data for comboboxes
    	SQLContainer cmbContainer= connectToDB("root", "mofo","Endpoints");
    	//fill endpoints
		cmbSourceEndpoint.setContainerDataSource(cmbContainer);
    	cmbDestinationEndpoint.setContainerDataSource(cmbContainer);   	
    	
    	cmbSourceEndpoint.setValue(cmbSourceEndpoint.getItemIds().iterator().next());
    	cmbDestinationEndpoint.setValue(cmbDestinationEndpoint.getItemIds().iterator().next());
    	
    	SQLContainer lstContainer =getSuggestedProperties("root", "mofo");
    	int lstSize=lstContainer.size();
    	int i=0;
        for (Object cityItemId : lstContainer.getItemIds()) 
        {
        	lstSuggestedProperties.addItem(i);
        	String g=lstContainer.getItem(cityItemId).getItemProperty("property").getValue().toString();
        	lstSuggestedProperties.setItemCaption(i,g);
        	i++;
        }
        lstSuggestedProperties.setValue(lstSuggestedProperties.getItemIds().iterator().next());

    	btnCorrect.addClickListener(new Button.ClickListener() 
        {
            @Override
			public void buttonClick(ClickEvent event) 
            {
            	try
            	{
            		Object rowId =tblSourceDestinationparam.getValue();
            		Item myitem = tblSourceDestinationparam.getItem(tblSourceDestinationparam.getValue());
            		lEndTime = System.currentTimeMillis();
            		long lEllapsedTime=lEndTime-lStartTime;
            		tblSourceDestinationparam.setEditable(true);
            		tblSourceDestinationparam.getContainerProperty(rowId, "decision").setValue("Correct"); 
            		tblSourceDestinationparam.getContainerProperty(rowId, "time").setValue(String.valueOf(lEllapsedTime)); 

            		tblSourceDestinationparam.setEditable(false);
            		SQLContainer c =(SQLContainer) tblSourceDestinationparam.getContainerDataSource();
            		try {
            			c.commit();
            		} catch (UnsupportedOperationException | SQLException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		int maxindex=tblSourceDestination.size();
            		SQLContainer s=(SQLContainer) tblSourceDestination.getContainerDataSource();
            		//Item x=s.getItem(tblSourceDestination.getValue());
            		if(!(tblSourceDestination.getValue().equals(tblSourceDestination.lastItemId())))
            		{
            			int index=s.indexOfId(tblSourceDestination.getValue());
            			index++;
            			tblSourceDestination.setValue(s.getIdByIndex(index));
            			try
                    	{
        	            	Object rowId2 =tblSourceDestination.getValue();
        	                Property sourceProperty=tblSourceDestination.getContainerProperty(rowId2,"sourceURI");
        	                Property destinationProperty=tblSourceDestination.getContainerProperty(rowId2,"destinationURI");
        	                
        	                source.setValue(sourceProperty.toString());
        	                destination.setValue(destinationProperty.toString());
        	                tblSourcePropertiesParam.removeAllItems();
        	                tblDestinationPropertiesParam.removeAllItems();
        	                
        	                
        	                Notification loadURI= new Notification("");
        	                loadURI.show("Links' URIs are successfully loaded ");
        	                if(chkAutomaticPropertiesLoad.getValue())// the automatic buton i schecked
        	                {
        	                	//load the properties automatically
        	                	String sourceEndpoint="",destinationEndpoint="";
        	                	sourceEndpoint=cmbSourceEndpoint.getItemCaption(cmbSourceEndpoint.getValue());
        	                	destinationEndpoint=cmbDestinationEndpoint.getItemCaption(cmbDestinationEndpoint.getValue());
        	                	try
        	    	    	  	{
        	                		String sparqlQuery=source.getValue();		        
        	    		            getURIProperties(sparqlQuery,sourceEndpoint,tblSourcePropertiesMapping);
        	    		            sparqlQuery=destination.getValue();		        
        	    		            getURIProperties(sparqlQuery,destinationEndpoint,tblDestinationPropertiesMapping);
        	    	    	  	}
        	                	catch(Exception e){Notification.show("ERROR");}
        	                	lStartTime= System.currentTimeMillis();
        	                	//start time for next one
        	                }
                    	}
                    	catch(Exception e)
                    	{
                    		Notification error= new Notification("Error");
                    		error.show("You did not select an item in the links table");
                    	}
            		}
            	}
            	catch(Exception e){Notification.show(e.getMessage());}
            	
            	///////////////////////////////////
            	
            	
            	
             }
        });
    	btnIncorrect.addClickListener(new Button.ClickListener() 
        {
            @Override
			public void buttonClick(ClickEvent event) 
            {
            	try
            	{
            		Object rowId =tblSourceDestinationparam.getValue();
            		Item myitem = tblSourceDestinationparam.getItem(tblSourceDestinationparam.getValue());
            		lEndTime = System.currentTimeMillis();
            		long lEllapsedTime=lEndTime-lStartTime;
            		tblSourceDestinationparam.setEditable(true);
            		tblSourceDestinationparam.getContainerProperty(rowId, "decision").setValue("Incorrect");
            		tblSourceDestinationparam.getContainerProperty(rowId, "time").setValue(String.valueOf(lEllapsedTime)); 
            		tblSourceDestinationparam.setEditable(false);
            		SQLContainer c =(SQLContainer) tblSourceDestinationparam.getContainerDataSource();
            		try {
            			c.commit();
            		} catch (UnsupportedOperationException | SQLException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            		int maxindex=tblSourceDestination.size();
            		SQLContainer s=(SQLContainer) tblSourceDestination.getContainerDataSource();
            		//Item x=s.getItem(tblSourceDestination.getValue());
            		if(!(tblSourceDestination.getValue().equals(tblSourceDestination.lastItemId())))
            		{
            			int index=s.indexOfId(tblSourceDestination.getValue());
            			index++;
            			tblSourceDestination.setValue(s.getIdByIndex(index));
            			try
                    	{
        	            	Object rowId2 =tblSourceDestination.getValue();
        	                Property sourceProperty=tblSourceDestination.getContainerProperty(rowId2,"sourceURI");
        	                Property destinationProperty=tblSourceDestination.getContainerProperty(rowId2,"destinationURI");
        	                
        	                source.setValue(sourceProperty.toString());
        	                destination.setValue(destinationProperty.toString());
        	                tblSourcePropertiesParam.removeAllItems();
        	                tblDestinationPropertiesParam.removeAllItems();
        	                
        	                
        	                Notification loadURI= new Notification("");
        	                loadURI.show("Links' URIs are successfully loaded ");
        	                
        	                if(chkAutomaticPropertiesLoad.getValue())// the automatic buton i schecked
        	                {
        	                	//load the properties automatically
        	                	String sourceEndpoint="",destinationEndpoint="";
        	                	sourceEndpoint=cmbSourceEndpoint.getItemCaption(cmbSourceEndpoint.getValue());
        	                	destinationEndpoint=cmbDestinationEndpoint.getItemCaption(cmbDestinationEndpoint.getValue());
        	                	try
        	    	    	  	{
        	                		String sparqlQuery=source.getValue();		        
        	    		            getURIProperties(sparqlQuery,sourceEndpoint,tblSourcePropertiesMapping);
        	    		            sparqlQuery=destination.getValue();		        
        	    		            getURIProperties(sparqlQuery,destinationEndpoint,tblDestinationPropertiesMapping);
        	    	    	  	}
        	                	catch(Exception e){Notification.show("ERROR Not Properties queried");}
        	                	lStartTime= System.currentTimeMillis();
        	                	//start time for next one
        	                }
                    	}
                    	catch(Exception e)
                    	{
                    		Notification error= new Notification("Error");
                    		error.show("You did not select an item in the links table");
                    	}
            		}
            	}
            	catch(Exception e){Notification.show(e.getMessage());}
				///////////////////////////////////
				            	
				
        		
             }
        });
    	btnUnsure.addClickListener(new Button.ClickListener() 
        {
            @Override
			public void buttonClick(ClickEvent event) 
            {
            	try
            	{
            		Object rowId =tblSourceDestinationparam.getValue();
            		Item myitem = tblSourceDestinationparam.getItem(tblSourceDestinationparam.getValue());
            		lEndTime = System.currentTimeMillis();
            		float lEllapsedTime=lEndTime-lStartTime;
            		String elapsedTime= String.valueOf(lEllapsedTime);
            		tblSourceDestinationparam.setEditable(true);
            		tblSourceDestinationparam.getContainerProperty(rowId, "decision").setValue("Unsure"); 
            		tblSourceDestinationparam.getContainerProperty(rowId, "time").setValue(elapsedTime);
            		tblSourceDestinationparam.setEditable(false);
            		SQLContainer c =(SQLContainer) tblSourceDestinationparam.getContainerDataSource();
            		try {
            			c.commit();
            		} catch (UnsupportedOperationException | SQLException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}

            		//int selectedId=Integer.parseInt(tblSourceDestination.getValue().toString());
            		int maxindex=tblSourceDestination.size();
            		SQLContainer s=(SQLContainer) tblSourceDestination.getContainerDataSource();
            		//Item x=s.getItem(tblSourceDestination.getValue());
            		if(!(tblSourceDestination.getValue().equals(tblSourceDestination.lastItemId())))
            		{
            			int index=s.indexOfId(tblSourceDestination.getValue());
            			index++;
            			tblSourceDestination.setValue(s.getIdByIndex(index));
            			try
                    	{
        	            	Object rowId2 =tblSourceDestination.getValue();
        	                Property sourceProperty=tblSourceDestination.getContainerProperty(rowId2,"sourceURI");
        	                Property destinationProperty=tblSourceDestination.getContainerProperty(rowId2,"destinationURI");
        	                
        	                source.setValue(sourceProperty.toString());
        	                destination.setValue(destinationProperty.toString());
        	                tblSourcePropertiesParam.removeAllItems();
        	                tblDestinationPropertiesParam.removeAllItems();
        	                
        	                
        	                Notification loadURI= new Notification("");
        	                loadURI.show("Links' URIs are successfully loaded ");
        	                if(chkAutomaticPropertiesLoad.getValue())// the automatic buton i schecked
        	                {
        	                	//load the properties automatically
        	                	String sourceEndpoint="",destinationEndpoint="";
        	                	sourceEndpoint=cmbSourceEndpoint.getItemCaption(cmbSourceEndpoint.getValue());
        	                	destinationEndpoint=cmbDestinationEndpoint.getItemCaption(cmbDestinationEndpoint.getValue());
        	                	try
        	    	    	  	{
        	                		String sparqlQuery=source.getValue();		        
        	    		            getURIProperties(sparqlQuery,sourceEndpoint,tblSourcePropertiesMapping);
        	    		            sparqlQuery=destination.getValue();		        
        	    		            getURIProperties(sparqlQuery,destinationEndpoint,tblDestinationPropertiesMapping);
        	    	    	  	}
        	                	catch(Exception e){Notification.show("ERROR Not Properties queried");}
        	                	lStartTime= System.currentTimeMillis();
        	                	//start time for next one
        	                }
                    	}
                    	catch(Exception e)
                    	{
                    		Notification error= new Notification("Error");
                    		error.show("You did not select an item in the links table");
                    	}
            		}
            	}
            	catch(Exception e){Notification.show(e.getMessage());}

             }
        });
    	btnGetProperties.addClickListener(new Button.ClickListener() 
        {
            @Override
			public void buttonClick(ClickEvent event) 
            {
            	String sourceEndpoint="",destinationEndpoint="";
            	sourceEndpoint=cmbSourceEndpoint.getItemCaption(cmbSourceEndpoint.getValue());
            	destinationEndpoint=cmbDestinationEndpoint.getItemCaption(cmbDestinationEndpoint.getValue());
           		lStartTime= System.currentTimeMillis();

            	try
	    	  	{
            		String sparqlQuery=source.getValue();		        
		            getURIProperties(sparqlQuery,sourceEndpoint,tblSourcePropertiesMapping);
		            sparqlQuery=destination.getValue();		        
		            getURIProperties(sparqlQuery,destinationEndpoint,tblDestinationPropertiesMapping);
	    	  	}
            	catch(Exception e){Notification.show("ERROR while sparqling the endpoint for resources' properties (Are they selected/loaded ?)");}
            	//cachingForTriples(tblSourceDestination, sourceEndpoint);
            }
        });
    	
    	lstSuggestedProperties.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) 
            {
                final String valueString = String.valueOf(event.getProperty().getValue());
                //Notification.show(valueString);
                List<Object> Ids= new ArrayList<Object>();
                Object first=null;
                for (Iterator i = tblSourcePropertiesMapping.getItemIds().iterator(); i.hasNext();) 
			    {
			        // Get the current item identifier, which is an integer.
                	first=i.next();
			        int iid = (Integer) first;
			        String other=tblSourcePropertiesMapping.getItem(iid).getItemProperty("Property").toString();
	                //Notification.show(other);
			        if(other.equals(valueString))   //if(other.equals(property))
			        {
			        	Ids.add(iid);
			        	break;
			        }
			    }
                tblSourcePropertiesMapping.setImmediate(true);
                tblSourcePropertiesMapping.setValue(Ids);
				tblDestinationPropertiesMapping.setCurrentPageFirstItemId(first);
            }
        });

    	
    	
    	
    	
    	tblSourcePropertiesMapping.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
			public void itemClick(ItemClickEvent event) 
            {
            	String property=tblSourcePropertiesMapping.getContainerProperty(event.getItemId(), event.getPropertyId()).toString();
                List<String> res= getRelatedProperties(property);
                
                if(res==null)
                {
                	Notification.show("No related Properties");
                	return;
                }
                boolean Found=false;
                List<Object> Ids= new ArrayList<Object>();
                Object first=null;
                int x=0;
				for (String relatedProperty : res) 
				{
				    for (Iterator i = tblDestinationPropertiesMapping.getItemIds().iterator(); i.hasNext();) 
				    {
				        // Get the current item identifier, which is an integer.
				    	Object theId=i.next();
				        int iid = (Integer) theId;
				        String other=tblDestinationPropertiesMapping.getItem(iid).getItemProperty(event.getPropertyId()).toString();
				    	
				        if(other.equals(relatedProperty))   //if(other.equals(property))
				        {
				        	Ids.add(iid);
				        	if(x==0)
				        	{
				        		first=theId;
				        		x=1;
				        	}
				        	Found=true;				        	
				        }
				    }
				}
				if(!Found)
					Notification.show("Related property is not Found in destination table try manual search");
				else
				{
					Notification.show("Found in destination table");
					tblDestinationPropertiesMapping.setValue(Ids);
					tblDestinationPropertiesMapping.setCurrentPageFirstItemId(first);
				}
            }
        });
    	// add component to the layout specifying its position on the layout
    	
    	layout.addComponent(btnCorrect, "left: "+leftStart+"px; top: "+(topStart+450)+"px;");
    	layout.addComponent(btnIncorrect, "left: "+(leftStart+space)+"px; top: "+(topStart+450)+"px;");
    	layout.addComponent(btnUnsure, "left: "+(leftStart+2*space)+"px; top: "+(topStart+450)+"px;"); 
    	layout.addComponent(btnGetProperties, "left: "+(leftStart+3*space+50)+"px; top: "+(topStart+450)+"px;");
    	layout.addComponent(chkAutomaticPropertiesLoad, "left: "+(leftStart+3*space+250)+"px; top: "+(topStart+450)+"px;");

    	/*layout.addComponent(sourceURI,"left: "+(leftStart-space/2)+"px; top: "+(topStart+space/2)+"px;");
    	layout.addComponent(destinationURI,"left: "+(leftStart+2*space)+"px; top: "+(topStart+space/2)+"px;");*/
    	
    	layout.addComponent(source,"left: 30px; top: "+(topStart+space/2)+"px;");
    	layout.addComponent(destination,"left: "+(leftStart+3*space+200)+"px; top: "+(topStart+space/2)+"px;");
    	
    	layout.addComponent(cmbSourceEndpoint,"left: 50px; top: "+(topStart+20)+"px;");
    	layout.addComponent(cmbDestinationEndpoint,"left: "+(leftStart+3*space+200)+"px; top: "+(topStart+20)+"px;");
    	layout.addComponent(lstSuggestedProperties,"left: "+(leftStart+100)+"px; top: "+(topStart+20)+"px;");
    	
    	
    	layout.addComponent(tblSourcePropertiesMapping,"left: 10px; top: "+(topStart+space)+"px;");
    	layout.addComponent(tblDestinationPropertiesMapping,"left: "+(leftStart+3*space+200)+"px; top: "+(topStart+space)+"px;");
    	
    	

    	pnlURIsProperties.setContent(layout);
    	
    	return pnlURIsProperties;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<String> getRelatedProperties(String property)
    {
    	Connection con = null;
		String url = "jdbc:mysql://localhost:3306/";
		String selectQuery= "select property from Properties where " +
				"(Id in (select firstProperty from propertyMappings " +
						"where secondProperty = (select Id from Properties Where property =\""+property+"\")))" +
				" OR " +
				"(Id in (select secondProperty from propertyMappings " +
						"where firstProperty = (select Id from Properties Where property =\""+property+"\")))";
		String driver = "com.mysql.jdbc.Driver";
		ResultSet linksRecords=null;
		List<String> relatedProperties= new ArrayList<String>();
		  try
		  {
			  try {
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  con = DriverManager.getConnection("jdbc:mysql://localhost:3306/linkeval","root","mofo");
			  Statement st = con.createStatement();
			  linksRecords=st.executeQuery(selectQuery);
			  while(linksRecords.next())
			  {
				  relatedProperties.add(linksRecords.getString("property"));
			  }
			  //linksRecords.close();
		  }
  		 
		  catch (SQLException s)
		  {
			  Notification.show("SQL statement is not executed! In getting related properties\n"+s.getMessage());
		  }
		  finally
		  {
			  try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		  return relatedProperties;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void getURIProperties(String subject,String endpoint, Table table)
    {
    	
    	try
    	{//"select * where { <"+subject+"> ?p  ?o .   FILTER(langMatches(lang(?o), \"EN\"))}"; idea about limiting results
        	String sparqlQuery="select distinct * where { <"+subject+"> ?p  ?o .}";
	        Query query = QueryFactory.create(sparqlQuery);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
			com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
			com.hp.hpl.jena.query.QuerySolution binding=null;
			table.removeAllItems();
		    while (results.hasNext()) 
		    {
		    	binding = results.next();
		    	String property=binding.getResource("?p").toString();
		    	String value;
		    	if(binding.get("?o").isResource())
		    		value=binding.getResource("?o").toString();
		    	else
		    		value=binding.getLiteral("?o").toString();
		    	table.addItem(new Object[] {property,value}, new Integer(table.size()+1));
		    }
		    qexec.close() ;
    	}
    	catch (Exception e)
	 	  {
		        Notification.show(e.toString());
	 	  }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<String> getTasksInfo(String url,String db,String userName,String password,String usr) throws SQLException
	{
    	Connection con = null;
		String driver = "com.mysql.jdbc.Driver";
		List<String> info=null;
		  try
		  { 
			  Class.forName(driver);
			  con = DriverManager.getConnection(url+db,userName,password);
			  String selectStatement="SELECT TasksUsers.taskId FROM TasksUsers WHERE  TasksUsers.userId = "+usr;
			  Statement st = con.createStatement();
			  ResultSet linksRecords=null;
			  linksRecords=st.executeQuery(selectStatement);
			  if(linksRecords!=null)
			  {
				  info=new ArrayList<String>();
				  while(linksRecords.next())
				  {
					  info.add(linksRecords.getString("taskId"));
				  }
			  }
			  else
			  {
				  Notification.show("Warning:","User has no tasks", Type.TRAY_NOTIFICATION);
			  }

			  linksRecords.close();
		  }
		 
		  catch (SQLException s)
		  {
			  
			  Notification.show("SQLException:", "Error occured while Login:\n"+s.getMessage(), Type.TRAY_NOTIFICATION);
		  } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  finally
		  {
			  try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		return info;
		
	}
    //////////////////////////////////////////////////////////////////////////////////
    private HashMap<String, String> getLoginInfo(String url,String db,String userName,String password) throws SQLException
	{
    	Connection con = null;
		String driver = "com.mysql.jdbc.Driver";
		HashMap<String, String> info=new HashMap<String, String>();
		  try
		  {
			  Class.forName(driver);
			  con = DriverManager.getConnection(url+db,userName,password);
			  String selectStatement="SELECT userId,userName FROM Users";
			  Statement st = con.createStatement();
			  ResultSet linksRecords=st.executeQuery(selectStatement);
			  
			  while(linksRecords.next())
			  {
				  info.put(linksRecords.getString("userId"),linksRecords.getString("userName"));
			  }
			  linksRecords.close();
		  }
		 
		  catch (SQLException s)
		  {
			  //System.out.println("SQL statement is not executed!\n"+s.getMessage());
		  } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  finally
		  {
			  try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		return info;
		
	}
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private List<LinkCandidate> getLinksCandidates(String url,String db,String userName,String password) throws SQLException
	{
    	//JDBCConnectionPool x=new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/", "hgf", "xxxxx");
		List<LinkCandidate> infoList= new ArrayList<LinkCandidate>();
		Connection con = null;
		String driver = "com.mysql.jdbc.Driver";
		  try
		  {
			  Class.forName(driver);
			  con = DriverManager.getConnection(url+db,userName,password);
			  String selectStatement="SELECT * FROM Links";
			  Statement st = con.createStatement();
			  ResultSet linksRecords=st.executeQuery(selectStatement);
			  
			  while(linksRecords.next())
			  {
				  //infoList.add(new LinkCandidate(linksRecords.getString("sourceURI"),linksRecords.getString("destinationURI"), linksRecords.getString("relationMapping")));
			  }
			  linksRecords.close();
		  }
		 
		  catch (SQLException s)
		  {
			  
		  } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  finally
		  {
			  try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		return infoList;
		
	}
/////inner class
    public class LinkCandidate 
    {
    	public String source,destination,relation,decision,time;
    	public LinkCandidate(String s, String d, String r,String de, String t)
    	{
    		source=s;
    		destination=d;
    		relation=r;
    		decision=de;
    		time=t;
    	}
    	@Override
    	public String toString() 
    	{
    		String info= source+":"+ destination+":"+relation+":"+decision+":"+time;
    		return info;
    	}
    }
}



