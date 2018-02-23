package AppointmentManager;

import DataLayer.DBSingleton;
import BusinessLayer.BusinessLayer;
import components.data.*;
import DataLayer.domain.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.*;
import java.io.StringWriter;

/**
 * REST Web Service
 *
 * @author Heena
 */
@Path("Services")
public class Appointment {
  
    @Context
    private UriInfo context;
    IComponentsData db;
    DBSingleton dbSingleton;
    MyAppointment myapp;
    BusinessLayer businessLayer;

    public Appointment() {
        db = new DB();
        dbSingleton = DBSingleton.getInstance();
        dbSingleton.db.initialLoad("LAMS");
        businessLayer = new BusinessLayer();
    }
    
     /**
     * GET method that returns a link to the WADL 
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/xml")
    public String getInfo(){
          IComponentsData db = new DB();
          db.initialLoad("LAMS"); 
          MyAppointmentList list = new MyAppointmentList();
           StringWriter writer = new StringWriter();
           String welcomeMessage = "Welcome to the LAMS Appointment Service";
          String result = "";
          try{
            JAXBContext jaxbContext = JAXBContext.newInstance(MyAppointmentList.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            list.setWadl(this.context.getBaseUri().toString()+"application.wadl");
            list.setIntro(welcomeMessage);
            jaxbMarshaller.marshal(list, writer);
            result = result.concat(writer.toString());
          }catch(JAXBException ex){
              ex.printStackTrace();
          }
          return result;
    }      
    
    /**
     * GET method that retrieves all instances of appointments 
     *
     * @return an instance of java.lang.String
     */
    @Path("/Appointments")
    @GET
    @Produces("application/xml")
    @Consumes("text/plain")
    public String get() {
        String appointments = businessLayer.getAllAppointments(this.context);
        return appointments;
        //return Response.ok().entity(list).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
    } 

    /**
     * GET method that retrieves a specific instance of an appointment by Id
     *
     * @return an instance of java.lang.String
     */
    @Path("/Appointment/{appointmentId}")
    @GET
    @Produces("application/xml")
    @Consumes("text/plain")
    public String get(@PathParam("appointmentId") String appointmentId) {

        String appointment = businessLayer.getAppointment(appointmentId, this.context);
        return appointment; 
         //return result;
        //return Response.ok().entity(list).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
    }

    /**
     * PUT method for updating or creating an instance of Appointment
     * 
     * @param content representation for the resource
     */
    @Path("Appointment/{appointmentId}")
    @PUT
    @Produces("application/xml")
    @Consumes("application/xml")
    public String put(String content, @PathParam("appointmentId") String appointmentId) {
        String updatedAppointment = businessLayer.updateAppointment(appointmentId, content,this.context);
        return updatedAppointment;
    }

     /**
     * POST method that creates a new instance of an appointment
     * 
     * @return an instance of java.lang.String
     */
    @Path("/Appointments")
    @POST
    @Consumes({"text/xml", "application/xml"})
    @Produces("application/xml")
    public String  post(String inXML) {

        BusinessLayer business = new BusinessLayer();
        String newAppointment = businessLayer.createAppointment(inXML, this.context);
        return newAppointment;   
        //return Response.ok(result).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").header("Access-Control-Allow-Headers","Content-Type").allow("OPTIONS").build();
    }
}



  