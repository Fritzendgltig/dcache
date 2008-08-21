/*
 * AuthRecordPersistenceManager.java
 *
 * Created on July 1, 2008, 12:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dcache.auth.persistence;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.dcache.auth.AuthorizationRecord;
import org.dcache.auth.GroupList;
import org.dcache.auth.Group;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author timur
 */
public class  AuthRecordPersistenceManager {
    
    EntityManager em ;
    public AuthRecordPersistenceManager(String propertiesFile) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(propertiesFile));
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("AuthRecordPersistenceUnit",p );
        em = emf.createEntityManager();
    }
    
    public AuthRecordPersistenceManager(String jdbcUrl,
    String jdbcDriver,
    String user,
    String pass) throws IOException {
        // This properties allow us to override the defaults
        Properties p = new Properties();
        // JPOX a.k.a. DataNucleous style properties
        p.put("javax.jdo.option.ConnectionDriverName",jdbcDriver);
        p.put("javax.jdo.option.ConnectionURL",jdbcUrl);
        p.put("javax.jdo.option.ConnectionUserName",user);
        p.put("javax.jdo.option.ConnectionPassword",pass);
        
        // GlassFish a.k.a. TopLink style 
        p.put("toplink.jdbc.driver",jdbcDriver);
        p.put("toplink.jdbc.url",jdbcUrl);
        p.put("toplink.jdbc.user",user);
        p.put("toplink.jdbc.password",pass);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("AuthRecordPersistenceUnit",p );
        em = emf.createEntityManager();
    }
    
    public long persist(AuthorizationRecord rec) {
        EntityTransaction t = em.getTransaction();
        try{
            t.begin();
            em.persist(rec);
            t.commit();
        }finally
        {
            if (t.isActive())
            {
                t.rollback();
            }

            em.close();
        }
        return 0;   
    }
    
    public AuthorizationRecord find(long id) {
        AuthorizationRecord ar = null;
        EntityTransaction t = em.getTransaction();
        try{
            t.begin();
            ar = em.find(AuthorizationRecord.class, id);
            ar.getGroupLists();
            t.commit();
        }finally
        {
            if (t.isActive())
            {
                t.rollback();
            }

            em.close();
        }
        return ar;   
        
    }
    
    
    
    public static void main(String[] args) throws Exception{
        if(args == null || args.length == 0) {
            persistTest();
        } else {
            findTest(Long.parseLong(args[0]));
        }
    }
    
    public static void persistTest() throws Exception{
        AuthRecordPersistenceManager pm = 
            new AuthRecordPersistenceManager("jdbc:postgresql://localhost/testjpa",
            "org.postgresql.Driver","srmdcache","");
        java.util.HashSet<String> principals = new java.util.HashSet<String>();
        principals.add("timur@FNAL.GOV");
        AuthorizationRecord ar =    
            new AuthorizationRecord();
        ar.setId(System.currentTimeMillis());
        ar.setHome("/");
        ar.setUid(10401);
        ar.setName("timur");
        ar.setIdentity("timur@FNAL.GOV");
        ar.setReadOnly(false);
        ar.setPriority(10);
        ar.setRoot("/pnfs/fnal.gov/usr");
        
        GroupList gl1 = new GroupList();
        gl1.setAuthRecord(ar);
        Group group11 = new Group();
        Group group12 = new Group();
        Group group13 = new Group();
        group11.setGroupList(gl1);
        group12.setGroupList(gl1);
        group13.setGroupList(gl1);
        group11.setName("Group1");
        group11.setGid(1530);
        group12.setName("Group2");
        group12.setGid(1531);
        group13.setName("Group3");
        group13.setGid(1533);
        List<Group> l1 = new LinkedList<Group> ();
        l1.add(group11);
        l1.add(group12);
        l1.add(group13);
        
        gl1.setAttribute(null);
        gl1.setGroups(l1);
        
        GroupList gl2 = new GroupList();
        gl2.setAuthRecord(ar);
        Group group21 = new Group();
        Group group22 = new Group();
        group21.setGroupList(gl2);
        group22.setGroupList(gl2);
        group21.setName("Group4");
        group21.setGid(2530);
        group22.setName("Group5");
        group22.setGid(2530);
        List<Group> l2 = new LinkedList<Group>();
        l2.add(group21);
        l2.add(group22);
        gl2.setAttribute(null);
        gl2.setGroups(l2);
        List<GroupList> gll = new LinkedList<GroupList>();
        gll.add(gl1);
        gll.add(gl2);
        ar.setGroupLists(gll);
        System.out.println("persisting "+ar);
        pm.persist(ar);
        System.out.println("persisted successfully ");
        System.out.println("id="+ar.getId());
    }
    
    public static void findTest(long id)  throws Exception {
        AuthRecordPersistenceManager pm = 
            new AuthRecordPersistenceManager("jdbc:postgresql://localhost/testjpa",
            "org.postgresql.Driver","srmdcache","");
      // AuthRecordPersistenceManager pm = 
      //      new AuthRecordPersistenceManager("/tmp/pm.properties");
        AuthorizationRecord ar = pm.find(id);
        if(ar == null) {
            System.out.println("AuthorizationRecord with id="+id +" not found ");
        }
        System.out.println(" found "+ar);
        
    }
    
}
