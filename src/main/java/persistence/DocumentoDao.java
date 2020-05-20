/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistence;

import commons.DaoEclipseLink;
import java.util.List;
import entities.Documento;

/**
 *
 * @author lorenzofabro
 */
public class DocumentoDao extends DaoEclipseLink<Documento, Integer>
{
    
    public DocumentoDao() {
        super(Documento.class);
    }

    public Documento retrieve (long id) {
         List<Documento> resp = entityManager.createNamedQuery("Documento.findById")
                .setParameter("id", id)
                .getResultList();
        if (resp.size() == 1) return resp.get(0);
        
        return null;
    }
}