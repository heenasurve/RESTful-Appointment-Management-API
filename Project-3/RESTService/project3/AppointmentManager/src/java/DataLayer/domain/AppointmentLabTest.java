package DataLayer.domain;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="appointmentLabTest")
@XmlAccessorType (XmlAccessType.FIELD)
public class AppointmentLabTest {
    
        @XmlAttribute
        public String appointmentId;
        
        @XmlAttribute
        public String dxcode;
        
        @XmlAttribute
        public String labTestId;
}
