/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import entities.Palabra;
import exceptions.TechnicalException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

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
