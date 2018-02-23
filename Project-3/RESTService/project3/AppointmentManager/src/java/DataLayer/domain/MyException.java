/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataLayer.domain;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;

@XmlRootElement(name="Error")
@XmlAccessorType (XmlAccessType.FIELD)
public class MyException extends Exception {

    @XmlElement
    public String message;
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

   
    
    
}
