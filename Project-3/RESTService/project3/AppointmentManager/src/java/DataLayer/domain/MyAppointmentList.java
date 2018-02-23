package DataLayer.domain;

import java.util.List;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="AppointmentList")
@XmlAccessorType (XmlAccessType.FIELD)
public class MyAppointmentList {
  
    @XmlElement(name="appointment")
    private List<MyAppointment> myAppointments = null;
    
    @XmlElement(name="uri")
    private String uri =null;
    
   @XmlElement(name="intro")
    private String intro;
    
   @XmlElement(name="wadl")
    private String wadl =null;
   
   @XmlElement(name="error")
   private String error = null;


    public void setMyAppointments(List<MyAppointment> myAppointments) {
        this.myAppointments = myAppointments;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setWadl(String wadl) {
        this.wadl = wadl;
    }
    
    public void setIntro(String intro){
        this.intro = intro;
    }
    
    public void setError(String error){
        this.error = error;
    }
      
}
