package DataLayer.domain;
import  java.util.List;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="allLabTests")
@XmlAccessorType (XmlAccessType.FIELD)
public class AppointmentLabTestList {
    
        @XmlElement(name="appointmentLabTest")
        private List<AppointmentLabTest> appointmentTests = null;

//    public List<AppointmentLabTest> getAppointmentTests() {
//        return appointmentTests;
//    }

    public void setAppointmentTests(List<AppointmentLabTest> appointmentTests) {
        this.appointmentTests = appointmentTests;
    }
        
        
}
