/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import commons.DaoEclipseLink;
import entities.Posteo;
import java.util.List;

/**
 *
 * @author lorenzofabro
 */
public class PosteoDao extends DaoEclipseLink<Posteo, Integer> {
    
    public PosteoDao(){
        super(Posteo.class);
    }

    public Posteo retrieve(long idPalabra, long idDocumento) {
        List<Posteo> resp = entityManager.createNamedQuery("Posteo.findByIdPalabraAndIdDocumento")
                    .setParameter("idPalabra", idPalabra)
                    .setParameter("idDocumento", idDocumento)
                    .getResultList();
        
        if (resp.size() == 1) {
            return resp.get(0);
        }

        return null;
    }
}
