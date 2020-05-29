/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import entities.Palabra;
import entities.Posteo;
import exceptions.TechnicalException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 *
 * @author lorenzofabro
 */
public class Util {

    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("tpmotorbusquedaPU");

    public List<Palabra> getPalabras() {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            List<Palabra> palabras = entityManager.createNamedQuery("Palabra.findAll").getResultList();
            entityManager.close();
            return palabras;
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }
    }
   
}
