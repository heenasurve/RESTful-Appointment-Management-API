package DataLayer.domain;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.*;
//import components.data.Patient;

@XmlRootElement(name="appointment")
@XmlAccessorType (XmlAccessType.FIELD)
public class MyAppointment {
     
    @XmlAttribute
    public String id;
    
    @XmlElement
    public String uri;
    
     @XmlAttribute
      public String date;
      
     @XmlAttribute
     public String time;
       
     @XmlElement  
     public Patient patient;
     
     @XmlElement  
     public Phlebotomist phlebotomist;
     
     @XmlElement  
     public Psc psc;
    
     @XmlElement(name="allLabTests")
    public AppointmentLabTestList tests; 

     private String patientId;
     private String physicianId;


    public String getPatientId() {
        return patientId;
    }

     //@XmlElement
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPhysicianId() {
        return physicianId;
    }

     //@XmlElement
    public void setPhysicianId(String physicianId) {
        this.physicianId = physicianId;
    }

//    public String getPhlebotomistId() {
//        return phlebotomistId;
//    }
//
//     //@XmlElement
//    public void setPhlebotomistId(String phlebotomistId) {
//        this.phlebotomistId = phlebotomistId;
//    }
//
//    public String getPscId() {
//        return pscId;
//    }
//
//     //@XmlElement
//    public void setPscId(String pscId) {
//        this.pscId = pscId;
//    }
//
//    public String getDate() {
//        return apptdate;
//    }
//
//     @XmlAttribute
//    public void setDate(String date) {
//        this.apptdate = date;
//    }
//
//    public String getTime() {
//        return appttime;
//    }
//
//    @XmlAttribute
//    public void setTime(String time) {
//        this.appttime = time;
//    }

//    public Patient getPatient() {
//        return apptpatient;
//    }
//
//    @XmlElement
//    public void setPatient(Patient patient) {
//        this.apptpatient = patient;
//    }
     
     
     
     
}
