package DataLayer.domain;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="psc")
@XmlAccessorType (XmlAccessType.FIELD)
public class Psc {
        
        @XmlAttribute
        public String id;
        
        @XmlElement
        public String name;
}
