/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import commons.DaoEclipseLink;
import java.util.List;
import entities.Palabra;

/**
 *
 * @author lorenzofabro
 */
public class PalabraDao extends DaoEclipseLink<Palabra, Integer> {

    public PalabraDao() {
        super(Palabra.class);
    }

    public Palabra retrieve(long id) {
        List<Palabra> resp = entityManager.createNamedQuery("Palabra.findById")
                .setParameter("id", id)
                .getResultList();
        if (resp.size() == 1) {
            return resp.get(0);
        }

        return null;
    }
}
