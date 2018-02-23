
package DataLayer.domain;
import javax.xml.bind.annotation.*;


@XmlRootElement(name="patient")
@XmlAccessorType (XmlAccessType.FIELD)
public class Patient {
        
        @XmlAttribute
        public String id;
        
        @XmlElement(name="name")
        public String name;
        
        @XmlElement(name="address")
        public String address;
        
        @XmlElement(name="insurance")
        public String insurance;
        
        @XmlElement(name="dob")
        public String dob;
        
        
        

//    public String getId() {
//        return id;
//    }
//
//    @XmlAttribute
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    @XmlElement
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    @XmlElement
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    public String getInsurance() {
//        return insurance;
//    }
//
//    @XmlElement
//    public void setInsurance(String insurance) {
//        this.insurance = insurance;
//    }
//
//    public String getDob() {
//        return dob;
//    }
//
//    
//    public void setDob(String dob) {
//        this.dob = dob;
//    }
//        
//    

    public Patient() {
    }
        
    
}
