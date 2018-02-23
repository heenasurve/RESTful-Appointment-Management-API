package BusinessLayer;
import DataLayer.DBSingleton;
import DataLayer.domain.AppointmentLabTestList;
import DataLayer.domain.MyAppointment;
import DataLayer.domain.MyAppointmentList;
import DataLayer.domain.MyException;
import components.data.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BusinessLayer {
        
         DB db;
         DBSingleton dbSingleton;
         StringWriter writer;
         
        public BusinessLayer(){
            db = new DB();
            dbSingleton = DBSingleton.getInstance();
            writer = new StringWriter();
        }

        /*Performs an intial load and returns all appointments*/
        public String getAllAppointments(UriInfo context){
            
        List<Object> objs = dbSingleton.db.getData("Appointment", "");
  
        List<MyAppointment> appointments = new ArrayList<>();
        MyAppointmentList list = new MyAppointmentList();
        MyAppointment myapp;
        String result = "";
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MyAppointmentList.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
            for (Object obj : objs) {
                components.data.Appointment app = (components.data.Appointment) obj;
                myapp = getMyApptFromAppointment(app,context);
                appointments.add(myapp);  
            }               
            list.setMyAppointments(appointments);
            jaxbMarshaller.marshal(list, writer);
            result = result.concat(writer.toString());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return result;
        }
        
        /*Gets a specific appointment by id*/
        public String getAppointment(String appointmentId, UriInfo context){
        
        List<Object> objs = dbSingleton.db.getData("Appointment", "id='" + appointmentId + "'");
        MyAppointment myapp=null;
        String result = "";

        List<MyAppointment> appointments = new ArrayList<>();
        MyAppointmentList list = new MyAppointmentList();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MyAppointmentList.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            if (objs.isEmpty()) {
               result = createXMLErrorResponse("ERROR:No appointment with this id");
                return result;
            }

            for (Object obj : objs) {
                components.data.Appointment app = (components.data.Appointment) obj;
                if (myapp == null) {
                    myapp = new MyAppointment();
                }
                myapp = getMyApptFromAppointment(app, context);
                //overwriting URI as the we do not need to append the appointmentId here
                myapp.uri = context.getRequestUri().toString();
                appointments.add(myapp);
            }
            
            list.setMyAppointments(appointments);
            jaxbMarshaller.marshal(list, writer);
            result = result.concat(writer.toString());

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return result;
     }
        
        /*Updates a specific appointment by Id  -No checks for conflicts made with existing appointments - all attributes of the specified appointment are updated*/
        public String updateAppointment(String appointmentId, String apptContent, UriInfo context){
        List<Object> objs = dbSingleton.db.getData("Appointment", "id='" + appointmentId + "'");
        //StringWriter writer = new StringWriter();
        String result = "";
        MyAppointment myapp=null;

        try {

            if (objs.isEmpty()) {         
                 result = createXMLErrorResponse("ERROR : No appointment with this id");
                return result;     
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(apptContent)));

            String rootElement = doc.getDocumentElement().getNodeName();
            Node n = doc.getElementsByTagName("appointment").item(0);

            components.data.Appointment obj = (components.data.Appointment) objs.get(0);
            MyAppointment myAppt = new MyAppointment();
            Element apptElem = (Element) n;

            myAppt.id = apptElem.getAttribute("id");
            myAppt.date = apptElem.getAttribute("date");
            myAppt.time = apptElem.getAttribute("time");
            obj.setId(myAppt.id);
            obj.setApptdate(java.sql.Date.valueOf(myAppt.date));
            obj.setAppttime(java.sql.Time.valueOf(myAppt.time+":00"));  

            myAppt.patient = new DataLayer.domain.Patient();

            Node patient = apptElem.getElementsByTagName("patient").item(0);
            NodeList patientChildren = patient.getChildNodes();

            Node idNode = patient.getAttributes().getNamedItem("id");
            myAppt.patient.id = idNode.getNodeValue();
            components.data.Patient patientObj = (components.data.Patient) dbSingleton.db.getData("Patient", "id='" + myAppt.patient.id + "'").get(0);

            for (int i = 0; i < patientChildren.getLength(); i++) {

                if (patientChildren.item(i).getNodeName().equals("name")) {
                    myAppt.patient.name = patientChildren.item(i).getTextContent();
                    patientObj.setName(myAppt.patient.name);
                }

                if (patientChildren.item(i).getNodeName().equals("dob")) {
                    myAppt.patient.dob = patientChildren.item(i).getTextContent();
                    System.out.println(myAppt.patient.dob);
                    patientObj.setDateofbirth(new SimpleDateFormat("yyyy-mm-dd").parse(myAppt.patient.dob));
                }

                if (patientChildren.item(i).getNodeName().equals("insurance")) {
                    myAppt.patient.insurance = patientChildren.item(i).getTextContent();
                    patientObj.setInsurance(myAppt.patient.insurance.charAt(0));
                }
                if (patientChildren.item(i).getNodeName().equals("address")) {
                    myAppt.patient.address = patientChildren.item(i).getTextContent();
                    patientObj.setAddress(myAppt.patient.address);
                }

            }

            dbSingleton.db.updateData(patientObj);
            obj.setPatientid(patientObj);

            myAppt.phlebotomist = new DataLayer.domain.Phlebotomist();
            components.data.Phlebotomist phleboObj = new components.data.Phlebotomist();
            Node phlebotomist = apptElem.getElementsByTagName("phlebotomist").item(0);
            NodeList pheloboChildren = phlebotomist.getChildNodes();

            Node idNodePhleb = phlebotomist.getAttributes().getNamedItem("id");
            myAppt.phlebotomist.id = idNodePhleb.getNodeValue();
            phleboObj.setId(myAppt.phlebotomist.id);

            for (int i = 0; i < pheloboChildren.getLength(); i++) {

                if (pheloboChildren.item(i).getNodeName().equals("name")) {
                    myAppt.phlebotomist.name = pheloboChildren.item(i).getTextContent();
                    phleboObj.setName(myAppt.phlebotomist.name);
                }
            }

            dbSingleton.db.updateData(phleboObj);
            obj.setPhlebid(phleboObj);

            myAppt.psc = new DataLayer.domain.Psc();
            components.data.PSC pscObj = new components.data.PSC();

            Node psc = apptElem.getElementsByTagName("psc").item(0);
            NodeList pscChildren = psc.getChildNodes();

            Node idNodePsc = psc.getAttributes().getNamedItem("id");
            myAppt.psc.id = idNodePsc.getNodeValue();
            pscObj.setId(myAppt.psc.id);

            for (int i = 0; i < pscChildren.getLength(); i++) {

                if (pscChildren.item(i).getNodeName().equals("name")) {
                    myAppt.psc.name = pscChildren.item(i).getTextContent();
                    pscObj.setName(myAppt.psc.name);
                }
            }
             dbSingleton.db.updateData(pscObj);
            obj.setPscid(pscObj);
            
            myAppt.tests = new DataLayer.domain.AppointmentLabTestList();
            List<components.data.AppointmentLabTest> tests = new ArrayList<components.data.AppointmentLabTest>();
            
            Node labTests = apptElem.getElementsByTagName("allLabTests").item(0);
            NodeList labTestChildren = labTests.getChildNodes();
            
            for(int j=0;j<labTestChildren.getLength();j++){
                if(labTestChildren.item(j).getNodeName().equals("appointmentLabTest")){
                    components.data.AppointmentLabTest test = new components.data.AppointmentLabTest();
                    Node currTest = labTestChildren.item(j);
                    String diagCode = currTest.getAttributes().getNamedItem("dxcode").getNodeValue();
                    String testId = currTest.getAttributes().getNamedItem("labTestId").getNodeValue();
                    String apptId = currTest.getAttributes().getNamedItem("appointmentId").getNodeValue();
                    tests.add(new components.data.AppointmentLabTest(apptId, testId, diagCode));
                }
            }  

           //dbSingleton.db.updateData(tests);
           obj.setAppointmentLabTestCollection(tests);
           
            dbSingleton.db.updateData(obj);

            List<Object> objsAfterUpdate = dbSingleton.db.getData("Appointment", "id='" + appointmentId + "'");
            List<MyAppointment> appointments = new ArrayList<>();
            MyAppointmentList list = new MyAppointmentList();

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(MyAppointmentList.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                for (Object objAfterUpdate : objsAfterUpdate) {
                    components.data.Appointment app = (components.data.Appointment) objAfterUpdate;
                    if (myapp == null) {
                        myapp = new MyAppointment();
                    }

                    myapp = getMyApptFromAppointment(app, context);
                  //overwriting URI as the we do not need to append the appointmentId here
                  myapp.uri = context.getRequestUri().toString();
                  appointments.add(myapp);
                }
                list.setMyAppointments(appointments);
                jaxbMarshaller.marshal(list, writer);
                result = result.concat(writer.toString());

            } catch (JAXBException e) {
                e.printStackTrace();
            }

        } catch (JAXBException | SAXException | IOException | ParserConfigurationException | ParseException e) {
            e.printStackTrace();
        }
        return result;
     }
        
        /*Creates a new appointment - Includes checks for date and time conflicts, valid values for psc, patient, phlebotomist as well as availablity of the Phlebotomist at the specified PSC*/
        public String createAppointment(String apptContent, UriInfo context){
        
        
        //StringWriter writer = new StringWriter();
        String result = "";
        MyAppointment myapp = null;

        try {

            JAXBContext jaxbContextException = JAXBContext.newInstance(MyAppointmentList.class);
                Marshaller jaxbMarshallerException = jaxbContextException.createMarshaller();
                jaxbMarshallerException.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                MyAppointmentList errorList = new MyAppointmentList();
                
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
           Document doc = dBuilder.parse(new InputSource(new StringReader(apptContent)));
           
            String rootElement = doc.getDocumentElement().getNodeName();
            Node n = doc.getElementsByTagName("appointment").item(0);
            Element apptElem = (Element) n;  
  
            components.data.Appointment obj = new components.data.Appointment();
            obj.setId(getNewApptId());
      
            String date = "";
            String time = "";
            List<components.data.Patient> patient = null;
            List<components.data.Physician> physician = null;
            List<components.data.Phlebotomist> phlebo = null;
            List<components.data.PSC> psc = null;
            List<components.data.AppointmentLabTest> tests = new ArrayList<>();

            NodeList apptData = apptElem.getChildNodes();

            for (int i = 0; i < apptData.getLength(); i++) {
                if (apptData.item(i).getNodeName().equals("date")) {
                    date = apptData.item(i).getTextContent();
                }

                if (apptData.item(i).getNodeName().equals("time")) {
                    time = apptData.item(i).getTextContent();
                }

                if (apptData.item(i).getNodeName().equals("patientId")) {
                    patient = (List<components.data.Patient>)(List)db.getData("Patient", "id='" + apptData.item(i).getTextContent() + "'");
                }

                if (apptData.item(i).getNodeName().equals("physicianId")) {
                    physician = (List<components.data.Physician>) (List)db.getData("Physician", "id='" + apptData.item(i).getTextContent() + "'");
                }
                if (apptData.item(i).getNodeName().equals("phlebotomistId")) {
                    phlebo = (List<components.data.Phlebotomist>) (List)db.getData("Phlebotomist", "id='" + apptData.item(i).getTextContent() + "'");
                }

                if (apptData.item(i).getNodeName().equals("pscId")) {
                    psc = (List<components.data.PSC>) (List) db.getData("PSC", "id='" + apptData.item(i).getTextContent() + "'");
                }

                if (apptData.item(i).getNodeName().equals("labTests")) {
                    
                    Node labTests = apptData.item(i);
                    NodeList appttests = labTests.getChildNodes();

                    System.out.println(appttests.getLength());
                    for (int t = 0; t < appttests.getLength(); t++) {
                        if (appttests.item(t).getNodeName().equals("test")) {
                            components.data.AppointmentLabTest test = new components.data.AppointmentLabTest();
                            String diagCode = appttests.item(t).getAttributes().getNamedItem("dxcode").getNodeValue();
                            String testId = appttests.item(t).getAttributes().getNamedItem("id").getNodeValue();
                            try{
                                test.setDiagnosis((Diagnosis) dbSingleton.db.getData("Diagnosis", "code='" + diagCode + "'").get(0));
                                test.setLabTest((LabTest) dbSingleton.db.getData("LabTest", "id='" + testId + "'").get(0));
                            }catch(IndexOutOfBoundsException e){
                                  errorList.setError("ERROR:Appointment is not available");
                                  jaxbMarshallerException.marshal(errorList, writer);
                                  result = result.concat(writer.toString());
                                  return result;
                            }
                            test.setAppointment(obj);   
                            tests.add(new AppointmentLabTest(test.getAppointment().getId(), testId, diagCode));
                        }  
                    }
                }
            }

            
                
                

                if (isApptDataValid(tests, phlebo, psc, patient, physician) && isDateAndTimeValid(date, time)) {
                   
                    obj.setApptdate(java.sql.Date.valueOf(date));
                    obj.setAppttime(java.sql.Time.valueOf(time.concat(":00")));
                    obj.setPatientid(patient.get(0));
                    obj.setPhlebid(phlebo.get(0));
                    obj.getPatientid().setPhysician(physician.get(0));
                    obj.setPscid(psc.get(0));
                    obj.setAppointmentLabTestCollection(tests);      
                } else{   
                    errorList.setError("ERROR:Appointment is not available");
                    jaxbMarshallerException.marshal(errorList, writer);
                    result = result.concat(writer.toString());
                    return result;
                    //return Response.ok(exception).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").header("Access-Control-Allow-Headers","Content-Type").build();
                }    
                if (!bookAppointment(obj)) {    //problems with the phlebotomists availability 
                    errorList.setError("ERROR:Appointment is not available");
                    jaxbMarshallerException.marshal(errorList, writer);
                    result = result.concat(writer.toString());
                    return result;
                    //return Response.ok(exception).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").header("Access-Control-Allow-Headers","Content-Type").build();
                } 
             
  
            dbSingleton.db.addData(obj);

            List<Object> objsAfterAddition = dbSingleton.db.getData("Appointment", "");

            List<MyAppointment> appointments = new ArrayList<>();
            MyAppointmentList list = new MyAppointmentList();

           
                JAXBContext jaxbContext = JAXBContext.newInstance(MyAppointmentList.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
  
                list.setUri(context.getRequestUri().toString() + obj.getId());
                jaxbMarshaller.marshal(list, writer);
                result = result.concat(writer.toString());

        } catch (JAXBException | SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return result;  
        }  
        
        /*Created Error responses*/
        private String createXMLErrorResponse(String errmessage) throws JAXBException{
                
                JAXBContext jaxbContextException = JAXBContext.newInstance(MyAppointmentList.class);
                Marshaller jaxbMarshallerException = jaxbContextException.createMarshaller();
                jaxbMarshallerException.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                MyAppointmentList errorList = new MyAppointmentList();
                errorList.setError(errmessage);
                jaxbMarshallerException.marshal(errorList, writer);
                String errorXML = writer.toString();
                return errorXML;
        }
        
        /*Returns an instance of the MyAppointment class with JAXB annotations from an instance of the Appointment class provided*/
        private MyAppointment getMyApptFromAppointment(Appointment app, UriInfo context){
                MyAppointment myapp = new MyAppointment();
                myapp.uri = context.getRequestUri().toString() + app.getId();
                myapp.id = app.getId();
                myapp.date = app.getApptdate().toString();
                myapp.time = app.getAppttime().toString();  
        
                myapp.patient = new DataLayer.domain.Patient();
                myapp.patient.id = app.getPatientid().getId();
                myapp.patient.name = app.getPatientid().getName();
                
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                myapp.patient.dob = sdf.format(app.getPatientid().getDateofbirth());
                myapp.patient.insurance = Character.toString(app.getPatientid().getInsurance());
                myapp.patient.address = app.getPatientid().getAddress();
  
                myapp.phlebotomist = new DataLayer.domain.Phlebotomist();
                myapp.phlebotomist.id = app.getPhlebid().getId();
                myapp.phlebotomist.name = app.getPhlebid().getName();

                myapp.psc = new DataLayer.domain.Psc();
                myapp.psc.id = app.getPscid().getId();
                myapp.psc.name = app.getPscid().getName();

                List<DataLayer.domain.AppointmentLabTest> labTestsFromAppt = new ArrayList<>();
                AppointmentLabTestList appLabTestList = new DataLayer.domain.AppointmentLabTestList();
                List<components.data.AppointmentLabTest> appLabTests = app.getAppointmentLabTestCollection();
                 
                System.out.println(appLabTests);
                for (components.data.AppointmentLabTest appLabTest : appLabTests) {
                    DataLayer.domain.AppointmentLabTest labTest = new DataLayer.domain.AppointmentLabTest();
                    labTest.appointmentId = appLabTest.getAppointmentLabTestPK().getApptid();
                    labTest.dxcode = appLabTest.getAppointmentLabTestPK().getDxcode();
                    labTest.labTestId = appLabTest.getAppointmentLabTestPK().getLabtestid();
                    labTestsFromAppt.add(labTest);
                }
                appLabTestList.setAppointmentTests(labTestsFromAppt);
                myapp.tests = appLabTestList;  
                
                return myapp;
        }
        
        private boolean isDateAndTimeValid(String date, String time){
            
            try{
            String date_time = date.concat(" " + time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false);
            String apptDateStr = date_time;
            
            Date apptDate = sdf.parse(apptDateStr);
            Calendar apptDateCal = Calendar.getInstance();
            apptDateCal.setTime(apptDate);
                
            Calendar now = Calendar.getInstance();
            if(apptDateCal.before(now)) {  
                //Date before now considered valid - to check for conflicts with exisitng appointments
                return true;
            }else{
                if(apptDateCal.get(Calendar.HOUR_OF_DAY) < 8 || apptDateCal.get(Calendar.HOUR_OF_DAY) > 16 ){
                    //invalid hour of day
                    return false;
                }
                if(apptDateCal.get(Calendar.HOUR_OF_DAY)==16 && apptDateCal.get(Calendar.MINUTE)>45){
                       //anything after 4.45PM is invalid
                        return false;
                }
            }
            
            }catch(ParseException e){
                System.out.println("Invalid date format");
                return false;
            }
            System.out.println("Date valid");
            return true;
        }

        private boolean isApptDataValid(List<AppointmentLabTest>tests, List<Phlebotomist> phlebId, List<PSC> pscId, List<Patient> patientId, List<Physician> physicianId){       
            List<Phlebotomist> phlebs = (List<Phlebotomist>)(List)dbSingleton.db.getData("Phlebotomist", "");
            List<PSC> pscs = (List<PSC>)(List)dbSingleton.db.getData("PSC", "");
            List<Patient> patients = (List<Patient>)(List)dbSingleton.db.getData("Patient", "");
            List<Physician> physicians = (List<Physician>)(List)dbSingleton.db.getData("Physician", "");
            
            try{
                if(phlebs.contains(phlebId.get(0)) && pscs.contains(pscId.get(0)) && patients.contains(patientId.get(0)) && physicians.contains(physicianId.get(0))) {
                    //valid ids entered for each of these 
                    return true;  
                } 
                if(tests!=null && isLabTestDataValid(tests)){
                    //Lab test data valid
                    return true;
                }
                if(tests==null){
                    return true;
                }
            }catch(IndexOutOfBoundsException ex){
                return false;
            }  
            return false;
        }
        
        private boolean isLabTestDataValid(List<AppointmentLabTest> list){
            try{
                for(AppointmentLabTest labTest : list){
                    LabTest test = (LabTest)dbSingleton.db.getData("LabTest","id='"+labTest.getLabTest().getId()+"'").get(0);
                    Diagnosis diag = (Diagnosis)dbSingleton.db.getData("Diagnosis","code='"+labTest.getDiagnosis().getCode()+"'").get(0);
                }
                return true;
            }catch(ArrayIndexOutOfBoundsException ex){
                //Lab test data invalid
                return false;
            }          
        }
        
        private boolean isPhlebotomistAndPSCAvailable(Appointment appt){
            List<Appointment> apptsWithSamePhlebo = (List<Appointment>)(List)dbSingleton.db.getData("Appointment", "phlebid='" 
                    + appt.getPhlebid().getId()+"'");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setLenient(false);
            
            try{
                if(!apptsWithSamePhlebo.isEmpty()){ 
                    
                    for(Appointment appointment : apptsWithSamePhlebo){ //for appointments with the same phlebotomist
                            
                            String currentApptDateTime = appointment.getApptdate().toString() + " " + appointment.getAppttime().toString();
                            Date currentDate = sdf.parse(currentApptDateTime);
                            Calendar currDate = Calendar.getInstance();
                            currDate.setTime(currentDate);

                            String newApptDateTime = appt.getApptdate().toString() + " " + appt.getAppttime().toString();
                            Date newDate = sdf.parse(newApptDateTime);
                            Calendar newCalDate = Calendar.getInstance();
                            newCalDate.setTime(newDate);
                            long diffInMinutes = ((currDate.getTimeInMillis()/60000) - (newCalDate.getTimeInMillis()/60000));
                            
                            
                            
                            if(!appointment.getPscid().equals(appt.getPscid())){ //different PSCs    
                                if((int)diffInMinutes > 45 || (int)diffInMinutes<-45){
                                    return true; //difference between the start times of the 2 appointments is greater than 45 minutes
                                }else{
                                    //Phlebo not available at this PSC
                                    return false;
                                }
                            }else{ //same PSC
                                if((int)diffInMinutes > 15 || (int)diffInMinutes<-15){
                                    return true; //difference between the start times of the 2 appointments should atleast be 15 minutes
                                }else{
                                    //Phlebo not available at this PSC
                                    return false;
                                }
                            }
                      }
                    
                }else{
                    //appointments with a different phlebotomist - all accepted    
                    return true;
                }  
            }catch(ParseException e){
                return false;
            }
            return false;
        }
        
        private boolean bookAppointment(Appointment appt){
            if(isPhlebotomistAndPSCAvailable(appt)){
                return true;
            }
          return false;  
        }
        
        /*generated a new id for a newly created appointment on POST*/
        private String getNewApptId(){
            List<Appointment> appts = (List<Appointment>)(List)dbSingleton.db.getData("Appointment", "");
            int len = appts.size();
            int id = Integer.parseInt(appts.get(len-1).getId())+10;
            return String.valueOf(id);
        }
       
}
