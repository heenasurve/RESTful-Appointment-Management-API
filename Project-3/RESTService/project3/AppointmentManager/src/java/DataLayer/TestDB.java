/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataLayer;

import java.util.*;
import components.data.*;
import java.lang.reflect.*;


/**
 *
 * @author Heena
 */

public class TestDB {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        IComponentsData db = new DB();
        DBSingleton dbSingleton = DBSingleton.getInstance();
		  
        // Populate database 
        // (need to do initial load before doing anything else; 
        //     if you don't you'll get an error message stating as much)
        System.out.println(dbSingleton.db.toString());
        db.initialLoad("LAMS");
        List<Object> objs;
        
        // Print out all appointments
        System.out.println("All appointments\n");
        objs = dbSingleton.db.getData("Appointment", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        
        // Print out appointments for patient #210
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("Getting appointments for patient #210:\n");
        objs = dbSingleton.db.getData("Appointment", "patientid='210'");
        Patient patient = null;
        Phlebotomist phleb = null;
        PSC psc = null;
        for (Object obj : objs){
            System.out.println(obj + "\n");
            patient = ((Appointment)obj).getPatientid();
            phleb = ((Appointment)obj).getPhlebid();
            psc = ((Appointment)obj).getPscid();
        }
        
        // Add a new appointment (for patient #210)
        System.out.println("\n\n**************************************************************\n\n");
        Appointment newAppt = new Appointment("800",java.sql.Date.valueOf("2009-09-01"),java.sql.Time.valueOf("10:15:00"));
        //extra steps here due to persistence api and join, need to create objects in list
        List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
        AppointmentLabTest test = new AppointmentLabTest("800","86900","292.9");
        test.setDiagnosis((Diagnosis)dbSingleton.db.getData("Diagnosis", "code='292.9'").get(0));
        test.setLabTest((LabTest)dbSingleton.db.getData("LabTest","id='86900'").get(0));
        tests.add(test);
        newAppt.setAppointmentLabTestCollection(tests);
        newAppt.setPatientid(patient);
        newAppt.setPhlebid(phleb);
        newAppt.setPscid(psc);
        boolean good = dbSingleton.db.addData(newAppt);

        // Print out all appointments including the newly added one (#800)
        System.out.println("All appointments, including newly added #800\n");
        objs = dbSingleton.db.getData("Appointment", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }

        // Print out all lab tests
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("Lab Tests:\n");
        objs = dbSingleton.db.getData("AppointmentLabTest","");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        
        // Add a new PSC and print out all PSCs
        dbSingleton.db.addData(new PSC("550","new PSC"));
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("Getting all PSCs:\n");
        objs = dbSingleton.db.getData("PSC", "");
        for (Object obj : objs){
            System.out.println(obj);
        }
         
        // Delete the new PSC and then print out all remaining PSCs
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("After deleting PSC #550:\n");
        dbSingleton.db.deleteData("PSC","id='550'");
        objs = dbSingleton.db.getData("PSC", "");
        for (Object obj : objs){
            System.out.println(obj);
        }

        // Print out appointments after Feb 1, 2004
        System.out.printf("%n%n%S%n%n","**************************************************************");
        System.out.println("Getting appointments > 2/1/04: \n");
        objs = dbSingleton.db.getData("Appointment", "apptdate>'2004-02-01'");
        for (Object obj : objs){
            System.out.println(obj);
            System.out.println(((Appointment)obj).getAppointmentLabTestCollection());
            System.out.println("");
        }
        
        // Add a new patient
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("After adding patient #260: \n");
        Patient newObj = new Patient("260","whatever","123 Main",'y',java.sql.Date.valueOf("1962-12-19"));
        newObj.setPhysician(new Physician("10","Dr. Howard"));
        dbSingleton.db.addData(newObj);
        objs = dbSingleton.db.getData("Patient", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        
        // Update a patient's name
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("After updating patient #260's name: \n");
        newObj.setName("A changed name");
        dbSingleton.db.updateData(newObj);
        objs = dbSingleton.db.getData("Patient", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        
        // Add another new patient
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("After adding patient #270 with modified name: \n");
        newObj.setId("270");
        newObj.setName("another new object");
        dbSingleton.db.addData(newObj);
        objs = dbSingleton.db.getData("Patient", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        }
        
        // Trying to add a patient with an ID that already exists
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("After trying to add patient with old ID 250: \n");
        newObj = new Patient("250","whatever","123 Main",'y',java.sql.Date.valueOf("1962-12-19"));
        dbSingleton.db.addData(newObj);
        objs = dbSingleton.db.getData("Patient", "");
        for (Object obj : objs){
            System.out.println(obj + "\n");
        } 
        
        System.out.println("\n\n**************************************************************\n\n");
        System.out.println("Database Table Structure: \n");
        //outputs database tables, PKs and FKs to console
        dbSingleton.db.getDBInfo();

        //int numRows = db.doUpdInsDel("insert/update or delete free form query");

        System.out.println("\n\nDone running.....");

  }

    static String getUnderline(String str)
	{
		String underLine = "";
		for(int i = 0; i < str.length(); i++)
		{
			underLine += "=";
		}

		return underLine;
	}
}

