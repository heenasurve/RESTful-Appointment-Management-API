package DataLayer.domain;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="phlebotomist")
@XmlAccessorType (XmlAccessType.FIELD)
public class Phlebotomist {

        @XmlAttribute
        public String id;
        
        @XmlElement
        public String name;    
}
